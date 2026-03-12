package org.example.ruledetect;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.example.chat.utils.BaizeClientFactory;
import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.Keyword;
import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.Slot;
import org.example.ruledetect.slotrule.AbstractSlotRule;
import org.example.ruledetect.slotrule.WhitelistSlotRule;
import org.example.utils.ConstUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SlotEngineTest extends TestCase {

    private static final String STR_ACCOUNT = ConstUtils.STR_ACCOUNT;

    public void testGetSlotList() {
        String domain = Domain.SHANGHUYUNYING;
        Set<Keyword> keywordSet = BaizeClientFactory.account2password.keySet().stream()
                .map(account -> new Keyword(STR_ACCOUNT, account.toLowerCase(), account.toLowerCase(), 1.0f, "custom_" + domain))
                .collect(Collectors.toSet());
        keywordSet.add(new Keyword(STR_ACCOUNT, "yunshenghua888", "yunshenghua888", 1.0f, "custom_" + domain));
        Set<String> domainSet = Sets.newHashSet(domain);
        AbstractSlotRule whitelistSLotRule = new WhitelistSlotRule(keywordSet, domainSet);
        SlotRuleDetect.resetCustomSlotRuleList(domain, Lists.newArrayList(whitelistSLotRule));

        String creator = "";

        QueryInfo queryInfo;
        List<Slot> slotList;

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "设置到十一点三十五结束");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(1, slotList.size());
        assertEquals("预期完成时间", slotList.get(0).getSlotName());
        assertEquals("11:35", slotList.get(0).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "设置到十一点零五分结束");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(1, slotList.size());
        assertEquals("预期完成时间", slotList.get(0).getSlotName());
        assertEquals("11:05", slotList.get(0).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "设置到12:05结束");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(1, slotList.size());
        assertEquals("预期完成时间", slotList.get(0).getSlotName());
        assertEquals("12:05", slotList.get(0).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "设置到12点一刻结束");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(1, slotList.size());
        assertEquals("预期完成时间", slotList.get(0).getSlotName());
        assertEquals("12:15", slotList.get(0).getNormedValue());


        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "设置到12点半结束");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(1, slotList.size());
        assertEquals("预期完成时间", slotList.get(0).getSlotName());
        assertEquals("12:30", slotList.get(0).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "设置到12点结束");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(1, slotList.size());
        assertEquals("预期完成时间", slotList.get(0).getSlotName());
        assertEquals("12:00", slotList.get(0).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "chen001：长沙 重庆放开全天屏蔽");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(4, slotList.size());
        assertEquals("账号", slotList.get(0).getSlotName());
        assertEquals("chen001", slotList.get(0).getNormedValue());
        assertEquals("屏蔽城市", slotList.get(1).getSlotName());
        assertEquals("长沙", slotList.get(1).getNormedValue());
        assertEquals("屏蔽省份", slotList.get(2).getSlotName());
        assertEquals("重庆", slotList.get(2).getNormedValue());
        assertEquals("全天生效", slotList.get(3).getSlotName());
        assertEquals("是", slotList.get(3).getNormedValue());


        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "yunshenghua888：长沙 重庆放开屏蔽");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(3, slotList.size());
        assertEquals("账号", slotList.get(0).getSlotName());
        assertEquals("yunshenghua888", slotList.get(0).getNormedValue());
        assertEquals("屏蔽城市", slotList.get(1).getSlotName());
        assertEquals("长沙", slotList.get(1).getNormedValue());
        assertEquals("屏蔽省份", slotList.get(2).getSlotName());
        assertEquals("重庆", slotList.get(2).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "yunshenghua888：长沙 重庆放开屏蔽\n全天生效：否");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(4, slotList.size());
        assertEquals("账号", slotList.get(0).getSlotName());
        assertEquals("yunshenghua888", slotList.get(0).getNormedValue());
        assertEquals("屏蔽城市", slotList.get(1).getSlotName());
        assertEquals("长沙", slotList.get(1).getNormedValue());
        assertEquals("屏蔽省份", slotList.get(2).getSlotName());
        assertEquals("重庆", slotList.get(2).getNormedValue());
        assertEquals("全天生效", slotList.get(3).getSlotName());
        assertEquals("否", slotList.get(3).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "任务名称包含移动、联通、电信");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(3, slotList.size());
        assertEquals("任务名称包含", slotList.get(0).getSlotName());
        assertEquals("移动", slotList.get(0).getNormedValue());
        assertEquals("任务名称包含", slotList.get(1).getSlotName());
        assertEquals("联通", slotList.get(1).getNormedValue());
        assertEquals("任务名称包含", slotList.get(2).getSlotName());
        assertEquals("电信", slotList.get(2).getNormedValue());

        queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, "任务创建时间在08:00到12:30之间");
        slotList = SlotEngine.getSlotList(queryInfo);
        assertEquals(2, slotList.size());
        assertEquals("任务创建时间上限", slotList.get(0).getSlotName());
        assertEquals("08:00", slotList.get(0).getNormedValue());
        assertEquals("任务创建时间下限", slotList.get(1).getSlotName());
        assertEquals("12:30", slotList.get(1).getNormedValue());
    }
}