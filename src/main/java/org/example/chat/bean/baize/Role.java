package org.example.chat.bean.baize;

import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.Set;

public enum Role {

    SHANGHU_ADMIN("商户超级管理员"),
    BAIZE_YUNYING("白泽运营"),
    JIAFANG_GUANLIYUAN("甲方管理员"),
    WAIBU_ZUOXIZUZHANG("外部坐席组长"),
    WAIBU_ZUOXI("外部坐席"),
    ;

    private String caption;

    Role(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static Role fromCaption(String caption) {
        Role result = null;
        for (Role role: Role.values()) {
            if (Objects.equals(role.getCaption(), caption)) {
                result = role;
                break;
            }
        }
        return result;
    }

    public static Set<Role> getCreatableRoleSet() {
        Set<Role> roleSet = Sets.newHashSet(Role.values());
        roleSet.remove(Role.SHANGHU_ADMIN);
        return roleSet;
    }
}

