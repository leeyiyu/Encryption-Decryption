package com.lee.aspect;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lee.annotation.GetParam;
import com.lee.uti.ReflectUtils;
import com.lee.uti.SM2Util;
import com.lee.uti.SM4;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述:SM2+SM4加解密
 *
 * @param:
 * @return:
 * @auther: Zywoo Lee
 * @date: 2022/11/3 21:48
 */
@Slf4j
@Aspect
@Component
public class SafetyAspect {

    /**
     * Pointcut 切入点
     * 匹配
     */
    @Pointcut(value = "execution(public * com.lee.controller.*.*(..))")
    public void safetyAspect() {
    }


    @Around(value = "safetyAspect()")
    public Object around(ProceedingJoinPoint pjp) {


        try {

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            //request对象
            HttpServletRequest request = attributes.getRequest();

            //http请求方法  post get
            String httpMethod = request.getMethod().toLowerCase();

            //method方法
            Method method = ((MethodSignature) pjp.getSignature()).getMethod();

            //method方法上面的注解
            Annotation[] annotations = method.getAnnotations();

            //方法的形参参数
            Object[] args = pjp.getArgs();


            //SM2私钥
            String privateKey = "774B2ABD43F6B82EC8D0E438DC48BCB5616AF733E1ED0438F6761CD05E480C35";
            String secretKey = request.getHeader("secretKey");
            String encrypt = "post".equals(httpMethod) ? (String)getBody(request).get("encrypt") : request.getParameter("encrypt");

            if (StrUtil.isEmpty(secretKey) || StrUtil.isEmpty(encrypt)) {
                //明文传输,直接执行
                Object result = pjp.proceed(args);
                return result;
            }

            String randomKey = SM2Util.decryp(secretKey, privateKey);
            String newRandomKey = new String(Base64.getDecoder().decode(randomKey));
            System.out.println(randomKey);
            System.out.println(newRandomKey);
            String json = SM4.decrypt(encrypt, randomKey);
            JSONObject jsonObject = JSONObject.parseObject(json);
            log.info("解密出来的data数据：" + json);
            if ("post".equals(httpMethod)) {
                if (pjp.getArgs().length > 0) {
                    //入参类属性填充
                    for (int i = 0; i < pjp.getArgs().length; i++) {
                        int finalI = i;
                        ReflectUtils.fieldSetter(pjp.getArgs()[i], (Field field) -> {
                            try {
                                field.set(pjp.getArgs()[finalI], jsonObject.get(field.getName()));
                            } catch (Exception e) {
                                return false;
                            }
                            return true;
                        });
                    }
                }
            }

            if ("get".equals(httpMethod)) {
                //参数注解，一维是参数，二维是注解
                Annotation[][] annotationArray = method.getParameterAnnotations();
                for (int i = 0; i < annotationArray.length; i++) {
                    Annotation[] paramAnn = annotationArray[i];
                    for (Annotation annotation : paramAnn) {
                        //判断当前注解是否为GetParam.class
                        if (annotation.annotationType().equals(GetParam.class)) {
                            GetParam bean = (GetParam) annotation;
                            args[i] = jsonObject.get(bean.value());
                            break;
                        }
                    }
                }
            }


            //执行目标方法
            Object result = pjp.proceed(args);

            String encryptResult = SM4.encrypt(JSONObject.toJSONString(result), randomKey);
            System.out.println(SM4.decrypt(encryptResult, randomKey));
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("encrypt", encryptResult);
            return resultMap;
        } catch (Throwable e) {
            e.printStackTrace();
            //输出到日志文件中
            log.error("加解密异常：" + e.getMessage());
            //TODO 加解密报错异常返回
            return "加解密异常";
        }
    }

    private Map getBody(HttpServletRequest request) {
        BufferedReader br;
        try {
            br = request.getReader();
            String str, wholeStr = "";
            while ((str = br.readLine()) != null) {
                wholeStr += str;
            }
            if(StrUtil.isNotBlank(wholeStr)){
                return JSON.parseObject(wholeStr,Map.class);
            }
        } catch (IOException e) {
            return null;
        }
        return new HashMap();
    }


}
