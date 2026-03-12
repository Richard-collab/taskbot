package org.example.ruledetect;

import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.Slot;

import java.util.ArrayList;
import java.util.List;

public class SlotEngine {

    public static List<Slot> getSlotList(QueryInfo queryInfo) {
        List<Slot> slotList = SlotRuleDetect.predict(queryInfo);
        slotList = postprocess(slotList);
        return  slotList;
    }

    public static List<Slot> postprocess(List<Slot> tmpSlotList) {
        List<Slot> slotList = new ArrayList<>(tmpSlotList);
        return SlotPostprocesser.postprocess(slotList);
    }

    public static void main(String[] args) {
//        Creator creator = Creator.Unknown;
        String domain = Domain.SHANGHUYUNYING;
//        String query = "1 2";
//        String query = "并发白泽加到1000,仙人500，得心6k";
//        String query = "360 661提了3个任务，分别使用3条线呼叫";
//        String query = "首轮呼，用仙人线路";
//        String query = "仙人线路并发开1000，白泽线路并发开500";
//        String query = "电信屏蔽泉州";
//        String query = "9点30分前呼完";
//        String query = "设置到13点30结束";
//        String query = "设置到12:05结束";
//        String query = "1111 呼移动和联通   白泽小并发呼  ";
//        String query = "极融挂短移动帮屏蔽上海，bzauto使用白泽新的路由线，剩余规则使用仙人线";
//        String query = "10:00-12:00";
//        String query = "10:00-12:00   得心\n13:30-15:00  仙人";
//        String query = "下午开始只跑01、chip01任务";
//        String query = "不包含电信、移动任务";
//        String query = "联通屏蔽地区广州帮忙放开一下";
//        String query = "chen001 并发改到30";
//        String query = "针对线路:仙人001";
//        String query = "针对线路:保险仙人线路";
//        String query = "2025-10-1到2025-10-12";
//        String query = "文本包含“对你来了一点了”";
        String query = "2025-01-01";
        QueryInfo queryInfo = QueryInfo.createQueryInfo(domain, query);
        List<Slot> slotList = getSlotList(queryInfo);
        System.out.println(slotList);
    }
}
