package org.example.chat.bean.baize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.example.instruction.bean.DistrictBean;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class DistrictBeanInfo implements Serializable {

    private Set<DistrictBean> forbiddenProvinceSet;
    private Set<DistrictBean> forbiddenCitySet;

    public DistrictBeanInfo(Set<DistrictBean> forbiddenProvinceSet, Set<DistrictBean> forbiddenCitySet) {
        if (forbiddenProvinceSet == null) {
            forbiddenProvinceSet = new LinkedHashSet<>();
        }
        if (forbiddenCitySet == null) {
            forbiddenCitySet = new LinkedHashSet<>();
        }
        this.forbiddenProvinceSet = forbiddenProvinceSet;
        this.forbiddenCitySet = forbiddenCitySet;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    public DistrictBeanInfo deepClone() {
        return SerializationUtils.clone(this);
    }
}
