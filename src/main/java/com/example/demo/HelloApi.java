
package com.example.demo;

import net.sf.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@EnableAutoConfiguration
public class HelloApi {

    //获取Bing壁纸URL，创建图片，返回到页面
    //类似获取验证码
    @RequestMapping("/")
    public ModelAndView handleRequest(HttpServletResponse response) throws Exception {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        URL url = new URL(getBingImgUrl(false)); //声明url对象
        URLConnection connection = url.openConnection(); //打开连接
        connection.setDoOutput(true);
        BufferedImage src = ImageIO.read(connection.getInputStream()); //读取连接的流，赋值给BufferedImage对象
        //输出图象到页面
        ImageIO.write(src, "JPEG", response.getOutputStream());
        ServletOutputStream out = response.getOutputStream();
        try {
            out.flush();
        } finally {
            out.close();
        }
        return null;
    }

    //获取Bing壁纸URL
    //flag=true 获取今天图片  flag=false 获取随机一天图片
    //后期扩展  每天自动存到图片服务器，抓取图床图片
    @ResponseBody
    @RequestMapping("/getImgUrl")
    public String getBingImgUrl(boolean flag) {
        String imgSrc = "";
        try {
            String url = "https://cn.bing.com/HPImageArchive.aspx";
            Map params = new HashMap();//请求参数
            params.put("format", "js");
            int day = flag == true ? 0 : new Random().nextInt(10);//获取必应最近7天壁纸，必应限制只显示最近7天，随机获取，大于7，显示7的壁纸
            params.put("idx", day);
            params.put("n", "2");
            String res = DoRequest.httpGet(url, params);
            JSONObject object = JSONObject.fromObject(res);
            String imgObj = object.get("images").toString();
            if (imgObj.startsWith("[")) {
                imgObj = imgObj.substring(1, imgObj.length() - 1);
            }
            JSONObject images = JSONObject.fromObject(imgObj);
            String imgUrl = images.get("url").toString();
            imgSrc = "https://cn.bing.com" + imgUrl;
        } catch (Exception e) {//报错则返回今天的图片
            String url = "https://cn.bing.com/HPImageArchive.aspx";
            Map params = new HashMap();//请求参数
            params.put("format", "js");
            params.put("idx", "0");
            params.put("n", "1");
            String res = DoRequest.httpGet(url, params);
            JSONObject object = JSONObject.fromObject(res);
            String imgObj = object.get("images").toString();
            if (imgObj.startsWith("[")) {
                imgObj = imgObj.substring(1, imgObj.length() - 1);
            }
            JSONObject images = JSONObject.fromObject(imgObj);
            String imgUrl = images.get("url").toString();
            imgSrc = "https://cn.bing.com" + imgUrl;
        }
        return imgSrc;
    }


    //获取必应壁纸最近7天  返回到html页面
    //flag=true 获取今天图片  flag=false 获取随机一天图片
    //后期扩展  每天自动存到图片服务器，抓取图床图片
    @RequestMapping("/bing")
    public String getBingPicture(HttpServletRequest request, boolean flag) {
        //方法一：返回图片Base64
//        String imgSrc = ImageUtils.getBase64ByImgUrl(getBingImgUrl(flag));
//        request.setAttribute("imgSrc", "data:image/jpg;base64, " + imgSrc);

        //方法二：返回图片URL
        String imgSrc = getBingImgUrl(flag);
        request.setAttribute("imgSrc", imgSrc);
        return "/index";
    }

}