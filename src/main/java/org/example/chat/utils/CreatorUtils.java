package org.example.chat.utils;

import java.util.HashMap;
import java.util.Map;

public class CreatorUtils {

    private static final Map<String, String> id2name = new HashMap<String, String>() {{
        put("wmeoDzcAAAVvKK9RIARTwuC06kcmwknw", "吉建勋");
        put("wmeoDzcAAAf3MbtM1Joaz_q_0DJ5Lw0w", "年华");
        put("wmeoDzcAAAZFdV4EYS9wS03gpH21VLKw", "杨秋子");
    }};

    public static String map(String id) {
        return id2name.getOrDefault(id, id);
    }
}
