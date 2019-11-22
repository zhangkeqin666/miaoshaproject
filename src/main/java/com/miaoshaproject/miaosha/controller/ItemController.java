package com.miaoshaproject.miaosha.controller;

import com.miaoshaproject.miaosha.controller.viewmodel.ItemViewModel;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.Imp.ItemServiceImpl;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController {

    @Autowired
    private ItemServiceImpl itemService;

    //创建商品的controller
    @RequestMapping(value = "/create",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setStock(stock);
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setPrice(price);
        ItemModel itemModelReturn = itemService.createItem(itemModel);
        ItemViewModel itemViewModel = convertToItemViewModel(itemModelReturn);
        return CommonReturnType.create(itemViewModel);
    }

    //商品详情浏览
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = itemService.getItemById(id);
        ItemViewModel itemViewModel = convertToItemViewModel(itemModel);
        return CommonReturnType.create(itemViewModel);

    }

    //商品列表浏览
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem() {
        List<ItemModel> itemModels = itemService.listItem();

        //使用stream api将itemmodel转化为itemviewmodel
        List<ItemViewModel> itemViewModels = itemModels.stream().map(itemModel -> {
            ItemViewModel itemViewModel = convertToItemViewModel(itemModel);
            return itemViewModel;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemViewModels);
    }

    private ItemViewModel convertToItemViewModel(ItemModel itemModelReturn) {
        if (itemModelReturn == null) {
            return null;
        }
        ItemViewModel itemViewModel = new ItemViewModel();
        BeanUtils.copyProperties(itemModelReturn,itemViewModel);
        return itemViewModel;
    }



}
