package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.spire.xls.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.chat.bean.baize.script.CorpusType;
import org.example.chat.bean.baize.script.Script;
import org.example.chat.bean.baize.script.ScriptAudioInfo;
import org.example.chat.bean.baize.script.ScriptUnitContent;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.MsgUtils;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReportScriptForLineInstructionBean extends AbstractInstructionBean {

    private static final String LINE_DELIMITER_STRING = ",，。!！?？";
    private static final String JIAFANG_DELIMITER_STRING = "。!！?？";

    private static final String pathZip = Paths.get("data","话术报备", "话术报备.zip").toString();

    private List<String> scriptNameList;

    public ReportScriptForLineInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, List<String> scriptNameList) {
        super(instructionId, instructionType, chatGroup, creator);
        this.scriptNameList = scriptNameList;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();

        sb.append("指令ID：");
        if (!StringUtils.isEmpty(this.getInstructionId())) {
            sb.append(this.getInstructionId());
        }
        sb.append("\n");

        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        sb.append("创建人：");
        if (!StringUtils.isEmpty(this.getCreator())) {
            sb.append(this.getCreator());
        }
        sb.append("\n");

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append("话术名称：");
        if (!CollectionUtils.isEmpty(scriptNameList)) {
            sb.append(String.join("、", scriptNameList));
        }
//        sb.append("\n");

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (CollectionUtils.isEmpty(scriptNameList)) {
            return new CheckResult(false, "话术名称为空");
        }
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
            String msg = "执行指令失败！\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }

    public synchronized ExecutionResult report(String robotToken) {
        BaizeClient client = BaizeClientFactory.getBaizeClient();

        List<Script> allScriptList = client.adminGetScriptList(ScriptStatus.ACTIVE);

        boolean isZip = scriptNameList.size() > 1;
        List<String> pathExcelList = new ArrayList<>();
        List<String> errorInfoList = new ArrayList<>();

        for (String scriptName : scriptNameList) {
            List<Script> scriptList = InstructionUtils.fuzzyFilterScript(allScriptList, scriptName);
            if (CollectionUtils.isEmpty(scriptList)) {
                String msg = "目标话术【" + scriptName + "】不存在或未生效" + "\n" + this.toDescription();
                errorInfoList.add(msg);
                continue;
            }

            for (Script script : scriptList) {
                ScriptAudioInfo scriptAudioInfo = client.getScriptAudioInfo(script.getId());
                if (scriptAudioInfo == null) {
                    String msg = "无法获取话术内容" + "\n" + this.toDescription();
                    errorInfoList.add(msg);
                    continue;
                }

                List<ScriptUnitContent> corpusList = scriptAudioInfo.getScriptUnitContents().stream()
                        .filter(corpus -> (corpus.getCorpusType() == CorpusType.MASTER_ORDINARY || corpus.getCorpusType() == CorpusType.MASTER_CONNECT)
                                && (corpus.getContentName().contains("开场白") || corpus.getContentName().contains("1")))
                        .collect(Collectors.toList());
                ScriptUnitContent firstCorpus = corpusList.stream()
                        .filter(corpus -> corpus.getContentName().equals("开场白")).findFirst().orElse(null);
                if (firstCorpus != null) {
                    corpusList.remove(firstCorpus);
                }

                List<CorpusInfo> corpusInfoList = new LinkedList<>();
                for (ScriptUnitContent corpus : corpusList) {
                    addCorpusInfoList(corpus, corpusInfoList, false, this.getInstructionType());
                }
                Collections.shuffle(corpusInfoList);
                addCorpusInfoList(firstCorpus, corpusInfoList, true, this.getInstructionType());
                if (this.getInstructionType() == InstructionType.ACTION_REPORT_SCRIPT_FOR_LINE) {
                    replaceWithIndex(corpusInfoList);
                }

                String pathExcel = Paths.get("data", "话术报备", script.getScriptName() + "话术报备.xlsx").toString();
                ExecutionResult tmpExecutionResult = writeAndSendExcel(corpusInfoList, robotToken, pathExcel, !isZip);
                if (tmpExecutionResult.isSuccess()) {
                    pathExcelList.add(pathExcel);
                } else {
                    errorInfoList.add(tmpExecutionResult.getMsg());
                }
            }
        }
        // 打包发送
        if (isZip && !pathExcelList.isEmpty()) {
            ExecutionResult tmpExecutionResult = zipAndSend(pathExcelList, robotToken);
            if (!tmpExecutionResult.isSuccess()) {
                errorInfoList.add(tmpExecutionResult.getMsg());
            }
        }
        // 删除生成的excel
        for (String pathExcel: pathExcelList) {
            File file = new File(pathExcel);
            if (file.exists()) {
                file.delete();
            }
        }

        String msg = errorInfoList.isEmpty()? "报备话术已导出，请查收": String.join("、", errorInfoList);
        ExecutionResult executionResult = new ExecutionResult(false, msg); // false是为了能艾特人
        return executionResult;
    }

    private static void addCorpusInfoList(
            ScriptUnitContent corpus, List<CorpusInfo> corpusInfoList, boolean isAddFirst, InstructionType instructionType) {
        if (corpus != null) {
            String name = corpus.getContentName();
            String content = corpus.getContent();
            String delimiterString = getDelimiterString(instructionType);
            List<String> textList = StringUtils.smartSplitChar(content, delimiterString, false);
            for (String text : textList) {
                CorpusInfo corpusInfo = new CorpusInfo(name, text);
                if (isAddFirst) {
                    corpusInfoList.add(0, corpusInfo);
                } else {
                    corpusInfoList.add(corpusInfo);
                }
            }
        }
    }

    private static void replaceWithIndex(List<CorpusInfo> corpusInfoList) {
        int idx = 0;
        for (CorpusInfo corpusInfo : corpusInfoList) {
            idx++;
            corpusInfo.setName(String.valueOf(idx));
        }
    }

    private synchronized ExecutionResult zipAndSend(List<String> pathExcelList, String robotToken) {
        try {
            ZipUtils.zipMultipleFiles(pathExcelList, pathZip);
            MsgUtils.sendQiweiFile(robotToken, pathZip);
            return new ExecutionResult(true, "");
        } catch (Exception e) {
            String msg = "打包发送excel出错\n" + e;
            return new ExecutionResult(false, msg);
        }
    }

    private static String getDelimiterString(InstructionType instructionType) {
        return (instructionType == InstructionType.ACTION_REPORT_SCRIPT_FOR_LINE)? LINE_DELIMITER_STRING: JIAFANG_DELIMITER_STRING;
    }

    private synchronized ExecutionResult writeAndSendExcel(List<CorpusInfo> corpusInfoList, String robotToken, String pathExcel, boolean isSend) {
        boolean flag = writeExcel(corpusInfoList, pathExcel);

        if (flag) {
            if (isSend) {
                MsgUtils.sendQiweiFile(robotToken, pathExcel);
            }
            return new ExecutionResult(true, "");
        } else {
            String msg = "生成excel出错" + "\n" + this.toDescription();
            return new ExecutionResult(false, msg);
        }
    }

    private static boolean writeExcel(List<CorpusInfo> infoList, String pathExcel) {
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

        Date today = new Date();
        String[] headerArray = new String[] {
                "语料名称",
                "文字内容"
        };
        //为列头单元格添加数据并应用样式
        for (int colIdx = 1 ; colIdx <= headerArray.length; colIdx++) {
            CellRange header = sheet.getCellRange(1, colIdx);
            header.setValue(headerArray[colIdx - 1]);
            header.setStyle(styleHeader);
//            header.setColumnWidth(15f);
        }

        int rowIdx = 2;
        for (CorpusInfo info: infoList) {
            CellRange cellName = sheet.getCellRange(rowIdx, 1);
            cellName.setStyle(styleData);
            String name = info.getName();
            cellName.setText(name);

            CellRange cellContent = sheet.getCellRange(rowIdx, 2);
            cellContent.setStyle(styleData);
            String content = info.getContent();
            cellContent.setText(content);

            rowIdx++;
        }

        //冻结首行
        sheet.freezePanes(2,1);

        //保存结果文件
        FileUtils.makeDirsIfParentDirNotExists(pathExcel);
        workbook.saveToFile(pathExcel, FileFormat.Version2013);

        return true;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class CorpusInfo {
        private String name;
        private String content;
    }
}
