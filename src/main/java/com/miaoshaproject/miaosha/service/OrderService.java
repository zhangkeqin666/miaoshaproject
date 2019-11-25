package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.service.model.OrderModel;

public interface OrderService {
    //通过前端传过来秒杀id，下单做校验（采用第一种方案）
    //直接在下单接口中判断是否存在秒杀活动
    OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException;
}
