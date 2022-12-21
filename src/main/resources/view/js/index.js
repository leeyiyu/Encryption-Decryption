var randomKeyUtil = {
    //获取key，
    genKey: function () {
        for (var t = 16, e = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"], n = "", A = e.length, r = 0; r < t; r++) {
            var i = Math.floor(Math.random() * A);
            n += e[i]
        }
        return n
    }
};

var sm2Util = {
    //加密
    encrypt: function (data, publicKey) {
        return SM2Utils.encs(data, publicKey, 1);
    },

    //解密
    decrypt: function (data, privateKey) {
        return SM2Utils.encs(data, privateKey, 1)
    }
};


var sm4Util = {

    //加密
    encrypt: function (data, publicKey) {
        return SM2Utils.encs(data, publicKey, 1);
    },

    //解密
    decrypt: function (data, privateKey) {
        SM2Utils.encs(data, privateKey, 1)
    }

};

function subimt() {
    //请求参数
    var data = {
        "username": $("#username").val(),
        "password": $("#password").val()
    };
    //SM2公钥
    var publicKey = "04C193B25587EDAA7AF26F5095BC4B7C3B076051A027030C1101B7A875F2F88603ABC16CAAAA064D14ED316669DC655BB61304813BB1E4737A89F54DC894FBD02D";

    var privateKey = "774B2ABD43F6B82EC8D0E438DC48BCB5616AF733E1ED0438F6761CD05E480C35";
    var randomKey = randomKeyUtil.genKey();
    var secretKey = sm2Util.encrypt(randomKey, publicKey);
    var randomkeytest = sm2Util.decrypt(secretKey, privateKey)
    console.log("randomkeytest:"+randomkeytest);

    var sm4 = new SM4Util(randomKey);
    var encrypt = sm4.encryptData_ECB(JSON.stringify(data));
    var dedata = sm4.decryptData_ECB(encrypt);
    console.log(dedata)

    $("#myh1").html("生成随机16位秘钥:" + randomKey);
    $("#myh2").html("sm2加密后的秘钥:" + JSON.stringify(secretKey));

    //发送请求之前随机获取AES的key
    data = {
        secretKey: secretKey,//
        encrypt: encrypt//
    };
    $("#myh2").html("sm4加密内容:" + JSON.stringify(data));
    $.ajax({
        url: 'http://localhost:7899/login' ,  //发送的地址
        type: 'POST',  //请求的方式
        data: JSON.stringify({
            encrypt:encrypt
        }),//要传入的数据
        contentType: 'application/json', //返回的数据类型
        beforeSend: function (request) {
            request.setRequestHeader("secretKey", secretKey);
            request.setRequestHeader("authorization","Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMzkwMDAwMDAwMCIsImNyZWF0ZWQiOjE2NzAzNzcxODUwNDYsInVzZXJpZCI6MTU5MTc4NTQ4NTM3Njk1NDM3MiwiZXhwIjoxNjcwOTgxOTg1fQ.w8w7axdjZHQVcnbZlImIMxQt2802VwxGBS6RoFZP5flhkWsbgoeklHwnOY4oeuQhU2tHP-hxJ4qXCTnFUIGntQ");
        },
        success: function (res) {
            var data = res;
            $("#myh3").html("后端返回内容:" + JSON.stringify(data));

            var sm4 = new SM4Util(randomKey);
            var realData = sm4.decryptData_ECB(data.encrypt);
            $("#myh4").html("后端解密内容:" + realData);

        }
    });

}


function subimt2() {
    //请求参数
    var data = {
        "pageNum":"1",
        "pageSize":"10",
        "username": $("#username").val()
    };
    //SM2公钥
    var publicKey = "04C193B25587EDAA7AF26F5095BC4B7C3B076051A027030C1101B7A875F2F88603ABC16CAAAA064D14ED316669DC655BB61304813BB1E4737A89F54DC894FBD02D";

    var privateKey = "774B2ABD43F6B82EC8D0E438DC48BCB5616AF733E1ED0438F6761CD05E480C35";
    var randomKey = randomKeyUtil.genKey();
    var secretKey = sm2Util.encrypt(randomKey, publicKey);
    var randomkeytest = sm2Util.decrypt(secretKey, privateKey)
    console.log("randomkeytest:"+randomkeytest);

    var sm4 = new SM4Util(randomKey);
    var encrypt = sm4.encryptData_ECB(JSON.stringify(data));
    var dedata = sm4.decryptData_ECB(encrypt);
    console.log(dedata)

    $("#myh1").html("生成随机16位秘钥:" + randomKey);
    $("#myh2").html("sm2加密后的秘钥:" + JSON.stringify(secretKey));

    //发送请求之前随机获取AES的key
    data = {
        secretKey: secretKey,//
        encrypt: encrypt//
    };
    $("#myh2").html("sm4加密内容:" + JSON.stringify(data));
    $.ajax({
        url: 'http://localhost:7899/get?encrypt=' + encodeURIComponent(encrypt),  //发送的地址
        type: 'GET',  //请求的方式
        //data: JSON.stringify({}),//要传入的数据
        contentType: 'application/json', //返回的数据类型
        beforeSend: function (request) {
            request.setRequestHeader("secretKey", secretKey);
        },
        success: function (res) {
            var data = res;
            $("#myh3").html("后端返回内容:" + JSON.stringify(data));

            var sm4 = new SM4Util(randomKey);
            var realData = sm4.decryptData_ECB(data.encrypt);
            $("#myh4").html("后端解密内容:" + realData);

        }
    });

}