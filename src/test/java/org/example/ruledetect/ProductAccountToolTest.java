package org.example.ruledetect;

import junit.framework.TestCase;
import org.example.preporcess.Preprocessor;
import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.Slot;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class ProductAccountToolTest extends TestCase {

    @Test
    public void testGetProductSet() {
        String creator = "";
        Set<String> productSet = ProductAccountTool.getInstance().getProductSet();
        for (String product: productSet) {
            String query = Preprocessor.getNormalizedQuery(product);
            QueryInfo queryInfo = new QueryInfo(Domain.SHANGHUYUNYING, query);
            List<Slot> slotList = SlotEngine.getSlotList(queryInfo);
            assertEquals(1, slotList.size());
            assertEquals(product, slotList.stream().map(slot -> slot.getNormedValue()).findFirst().get());
        }
    }
}