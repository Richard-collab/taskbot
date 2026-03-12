package org.example.instruction.bean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.chat.bean.baize.script.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.ScriptUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;
import org.example.utils.ThreadUtils;
import org.example.utils.bean.HttpResult;

import java.util.*;
import java.util.concurrent.Callable;

public class ReplacePhraseInScriptInstructionBean extends AbstractInstructionBean {

    private static final long MILLIS = 100;
    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPLACE_PHRASE_IN_SCRIPT;
    private ReplaceType replaceType;
    private String scriptName;
    private String oldPhrase;
    private String newPhrase;

    public ReplacePhraseInScriptInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, ReplaceType replaceType, String scriptName,
            String oldPhrase, String newPhrase) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.replaceType = replaceType;
        this.scriptName = scriptName;
        this.oldPhrase = oldPhrase;
        this.newPhrase = newPhrase;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();

        String tmpInstructionId = this.getInstructionId();
        if (StringUtils.isEmpty(tmpInstructionId)) {
            tmpInstructionId = "";
        }
        sb.append("指令ID：").append(tmpInstructionId).append("\n");

        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        String tmpCreator = this.getCreator();
        if (StringUtils.isEmpty(tmpCreator)) {
            tmpCreator = "";
        }
        sb.append("创建人：").append(tmpCreator).append("\n");

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append("替换类型：").append(replaceType.getCaption()).append("\n");
        sb.append("话术名称：").append(scriptName).append("\n");
        sb.append("原文本：").append(oldPhrase).append("\n");
        sb.append("新文本：").append(newPhrase).append("\n");

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
        if (replaceType == null) {
            return new CheckResult(false,"替换类型为空");
        }
        if (StringUtils.isEmpty(scriptName)) {
            return new CheckResult(false,"话术名称为空");
        }
        if (StringUtils.isEmpty(oldPhrase)) {
            return new CheckResult(false,"原文本为空");
        }
        if (StringUtils.isEmpty(newPhrase)) {
            return new CheckResult(false,"新文本为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    Long scriptId = null;
                    try {
                        BaizeClient client = BaizeClientFactory.getBaizeClient();

                        // 获取话术
                        List<Script> scriptList = client.getScriptList(ScriptStatus.EDIT);
                        Script script = scriptList.stream()
                                .filter(x -> Objects.equals(x.getScriptName(), scriptName))
                                .findFirst().orElse(null);
                        if (script == null) {
                            String msg = "执行指令失败！话术不存在或不在编辑状态" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        scriptId = script.getId();
                        boolean isLocked = client.lockScript(scriptId);
                        if (!isLocked) {
                            String msg = "执行指令失败！无法锁定话术" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        // 处理主流程语料
                        List<String> succeedMainCorpusNameList = new ArrayList<>();
                        List<String> failedMainCorpusNameList = new ArrayList<>();
                        List<String> failedMainScriptCanvasNameList = new ArrayList<>();
                        List<ScriptCanvas> mainScriptCanvasList = client.getMainScriptCanvasList(scriptId);
                        for (ScriptCanvas canvas : mainScriptCanvasList) {
                            boolean isMainChanged = false;
                            ScriptCanvas scriptCanvas = client.getMainScriptCanvas(canvas.getId());
                            Map<Long, CanvasCorpus> canvasCorpusDataMap = scriptCanvas.getCanvasCorpusDataMap();
                            Set<Long> corpusIdSet = canvasCorpusDataMap.keySet();
                            for (long corpusId : corpusIdSet) {
                                ScriptCorpus scriptCorpus = client.getScriptCorpus(corpusId);
                                boolean isChanged = processScriptCorpus(scriptCorpus, scriptCanvas, replaceType, oldPhrase, newPhrase, client,
                                        succeedMainCorpusNameList, failedMainCorpusNameList);
                                if (isChanged) {
                                    isMainChanged = true;
                                }
                            }
                            if (isMainChanged) {
                                boolean success = client.saveMainScriptCanvas(scriptCanvas);
                                ThreadUtils.sleep(MILLIS);

                                if (!success) {
                                    failedMainScriptCanvasNameList.add(scriptCanvas.getName());
                                }
                            }
                        }

                        // 处理知识库
                        List<String> succeedKbQaCorpusNameList = new ArrayList<>();
                        List<String> failedKbQaCorpusNameList = new ArrayList<>();
                        List<String> succeedKbCanvasCorpusNameList = new ArrayList<>();
                        List<String> failedKbCanvasCorpusNameList = new ArrayList<>();
                        List<String> failedKbScriptCanvasNameList = new ArrayList<>();
                        List<KnowledgeGroup> knowledgeGroupList = client.getKnowledgeBaseGroupList(scriptId);
                        for (KnowledgeGroup knowledgeGroup : knowledgeGroupList) {
                            long groupId = knowledgeGroup.getId();

                            // 处理知识库基本问答
                            List<ScriptCorpus> scriptCorpusList = client.getKbQaScriptCorpusList(groupId);
                            for (ScriptCorpus scriptCorpus : scriptCorpusList) {
                                processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                        succeedKbQaCorpusNameList, failedKbQaCorpusNameList);
                            }

                            // 处理知识库深层沟通
                            List<ScriptCanvas> kbScriptCanvasList = client.getKbScriptCanvasList(groupId);
                            for (ScriptCanvas canvas : kbScriptCanvasList) {
                                boolean isMainChanged = false;
                                ScriptCanvas scriptCanvas = client.getKbMainScriptCanvas(canvas.getId());
                                Map<Long, CanvasCorpus> canvasCorpusDataMap = scriptCanvas.getCanvasCorpusDataMap();
                                Set<Long> corpusIdSet = canvasCorpusDataMap.keySet();
                                for (long corpusId : corpusIdSet) {
                                    ScriptCorpus scriptCorpus = client.getScriptCorpus(corpusId);
                                    boolean isChanged = processScriptCorpus(scriptCorpus, scriptCanvas, replaceType, oldPhrase, newPhrase, client,
                                            succeedKbCanvasCorpusNameList, failedKbCanvasCorpusNameList);
                                    if (isChanged) {
                                        isMainChanged = true;
                                    }
                                }
                                if (isMainChanged) {
                                    boolean success = client.saveMainScriptCanvas(scriptCanvas);
                                    ThreadUtils.sleep(MILLIS);

                                    if (!success) {
                                        failedKbScriptCanvasNameList.add(scriptCanvas.getName());
                                    }
                                }
                            }
                        }

                        // 处理最高优先
                        List<String> succeedFuncPriorQaCorpusNameList = new ArrayList<>();
                        List<String> failedFuncPriorQaCorpusNameList = new ArrayList<>();
                        List<ScriptCorpus> funcPriorQaScriptCorpusList = client.getFuncPriorQaScriptCorpusList(scriptId);
                        for (ScriptCorpus scriptCorpus : funcPriorQaScriptCorpusList) {
                            processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                    succeedFuncPriorQaCorpusNameList, failedFuncPriorQaCorpusNameList);
                        }

                        // 处理重复语料
                        List<String> succeedFuncRepeatCorpusNameList = new ArrayList<>();
                        List<String> failedFuncRepeatCorpusNameList = new ArrayList<>();
                        List<ScriptCorpus> funcRepeatScriptCorpusList = client.getFuncRepeatScriptCorpusList(scriptId);
                        for (ScriptCorpus scriptCorpus : funcRepeatScriptCorpusList) {
                            processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                    succeedFuncRepeatCorpusNameList, failedFuncRepeatCorpusNameList);
                        }

                        // 处理沉默语料
                        List<String> succeedFuncSilenceCorpusNameList = new ArrayList<>();
                        List<String> failedFuncSilenceCorpusNameList = new ArrayList<>();
                        List<ScriptCorpus> funcSilenceScriptCorpusList = client.getFuncSilenceScriptCorpusList(scriptId);
                        for (ScriptCorpus scriptCorpus : funcSilenceScriptCorpusList) {
                            processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                    succeedFuncSilenceCorpusNameList, failedFuncSilenceCorpusNameList);
                        }

                        // 处理话术2.0版本的打断垫句、续播垫句、承接语料
                        List<String> succeedPreInterruptCorpusNameList = new ArrayList<>();
                        List<String> failedPreInterruptCorpusNameList = new ArrayList<>();
                        List<String> succeedPreContinueCorpusNameList = new ArrayList<>();
                        List<String> failedPreContinueCorpusNameList = new ArrayList<>();
                        List<String> succeedPreUndertakeCorpusNameList = new ArrayList<>();
                        List<String> failedPreUndertakeCorpusNameList = new ArrayList<>();
                        if (script.getMultiContentVersion()) {
                            // 打断垫句
                            List<ScriptCorpus> preInterruptScriptCorpusList = client.getPreInterruptCorpusList(scriptId);
                            for (ScriptCorpus scriptCorpus : preInterruptScriptCorpusList) {
                                processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                        succeedPreInterruptCorpusNameList, failedPreInterruptCorpusNameList);
                            }

                            // 续播垫句
                            List<ScriptCorpus> preContinueScriptCorpusList = client.getPreContinueCorpusList(scriptId);
                            for (ScriptCorpus scriptCorpus : preContinueScriptCorpusList) {
                                processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                        succeedPreContinueCorpusNameList, failedPreContinueCorpusNameList);
                            }

                            // 承接语料
                            List<ScriptCorpus> preUndertakeScriptCorpusList = client.getPreUndertakeCorpusList(scriptId);
                            for (ScriptCorpus scriptCorpus : preUndertakeScriptCorpusList) {
                                processScriptCorpus(scriptCorpus, null, replaceType, oldPhrase, newPhrase, client,
                                        succeedPreUndertakeCorpusNameList, failedPreUndertakeCorpusNameList);
                            }
                        }

                        boolean isUnlocked = client.unlockScript(scriptId);

                        client.addTraceInfo(this.getInstructionType().getName(), ImmutableMap.of("replaceType", replaceType, "scriptId", script.getId(), "oldPhrase", oldPhrase, "newPhrase", newPhrase));

                        int succeedCount = succeedMainCorpusNameList.size()
                                + succeedKbQaCorpusNameList.size() + succeedKbCanvasCorpusNameList.size()
                                + succeedFuncPriorQaCorpusNameList.size() + succeedFuncRepeatCorpusNameList.size() + succeedFuncSilenceCorpusNameList.size();
                        int failedCount = failedMainCorpusNameList.size()
                                + failedKbQaCorpusNameList.size() + failedKbCanvasCorpusNameList.size()
                                + failedFuncPriorQaCorpusNameList.size() + failedFuncRepeatCorpusNameList.size() + failedFuncSilenceCorpusNameList.size()
                                + failedMainScriptCanvasNameList.size() + failedKbScriptCanvasNameList.size();
                        boolean success = (succeedCount > 0 && failedCount == 0);

                        StringBuilder sbSucceed = new StringBuilder();
                        StringBuilder sbFailed = new StringBuilder();

                        if (!CollectionUtils.isEmpty(succeedMainCorpusNameList)) {
                            sbSucceed.append("成功更新主流程语料：【").append(String.join("】、【", succeedMainCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedMainCorpusNameList)) {
                            sbFailed.append("无法更新主流程语料：【").append(String.join("】、【", failedMainCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedKbQaCorpusNameList)) {
                            sbSucceed.append("成功更新基本问答语料：【").append(String.join("】、【", succeedKbQaCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedKbQaCorpusNameList)) {
                            sbSucceed.append("无法更新基本问答语料：【").append(String.join("】、【", failedKbQaCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedKbCanvasCorpusNameList)) {
                            sbSucceed.append("成功更新深层沟通语料：【").append(String.join("】、【", succeedKbCanvasCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedKbCanvasCorpusNameList)) {
                            sbSucceed.append("无法更新深层沟通语料：【").append(String.join("】、【", failedKbCanvasCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedFuncPriorQaCorpusNameList)) {
                            sbSucceed.append("成功更新最高优先语料：【").append(String.join("】、【", succeedFuncPriorQaCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedFuncPriorQaCorpusNameList)) {
                            sbSucceed.append("无法更新最高优先语料：【").append(String.join("】、【", failedFuncPriorQaCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedFuncRepeatCorpusNameList)) {
                            sbSucceed.append("成功更新重复语料：【").append(String.join("】、【", succeedFuncRepeatCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedFuncRepeatCorpusNameList)) {
                            sbSucceed.append("无法更新重复语料：【").append(String.join("】、【", failedFuncRepeatCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedFuncSilenceCorpusNameList)) {
                            sbSucceed.append("成功更新沉默语料：【").append(String.join("】、【", succeedFuncSilenceCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedFuncSilenceCorpusNameList)) {
                            sbSucceed.append("无法更新沉默语料：【").append(String.join("】、【", failedFuncSilenceCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedPreInterruptCorpusNameList)) {
                            sbSucceed.append("成功更新打断垫句：【").append(String.join("】、【", succeedPreInterruptCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedPreInterruptCorpusNameList)) {
                            sbSucceed.append("无法更新打断垫句：【").append(String.join("】、【", failedPreInterruptCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedPreContinueCorpusNameList)) {
                            sbSucceed.append("成功更新续播垫句：【").append(String.join("】、【", succeedPreContinueCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedPreContinueCorpusNameList)) {
                            sbSucceed.append("无法更新续播垫句：【").append(String.join("】、【", failedPreContinueCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(succeedPreUndertakeCorpusNameList)) {
                            sbSucceed.append("成功更新承接语料：【").append(String.join("】、【", succeedPreUndertakeCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedPreUndertakeCorpusNameList)) {
                            sbSucceed.append("无法更新承接语料：【").append(String.join("】、【", failedPreUndertakeCorpusNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedMainScriptCanvasNameList)) {
                            sbSucceed.append("无法更新主流程画布布局：【").append(String.join("】、【", failedMainScriptCanvasNameList)).append("】\n");
                        }
                        if (!CollectionUtils.isEmpty(failedKbScriptCanvasNameList)) {
                            sbSucceed.append("无法更新深层沟通画布布局：【").append(String.join("】、【", failedKbScriptCanvasNameList)).append("】\n");
                        }

                        String msg;
                        if (success) {
                            msg = "执行指令成功！\n";
                        } else {
                            msg = "执行指令失败！\n";
                        }
                        if (!isUnlocked) {
                            msg += "解锁话术失败！\n";
                        }
                        msg = msg + succeedCount + "个语料更新成功，" + failedCount + "个语料更新失败" + "\n" + sbSucceed.toString() + sbFailed.toString() + this.toDescription();
                        return new ExecutionResult(success, msg);
                    } catch (Exception e) {
                        if (scriptId != null) {
                            try {
                                BaizeClientFactory.getBaizeClient().unlockScript(scriptId);
                            } catch (Exception ee) {

                            }
                        }
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

    private static boolean processScriptCorpus(
            ScriptCorpus scriptCorpus, ScriptCanvas scriptCanvas, ReplaceType replaceType,
            String oldPhrase, String newPhrase, BaizeClient client,
            List<String> succeedCorpusNameList, List<String> failedCorpusNameList) {
        boolean isChanged = false;
        if (replaceType == ReplaceType.SCRIPT_NAME || replaceType == ReplaceType.SCRIPT_NAME_AND_CONTENT) {
            String corpusName = scriptCorpus.getName();
            if (!StringUtils.isEmpty(corpusName) && corpusName.contains(oldPhrase)) {
                corpusName = corpusName.replace(oldPhrase, newPhrase);
                scriptCorpus.setName(corpusName);
                isChanged = true;
            }
        }

        if (replaceType == ReplaceType.SCRIPT_CONTENT || replaceType == ReplaceType.SCRIPT_NAME_AND_CONTENT) {
            String content = scriptCorpus.getContent();
            if (!StringUtils.isEmpty(content) && content.contains(oldPhrase)) {
                content = content.replace(oldPhrase, newPhrase);
                scriptCorpus.setContent(content);
                isChanged = true;
            }
            List<ScriptMultiContent> multiContentList = scriptCorpus.getScriptMultiContents();
            if (!CollectionUtils.isEmpty(multiContentList)) {
                for (ScriptMultiContent multiContent: multiContentList) {
                    List<ScriptUnitContent> unitContentList = multiContent.getScriptUnitContents();
                    if (!CollectionUtils.isEmpty(unitContentList)) {
                        for (ScriptUnitContent unitContent: unitContentList) {
                            String contentText = unitContent.getContent();
                            if (!StringUtils.isEmpty(contentText) && contentText.contains(oldPhrase)) {
                                contentText = contentText.replace(oldPhrase, newPhrase);
                                unitContent.setContent(contentText);
                                isChanged = true;
                            }
                        }
                    }
                }
            }
        }
        if (isChanged) {
            String corpusName = scriptCorpus.getName();
            String corpusContent = ScriptUtils.getContent(scriptCorpus);
            if (scriptCanvas != null) {
                // 设置画布，但不立即提交保存
                Map<Long, CanvasCorpus> canvasCorpusDataMap = scriptCanvas.getCanvasCorpusDataMap();
                CanvasCorpus canvasCorpus = canvasCorpusDataMap.get(scriptCorpus.getId());
                if (canvasCorpus != null) {
                    canvasCorpus.setName(corpusName);
                    canvasCorpus.setContent(corpusContent);
                }
            }
            //保存话术
            HttpResult<ScriptCorpus> savedCorpus = client.saveScriptCorpus(scriptCorpus);
            boolean isSuccess = savedCorpus.isSuccess();
            if (isSuccess) {
                succeedCorpusNameList.add(corpusName);
            } else {
                failedCorpusNameList.add(corpusName);
            }
            ThreadUtils.sleep(MILLIS);
        }
        return isChanged;
    }

}
