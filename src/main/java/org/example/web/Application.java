package org.example.web;

import io.jooby.Jooby;
import io.jooby.ServerOptions;
import io.jooby.CorsHandler;
import org.example.chat.bean.MsgType;
import org.example.chat.utils.MsgUtils;

import java.util.ArrayList;

public class Application extends Jooby {

    // 官方文档：https://jooby.io/v2

    {
        setServerOptions(new ServerOptions()
                        .setPort(37782)
//                        .setSecurePort(37782)
                        .setHttpsOnly(false));

        // 允许跨域访问，比如直接通过本地html访问服务器
        decorator(new CorsHandler());

        // 设置 JSON 支持
        install(new GsonModule());

        // 注册控制器
        mvc(new Controller());

//        get("/", ctx -> "{\"data\": \"welcome\"}");

//        post("/submit", ctx -> {
//            Request request = ctx.body().to(Request.class);
//            System.out.println(request.getContent());
//            // 业务逻辑...
//            return request.getContent();
//        });

    }

    public static void main(String[] args) {
        MsgUtils.isSendLocalMsg = true;
        Jooby.runApp(args, Application::new);
    }
}
