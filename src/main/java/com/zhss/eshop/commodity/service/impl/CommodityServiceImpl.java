package com.zhss.eshop.commodity.service.impl;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zhss.eshop.commodity.domain.GoodsDTO;
import com.zhss.eshop.commodity.domain.GoodsSkuDTO;
import com.zhss.eshop.commodity.service.CommodityService;

/**
 * 商品中心对外接口service组件
 * @author zhonghuashishan
 *
 */
@Service
public class CommodityServiceImpl implements CommodityService {
	
	private static final Logger logger = LoggerFactory.getLogger(CommodityServiceImpl.class);
	
	/**
	 * 根据id查询商品sku
	 * @param goodsSkuId 商品sku id
	 * @return 商品sku DTO
	 */
	@Override
	public GoodsSkuDTO getGoodsSkuById(Long goodsSkuId) {
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			GoodsSkuDTO goodsSkuDTO = new GoodsSkuDTO();
			
			goodsSkuDTO.setId(goodsSkuId); 
			goodsSkuDTO.setGoodsSkuCode("FDL04300234"); 
			goodsSkuDTO.setSaleProperties("机身颜色:白色,机身存储:256G"); 
			goodsSkuDTO.setGoodsId(999L); 
			goodsSkuDTO.setGoodsName("iPhonePlus"); 
			goodsSkuDTO.setGoodsLength(125.90); 
			goodsSkuDTO.setGoodsWidth(29.60); 
			goodsSkuDTO.setGoodsHeight(59.50); 
			goodsSkuDTO.setGrossWeight(599.80); 
			goodsSkuDTO.setDiscountPrice(8999.00); 
			goodsSkuDTO.setPurchasePrice(6856.70);
			goodsSkuDTO.setSalePrice(9599.50);
			goodsSkuDTO.setGmtCreate(dateFormatter.parse("2018-01-01 10:00:00"));  
			goodsSkuDTO.setGmtModified(dateFormatter.parse("2018-01-01 10:00:00")); 
			
			return goodsSkuDTO;
		} catch (Exception e) {
			logger.error("error", e); 
		}
		return null;
	}
	
	/**
	 * 根据id查询商品
	 * @param goodsId 商品id
	 * @return 商品
	 */
	@Override
	public GoodsDTO getGoodsById(Long goodsId) {
		return null;
	}
	
}
