package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spire.xls.*;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.bean.baize.LineTaskInfo;
import org.example.chat.bean.baize.TaskStatus;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.MsgUtils;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.ExcelUtils;
import org.example.utils.FileUtils;
import org.example.utils.StringUtils;

import java.awt.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReportAccountLockedConcurrencyInstructionBean extends AbstractInstructionBean {

    private static final String PATH_EXCEL = Paths.get("data/运营/任务并发评估.xlsx").toString();
    private static final String PATH_IMG = Paths.get("data/运营/任务并发评估.png").toString();

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_ACCOUNT_LOCKED_CONCURRENCY;

    private static final Set<TaskStatus> TASK_STATUS_SET = Sets.newHashSet(TaskStatus.ONGONIG);
    private static final int thTotalLockedConcurrency = 180000; // 系统锁定并发没超阈值的，不重新分配并发

    public ReportAccountLockedConcurrencyInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator) {
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
        if (StringUtils.isEmpty(getInstructionId())) {
            return new CheckResult(false,"指令ID为空");
        }
        if (getInstructionType() == null) {
            return new CheckResult(false,"指令类型为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> report(false, this.getChatGroup().getRobotToken());
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

    private static List<LineTaskInfo> getLineTaskInfoList() {
        BaizeClient client = BaizeClientFactory.getBaizeClient();
//                    SystemConcurrency systemConcurrency = client.adminGetSystemConcurrency();
//                    int totalLockedConcurrency = systemConcurrency.getTenantConcurrent();

        List<AiTask> allTaskList = client.adminFindAiOutboundTasks(null, null, null);
        allTaskList = InstructionUtils.filterAiTask(allTaskList, TASK_STATUS_SET);
        Map<String, List<AiTask>> account2taskList = allTaskList.stream()
                .collect(Collectors.groupingBy(AiTask::getAccount, Collectors.mapping(y -> y, Collectors.toList())));

        List<LineTaskInfo> lineTaskInfoList = new ArrayList<>();
        for (Map.Entry<String, List<AiTask>> accountEntry: account2taskList.entrySet()) {
            String account = accountEntry.getKey();
            List<AiTask> accountTaskList = accountEntry.getValue();
            Map<Integer, List<AiTask>> lineId2taskList = accountTaskList.stream()
                    .collect(Collectors.groupingBy(AiTask::getLineId, Collectors.mapping(y -> y, Collectors.toList())));
            for (Map.Entry<Integer, List<AiTask>> entry: lineId2taskList.entrySet()) {
                Integer lineId = entry.getKey();
                List<AiTask> taskList = entry.getValue();
                String lineName = taskList.get(0).getLineName();
                String lineCode = taskList.get(0).getLineCode();
                LineTaskInfo lineTaskInfo = new LineTaskInfo(account, lineId, lineCode, lineName, taskList, null, null);
                lineTaskInfoList.add(lineTaskInfo);
            }
        }

        updateRequiredConcurrency(lineTaskInfoList);
        return lineTaskInfoList;
    }

    public static List<LineTaskInfo> updateRequiredConcurrency(List<LineTaskInfo> lineTaskInfoList) {

        int totalRestPhoneCount = lineTaskInfoList.stream().mapToInt(info -> info.getRestPhoneCount()).sum();

        int totalLockedConcurrency = lineTaskInfoList.stream().mapToInt(info -> info.getLockedConcurrency()).sum();
        if (totalLockedConcurrency <= thTotalLockedConcurrency) {
            for (LineTaskInfo lineTaskInfo: lineTaskInfoList) {
                lineTaskInfo.setRequiredConcurrencyByLock(lineTaskInfo.getLockedConcurrency());
            }
        } else {
            int totalRestConcurrency = thTotalLockedConcurrency;
            Set<LineTaskInfo> restLineTaskInfoSet = new HashSet<>(lineTaskInfoList);
            while (totalRestConcurrency > 0 && restLineTaskInfoSet.size() > 0) {
                for (LineTaskInfo lineTaskInfo : lineTaskInfoList) {
                    if (restLineTaskInfoSet.contains(lineTaskInfo)) {
                        int requiredConcurrency = (int) (totalRestConcurrency * (lineTaskInfo.getRestPhoneCount() / (float) totalRestPhoneCount));
                        if (lineTaskInfo.getRequiredConcurrencyByLock() == null) {
                            if (requiredConcurrency < lineTaskInfo.getTaskCount()) {
                                requiredConcurrency = lineTaskInfo.getTaskCount();
                            }
                            if (requiredConcurrency >= lineTaskInfo.getLockedConcurrency()) {
                                requiredConcurrency = lineTaskInfo.getLockedConcurrency();
                                restLineTaskInfoSet.remove(lineTaskInfo);
                            }
                            lineTaskInfo.setRequiredConcurrencyByLock(requiredConcurrency);
                            totalRestConcurrency -= requiredConcurrency;
                        } else {
                            if (requiredConcurrency <= 0) {
                                restLineTaskInfoSet.remove(lineTaskInfo);
                            } else {
                                int allocatedConcurrency = lineTaskInfo.getRequiredConcurrencyByLock();
                                if (requiredConcurrency >= lineTaskInfo.getLockedConcurrency() - allocatedConcurrency) {
                                    requiredConcurrency = lineTaskInfo.getLockedConcurrency() - allocatedConcurrency;
                                    restLineTaskInfoSet.remove(lineTaskInfo);
                                }
                                lineTaskInfo.setRequiredConcurrencyByLock(requiredConcurrency + allocatedConcurrency);
                                totalRestConcurrency -= requiredConcurrency;
                            }
                        }
                    }
                }
            }
        }
        for (LineTaskInfo lineTaskInfo: lineTaskInfoList) {
            int requiredConcurrency = (int) (thTotalLockedConcurrency * (lineTaskInfo.getRestPhoneCount() / (float) totalRestPhoneCount));
            if (requiredConcurrency < lineTaskInfo.getTaskCount()) {
                requiredConcurrency = lineTaskInfo.getTaskCount();
            }
            lineTaskInfo.setRequiredConcurrencyByPhone(requiredConcurrency);
        }
        return lineTaskInfoList;
    }

    public static ExecutionResult report(boolean isOverkillOnly, String robotToken) {
        return report(isOverkillOnly, PATH_EXCEL, PATH_IMG, robotToken);
    }

    public static ExecutionResult report(boolean isOverkillOnly, String pathExcel, String pathImg, String robotToken) {
        List<LineTaskInfo> lineTaskInfoList = getLineTaskInfoList();
        writeAndSendExcel(lineTaskInfoList, isOverkillOnly, pathExcel, pathImg, robotToken);
        return new ExecutionResult(true, "");
    }

    private static synchronized void writeAndSendExcel(
            List<LineTaskInfo> infoList, boolean isOverkillOnly, String pathExcel, String pathImg, String robotToken) {
        boolean flag = writeExcel(infoList, isOverkillOnly, pathExcel, pathImg);

        if (flag) {
            MsgUtils.sendQiweiFile(robotToken, pathExcel);
            MsgUtils.sendQiweiImage(robotToken, pathImg);
        }
    }

    private static boolean writeExcel(List<LineTaskInfo> infoList, boolean isOverkillOnly, String pathExcel, String pathImg) {
        // 加入总计
//        infoList = new ArrayList<>(infoList);
        infoList = infoList.stream()
                .sorted(Comparator.comparing(x -> x.getLockedConcurrency() - x.getRequiredConcurrencyByLock(), Comparator.reverseOrder()))
                .collect(Collectors.toList());
        List<AiTask> allTaskList = infoList.stream().flatMap(info -> info.getTaskList().stream()).collect(Collectors.toList());
        int totalRequiredConcurrencyByLock = infoList.stream().mapToInt(info -> info.getRequiredConcurrencyByLock()).sum();
        int totalRequiredConcurrencyByPhone = infoList.stream().mapToInt(info -> info.getRequiredConcurrencyByPhone()).sum();
        LineTaskInfo lineTaskInfo = new LineTaskInfo("总计", null, null, "", allTaskList, totalRequiredConcurrencyByLock, totalRequiredConcurrencyByPhone);

        if (isOverkillOnly) {
            infoList = infoList.stream()
                    .filter(info -> info.isConcurrencyOverkill())
                    .collect(Collectors.toList());
        }

        if (infoList.isEmpty()) {
            return false;
        }

        infoList.add(lineTaskInfo);

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
        sheet.setColumnWidth(2, 62);
        sheet.setColumnWidth(3, 10);
        sheet.setColumnWidth(4, 15);
        sheet.setColumnWidth(5, 10);
        sheet.setColumnWidth(6, 10);
        sheet.setColumnWidth(7, 10);
        sheet.setColumnWidth(8, 10);

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
        styleData.setHorizontalAlignment(HorizontalAlignType.Center);
        styleData.setVerticalAlignment(VerticalAlignType.Center);
        ExcelUtils.setBordersAround(styleData.getBorders(), LineStyleType.Thin, Color.BLACK);

        String[] headerArray = new String[] {"账号", "商户线路", "任务数", "剩余名单", "已锁定并发", "应锁定并发\n（按已锁）", "应锁定并发\n（按名单）", "是否超锁"};
        //为列头单元格添加数据并应用样式
        for (int colIdx = 1 ; colIdx <= headerArray.length; colIdx++) {
            CellRange header = sheet.getCellRange(1, colIdx);
            header.setValue(headerArray[colIdx - 1]);
            header.setStyle(styleHeader);
//            header.setColumnWidth(15f);
        }

        int rowIdx = 2;
        for (LineTaskInfo info: infoList) {
            CellRange cellAccount = sheet.getCellRange(rowIdx, 1);
            cellAccount.setStyle(styleData);
            String account = info.getAccount();
            cellAccount.setText(account);

            CellRange cellLine = sheet.getCellRange(rowIdx, 2);
            cellLine.setStyle(styleData);
            String lineName = info.getLineName();
            cellLine.setText(lineName);

            CellRange cellTaskCount = sheet.getCellRange(rowIdx, 3);
            cellTaskCount.setStyle(styleData);
            String taskCount = String.valueOf(info.getTaskCount());
            cellTaskCount.setText(taskCount);

            CellRange cellPhoneCount = sheet.getCellRange(rowIdx, 4);
            cellPhoneCount.setStyle(styleData);
            String phoneCount = String.valueOf(info.getRestPhoneCount());
            cellPhoneCount.setText(phoneCount);

            CellRange cellLockedConcurrency = sheet.getCellRange(rowIdx, 5);
            cellLockedConcurrency.setStyle(styleData);
            String lockedConcurrency = String.valueOf(info.getLockedConcurrency());
            cellLockedConcurrency.setText(lockedConcurrency);

            CellRange cellRequiredConcurrencyByLock = sheet.getCellRange(rowIdx, 6);
            cellRequiredConcurrencyByLock.setStyle(styleData);
            String requiredConcurrencyByLock = String.valueOf(info.getRequiredConcurrencyByLock());
            cellRequiredConcurrencyByLock.setText(requiredConcurrencyByLock);

            CellRange cellRequiredConcurrencyByPhone = sheet.getCellRange(rowIdx, 7);
            cellRequiredConcurrencyByPhone.setStyle(styleData);
            String requiredConcurrencyByPhone = String.valueOf(info.getRequiredConcurrencyByPhone());
            cellRequiredConcurrencyByPhone.setText(requiredConcurrencyByPhone);

            CellRange cellOverkill = sheet.getCellRange(rowIdx, 8);
            cellOverkill.setStyle(styleData);
            String overkill = info.isConcurrencyOverkill()? "是": "否";
            cellOverkill.setText(overkill);

            rowIdx++;
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

        return true;
    }

    public static void main(String[] args) throws Exception {
        List<LineTaskInfo> infoList = Lists.newArrayList(
            new LineTaskInfo("account1", 0, "0", "线路1", Lists.newArrayList(AiTask.builder().callingPhoneNum(10000000).recallingPhoneNum(2000000).concurrency(1000).build()), null, null),
            new LineTaskInfo("account2", 1, "1", "【贷款】10众安贷道泰限时指定线路（主叫dt994）（金融贷款推广）（转人工）", Lists.newArrayList(AiTask.builder().callingPhoneNum(20000000).recallingPhoneNum(3000000).concurrency(200000).build()), null, null)
        );
        updateRequiredConcurrency(infoList);
        writeExcel(infoList, false, PATH_EXCEL, PATH_IMG);
    }
}
