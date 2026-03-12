package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
public class BlacklistInfo implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String groupName;
    private String targetType;
    private Integer targetLevel;
    private Integer limitDuration;
    private String targetComment;
    private String putThroughComment;
    private String benefitComment;
    private String costBenefitComment;
    private String comment;
    private int phoneCount;
    private String groupType; // "1"：运营端黑名单；"2"：商户端黑名
    private String groupStatus;
    private String groupId; // 商户端黑名单是对应的groupId，运营端黑名单为null

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
