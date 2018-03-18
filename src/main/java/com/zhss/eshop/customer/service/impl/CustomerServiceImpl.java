package com.zhss.eshop.customer.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhss.eshop.customer.constant.ReturnGoodsWorksheetStatus;
import com.zhss.eshop.customer.dao.ReturnGoodsWorksheetDAO;
import com.zhss.eshop.customer.domain.ReturnGoodsWorksheetDO;
import com.zhss.eshop.customer.service.CustomerService;

/**
 * 客服中心接口
 * @author zhonghuashishan
 *
 */
@Service
public class CustomerServiceImpl implements CustomerService {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);
	
	/**
	 * 退货工单管理DAO组件
	 */
	@Autowired
	private ReturnGoodsWorksheetDAO returnGoodsWorksheetDAO;
	
	/**
	 * 创建退货工单
	 * @param orderId 订单id
	 * @param orderNo 订单编号
	 * @param returnGoodsReason 退货原因
	 * @param returnGoodsRemark 退货备注
	 * @return 处理结果
	 */
	public Boolean createReturnGoodsWorksheet(Long orderId, String orderNo, 
			Integer returnGoodsReason, String returnGoodsRemark) {
		try {
			ReturnGoodsWorksheetDO worksheet = new ReturnGoodsWorksheetDO();
			worksheet.setOrderId(orderId); 
			worksheet.setOrderNo(orderNo); 
			worksheet.setReturnGoodsReason(returnGoodsReason); 
			worksheet.setReturnGoodsRemark(returnGoodsRemark); 
			worksheet.setStatus(ReturnGoodsWorksheetStatus.WAIT_FOR_APPROVE);  
			
			returnGoodsWorksheetDAO.save(worksheet); 
			
			return true;
		} catch (Exception e) {
			logger.error("error", e); 
			return false;
		}
	}
	
	/**
	 * 同步物流单号
	 * @param orderInfoId 订单id
	 * @param returnGoodsLogisticsCode 退货物流单号
	 * @return 处理结果
	 */
	public Boolean syncReturnGoodsLogisticsCode(Long orderInfoId, 
			String returnGoodsLogisticsCode) {
		return true;
	}
	
	/**
	 * 通知客服中心，“完成退货入库”事件发生了
	 * @param returnGoodsWorksheetId 退货工单id
	 * @return 处理结果
	 */
	public Boolean informReturnGoodsInputFinishedEvent(Long returnGoodsWorksheetId) {
		return true;
	}
	
	/**
	 * 通知客服中心，“完成退款”事件发生了
	 * @param returnGoodsWorkwheetId 退货工单id
	 * @return 处理结果
	 */
	public Boolean informRefundFinishedEvent(Long returnGoodsWorkwheetId) {
		return true;
	}
	
}
