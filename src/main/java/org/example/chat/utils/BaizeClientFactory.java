package org.example.chat.utils;

import com.google.common.collect.ImmutableSet;
import com.google.gson.reflect.TypeToken;
import org.example.utils.JsonUtils;
import org.example.utils.ResourceUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BaizeClientFactory {

    private static final Set<String> LOCAL_ACCOUNT_SET = ImmutableSet.of("admin", "chen001");
    private static final String PATH_ACCOUNT_INFO = ResourceUtils.getAbsolutePath("/account_info.json");
    public static final Map<String, String>
            account2password = getAccount2password(PATH_ACCOUNT_INFO);

    private static final Map<String, BaizeClient> account2client = new ConcurrentHashMap<>();


    public static BaizeClient getBaizeClient() {
        String account = BaseUtils.isLocal()? "admin": "xiaozhushou";
        return getBaizeClient(account);
    }

    public static BaizeClient getBaizeClient(String account, String password) {
        BaizeClient client = account2client.get(account);
        if (client == null) {
            client = new BaizeClient(account, password);
        }
        return client;
    }

    public static BaizeClient getBaizeClient(String account) {
        BaizeClient client = account2client.get(account);
        if (client == null) {
            try {
                String password = account2password.getOrDefault(account, "baotai123");
                try {
                    client = new BaizeClient(account, password);
                } catch (NullPointerException e1) {
                    try {
                        client = new BaizeClient(account, "wangqi123@");
                    } catch (NullPointerException e2) {
                        try {
                            client = new BaizeClient(account, "wangqi@123");
                        } catch (NullPointerException e3) {
                            try {
                                client = new BaizeClient(account, "baotai@123");
                            } catch (NullPointerException e4) {
                                try {
                                    client = new BaizeClient(account, "luoqijia123@");
                                } catch (NullPointerException e5) {
                                    client = new BaizeClient(account, account.replaceAll("[0-9]+", "") + "123");
                                }
                            }
                        }
                    }
                }
                account2client.put(account, client);
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " 登录白泽系统错误：" + e);
            }
        }
        return client;
    }

    public static void warmup() {
        getBaizeClient();
        for (String account: account2password.keySet()) {
            if (!LOCAL_ACCOUNT_SET.contains(account)) {
                getBaizeClient(account);
            }
        }
    }

    private static Map<String, String> getAccount2password(String pathInputJson) {
        try {
            return JsonUtils.fromJsonFile(pathInputJson, new TypeToken<Map<String, String>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
