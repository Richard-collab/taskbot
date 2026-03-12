package org.example.ruledetect;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import javafx.util.Pair;
import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.IntentInfo;
import org.example.ruledetect.bean.QueryInfo;
import org.example.utils.CollectionUtils;
import org.example.utils.ResourceUtils;
import org.example.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IntentRuleDetectTest {

    public static final List<Pair<String, String>> pathInputTsvPairList = Arrays.asList(
            new Pair<>(Domain.SHANGHUYUNYING, ResourceUtils.getAbsolutePath("/regression_test_data.tsv"))
    );

    @Test
    public void regressionTest() {
        IntentRuleDetect.init();
        String creator = "";
        for (Pair<String, String> pair: pathInputTsvPairList) {
            String domain = pair.getKey();
            String pathInputTsv = pair.getValue();
            List<RegressionInput> regressionInputList = getRegressionInputList(pathInputTsv);
            Stopwatch stopwatch = Stopwatch.createStarted();
            for (RegressionInput regressionInput : regressionInputList) {
                String originalGoldIntent = regressionInput.getGoldIntent();
                String goldIntent = getMappedIntent(originalGoldIntent, domain);
                String query = regressionInput.getQuery();
                QueryInfo queryInfo = new QueryInfo(domain, query);
                List<IntentInfo> intentInfoList = IntentRuleDetect.predict(queryInfo);
                if (regressionInput.isNeedHit()) {
                    if (regressionInput.isFirstIntent()) {
                        String predictIntent = CollectionUtils.isEmpty(intentInfoList) ? null : intentInfoList.get(0).getIntent();
                        predictIntent = getMappedIntent(predictIntent, domain);
                        if (!Objects.equals(goldIntent, predictIntent)) {
                            String info = "goldIntent: " + originalGoldIntent + ", predictIntent: " + predictIntent
                                    + ", queryInfo: " + queryInfo + ", intentInfoList: " + intentInfoList;
                            Assert.assertEquals(info, goldIntent, predictIntent);
                        }
                    } else {
                        List<String> intentList = intentInfoList.stream()
                                .map(IntentInfo::getIntent)
                                .map(intent -> getMappedIntent(intent, domain))
                                .collect(Collectors.toList());
                        if (!intentList.contains(goldIntent)) {
                            String info = "goldIntent: " + originalGoldIntent + ", predictIntent: " + intentList
                                    + ", queryInfo: " + queryInfo + ", intentInfoList: " + intentInfoList;
                            Assert.assertTrue(info, intentList.contains(goldIntent));
                        }
                    }
                } else {
                    List<String> intentList = intentInfoList.stream()
                            .map(IntentInfo::getIntent)
                            .map(intent -> getMappedIntent(intent, domain))
                            .collect(Collectors.toList());
                    if (intentList.contains(goldIntent)) {
                        String info = "goldNotHitIntent: " + originalGoldIntent + ", predictIntent: " + intentList
                                + ", queryInfo: " + queryInfo + ", intentInfoList: " + intentInfoList;
                        Assert.assertFalse(info, intentList.contains(goldIntent));
                    }
                }
            }
            float avgCost = 1.0f * stopwatch.elapsed(TimeUnit.MILLISECONDS) / regressionInputList.size();
            System.out.println(avgCost + " ms");
        }
    }

    private static String getMappedIntent(String intent, String domain) {
        if (ImmutableSet.of("反感_高度厌烦", "反感_中度厌烦", "反感_轻度厌烦").contains(intent)) {
            return "反感_厌烦";
        } else if (ImmutableSet.of("反感_脏话", "反感_轻度脏话").contains(intent)) {
            return "反感_脏话";
        } else if (ImmutableSet.of("肯定_高度肯定", "肯定_中度肯定", "肯定_轻度肯定").contains(intent)) {
            return "肯定_肯定";
//        } else if (ImmutableSet.of("已操作过", "操作完成").contains(intent)) {
//            return "已操作过";
//        } else if (intent != null && intent.startsWith("保费_")) {
//            return intent.replace("保费_","费用_");
//        } else if (ImmutableSet.of("投保条件_71岁及以上", "投保条件_81岁及以上").contains(intent)) {
//                return "投保条件_71岁及以上";
        } else {
//            return (intent == null)? null: WritePreRuleTsv.getIntent(intent, domain);
            return intent;
        }
    }

    static List<RegressionInput> getRegressionInputList(String pathInputTsv) {
        List<RegressionInput> regressionInputList = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(pathInputTsv), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                String[] items = line.split("\t");
                String query = items[0];
                String goldIntent = (items.length >= 2 && !StringUtils.isEmpty(items[1])) ? items[1] : null;
                boolean isFirstIntent = (items.length >= 3 && !StringUtils.isEmpty(items[2])) ? Boolean.parseBoolean(items[2]): true;
                boolean isNeedHit = (items.length >= 4 && !StringUtils.isEmpty(items[3])) ? Boolean.parseBoolean(items[3]): true;
                RegressionInput regressionInput = new RegressionInput(query, goldIntent, isFirstIntent, isNeedHit);
                regressionInputList.add(regressionInput);
            }
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return regressionInputList;
    }

    static class RegressionInput {

        private String query;
        private String goldIntent;

        private boolean firstIntent;

        private boolean needHit;

        public RegressionInput(String query, String goldIntent, boolean firstIntent, boolean needHit) {
            this.query = query;
            this.goldIntent = goldIntent;
            this.firstIntent = firstIntent;
            this.needHit = needHit;
        }

        public String getQuery() {
            return query;
        }

        public String getGoldIntent() {
            return goldIntent;
        }

        public boolean isFirstIntent() {
            return firstIntent;
        }

        public boolean isNeedHit() {
            return needHit;
        }
    }
}