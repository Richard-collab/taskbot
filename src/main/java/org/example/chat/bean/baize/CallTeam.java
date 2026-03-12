package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CallTeam implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String callTeamName;
    private String tenantLineNumber;
    private Integer leaderAccountId;
    private List<Integer> callSeatIds;
    private String groupId;
    private List<Integer> taskIds;
    private Map<Integer, List<Integer>> taskIdActiveCallSeatIdMap;
    private List<CallSeat> callSeats;
    private UserInfo leaderAccount;
//    private UserInfo masterAccount;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
