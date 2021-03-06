package com.miaoshaproject.miaosha.dao;

import com.miaoshaproject.miaosha.domain.Item;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Tue Nov 19 20:43:33 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Tue Nov 19 20:43:33 CST 2019
     */
    int insert(Item record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Tue Nov 19 20:43:33 CST 2019
     */
    int insertSelective(Item record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Tue Nov 19 20:43:33 CST 2019
     */
    Item selectByPrimaryKey(Integer id);

    List<Item> listItem();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Tue Nov 19 20:43:33 CST 2019
     */
    int updateByPrimaryKeySelective(Item record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Tue Nov 19 20:43:33 CST 2019
     */
    int updateByPrimaryKey(Item record);

    void increaseSales(@Param("id") Integer id,@Param("amount") Integer amount);
}