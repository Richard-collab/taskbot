package org.example.chat.bean.baize.designedtenantline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.chat.bean.baize.customtenantline.CustomDistrictSupplyLineInfo;
import org.example.chat.bean.baize.customtenantline.CustomSupplyLineGroup;
import org.example.instruction.bean.DistrictBean;
import org.example.instruction.bean.Operator;
import org.example.instruction.utils.DistrictUtils;
import org.example.ruledetect.SlotEngine;
import org.example.ruledetect.bean.QueryInfo;
import org.example.utils.ConstUtils;
import org.example.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class DesignedSupplyLineGroup {
    private static final String STR_DOMAIN_SYSTEM = ConstUtils.STR_DOMAIN_SYSTEM;

    private Operator operator;

    private List<DesignedDistrictSupplyLineInfo> districtSupplyLineInfoList;

    public DesignedSupplyLineGroup(CustomSupplyLineGroup customSupplyLineGroup) {
        this.operator = Operator.fromCaption(customSupplyLineGroup.getOperator());
        this.districtSupplyLineInfoList = new ArrayList<>();
        for (CustomDistrictSupplyLineInfo customDistrictSupplyLineInfo: customSupplyLineGroup.getDistrictSupplyLineInfoList()) {
            List<DistrictBean> provinceList;
            if (customDistrictSupplyLineInfo.getProvinceList() == null) {
                provinceList = Collections.emptyList();
            } else {
                provinceList = customDistrictSupplyLineInfo.getProvinceList().stream()
                        .map(province -> {
                            QueryInfo queryInfo = QueryInfo.createQueryInfo(STR_DOMAIN_SYSTEM, province, province);
                            String name = SlotEngine.getSlotList(queryInfo).get(0).getNormedValue();
                            String code = DistrictUtils.getCityCode(name);
                            return new DistrictBean(name, code);
                        })
                        .collect(Collectors.toList());
            }
            List<DistrictBean> cityList;
            if (customDistrictSupplyLineInfo.getCityList() == null) {
                cityList = Collections.emptyList();
            } else {
                cityList = customDistrictSupplyLineInfo.getCityList().stream()
                        .map(city -> {
                            QueryInfo queryInfo = QueryInfo.createQueryInfo(STR_DOMAIN_SYSTEM, city, city);
                            String name = SlotEngine.getSlotList(queryInfo).get(0).getNormedValue();
                            String code = DistrictUtils.getCityCode(name);
                            return new DistrictBean(name, code);
                        })
                        .collect(Collectors.toList());
            }
            List<String> supplyLineCodeList = customDistrictSupplyLineInfo.getSupplyLineNameList();
            DesignedDistrictSupplyLineInfo info = new DesignedDistrictSupplyLineInfo(provinceList, cityList, supplyLineCodeList);
            this.districtSupplyLineInfoList.add(info);
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
