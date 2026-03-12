package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spire.xls.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.Creator;
import org.example.chat.bean.MsgType;
import org.example.chat.bean.baize.ScriptCallStatistic;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.chat.bean.baize.TaskType;
import org.example.chat.bean.baize.script.Script;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.MsgUtils;
import org.example.utils.*;

import java.awt.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReportRecentlyUpdatedScriptStatisticInstructionBean extends AbstractInstructionBean {

    private static final long MILLIS = 100;
    private static final long TH_COUNT = 100;
    private static final String STR_BLANK = "-";
    private static final String PATH_EXCEL = Paths.get("data/运营/新做话术外呼统计.xlsx").toString();
    private static final String PATH_IMG = Paths.get("data/运营/新做话术外呼统计.png").toString();

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_RECENTLY_UPDATED_SCRIPT_STATISTIC;

    private static final Set<TaskType> TASK_TYPE_SET = Sets.newHashSet(TaskType.AI_AUTO, TaskType.AI_MANUAL);
    private static final List<String> MENTIONED_LIST = Lists.newArrayList(Creator.LuoQiJia.toString(), Creator.ShiLin.toString());

    public ReportRecentlyUpdatedScriptStatisticInstructionBean(String instructionId, ChatGroup chatGroup, String creator) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        sb.append("创建人：");
        if (!StringUtils.isEmpty(this.getCreator())) {
            sb.append(this.getCreator());
        }
        sb.append("\n");

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    try {
                        ExecutionResult executionResult = report(getChatGroup().getRobotToken());
                        if (executionResult.isSuccess()) {
                            executionResult.setMsg("");
                        } else {
                            String msg = "执行指令失败！" + executionResult.getMsg() + "\n" + this.toDescription();
                            executionResult.setMsg(msg);
                        }
                        return executionResult;
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                };
                return new CallableInfo(callable, 0);
            } else {
                String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                return new CallableInfo(callable, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }

    public static ExecutionResult report(String robotToken) {
        return report(PATH_EXCEL, PATH_IMG, robotToken);
    }

    public static ExecutionResult report(String pathExcel, String pathImg, String robotToken) {
        List<ScriptDailyInfo> infoList = getScriptDailyInfoList();
        writeAndSendExcel(infoList, pathExcel, pathImg, robotToken);
        return new ExecutionResult(true, "");
    }

    public static synchronized void writeAndSendExcel(List<ScriptDailyInfo> infoList, String pathExcel, String pathImg, String robotToken) {
        List<ScriptDailyInfo> uncalledInfoList = infoList.stream()
                .filter(info -> info.getT_0count() <= TH_COUNT)
                .collect(Collectors.toList());

        boolean flag = writeExcel(infoList, pathExcel, pathImg);

        if (flag) {
            MsgUtils.sendQiweiFile(robotToken, pathExcel);
            MsgUtils.sendQiweiImage(robotToken, pathImg);
        }

        if (!CollectionUtils.isEmpty(uncalledInfoList)) {
            Set<String> mentionedSet = new HashSet<>(MENTIONED_LIST);
            StringBuilder sb = new StringBuilder()
                    .append("以下话术今日未外呼，请排查：");
            for (ScriptDailyInfo info: uncalledInfoList) {
                String scriptName = info.getScriptName();
                sb.append("\n").append(scriptName).append("  ")
                        .append(getStrCircle(info.t_0count))
                        .append(getStrCircle(info.t_1count))
                        .append(getStrCircle(info.t_2count))
                        .append(getStrCircle(info.t_3count))
                        .append(getStrCircle(info.t_4count));

                Creator creator = Creator.fromScriptName(scriptName);
                if (creator != null) {
                    mentionedSet.add(creator.toString());
                }
            }
            String msg = sb.toString();
            MsgUtils.sendQiweiMsg(robotToken, msg, MsgType.TEXT, mentionedSet);
        }
    }

    private static String getStrCircle(Long count) {
        if (count == null) {
            return "×";
        } else if (count < TH_COUNT) {
            return "○";
        } else {
            return "●";
        }
    }

    private static List<ScriptDailyInfo> getScriptDailyInfoList() {
        Date nowDate = new Date();
        nowDate = DatetimeUtils.setHour(nowDate, 0);
        nowDate = DatetimeUtils.setMinute(nowDate, 0);
        nowDate = DatetimeUtils.setSecond(nowDate, 0);
        nowDate = DatetimeUtils.setMillisecond(nowDate, 0);
        BaizeClient client = BaizeClientFactory.getBaizeClient();
        List<Script> scriptList = client.getScriptList(ScriptStatus.ACTIVE);
        Date dateThreshold = DatetimeUtils.addDay(nowDate, -4);
        scriptList = scriptList.stream()
                .filter(script -> DatetimeUtils.getDatetime(script.getUpdateTime()).after(dateThreshold))
                .collect(Collectors.toList());

        List<ScriptDailyInfo> infoList = new ArrayList<>();
        if (scriptList.size() > 0) {
            String strStartDate = DatetimeUtils.getStrDate(DatetimeUtils.addDay(nowDate, -4));
            String strEndDate = DatetimeUtils.getStrDate(nowDate);
            List<ScriptCallStatistic> scriptCallStatisticList = client.adminGetScriptCallStatisticList(strStartDate, strEndDate);
            Date datetime;
            for (Script script : scriptList) {
                Date updateTime = DatetimeUtils.getDatetime(script.getUpdateTime());
                updateTime = DatetimeUtils.setHour(updateTime, 0);
                updateTime = DatetimeUtils.setMinute(updateTime, 0);
                updateTime = DatetimeUtils.setSecond(updateTime, 0);
                updateTime = DatetimeUtils.setMillisecond(updateTime, 0);

                String scriptStringId = script.getScriptStringId();

                Long t_0count = null;
                datetime = DatetimeUtils.addDay(nowDate, 0);
                if (!updateTime.after(datetime)) {
                    String strDate = DatetimeUtils.getStrDate(datetime);
                    t_0count = scriptCallStatisticList.stream()
                            .filter(x -> Objects.equals(x.getScriptStringId(), scriptStringId) && Objects.equals(x.getStatisticDate(), strDate))
                            .map(x -> (long) x.getTotalNum())
                            .findFirst().orElse(0L);
                    ThreadUtils.sleep(MILLIS);
                }

                Long t_1count = null;
                datetime = DatetimeUtils.addDay(nowDate, -1);
                if (!updateTime.after(datetime)) {
                    String strDate = DatetimeUtils.getStrDate(datetime);
                    t_1count = scriptCallStatisticList.stream()
                            .filter(x -> Objects.equals(x.getScriptStringId(), scriptStringId) && Objects.equals(x.getStatisticDate(), strDate))
                            .map(x -> (long) x.getTotalNum())
                            .findFirst().orElse(0L);
                    ThreadUtils.sleep(MILLIS);
                }

                Long t_2count = null;
                datetime = DatetimeUtils.addDay(nowDate, -2);
                if (!updateTime.after(datetime)) {
                    String strDate = DatetimeUtils.getStrDate(datetime);
                    t_2count = scriptCallStatisticList.stream()
                            .filter(x -> Objects.equals(x.getScriptStringId(), scriptStringId) && Objects.equals(x.getStatisticDate(), strDate))
                            .map(x -> (long) x.getTotalNum())
                            .findFirst().orElse(0L);
                    ThreadUtils.sleep(MILLIS);
                }

                Long t_3count = null;
                datetime = DatetimeUtils.addDay(nowDate, -3);
                if (!updateTime.after(datetime)) {
                    String strDate = DatetimeUtils.getStrDate(datetime);
                    t_3count = scriptCallStatisticList.stream()
                            .filter(x -> Objects.equals(x.getScriptStringId(), scriptStringId) && Objects.equals(x.getStatisticDate(), strDate))
                            .map(x -> (long) x.getTotalNum())
                            .findFirst().orElse(0L);
                    ThreadUtils.sleep(MILLIS);
                }

                Long t_4count = null;
                datetime = DatetimeUtils.addDay(nowDate, -4);
                if (!updateTime.after(datetime)) {
                    String strDate = DatetimeUtils.getStrDate(datetime);
                    t_4count = scriptCallStatisticList.stream()
                            .filter(x -> Objects.equals(x.getScriptStringId(), scriptStringId) && Objects.equals(x.getStatisticDate(), strDate))
                            .map(x -> (long) x.getTotalNum())
                            .findFirst().orElse(0L);
                    ThreadUtils.sleep(MILLIS);
                }

                ScriptDailyInfo info = new ScriptDailyInfo(script.getScriptName(), script.getUpdateTime(), t_0count, t_1count, t_2count, t_3count, t_4count);
                infoList.add(info);
            }
        }

        return infoList;
    }

    private static boolean writeExcel(List<ScriptDailyInfo> infoList, String pathExcel, String pathImg) {
        // 加入总计
//        infoList = new ArrayList<>(infoList);
        infoList = infoList.stream()
                .sorted(Comparator.comparing(x -> x.getUpdateTime(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        //创建Workbook实例
        Workbook workbook = new Workbook();

        //获取第一张工作表（新建的Workbook默认包含3张工作表）
        Worksheet sheet = workbook.getWorksheets().get(0);
        //为第一张工作表设置名称
//        sheet.setName("Data Sheet");

        //设置行高、列宽为自适应（应用于整个工作表）
        sheet.getAllocatedRange().autoFitRows();
//        sheet.getAllocatedRange().autoFitColumns();
        sheet.setColumnWidth(1, 62);
        sheet.setColumnWidth(2, 16);
        sheet.setColumnWidth(3, 16);
        sheet.setColumnWidth(4, 16);
        sheet.setColumnWidth(5, 16);
        sheet.setColumnWidth(6, 16);
        sheet.setColumnWidth(7, 16);

        //创建列头单元格样式
        CellStyle styleHeader = workbook.getStyles().addStyle("Header Style");
//        styleHeader.setNumberFormat("@"); // @ 表示文本
        styleHeader.getFont().setSize(10f);
        styleHeader.getFont().setColor(Color.BLACK);
        styleHeader.getFont().isBold(true);
        styleHeader.getFont().setFontName("微软雅黑");
        styleHeader.setWrapText(true); // 设置自动换行
        styleHeader.setHorizontalAlignment(HorizontalAlignType.Center);
        styleHeader.setVerticalAlignment(VerticalAlignType.Center);
        ExcelUtils.setBordersAround(styleHeader.getBorders(), LineStyleType.Thin, Color.BLACK);

        //创建数据单元格样式
        CellStyle styleData = workbook.getStyles().addStyle("Data Style");
//        styleData.setNumberFormat("@"); // @ 表示文本
        styleData.getFont().setSize(10f);
        styleData.getFont().setColor(Color.BLACK);
//        styleData.getFont().isBold(true);
        styleData.getFont().setFontName("微软雅黑");
        styleData.setWrapText(true); // 设置自动换行
        styleData.setHorizontalAlignment(HorizontalAlignType.Left);
        styleData.setVerticalAlignment(VerticalAlignType.Center);
        ExcelUtils.setBordersAround(styleData.getBorders(), LineStyleType.Thin, Color.BLACK);

        Date today = new Date();
        String[] headerArray = new String[] {
                "话术名称",
                "上线时间",
                "今日外呼",
                DatetimeUtils.getStrDate(DatetimeUtils.addDay(today,-1)) + "外呼",
                DatetimeUtils.getStrDate(DatetimeUtils.addDay(today,-2)) + "外呼",
                DatetimeUtils.getStrDate(DatetimeUtils.addDay(today,-3)) + "外呼",
                DatetimeUtils.getStrDate(DatetimeUtils.addDay(today,-4)) + "外呼"
        };
        //为列头单元格添加数据并应用样式
        for (int colIdx = 1 ; colIdx <= headerArray.length; colIdx++) {
            CellRange header = sheet.getCellRange(1, colIdx);
            header.setValue(headerArray[colIdx - 1]);
            header.setStyle(styleHeader);
//            header.setColumnWidth(15f);
        }

        int rowIdx = 2;
        for (ScriptDailyInfo info: infoList) {
            CellRange cellScriptName = sheet.getCellRange(rowIdx, 1);
            cellScriptName.setStyle(styleData);
            String scriptName = info.getScriptName();
            cellScriptName.setText(scriptName);

            CellRange cellUpdateTime = sheet.getCellRange(rowIdx, 2);
            cellUpdateTime.setStyle(styleData);
            String updateTime = info.getUpdateTime();
            String updateDate = DatetimeUtils.getStrDate(DatetimeUtils.getDatetime(updateTime));
            cellUpdateTime.setText(updateDate);

            CellRange cellT_0count = sheet.getCellRange(rowIdx, 3);
            cellT_0count.setStyle(styleData);
            String t_0count = (info.getT_0count() == null)? STR_BLANK: String.valueOf(info.getT_0count());
            cellT_0count.setText(t_0count);

            CellRange cellT_1count = sheet.getCellRange(rowIdx, 4);
            cellT_1count.setStyle(styleData);
            String t_1count = (info.getT_1count() == null)? STR_BLANK: String.valueOf(info.getT_1count());
            cellT_1count.setText(t_1count);

            CellRange cellT_2count = sheet.getCellRange(rowIdx, 5);
            cellT_2count.setStyle(styleData);
            String t_2count = (info.getT_2count() == null)? STR_BLANK: String.valueOf(info.getT_2count());
            cellT_2count.setText(t_2count);

            CellRange cellT_3count = sheet.getCellRange(rowIdx, 6);
            cellT_3count.setStyle(styleData);
            String t_3count = (info.getT_3count() == null)? STR_BLANK: String.valueOf(info.getT_3count());
            cellT_3count.setText(t_3count);

            CellRange cellT_4count = sheet.getCellRange(rowIdx, 7);
            cellT_4count.setStyle(styleData);
            String t_4count = (info.getT_4count() == null)? STR_BLANK: String.valueOf(info.getT_4count());
            cellT_4count.setText(t_4count);

            rowIdx++;
        }

        //冻结首行
        sheet.freezePanes(2,1);

        if (!StringUtils.isEmpty(pathImg)) {
            //设置水平分辨率及垂直分辨率
            workbook.getConverterSetting().setXDpi(300);
            workbook.getConverterSetting().setYDpi(300);
            //保存图片
            FileUtils.makeDirsIfParentDirNotExists(pathImg);
            sheet.saveToImage(pathImg);
        }

        //保存结果文件
        FileUtils.makeDirsIfParentDirNotExists(pathExcel);
        workbook.saveToFile(pathExcel, FileFormat.Version2013);

        return true;
    }

    @Getter
    @AllArgsConstructor
    static class ScriptDailyInfo {

        private String scriptName;
        private String updateTime;
        private Long t_0count;
        private Long t_1count;
        private Long t_2count;
        private Long t_3count;
        private Long t_4count;
    }

    public static void main(String[] args) {
        report(null);
    }
}
