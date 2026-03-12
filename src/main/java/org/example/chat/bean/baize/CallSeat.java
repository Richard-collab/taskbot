package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CallSeat implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String name;
    private Integer accountId;
    private String account;
    private String adminRole;
    private String groupId;
    private Integer callTeamId;
    private CallSeatStatus callSeatStatus;
    private List<Integer> taskIds;
    private List<Integer> clueIds;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
