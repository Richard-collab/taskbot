package org.example.instruction.bean;

import java.util.List;

@Deprecated
public class ForbiddenInfoBean {

    private DistrictBean district;

    private List<Operator> operatorList;

    public ForbiddenInfoBean(DistrictBean district, List<Operator> operatorList) {
        this.district = district;
        this.operatorList = operatorList;
    }

    public DistrictBean getDistrict() {
        return district;
    }

    public List<Operator> getOperatorList() {
        return operatorList;
    }
}
