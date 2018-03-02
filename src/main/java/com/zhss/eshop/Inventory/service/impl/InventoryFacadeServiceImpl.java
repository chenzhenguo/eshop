package com.zhss.eshop.Inventory.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhss.eshop.Inventory.dao.GoodsStockDAO;
import com.zhss.eshop.Inventory.domain.GoodsStockDO;
import com.zhss.eshop.Inventory.service.InventoryFacadeService;
import com.zhss.eshop.Inventory.updater.CancelOrderStockUpdaterFactory;
import com.zhss.eshop.Inventory.updater.GoodsStockUpdater;
import com.zhss.eshop.Inventory.updater.PayOrderStockUpdaterFactory;
import com.zhss.eshop.Inventory.updater.PurchaseInputStockUpdaterFactory;
import com.zhss.eshop.Inventory.updater.ReturnGoodsInputStockUpdaterFactory;
import com.zhss.eshop.Inventory.updater.SubmitOrderStockUpdaterFactory;
import com.zhss.eshop.order.domain.OrderInfoDTO;
import com.zhss.eshop.wms.domain.PurchaseInputOrderDTO;
import com.zhss.eshop.wms.domain.ReturnGoodsInputOrderDTO;

/**
 * 库存中心对外接口service组件
 * @author zhonghuashishan
 *
 */
@Service
public class InventoryFacadeServiceImpl implements InventoryFacadeService {
	
	private static final Logger logger = LoggerFactory.getLogger(InventoryFacadeServiceImpl.class);
	
	/**
	 * 采购入库库存更新命令工厂
	 */
	@Autowired
	private PurchaseInputStockUpdaterFactory<PurchaseInputOrderDTO> 
			purchaseInputStockUpdateCommandFactory;
	/**
	 * 退货入库库存更新命令工厂
	 */
	@Autowired
	private ReturnGoodsInputStockUpdaterFactory<ReturnGoodsInputOrderDTO> 
			returnGoodsInputStockUpdateCommandFactory;
	/**
	 * 提交订单库存更新组件工厂
	 */
	@Autowired
	private SubmitOrderStockUpdaterFactory<OrderInfoDTO> 
			submitOrderStockUpdaterFactory;
	/**
	 * 支付订单库存更新组件工厂
	 */
	@Autowired
	private PayOrderStockUpdaterFactory<OrderInfoDTO> 
			payOrderStockUpdaterFactory;
	/**
	 * 取消订单库存更新组件工厂
	 */
	@Autowired
	private CancelOrderStockUpdaterFactory<OrderInfoDTO> 
			cancelOrderStockUpdaterFactory;
	/**
	 * 商品库存管理模块DAO组件
	 */
	@Autowired
	private GoodsStockDAO goodsStockDAO;
	
	/**
	 * 通知库存中心，“采购入库完成”事件发生了
	 * @param purchaseInputOrderDTO 采购入库单DTO
	 * @return 处理结果
	 */
	public Boolean informPurchaseInputFinished(
			PurchaseInputOrderDTO purchaseInputOrderDTO) {
		try {
			GoodsStockUpdater goodsStockUpdateCommand = 
					purchaseInputStockUpdateCommandFactory.create(purchaseInputOrderDTO);
			goodsStockUpdateCommand.updateGoodsStock();
		} catch (Exception e) {
			logger.error("error", e); 
			return false;
		}
		return true;
	}
	
	/**
	 * 通知库存中心，“完成退货入库”事件发生了
	 * @param returnGoodsInputOrderDTO 退货入库单DTO
	 * @return 处理结果
	 */
	public Boolean informReturnGoodsInputFinished(
			ReturnGoodsInputOrderDTO returnGoodsInputOrderDTO) {
		try {
			GoodsStockUpdater goodsStockUpdateCommand = 
					returnGoodsInputStockUpdateCommandFactory.create(returnGoodsInputOrderDTO);
			goodsStockUpdateCommand.updateGoodsStock();
		} catch (Exception e) {
			logger.error("error", e); 
			return false;
		}
		return true;
	}
	
	/**
	 * 通知库存中心，“提交订单”事件发生了
	 * @param orderDTO 订单DTO
	 * @return 处理结果
	 */
	public Boolean informSubmitOrderEvent(OrderInfoDTO orderDTO) {
		try {
			GoodsStockUpdater goodsStockUpdateCommand = 
					submitOrderStockUpdaterFactory.create(orderDTO); 
			goodsStockUpdateCommand.updateGoodsStock();
		} catch (Exception e) {
			logger.error("error", e); 
			return false;
		}
		return true;
	}
	
	/**
	 * 通知库存中心，“支付订单”事件发生了
	 * @param orderDTO 订单DTO
	 * @return 处理结果
	 */
	public Boolean informPayOrderEvent(OrderInfoDTO orderDTO) {
		try {
			GoodsStockUpdater goodsStockUpdateCommand = 
					payOrderStockUpdaterFactory.create(orderDTO); 
			goodsStockUpdateCommand.updateGoodsStock();
		} catch (Exception e) {
			logger.error("error", e); 
			return false;
		}
		return true;
	}
	
	/**
	 * 通知库存中心，“取消订单”事件发生了
	 * @param orderDTO 订单DTO
	 * @return 处理结果
	 */
	public Boolean informCancelOrderEvent(OrderInfoDTO orderDTO) {
		try {
			GoodsStockUpdater goodsStockUpdateCommand = 
					cancelOrderStockUpdaterFactory.create(orderDTO); 
			goodsStockUpdateCommand.updateGoodsStock();
		} catch (Exception e) {
			logger.error("error", e); 
			return false;
		}
		return true;
	}
	
	/**
	 * 查询商品sku的库存
	 * @param goodsSkuId 商品sku id
	 * @return 商品sku的库存
	 */
	public Long getSaleStockQuantity(Long goodsSkuId) {
		try {
			GoodsStockDO goodsStockDO = goodsStockDAO
					.getGoodsStockBySkuId(goodsSkuId);
			if(goodsStockDO == null) {
				return 0L;
			}
			
			return goodsStockDO.getSaleStockQuantity();
		} catch (Exception e) {
			logger.error("error", e); 
		}
		return 0L;
	}

}
