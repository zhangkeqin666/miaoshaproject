package com.miaoshaproject.miaosha.service.Imp;

import com.miaoshaproject.miaosha.dao.PasswordMapper;
import com.miaoshaproject.miaosha.dao.UserMapper;
import com.miaoshaproject.miaosha.domain.Password;
import com.miaoshaproject.miaosha.domain.User;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import com.miaoshaproject.miaosha.validator.ValidationResult;
import com.miaoshaproject.miaosha.validator.ValidatorImpl;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PseudoColumnUsage;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordMapper passwordMapper;

    @Autowired
    private ValidatorImpl validator;


    @Override
    public UserModel getUserById(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return null;
        }
         Password password = passwordMapper.selectByUserId(user.getId());
         return convertFromUserToUserModel(user,password);
    }


    @Transactional
    @Override
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR);
        }


//        if (StringUtils.isEmpty(userModel.getName()) || userModel.getAge() == null || userModel.getGender() == null
//                || StringUtils.isEmpty(userModel.getTelphone())) {
//            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, result.getErrMsg());
        }



        User user = convertToUser(userModel);
        try {
            userMapper.insertSelective(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, "用户已存在");
        }
        userModel.setId(user.getId());
        Password password = convertToPassword(userModel);
        passwordMapper.insertSelective(password);
        return;
}

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户手机获取用户信息
        User user = userMapper.selectByTelphone(telphone);
        if (user == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        Password password = passwordMapper.selectByUserId(user.getId());
        UserModel userModel = convertFromUserToUserModel(user,password);
        //比对用户密码与传输进来的密码是否匹配
        if (!StringUtils.equals(encrptPassword, userModel.getEncrptPassword())) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private User convertToUser(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userModel, user);
        return user;
    }

    private Password convertToPassword(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        Password password = new Password();
        password.setEncrptPassword(userModel.getEncrptPassword());
        password.setUserId(userModel.getId());
        return password;

    }

    private UserModel convertFromUserToUserModel(User user, Password password) {
        if (user == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(user,userModel);
        if (password != null) {
            userModel.setEncrptPassword(password.getEncrptPassword());
            return userModel;
        }
        return userModel;
    }
}
