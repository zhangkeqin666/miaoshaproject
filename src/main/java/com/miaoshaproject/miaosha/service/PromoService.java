package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId) throws BusinessException;

}
