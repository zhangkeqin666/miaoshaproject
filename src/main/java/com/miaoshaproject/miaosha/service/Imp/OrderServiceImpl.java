package com.miaoshaproject.miaosha.service.Imp;

import com.miaoshaproject.miaosha.dao.OrderMapper;
import com.miaoshaproject.miaosha.dao.SequenceMapper;
import com.miaoshaproject.miaosha.domain.Order;
import com.miaoshaproject.miaosha.domain.Sequence;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.service.model.OrderModel;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SequenceMapper sequenceMapper;


    @Transactional
    @Override
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {
        //1、校验下单状态  用户是否合法 购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, "用户信息不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, "数量信息不正确");
        }
        //校验活动信息
        if (promoId != null) {
            if (promoId != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, "活动信息不正确");
            } else if(itemModel.getPromoModel().getStatus() != 2){
                throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, "活动还未开始");
            }
        }



        //2、落单减库存（支付减库存）
        boolean flag = itemService.decreaseStock(itemId, amount);
        if (!flag) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3、订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        orderModel.setPromoId(promoId);
        Order order = convertToOrder(orderModel);
        //生成交易流水号
        String orderId = generateOrderNo();
        order.setId(orderId);
        orderModel.setId(orderId);
        orderMapper.insertSelective(order);
        itemService.increaseSales(itemId,amount);
        //4、返回前端
        return orderModel;


    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {
        //订单号为16位
        StringBuilder sb = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(time);
        //中间6位为自增序列
        int seq = 0;
        Sequence sequence = sequenceMapper.getSequenceByName("order_info");
        seq = sequence.getCurrentValue();
        sequence.setCurrentValue(sequence.getCurrentValue()+sequence.getStep());
        sequenceMapper.updateByPrimaryKeySelective(sequence);
        String seqStr = String.valueOf(seq);
        for (int i = 0; i < 6 - seqStr.length(); i++) {
            sb.append("0");
        }
        sb.append(seqStr);
        //最后两位为分库分表位
        //暂时写死
        sb.append("00");
        return sb.toString();
    }

    private Order convertToOrder(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        Order order = new Order();
        BeanUtils.copyProperties(orderModel,order);
        return order;
    }
}
