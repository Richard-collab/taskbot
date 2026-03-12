package org.example.chat.bean.baize.customtenantline;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.util.List;

@Getter
public class CustomDistrictSupplyLineInfo {

    @SerializedName("省份列表")
    private List<String> provinceList;

    @SerializedName("城市列表")
    private List<String> cityList;

    @SerializedName("线路列表")
    private List<String> supplyLineNameList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
