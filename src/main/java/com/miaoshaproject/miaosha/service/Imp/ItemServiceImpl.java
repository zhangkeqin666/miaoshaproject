package com.miaoshaproject.miaosha.service.Imp;

import com.miaoshaproject.miaosha.dao.ItemMapper;
import com.miaoshaproject.miaosha.dao.ItemStockMapper;
import com.miaoshaproject.miaosha.dao.OrderMapper;
import com.miaoshaproject.miaosha.domain.Item;
import com.miaoshaproject.miaosha.domain.ItemStock;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.validator.ValidationResult;
import com.miaoshaproject.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;


    @Transactional
    @Override
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //入参校验
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, result.getErrMsg());
        }
        //转化itemmodel到item
        Item item = convertToItem(itemModel);
        //写入数据库
        itemMapper.insertSelective(item);
        itemModel.setId(item.getId());
        ItemStock itemStock = converToItemStock(itemModel);
        itemStockMapper.insertSelective(itemStock);

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    private ItemStock converToItemStock(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStock itemStock = new ItemStock();
        itemStock.setItemId(itemModel.getId());
        itemStock.setStock(itemModel.getStock());
        return itemStock;

    }

    private Item convertToItem(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        Item item = new Item();
        BeanUtils.copyProperties(itemModel,item);
        return item;
    }

    @Override
    public List<ItemModel> listItem() {
        List<Item> items = itemMapper.listItem();
        List<ItemModel> itemModels = items.stream().map(item -> {
            ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
            ItemModel itemModel = convertToModel(item, itemStock);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModels;
    }

    private ItemModel convertToModel(Item item, ItemStock itemStock) {
        if (item == null) {
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(item,itemModel);
        itemModel.setStock(itemStock.getStock());
        return itemModel;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        Item item = itemMapper.selectByPrimaryKey(id);
        if (item == null) {
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(item,itemModel);
        ItemStock itemStock = itemStockMapper.selectByItemId(id);
        itemModel.setStock(itemStock.getStock());
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedRow = itemStockMapper.decreaseStock(itemId, amount);
        if (affectedRow > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemMapper.increaseSales(itemId,amount);
    }
}
