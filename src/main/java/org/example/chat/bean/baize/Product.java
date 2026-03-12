package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
public class Product implements Serializable {

    private int id;
    private String productName;
    @SerializedName("industrySecondFieldId")
    private int secondIndustryId;
    @SerializedName("industrySecondFieldName")
    private String secondIndustryName;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
