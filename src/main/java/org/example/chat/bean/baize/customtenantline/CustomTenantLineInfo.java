package org.example.chat.bean.baize.customtenantline;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.util.List;
import java.util.Set;

@Getter
public class CustomTenantLineInfo {

    @SerializedName("所属商户")
    private String account;
    @SerializedName("商户线路名称")
    private String tenantLineName;
    @SerializedName("线路状态")
    private String lineStatus;
    @SerializedName("并发上限")
    private Integer maxConcurrency;
    @SerializedName("适用行业")
    private List<String> secondIndustryList;
    @SerializedName("商户线路组成")
    private List<CustomSupplyLineGroup> supplyLineGroupList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    public static void main(String[] args) {
        String json = "["+
                "                {\n" +
                "                        \"所属商户\": \"baotai27\",\n" +
                "                        \"商户线路名称\": null,\n" +
                "                        \"商户线路组成\": [\n" +
                "                                {\n" +
                "                                        \"运营商\": \"联通\",\n" +
                "                                        \"分省线路列表\": [\n" +
                "                                                {\n" +
                "                                                        \"省份列表\": [\n" +
                "                                                                \"河南\"\n" +
                "                                                        ],\n" +
                "                                                        \"线路列表\": [\n" +
                "                                                                \"道泰保险指定限时线路test（主叫dt998）\",\n" +
                "                                                                \"27道泰指定仙人线路test（主叫jrdt006）\",\n" +
                "                                                                \"道泰保险指定限时线路test（主叫dt995）\"\n" +
                "                                                        ]\n" +
                "                                                },\n" +
                "                                                {\n" +
                "                                                        \"省份列表\": [\n" +
                "                                                                \"湖南\"\n" +
                "                                                        ],\n" +
                "                                                        \"线路列表\": [\n" +
                "                                                                \"道泰保险指定限时线路test（主叫dt998）\",\n" +
                "                                                                \"道泰保险指定限时线路test（主叫dt995）\",\n" +
                "                                                                \"27道泰指定仙人线路test（主叫jrdt006）\"\n" +
                "                                                        ]\n" +
                "                                                }\n" +
                "                                        ]\n" +
                "                                }\n" +
                "                        ]\n" +
                "                }\n" +
                "        ]";
        List<CustomTenantLineInfo> customTenantLineInfoList = JsonUtils.fromJson(json, new TypeToken<List<CustomTenantLineInfo>>(){});
        System.out.println(customTenantLineInfoList);
    }
}
