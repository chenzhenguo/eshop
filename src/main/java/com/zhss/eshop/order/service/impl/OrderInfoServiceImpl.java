package com.zhss.eshop.order.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhss.eshop.Inventory.service.InventoryService;
import com.zhss.eshop.common.util.DateProvider;
import com.zhss.eshop.common.util.ObjectUtils;
import com.zhss.eshop.order.constant.OrderOperateType;
import com.zhss.eshop.order.constant.OrderStatus;
import com.zhss.eshop.order.constant.PublishedComment;
import com.zhss.eshop.order.dao.OrderInfoDAO;
import com.zhss.eshop.order.dao.OrderItemDAO;
import com.zhss.eshop.order.dao.OrderOperateLogDAO;
import com.zhss.eshop.order.domain.OrderInfoDO;
import com.zhss.eshop.order.domain.OrderInfoDTO;
import com.zhss.eshop.order.domain.OrderInfoQuery;
import com.zhss.eshop.order.domain.OrderItemDO;
import com.zhss.eshop.order.domain.OrderItemDTO;
import com.zhss.eshop.order.domain.OrderOperateLogDTO;
import com.zhss.eshop.order.price.CouponCalculator;
import com.zhss.eshop.order.price.CouponCalculatorFactory;
import com.zhss.eshop.order.price.DefaultOrderPriceCalculatorFactory;
import com.zhss.eshop.order.price.DiscountOrderPriceCalculatorFactory;
import com.zhss.eshop.order.price.FreightCalculator;
import com.zhss.eshop.order.price.GiftOrderPriceCalculatorFactory;
import com.zhss.eshop.order.price.OrderPriceCalculatorFactory;
import com.zhss.eshop.order.price.PromotionActivityCalculator;
import com.zhss.eshop.order.price.PromotionActivityResult;
import com.zhss.eshop.order.price.TotalPriceCalculator;
import com.zhss.eshop.order.service.OrderInfoService;
import com.zhss.eshop.order.state.OrderStateManager;
import com.zhss.eshop.pay.service.PayService;
import com.zhss.eshop.promotion.constant.PromotionActivityType;
import com.zhss.eshop.promotion.domain.CouponDTO;
import com.zhss.eshop.promotion.domain.PromotionActivityDTO;
import com.zhss.eshop.promotion.service.PromotionService;

/**
 * 订单管理service组件
 * @author zhonghuashishan
 *
 */
@Service
@Transactional
public class OrderInfoServiceImpl implements OrderInfoService {

	/**
	 * 折扣减免型的订单价格计算组件工厂
	 */
	@Autowired
	private DiscountOrderPriceCalculatorFactory discountOrderPriceCalculatorFactory;
	/**
	 * 赠品型的订单价格计算组件工厂
	 */
	@Autowired
	private GiftOrderPriceCalculatorFactory giftOrderPriceCalculatorFactory;
	/**
	 * 默认的订单价格计算组件工厂
	 */
	@Autowired
	private DefaultOrderPriceCalculatorFactory defaultOrderPriceCalculatorFactory;
	/**
	 * 优惠券计算组件工厂
	 */
	@Autowired
	private CouponCalculatorFactory couponCalculatorFactory;
	/**
	 * 促销中心接口
	 */
	@Autowired
	private PromotionService promotionService;
	/**
	 * 订单管理DAO组件
	 */
	@Autowired
	private OrderInfoDAO orderInfoDAO;
	/**
	 * 订单条目管理DAO组件
	 */
	@Autowired
	private OrderItemDAO orderItemDAO;
	/**
	 * 日期辅助组件
	 */
	@Autowired
	private DateProvider dateProvider;
	/**
	 * 库存中心接口
	 */
	@Autowired
	private InventoryService inventoryService;
	/**
	 * 订单操作日志DAO组件
	 */
	@Autowired
	private OrderOperateLogDAO orderOperateLogDAO;
	/**
	 * 订单操作内容工厂
	 */
	@Autowired
	private OrderOperateLogFactory orderOperateLogFactory;
	/**
	 * 订单状态管理器
	 */
	@Autowired
	private OrderStateManager orderStateManager;
	
	/**
	 * 支付中心接口
	 */
	@Autowired
	private PayService payService;
	
	/**
	 * 计算订单价格
	 * @param order 订单
	 */
	public OrderInfoDTO calculateOrderPrice(OrderInfoDTO order) {
		// 定义订单的各种价格
		Double totalAmount = 0.0;
		Double discountAmount = 0.0;
		Double freight = 0.0;
		
		List<OrderItemDTO> giftOrderItems = new ArrayList<OrderItemDTO>();
		
		for(OrderItemDTO item : order.getOrderItems()) {
			// 查询订单条目使用的促销活动
			PromotionActivityDTO promotionActivity = promotionService.getById(
					item.getPromotionActivityId());
			
			// 根据促销活动获取到订单计算组件的工厂
			OrderPriceCalculatorFactory orderPriceCalculatorFactory = 
					getOrderPriceCalculatorFactory(promotionActivity); 
			
			// 从订单计算组件工厂中获取一套订单的价格计算组件
			TotalPriceCalculator totalPriceCalculator = orderPriceCalculatorFactory
					.createTotalPriceCalculator();
			PromotionActivityCalculator promotionActivityCalculator = orderPriceCalculatorFactory
					.createPromotionActivityCalculator(promotionActivity); 
			FreightCalculator freightCalculator = orderPriceCalculatorFactory
					.createFreightCalculator();
			
			// 计算订单条目的总金额
			totalAmount += totalPriceCalculator.calculate(item);
			
			// 处理促销活动，计算促销活动的减免金额，以及促销活动的赠品
			PromotionActivityResult result = promotionActivityCalculator.calculate(
					item, promotionActivity); 
			discountAmount += result.getDiscountAmount();
			giftOrderItems.addAll(result.getOrderItems());
			
			// 计算订单条目的运费
			freight += freightCalculator.calculate(order, item, result);
		}
		
		// 给订单设置计算后的结果（同时已经包含了所有的赠品）
		order.setTotalAmount(totalAmount);
		order.setDiscountAmount(discountAmount); 
		order.setFreight(freight); 
		order.setPayableAmount(totalAmount + freight - discountAmount);  
		order.getOrderItems().addAll(giftOrderItems);
		
		return order;
	}
	
	/**
	 * 获取一个订单价格计算工厂
	 * @param promotionActivityType 促销活动类型
	 * @return 订单价格计算工厂
	 */
	private OrderPriceCalculatorFactory getOrderPriceCalculatorFactory(
			PromotionActivityDTO promotionActivity) {
		if(promotionActivity == null) {
			return defaultOrderPriceCalculatorFactory;
		}
		
		Integer promotionActivityType = promotionActivity.getType();
		
		if(PromotionActivityType.DIRECT_DISCOUNT.equals(promotionActivityType) 
				|| PromotionActivityType.MULTI_DISCOUNT.equals(promotionActivityType)
				|| PromotionActivityType.REACH_DISCOUNT.equals(promotionActivityType)) {  
			return discountOrderPriceCalculatorFactory;
		} else {
			return giftOrderPriceCalculatorFactory;
		}
	}

	/**
	 * 计算优惠券抵扣的金额
	 * @param order 
	 * @param coupon
	 * @return
	 */
	public OrderInfoDTO calculateCouponDiscountPrice(
			OrderInfoDTO order, CouponDTO coupon) {
		CouponCalculator couponCalculator = couponCalculatorFactory.create(coupon);
		Double couponAmount = couponCalculator.calculate(order, coupon);
		order.setCouponAmount(couponAmount); 
		order.setPayableAmount(order.getPayableAmount() - couponAmount);  
		return order;
	}
	
	/**
	 * 新增一个订单
	 * @param order
	 */
	public OrderInfoDTO save(OrderInfoDTO order) throws Exception {
		if(!isStockEnough(order)) {
			return order;
		}
		
		saveOrder(order);
		orderStateManager.create(order); 
		orderOperateLogDAO.save(orderOperateLogFactory.get(
				order, OrderOperateType.CREATE_ORDER));      
		inventoryService.informSubmitOrderEvent(order);
		promotionService.useCoupon(order.getCouponId(), order.getUserAccountId());
		
		return order;
	}
	
	/**
	 * 判断库存是否充足
	 * @param order 订单
	 * @return 库存是否充足
	 * @throws Exception
	 */
	private Boolean isStockEnough(OrderInfoDTO order) throws Exception {
		for(OrderItemDTO orderItem : order.getOrderItems()) {
			Long saleStockQuantity = inventoryService.getSaleStockQuantity(
					orderItem.getGoodsSkuId());
			if(saleStockQuantity < orderItem.getPurchaseQuantity()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 新增订单
	 * @param order 订单
	 * @return 订单
	 * @throws Exception
	 */
	private OrderInfoDTO saveOrder(OrderInfoDTO order) throws Exception {
		order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));  
		order.setPublishedComment(PublishedComment.NO); 
		order.setOrderStatus(OrderStatus.UNKNOWN);  
		order.setGmtCreate(dateProvider.getCurrentTime()); 
		order.setGmtModified(dateProvider.getCurrentTime());
		
		Long orderInfoId = orderInfoDAO.save(order.clone(OrderInfoDO.class)); 
		order.setId(orderInfoId); 
		
		for(OrderItemDTO orderItem : order.getOrderItems()) {
			orderItem.setOrderInfoId(orderInfoId); 
			orderItem.setGmtCreate(dateProvider.getCurrentTime()); 
			orderItem.setGmtModified(dateProvider.getCurrentTime());  
			orderItemDAO.save(orderItem.clone(OrderItemDO.class));   
		}
		
		return order;
	}
	
	/**
	 * 分页查询订单
	 * @param query 查询条件 
	 * @return 订单
	 * @throws Exception
	 */
	public List<OrderInfoDTO> listByPage(OrderInfoQuery query) throws Exception {
		List<OrderInfoDTO> orders = ObjectUtils.convertList(
				orderInfoDAO.listByPage(query), OrderInfoDTO.class); 
		
		for(OrderInfoDTO order : orders) {
			List<OrderItemDTO> orderItems = ObjectUtils.convertList(
					orderItemDAO.listByOrderInfoId(order.getId()), OrderItemDTO.class);
			order.setOrderItems(orderItems); 
		}
		
		return orders;
	}
	
	/**
	 * 根据id查询订单
	 * @param id 订单id
	 * @return 订单
	 * @throws Exception
	 */
	public OrderInfoDTO getById(Long id) throws Exception {
		OrderInfoDTO order = orderInfoDAO.getById(id).clone(OrderInfoDTO.class);
		
		List<OrderItemDTO> orderItems = ObjectUtils.convertList(
				orderItemDAO.listByOrderInfoId(order.getId()), 
				OrderItemDTO.class); 
		
		List<OrderOperateLogDTO> logs = ObjectUtils.convertList(
				orderOperateLogDAO.listByOrderInfoId(order.getId()), 
				OrderOperateLogDTO.class);
		
		order.setOrderItems(orderItems); 
		order.setLogs(logs); 
		
		return order;
	}
	
	/**
	 * 取消订单
	 * @param id 订单id
	 * @return 处理结果
	 * @throws Exception
	 */
	public Boolean cancel(Long id) throws Exception {
		OrderInfoDTO order = getById(id);
		if(order == null ) {
			return false;
		}
		
		if(!orderStateManager.canCancel(order)) {  
			return false;
		}
		
		orderStateManager.cancel(order);
		inventoryService.informCancelOrderEvent(order);
		orderOperateLogDAO.save(orderOperateLogFactory.get(
				order, OrderOperateType.MANUAL_CANCEL_ORDER)); 
		
		return true;
	}
	
	/**
	 * 支付订单
	 * @param id 订单id
	 * @return 处理结果
	 * @throws Exception
	 */
	public String pay(Long id) throws Exception {
		OrderInfoDTO order = getById(id);
		if(!orderStateManager.canPay(order)) {
			return null;
		}
		return payService.getQrCode(order);
	}
	
}
