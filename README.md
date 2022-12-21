# 项目背景

公司的项目为了提高安全性，不希望接口请求报文明文传输，需要在客户端、服务端上引入加解密算法。由于我在上家公司负责过RSA+AES的前后端加密，于是主动请求负责公司项目国密升级落地，此次加密算法上选择了性能更高更安全、符合国密标准的SM2,SM4算法。

为什么要混合加密，而不只使用一种加密算法呢？

如果只选择对称性加密算法的话，加密的秘钥明文传输很容易被解析破解获取明文。而如果只选择非对称加密算法的话，因为**非对称性加密算法加解密效率与加密内容长度相关**，非对称加密算法不适合加密较长报文。于是选择混合加密，每次请求生成固定长度的秘钥key做为对称性加密的秘钥，然后用非对称性加密算法对key进行加密传输。

# 简单介绍

## SM2加密

[SM2在线]: http://lzltool.com/SM2

SM2算法和RSA算法都是公钥密码算法，SM2算法是一种更先进安全的算法，在我们国家商用密码体系中被用来替换RSA算法。

## SM4加密

[SM4在线]: http://lzltool.com/SM4

SMS4分组加密算法是中国无线标准中使用的分组加密算法，在2012年已经被国家商用密码管理局确定为国家密码行业标准。SMS4算法的分组长度为128bit，密钥长度也是128bit。

# 加解密流程

## 基础配置

前端配置SM2公钥

```
04C193B25587EDAA7AF26F5095BC4B7C3B076051A027030C1101B7A875F2F88603ABC16CAAAA064D14ED316669DC655BB61304813BB1E4737A89F54DC894FBD02D
```

后端配置SM2私钥

```
774B2ABD43F6B82EC8D0E438DC48BCB5616AF733E1ED0438F6761CD05E480C35
```

**注意：**上述公钥在头部加了字符串“04”，代表非压缩密钥，根据不同加密工具该字符串可能不需要加；加密结果头部也有字符串“04”，代表非压缩结果。

## 前端GET请求

1.前端在每一次请求前，生成随机16位长度字符串并存储在内存

```js
function () {
        for (var t = 16, e = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"], n = "", A = e.length, r = 0; r < t; r++) {
            var i = Math.floor(Math.random() * A);
            n += e[i]
        }
        return n
    }
```

获得randomKey

```
nc8WJtfjIxRIz9qU
```

2.通过 publicKey 对 randomKey 使用国密算法 sm2 加密（新标准 C1, C3, C2）,randomKey经过SM2加密得到secretKey

```
041b6a4e21a78106edd825adab6e7b5d1cd654efe2feb661041102b19e31387f5ef699e273b5883691944add321d64a21ee6161de2fdf10a8ff853340940ddf2e42fcd3751c602c2ed76f7570bc06fa0c3fa67c5f1b640d061ab2f4d304dc3e5a2c04eaa81c1aa4127aa284d374e3f41204c15df64964c8157
```

3.请求参数转换为json字符串，使用 randomKey（nc8WJtfjIxRIz9qU） 作为密钥，通过国密算法 sm4 加密得到结果

```
weV8YwUr3MIJRUZwGri9frxZrDLXdKz31p90CU27ZmLfksx1Ai3cgVE4JwcaUWj2jKh3VFkypNswjxx4cniLmQ==
```

4.将加密结果encodeURIComponent，生成新参数 encrypt

![image](https://user-images.githubusercontent.com/43566239/208850709-f3f1aaa6-f8e4-4317-bfc1-66556b742161.png)

5.请求方式如上，后端返回加密过的字符串

6.前端用之前生产的randomKey解密，得出结果

```
{"pageSize":"10","pageNum":"1","username":"lee"}
```

## 前端POST请求

前端POST请求前4步骤同GET请求,不同在于请求方式

![image](https://user-images.githubusercontent.com/43566239/208850740-8fa2cd16-4f7e-41c6-a83f-faa461bd82b1.png)



## 后端解密流程

后端的解密+返参加密在切面进行判断处理，不同在于get和post的字段填充方式

```java
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
```

附上整个前端到后端的加解密流程图

![image](https://user-images.githubusercontent.com/43566239/208850774-a4569f85-8e57-4d45-8cd7-2b004d3b3f95.png)

