/*
 * Copyright (C) 2011-2019 DL
 *
 * All right reserved.
 *
 * This software is the confidential and proprietary information of DL of China.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the argeements
 * reached into with DL himself.
 *
 */
package com.lee.uti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 *
 * 功能描述:
 *
 * @param:
 * @return:
 * @auther: Zywoo Lee
 * @date: 2022/11/4 11:01
 */
public class ReflectUtils {


    public static void fieldSetter(Object object, Predicate<Field> predicate) {
        Class<?> classType = object.getClass();
        for (; classType != Object.class; classType = classType.getSuperclass()) {
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                predicate.test(field);
            }
        }
    }
}
