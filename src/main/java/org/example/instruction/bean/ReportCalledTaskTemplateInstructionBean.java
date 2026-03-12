package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.spire.xls.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AccountOutboundStatisticInfo;
import org.example.chat.bean.baize.TaskTemplate;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.bean.baize.TaskType;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.MsgUtils;
import org.example.utils.ExcelUtils;
import org.example.utils.FileUtils;
import org.example.utils.StringUtils;
import org.example.utils.ThreadUtils;

import java.awt.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReportCalledTaskTemplateInstructionBean extends AbstractInstructionBean {

    private static final String PATH_EXCEL = Paths.get("data/运营/今日在呼任务模板.xlsx").toString();
    private static final String PATH_IMG = Paths.get("data/运营/今日在呼任务模板.png").toString();

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_CALLED_TASK_TEMPLATE;

    public ReportCalledTaskTemplateInstructionBean(String instructionId, ChatGroup chatGroup, String creator) {
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
                Callable<ExecutionResult> callable = () -> report(this.getChatGroup().getRobotToken());
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
        List<AccountScriptTemplateInfo> infoList = getAccountScriptTemplateInfoList();
        writeAndSendExcel(infoList, pathExcel, pathImg, robotToken);
        return new ExecutionResult(true, "");
    }

    public static List<AccountScriptTemplateInfo> getAccountScriptTemplateInfoList() {
        BaizeClient client = BaizeClientFactory.getBaizeClient();
        List<AccountOutboundStatisticInfo> statusInfoList = client.adminGetAccountOutboundStatistic();
        List<AccountScriptTemplateInfo> infoList = new ArrayList<>();
        for (AccountOutboundStatisticInfo statusInfo: statusInfoList) {
            String account = statusInfo.getAccount();
            String groupId = statusInfo.getGroupId();

            List<TaskTemplate> allTaskTemplateList = client.adminGetTaskTemplateList(groupId);
            Map<String, List<TaskTemplate>> scriptStringId2templateInfoList = allTaskTemplateList.stream()
                    .collect(Collectors.groupingBy(x -> x.getScriptStringId(), Collectors.mapping(y -> y, Collectors.toList())));

            List<AiTask> aiTaskList = client.adminFindAiOutboundTasks(account);
            Map<String, String> scriptStringId2scriptName = aiTaskList.stream()
                    .collect(Collectors.toMap(x -> x.getScriptStringId(), y -> y.getSpeechCraftName(), (o1, o2) -> o1));
            Map<String, List<TaskType>> scriptStringId2taskTypeList = aiTaskList.stream()
                    .collect(Collectors.groupingBy(x -> x.getScriptStringId(), Collectors.mapping(y -> y.getTaskType(), Collectors.collectingAndThen(Collectors.toSet(), ArrayList::new))));
            Set<String> scriptStringIdSet = aiTaskList.stream()
                    .map(task -> task.getScriptStringId()).collect(Collectors.toSet());

            List<ScriptTemplateInfo> scriptTemplateInfoList = new ArrayList<>();
            for (String scriptStringId: scriptStringIdSet) {
                String scriptName = scriptStringId2scriptName.get(scriptStringId);
                List<TaskType> taskTypeList = scriptStringId2taskTypeList.get(scriptStringId);
                List<TaskTemplate> taskTemplateList = scriptStringId2templateInfoList.getOrDefault(scriptStringId, Collections.emptyList());
                List<Integer> templateIdList = taskTemplateList.stream()
                            .map(taskTemplate -> taskTemplate.getId())
                            .sorted(Comparator.comparing(x -> x))
                            .collect(Collectors.toList());
                ScriptTemplateInfo scriptTemplateInfo = new ScriptTemplateInfo(scriptName, templateIdList, taskTypeList);
                scriptTemplateInfoList.add(scriptTemplateInfo);
            }
            AccountScriptTemplateInfo info = new AccountScriptTemplateInfo(account, scriptTemplateInfoList);
            infoList.add(info);

            ThreadUtils.sleep(100);
        }
        return infoList;
    }

    private static synchronized void writeAndSendExcel(
            List<AccountScriptTemplateInfo> infoList, String pathExcel, String pathImg, String robotToken) {
        writeExcel(infoList, pathExcel, pathImg);

        MsgUtils.sendQiweiFile(robotToken, pathExcel);
        if (!StringUtils.isEmpty(pathImg)) {
            MsgUtils.sendQiweiImage(robotToken, pathImg);
        }
    }

    private static void writeExcel(List<AccountScriptTemplateInfo> infoList, String pathExcel, String pathImg) {
        //创建Workbook实例
        Workbook workbook = new Workbook();

        //获取第一张工作表（新建的Workbook默认包含3张工作表）
        Worksheet sheet = workbook.getWorksheets().get(0);
        //为第一张工作表设置名称
//        sheet.setName("Data Sheet");

        //设置行高、列宽为自适应（应用于整个工作表）
        sheet.getAllocatedRange().autoFitRows();
//        sheet.getAllocatedRange().autoFitColumns();
        sheet.setColumnWidth(1, 16);
        sheet.setColumnWidth(2, 50);
        sheet.setColumnWidth(4, 10);

        //创建列头单元格样式
        CellStyle styleHeader = workbook.getStyles().addStyle("Header Style");
//        styleHeader.setNumberFormat("@"); // @ 表示文本
        styleHeader.getFont().setSize(10f);
        styleHeader.getFont().setColor(Color.BLACK);
        styleHeader.getFont().isBold(true);
//        styleHeader.getExcelFont().setFontName("");
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
        styleData.setWrapText(true); // 设置自动换行
        styleData.setHorizontalAlignment(HorizontalAlignType.Center);
        styleData.setVerticalAlignment(VerticalAlignType.Center);
        ExcelUtils.setBordersAround(styleData.getBorders(), LineStyleType.Thin, Color.BLACK);

        String[] headerArray = new String[] {"账号", "话术", "模板id", "任务类型"};
        //为列头单元格添加数据并应用样式
        for (int colIdx = 1 ; colIdx <= headerArray.length; colIdx++) {
            CellRange header = sheet.getCellRange(1, colIdx);
            header.setValue(headerArray[colIdx - 1]);
            header.setStyle(styleHeader);
//            header.setColumnWidth(15f);
        }

        int rowIdx = 2;
        for (AccountScriptTemplateInfo info: infoList) {
            int startHeaderRowIndex = rowIdx;

            CellRange cellAccount = sheet.getCellRange(rowIdx, 1);
            cellAccount.setStyle(styleData);
            String account = info.getAccount();
            cellAccount.setText(account);

            for (ScriptTemplateInfo scriptTemplateInfo: info.getScriptTemplateInfoList()) {
                //按范围合并单元格
                CellRange cellAccountMerge = sheet.getRange().get("A" + startHeaderRowIndex + ":A" + rowIdx);
                cellAccountMerge.merge();
                ExcelUtils.setBordersAround(cellAccountMerge.getBorders(), LineStyleType.Thin, Color.BLACK);

                CellRange cellScript= sheet.getCellRange(rowIdx, 2);
                cellScript.setStyle(styleData);
                String scriptName = scriptTemplateInfo.getScriptName();
                cellScript.setText(scriptName);

                CellRange cellTemplateIds = sheet.getCellRange(rowIdx, 3);
                cellTemplateIds.setStyle(styleData);
                String strTemplateIds = scriptTemplateInfo.getTemplateIdList().stream()
                        .map(x -> String.valueOf(x)).collect(Collectors.joining("\n"));
                cellTemplateIds.setText(strTemplateIds);

                CellRange cellTaskTypes = sheet.getCellRange(rowIdx, 4);
                cellTaskTypes.setStyle(styleData);
                String strTaskTypes = scriptTemplateInfo.getTaskTypeList().stream()
                        .map(x -> x.getCaption()).collect(Collectors.joining("\n"));
                cellTaskTypes.setText(strTaskTypes);

                rowIdx++;
            }
        }

        //冻结前首行
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
    }


    @AllArgsConstructor
    @Getter
    @Setter
    static class AccountScriptTemplateInfo {
        private String account;
        private List<ScriptTemplateInfo> scriptTemplateInfoList;

    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class ScriptTemplateInfo {
        private String scriptName;
        private List<Integer> templateIdList;
        private List<TaskType> taskTypeList;
    }


    public static void main(String[] args) {
        List<AccountScriptTemplateInfo> infoList = Lists.newArrayList(
                new AccountScriptTemplateInfo("baotai01", Lists.newArrayList(new ScriptTemplateInfo("【小号提醒】云保（平安百意加千医赠转泰百）-L7.7.1-yy", Lists.newArrayList(1,2), Lists.newArrayList(TaskType.AI_AUTO, TaskType.AI_MANUAL)))),
                new AccountScriptTemplateInfo("baotai02", Lists.newArrayList(new ScriptTemplateInfo("话术2", Lists.newArrayList(2,3), Lists.newArrayList(TaskType.AI_AUTO, TaskType.MANUAL_DIRECT)))),
                new AccountScriptTemplateInfo("yingdian888", Lists.newArrayList(
                        new ScriptTemplateInfo("话术3", Lists.newArrayList(5,6), Lists.newArrayList(TaskType.AI_AUTO, TaskType.AI_MANUAL)),
                        new ScriptTemplateInfo("话术4", Lists.newArrayList(7,8), Lists.newArrayList(TaskType.AI_AUTO, TaskType.AI_MANUAL)))
                )

        );
        writeExcel(infoList, PATH_EXCEL, PATH_IMG);
    }
}
