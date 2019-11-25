package com.miaoshaproject.miaosha.service.Imp;

import com.miaoshaproject.miaosha.dao.PromoMapper;
import com.miaoshaproject.miaosha.domain.Promo;
import com.miaoshaproject.miaosha.service.PromoService;
import com.miaoshaproject.miaosha.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoMapper promoMapper;


    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        Promo promo = promoMapper.selectByItemId(itemId);
        PromoModel promoModel = convertToPromoModel(promo);
        if (promoModel == null) {
            return null;
        }
        DateTime now = new DateTime();
        if (promoModel.getStartDate().isAfter(now)) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBefore(now)) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        return promoModel;

    }

    private PromoModel convertToPromoModel(Promo promo) {
        if (promo == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo,promoModel);
        promoModel.setStartDate(new DateTime(promo.getStartDate()));
        promoModel.setEndDate(new DateTime(promo.getEndDate()));
        return promoModel;
    }
}
