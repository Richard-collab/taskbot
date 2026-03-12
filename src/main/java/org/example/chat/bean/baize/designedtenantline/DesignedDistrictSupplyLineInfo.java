package org.example.chat.bean.baize.designedtenantline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.instruction.bean.DistrictBean;
import org.example.utils.JsonUtils;

import java.util.List;

@Getter
@AllArgsConstructor
public class DesignedDistrictSupplyLineInfo {

    private List<DistrictBean> provinceList;
    private List<DistrictBean> cityList;

    private List<String> supplyLineNameList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
