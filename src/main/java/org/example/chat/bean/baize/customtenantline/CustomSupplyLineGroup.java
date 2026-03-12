package org.example.chat.bean.baize.customtenantline;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.util.List;

@Getter
public class CustomSupplyLineGroup {

    @SerializedName("运营商")
    private String operator;

    @SerializedName("分地区线路列表")
    private List<CustomDistrictSupplyLineInfo> districtSupplyLineInfoList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
