package org.example.chat.utils;

import com.google.gson.reflect.TypeToken;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.MsgType;
import org.example.chat.bean.baize.DistrictBeanInfo;
import org.example.instruction.bean.DistrictBean;
import org.example.utils.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForbiddenDistrictUtils {

    private static final String PATH_FORBIDDEN_DISTRICT_INFO = ConstUtils.PATH_FORBIDDEN_DISTRICT_INFO;
    private static final int DEFAULT_START_HOUR = MsgListener.DEFAULT_START_HOUR;
    private static final SynchronizedMap<String, DistrictBeanInfo> account2districtBeanInfo = getSynchronizedMapFromFile(true, PATH_FORBIDDEN_DISTRICT_INFO);

    private static SynchronizedMap<String, DistrictBeanInfo> getSynchronizedMapFromFile(boolean fair, String pathInputJson) {
        Map<String, DistrictBeanInfo> map = new HashMap<>();
        try {
            map = JsonUtils.fromJsonFile(pathInputJson, new TypeToken<Map<String, DistrictBeanInfo>>(){});
        } catch (Exception e) {

        }
        return new SynchronizedMap<>(map, fair, pathInputJson);
    }

    public static void clearAccount2districtBeanInfo() {
        Date nowTime = new Date();
        int nowHour = DatetimeUtils.getHour(nowTime);
        if (0 < nowHour && nowHour < DEFAULT_START_HOUR) {
            account2districtBeanInfo.clear();
        }
    }

    public static void add(String account, Set<DistrictBean> forbiddenProvinceSet, Set<DistrictBean> forbiddenCitySet) {
        if (forbiddenProvinceSet == null) {
            forbiddenProvinceSet = Collections.emptySet();
        }
        if (forbiddenCitySet == null) {
            forbiddenCitySet = Collections.emptySet();
        }
        Set<DistrictBean> finalForbiddenProvinceSet = forbiddenProvinceSet;
        Set<DistrictBean> finalForbiddenCitySet = forbiddenCitySet;
        Function<DistrictBeanInfo, DistrictBeanInfo> function = districtBeanInfo -> {
            if (districtBeanInfo == null) {
                return new DistrictBeanInfo(finalForbiddenProvinceSet, finalForbiddenCitySet);
            } else {
                districtBeanInfo.getForbiddenProvinceSet().addAll(finalForbiddenProvinceSet);
                districtBeanInfo.getForbiddenCitySet().addAll(finalForbiddenCitySet);
                return districtBeanInfo;
            }
        };
        account2districtBeanInfo.put(account, function);
    }

    public static void remove(String account, Set<DistrictBean> forbiddenProvinceSet, Set<DistrictBean> forbiddenCitySet) {
        if (forbiddenProvinceSet == null) {
            forbiddenProvinceSet = Collections.emptySet();
        }
        if (forbiddenCitySet == null) {
            forbiddenCitySet = Collections.emptySet();
        }
        Set<DistrictBean> finalForbiddenProvinceSet = forbiddenProvinceSet;
        Set<DistrictBean> finalForbiddenCitySet = forbiddenCitySet;
        Function<DistrictBeanInfo, DistrictBeanInfo> function = districtBeanInfo -> {
            if (districtBeanInfo == null) {
                return null;
            } else {
                districtBeanInfo.getForbiddenProvinceSet().removeAll(finalForbiddenProvinceSet);
                districtBeanInfo.getForbiddenCitySet().removeAll(finalForbiddenCitySet);
                if (CollectionUtils.isEmpty(districtBeanInfo.getForbiddenProvinceSet()) && CollectionUtils.isEmpty(districtBeanInfo.getForbiddenCitySet())) {
                    districtBeanInfo = null;
                }
                return districtBeanInfo;
            }
        };
        account2districtBeanInfo.put(account, function);
    }

    public static DistrictBeanInfo get(String account) {
        Function<DistrictBeanInfo, DistrictBeanInfo> function = districtBeanInfo -> {
          if (districtBeanInfo == null) {
              return null;
          } else {
              return districtBeanInfo.deepClone();
          }
        };
        return account2districtBeanInfo.get(account, function);
    }

    public static void report(String robotToken) {
        Function<Map<String, DistrictBeanInfo>, String> function = map -> {
            StringBuilder sb = new StringBuilder();
            sb.append("全天屏蔽地区：").append("\n");
            if (map.isEmpty()) {
                sb.append("无");
            } else {
                map.forEach((account, districtBeanInfo) -> {
                    Set<DistrictBean> forbiddenProvinceSet = districtBeanInfo.getForbiddenProvinceSet();
                    Set<DistrictBean> forbiddenCitySet = districtBeanInfo.getForbiddenCitySet();
                    Set<String> districtSet = Stream.concat(forbiddenProvinceSet.stream(), forbiddenCitySet.stream())
                            .map(districtBean -> districtBean.getName()).collect(Collectors.toSet());
                    sb.append(account).append("：").append(String.join("、", districtSet)).append("\n");
                });
            }
            return sb.toString().trim();
        };
        String msg = account2districtBeanInfo.apply(function);
        MsgUtils.sendQiweiMsg(robotToken, msg, MsgType.TEXT, Collections.emptyList());
    }
}
