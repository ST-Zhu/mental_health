package com.example.mental_health.util;



public class ReplaceBackground {

    //更换背景图片
    public static String replaceImg() {
        String path = System.getProperty("user.dir");// 获取工程所在路径D:\Learn_Java\JavaProject\tea-web
        String imgBase64Str = ImageBase64Converter.getImgBase64Str(path + "/src/main/resources/static/image/background.png");
        return "background-repeat:no-repeat ;background-size:100% 100%;background-attachment: fixed;background-image: url(data:image/png;base64," + imgBase64Str + ")";
    }

}
