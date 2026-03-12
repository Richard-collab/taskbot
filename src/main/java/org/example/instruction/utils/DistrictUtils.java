package org.example.instruction.utils;

import com.google.gson.reflect.TypeToken;
import org.example.utils.JsonUtils;
import org.example.utils.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DistrictUtils {

    private static final String PROVINCE_CITY_CODE_DICT_PATH = ResourceUtils.getAbsolutePath("/province_city_code_dict.json");
    private static final String PROVINCE_CODE_DICT_PATH = ResourceUtils.getAbsolutePath("/province_code_dict.json");
    private static final String CITY_CODE_DICT_PATH = ResourceUtils.getAbsolutePath("/city_code_dict.json");

    private static Map<String, String> provinceName2provinceCode;
    private static Map<String, String> cityName2cityCode;
    private static Map<String, List<String>> provinceName2cityNameList;
    private static Map<String, String> cityName2provinceName;

    static {
        init();
    }

    private static void init() {
        provinceName2provinceCode = new HashMap<>();
        cityName2cityCode = new HashMap<>();
        provinceName2cityNameList = new HashMap<>();
        cityName2provinceName = new HashMap<>();
        try {
            Map<String, List<String>> map = JsonUtils.fromJsonFile(PROVINCE_CITY_CODE_DICT_PATH, new TypeToken<Map<String, List<String>>>(){});
            map.forEach((provinceInfo, cityInfoList) -> {
                String[] provinceItems = provinceInfo.split(",");
                String provinceName = provinceItems[0];
                String provinceCode = provinceItems[1];
                provinceName2provinceCode.put(provinceName, provinceCode);
                for (String cityInfo: cityInfoList) {
                    String[] cityItems = cityInfo.split(",");
                    String cityName = cityItems[0];
                    String cityCode = cityItems[1];
                    cityName2cityCode.put(cityName, cityCode);
                    cityName2provinceName.put(cityName, provinceName);
                    provinceName2cityNameList.putIfAbsent(provinceName, new ArrayList<>());
                    provinceName2cityNameList.get(provinceName).add(cityName);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        Map<String, String> tmpProvinceCodeDict = getProvinceCodeDict();
//        Map<String, String> tmpCityCodeDict = getCityCodeDict();
//        System.out.println(tmpProvinceCodeDict.keySet().stream().filter(x -> !provinceCodeDict.containsKey(x) || !tmpProvinceCodeDict.get(x).equals(provinceCodeDict.get(x))).collect(Collectors.toList()));
//        System.out.println(provinceCodeDict.keySet().stream().filter(x -> !tmpProvinceCodeDict.containsKey(x)).collect(Collectors.toList()));
//        System.out.println(tmpCityCodeDict.keySet().stream().filter(x -> !cityCodeDict.containsKey(x) || !tmpCityCodeDict.get(x).equals(cityCodeDict.get(x))).collect(Collectors.toList()));
//        System.out.println(cityCodeDict.keySet().stream().filter(x -> !tmpCityCodeDict.containsKey(x)).collect(Collectors.toList()));
    }

//    private static Map<String, String> getProvinceCodeDict() {
//        try {
//            return JsonUtils.fromJsonFile(PROVINCE_CODE_DICT_PATH, new TypeToken<Map<String, String>>(){});
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static Map<String, String> getCityCodeDict() {
//        try {
//            return JsonUtils.fromJsonFile(CITY_CODE_DICT_PATH, new TypeToken<Map<String, String>>(){});
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static String getProvinceCode(String provinceName) {
        return provinceName2provinceCode.get(provinceName);
    }

    public static String getCityCode(String cityName) {
        return cityName2cityCode.get(cityName);

    }

    public static String getProvinceName(String cityName) {
        return cityName2provinceName.get(cityName);
    }

    public static List<String> getCityNameList(String provinceName) {
        return provinceName2cityNameList.get(provinceName);
    }

    public static void main(String[] args) {
        System.out.println(getCityCode("苏州"));
    }
}
