package com.zhss.eshop.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhss.eshop.common.util.CloneDirection;
import com.zhss.eshop.membership.domain.DeliveryAddressDTO;
import com.zhss.eshop.membership.domain.DeliveryAddressVO;
import com.zhss.eshop.order.domain.OrderInfoDTO;
import com.zhss.eshop.order.domain.OrderInfoVO;
import com.zhss.eshop.order.service.OrderInfoService;
import com.zhss.eshop.promotion.domain.CouponDTO;
import com.zhss.eshop.promotion.domain.CouponVO;

/**
 * 订单管理controller组件
 * @author zhonghuashishan
 *
 */
@RestController
@RequestMapping("/order")  
public class OrderInfoController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderInfoController.class);

	/**
	 * 订单管理service组件
	 */
	@Autowired
	private OrderInfoService orderInfoService;
	
	/**
	 * 计算订单价格
	 * @param order
	 * @param deliveryAddress
	 * @return
	 */
	@GetMapping("/price")  
	public OrderInfoVO calculateOrderPrice(OrderInfoVO order, 
			DeliveryAddressVO deliveryAddress) {
		try {
			OrderInfoDTO resultOrder = orderInfoService.calculateOrderPrice(
					order.clone(OrderInfoDTO.class, CloneDirection.FORWARD), 
					deliveryAddress.clone(DeliveryAddressDTO.class));  
			return resultOrder.clone(OrderInfoVO.class, CloneDirection.OPPOSITE);
		} catch (Exception e) {
			logger.error("error", e); 
			return order;
		}
	}
	
	/**
	 * 计算优惠券抵扣的金额
	 * @param order 
	 * @param coupon
	 * @return
	 */
	@GetMapping("/coupon")  
	public OrderInfoVO calculateCouponDiscountPrice(
			OrderInfoVO order, CouponVO coupon) {
		try {
			OrderInfoDTO resultOrder = orderInfoService.calculateCouponDiscountPrice(
					order.clone(OrderInfoDTO.class, CloneDirection.FORWARD), 
					coupon.clone(CouponDTO.class));  
			return resultOrder.clone(OrderInfoVO.class, CloneDirection.OPPOSITE);
		} catch (Exception e) {
			logger.error("error", e); 
			return order;
		}
	}
	
	/**
	 * 新建订单
	 * @param order
	 * @return
	 */
	@PostMapping("/")
	public OrderInfoVO save(@RequestBody OrderInfoVO order) {
		try {
			OrderInfoDTO resultOrder = orderInfoService.save(order.clone(
					OrderInfoDTO.class, CloneDirection.FORWARD));
			return resultOrder.clone(OrderInfoVO.class, CloneDirection.OPPOSITE);
		} catch (Exception e) {
			logger.error("error", e); 
			return order;
		}
	}
	
}
