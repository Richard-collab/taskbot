package org.example.chat.utils;

import com.google.common.collect.ImmutableMap;
import org.example.utils.*;
import org.example.utils.bean.HttpResponse;

import java.util.*;
import java.util.stream.Collectors;

public class MsgRateLimiter {

    private static final SynchronizedMap<String, Limiter> token2limiter = new SynchronizedMap<>(new HashMap<>(), true);

    public static void acquire(String token) {
        Limiter limiter = token2limiter.get(token);
        if (limiter == null) {
            Limiter tmpLimiter = new Limiter(20);
            limiter = token2limiter.putIfAbsent(token, tmpLimiter);
            if (limiter == null) {
                limiter = tmpLimiter;
            }
        }
        limiter.acquire();
    }

    static class Limiter {
        private List<Long> millisList;
        private final int permits;

        public Limiter(int permits) {
            this.permits = permits;
            this.millisList = Collections.emptyList();
        }

        public synchronized void acquire() {
            long currentTime = System.currentTimeMillis();
            millisList = millisList.stream().filter(x -> x >= currentTime - 60000).collect(Collectors.toCollection(LinkedList::new));
            if (millisList.size() + 1 < permits) {
                millisList.add(currentTime);
            } else {
                // 等待到下一分钟开始
                long oldestTime = millisList.stream().mapToLong(x -> x).min().getAsLong();
                long waitTime = oldestTime + 60000 + 5000 - currentTime;
                ThreadUtils.sleep(waitTime);
            }
        }

    }

    public static void main(String[] args) {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=723fd9d6-4071-4551-8556-7349fd8422c0";

        for (int i = 0; i < 360; i++) {
            acquire("1");
            System.out.print(i + " " + DatetimeUtils.getStrDatetime(new Date()) + " ");
            HttpResponse rsp = HttpUtils.doPost(url, "{\"msgtype\": \"text\",\"text\": {\"content\": \"" + i + "\"}}", ImmutableMap.of("Content-Type", "application/json"));
            System.out.println(JsonUtils.toJson(rsp, false));
        }
    }
}
