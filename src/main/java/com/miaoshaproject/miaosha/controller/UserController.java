package com.miaoshaproject.miaosha.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.miaoshaproject.miaosha.controller.viewmodel.UserViewModel;
import com.miaoshaproject.miaosha.dao.UserMapper;
import com.miaoshaproject.miaosha.domain.User;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.Imp.UserServiceImp;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.sound.midi.Soundbank;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private UserServiceImp userServiceImp;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR);
        }
        //是否登陆合法
        UserModel userModel = userServiceImp.validateLogin(telphone, this.encodeMD5(password));

        //加入到session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        System.out.println(httpServletRequest.getSession().getAttribute("IS_LOGIN"));
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);




    }



    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应otpCode相符合
        String inSessionOtpCode  = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (! com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_INVALIDATION_ERROR, "短信不符合手机号");
        }
        UserModel userModel = new UserModel();
        userModel.setTelphone(telphone);
        userModel.setName(name);
        userModel.setGender(gender.byteValue());
        userModel.setAge(age);
        userModel.setRegisterMode("byPhone");
        userModel.setEncrptPassword(this.encodeMD5(password));
        userModel.setThirdPartyId("test");
        userServiceImp.register(userModel);
        return CommonReturnType.create(null);


    }

    private String encodeMD5(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        BASE64Encoder encoder = new BASE64Encoder();
        String res = encoder.encode(md5.digest(string.getBytes("utf-8")));
        return res;

    }

    //用户验证码接口
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        //按照一定规则生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);


        //opt短信与手机号关联,httpsession

        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        //将验证码发送给用户
        System.out.println("telphone = " + telphone + "& optCode =" + otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userServiceImp.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserViewModel userViewModel = convertToViewModel(userModel);
        return CommonReturnType.create(userViewModel);

    }

    private UserViewModel convertToViewModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserViewModel userViewModel = new UserViewModel();
        BeanUtils.copyProperties(userModel,userViewModel);
        return userViewModel;
    }

    @RequestMapping(value = "/getsession")
    public void getSession() {
        System.out.println(httpServletRequest.getSession().getAttribute("IS_LOGIN"));
    }


}
