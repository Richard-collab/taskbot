package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.spire.xls.*;
import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.chat.utils.MsgUtils;
import org.example.utils.*;

import java.awt.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReportFinishedInstructionInstructionBean extends AbstractInstructionBean {

    private static final String PATH_EXCEL = Paths.get("data/运营/今日已完成指令.xlsx").toString();
    private static final String PATH_IMG = Paths.get("data/运营/今日已完成指令.png").toString();

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_FINISHED_INSTRUCTION;

    public ReportFinishedInstructionInstructionBean(
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

//        sb.append("\n");

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
                    ChatGroup chatGroup = this.getChatGroup();
                    return report(chatGroup.getRobotToken());
                };
                return new CallableInfo(callable, 0);
            } else {
                String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                return new CallableInfo(callable, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "执行指令失败！\n" + e + "\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }

    public static ExecutionResult report(String robotToken) {
        return report(PATH_EXCEL, PATH_IMG, robotToken);
    }

    public static ExecutionResult report(String pathExcel, String pathImg, String robotToken) {
        Set<AbstractInstructionBean> finishedInstructionBeanSet = ExecuteInstructionUtils.getFinishedInstructionBeanSet();

        writeAndSendExcel(finishedInstructionBeanSet, pathExcel, pathImg, robotToken);
        return new ExecutionResult(true, "");
    }

    private static synchronized void writeAndSendExcel(
            Set<AbstractInstructionBean> finishedInstructionBeanSet, String pathExcel, String pathImg, String robotToken) {
        writeExcel(finishedInstructionBeanSet, pathExcel, pathImg);

//        MsgUtils.sendQiweiFile(robotToken, pathExcel);
        MsgUtils.sendQiweiImage(robotToken, pathImg);
    }

    private static void writeExcel(
            Set<AbstractInstructionBean> finishedInstructionBeanSet, String pathExcel, String pathImg) {
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
        sheet.setColumnWidth(2, 20);
        sheet.setColumnWidth(3, 30);

        //创建列头单元格样式
        CellStyle styleHeader = workbook.getStyles().addStyle("Header Style");
//        styleHeader.setNumberFormat("@"); // @ 表示文本
        styleHeader.getFont().setSize(10f);
        styleHeader.getFont().setColor(Color.BLACK);
        styleHeader.getFont().isBold(true);
        styleHeader.getFont().setFontName("Arial");
        styleHeader.setWrapText(true); // 设置自动换行
        styleHeader.setHorizontalAlignment(HorizontalAlignType.Center);
        styleHeader.setVerticalAlignment(VerticalAlignType.Center);
        ExcelUtils.setBordersAround(styleHeader.getBorders(), LineStyleType.Thin, Color.BLACK);

        //创建数据单元格样式
        CellStyle styleData = workbook.getStyles().addStyle("Data Style");
//        styleData.setNumberFormat("@"); // @ 表示文本
        styleData.getFont().setSize(10f);
        styleData.getFont().setColor(Color.BLACK);
        styleData.getFont().isBold(false);
        styleData.getFont().setFontName("Arial");
        styleData.setWrapText(true); // 设置自动换行
        styleData.setHorizontalAlignment(HorizontalAlignType.Center);
        styleData.setVerticalAlignment(VerticalAlignType.Center);
        ExcelUtils.setBordersAround(styleData.getBorders(), LineStyleType.Thin, Color.BLACK);

        // 标题
        CellRange cellTitle = sheet.getCellRange(1, 1);
        cellTitle.setStyle(styleHeader);
        cellTitle.setText("运营小助手指令汇报   " + DatetimeUtils.getStrDatetime(new Date()));
        //按范围合并单元格
        CellRange cellTitleMerge = sheet.getRange().get("A" + 1 + ":C" + 1);
        cellTitleMerge.merge();
        ExcelUtils.setBordersAround(cellTitleMerge.getBorders(), LineStyleType.Thin, Color.BLACK);


        String[] headerArray = new String[] {"指令类型", "今日执行数量", "人员分布与占比"};
        //为列头单元格添加数据并应用样式
        for (int colIdx = 1 ; colIdx <= headerArray.length; colIdx++) {
            CellRange header = sheet.getCellRange(2, colIdx);
            header.setValue(headerArray[colIdx - 1]);
            header.setStyle(styleHeader);
//            header.setColumnWidth(15f);
        }

        Map<InstructionType, Set<AbstractInstructionBean>> instructionType2instructionBeanSet = finishedInstructionBeanSet.stream().collect(
                Collectors.groupingBy(x -> x.getInstructionType(), LinkedHashMap::new, Collectors.toSet()));

        int rowIdx = 3;
        List<Map.Entry<InstructionType, Set<AbstractInstructionBean>>> entryList = instructionType2instructionBeanSet.entrySet().stream()
                .sorted(Comparator.comparing(x -> x.getValue().size(), Comparator.reverseOrder()))
                .collect(Collectors.toList());
        for (Map.Entry<InstructionType, Set<AbstractInstructionBean>> entry: entryList) {
            InstructionType instructionType = entry.getKey();
            Set<AbstractInstructionBean> instructionBeanSet = entry.getValue();
            Map<String, Set<AbstractInstructionBean>> creator2instructionBeanSet = instructionBeanSet.stream().collect(
                    Collectors.groupingBy(x -> x.getCreator(), LinkedHashMap::new, Collectors.toSet()));
            String creatorInfo = creator2instructionBeanSet.entrySet().stream()
                    .sorted(Comparator.comparing(x -> x.getValue().size(), Comparator.reverseOrder()))
                    .map(x -> x.getKey() + "  " + x.getValue().size() + " 个（" + NumUtils.roundStringFormat(100.0 * x.getValue().size() / instructionBeanSet.size(), 2)  + "%）")
                    .collect(Collectors.joining("\n"));

            CellRange cellType = sheet.getCellRange(rowIdx, 1);
            cellType.setStyle(styleData);
            String type = instructionType.getName().replace("操作类型_", "");
            cellType.setText(type);

            CellRange cellTypeCnt = sheet.getCellRange(rowIdx, 2);
            cellTypeCnt.setStyle(styleData);
            String typeCnt = instructionBeanSet.size() + " 个（" + NumUtils.roundStringFormat(100.0 * instructionBeanSet.size() / finishedInstructionBeanSet.size(), 2) + "%）";
            cellTypeCnt.setText(typeCnt);

            CellRange cellCreatorInfo = sheet.getCellRange(rowIdx, 3);
            cellCreatorInfo.setStyle(styleData);
            cellCreatorInfo.setText(creatorInfo);

            rowIdx++;
        }

        // 总计
        Map<String, Set<AbstractInstructionBean>> creator2instructionBeanSet = finishedInstructionBeanSet.stream().collect(
                Collectors.groupingBy(x -> x.getCreator(), LinkedHashMap::new, Collectors.toSet()));
        String creatorInfo = creator2instructionBeanSet.entrySet().stream()
                .sorted(Comparator.comparing(x -> x.getValue().size(), Comparator.reverseOrder()))
                .map(x -> x.getKey() + "  " + x.getValue().size() + " 个（" + NumUtils.roundStringFormat(100.0 * x.getValue().size() / finishedInstructionBeanSet.size(), 2)  + "%）")
                .collect(Collectors.joining("\n"));

        CellRange cellType = sheet.getCellRange(rowIdx, 1);
        cellType.setStyle(styleData);
        String type = "总计";
        cellType.setText(type);

        CellRange cellTypeCnt = sheet.getCellRange(rowIdx, 2);
        cellTypeCnt.setStyle(styleData);
        String typeCnt = finishedInstructionBeanSet.size() + " 个（100.00%）";
        cellTypeCnt.setText(typeCnt);

        CellRange cellCreatorInfo = sheet.getCellRange(rowIdx, 3);
        cellCreatorInfo.setStyle(styleData);
        cellCreatorInfo.setText(creatorInfo);

        rowIdx++;

        //冻结前首行
        sheet.freezePanes(3,1);

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

    public static void main(String[] args) {
        report("");
    }
}
