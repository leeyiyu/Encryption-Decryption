package com.lee.uti;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * 功能描述:
 *
 * @param:
 * @return:
 * @auther: Zywoo Lee
 * @date: 2022/11/3 19:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SM2Key implements Serializable {
    private String privateKey;
    private String publicKey;
}
