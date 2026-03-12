package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.chat.bean.baize.script.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;
import org.example.utils.ThreadUtils;
import org.example.utils.bean.HttpResult;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CopyMainScriptCanvasInstructionBean extends AbstractInstructionBean {

    private static final long MILLIS = 100;
    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_COPY_MAIN_SCRIPT_CANVAS;
    private String sourceScriptName;
    private String targetScriptName;
    private Set<String> canvasNameSet;

    public CopyMainScriptCanvasInstructionBean(String instructionId, ChatGroup chatGroup, String creator, String sourceScriptName, String targetScriptName, Set<String> canvasNameSet) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.sourceScriptName = sourceScriptName;
        this.targetScriptName = targetScriptName;
        this.canvasNameSet = canvasNameSet;
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

        sb.append("原话术名称：").append(sourceScriptName).append("\n");
        sb.append("新话术名称：").append(targetScriptName).append("\n");

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (StringUtils.isEmpty(getInstructionId())) {
            return new CheckResult(false, "指令ID为空");
        }
        if (getInstructionType() == null) {
            return new CheckResult(false, "指令类型为空");
        }
        if (StringUtils.isEmpty(sourceScriptName)) {
            return new CheckResult(false, "原话术名称为空");
        }
        if (StringUtils.isEmpty(targetScriptName)) {
            return new CheckResult(false, "新话术名称为空");
        }
        if (CollectionUtils.isEmpty(canvasNameSet)) {
            return new CheckResult(false, "画布名称为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    Long targetScriptId = null;
                    try {
                        BaizeClient client = BaizeClientFactory.getBaizeClient();

                        // 获取原话术
                        Script sourceScript = null;
                        for (ScriptStatus scriptStatus : ScriptStatus.values()) {
                            List<Script> scriptList = client.getScriptList(scriptStatus);
                            if (scriptList != null) {
                                Script script = scriptList.stream()
                                        .filter(x -> Objects.equals(x.getScriptName(), sourceScriptName))
                                        .findFirst().orElse(null);
                                if (script != null) {
                                    sourceScript = script;
                                    break;
                                }
                            }
                        }
                        if (sourceScript == null) {
                            String msg = "执行指令失败！原话术不存在" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        // 获取新话术
                        Script targetScript = null;
                        List<Script> scriptList = client.getScriptList(ScriptStatus.EDIT);
                        if (scriptList != null) {
                            targetScript = scriptList.stream()
                                    .filter(x -> Objects.equals(x.getScriptName(), targetScriptName))
                                    .findFirst().orElse(null);
                        }
                        if (targetScript == null) {
                            String msg = "执行指令失败！新话术不存在或不在编辑状态" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        // 检查话术合法性
                        if (!Objects.equals(sourceScript.getSecondaryIndustryId(), targetScript.getSecondaryIndustryId())) {
                            String msg = "执行指令失败！两个话术的二级行业不相同" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        if (!Objects.equals(sourceScript.getMultiContentVersion(), targetScript.getMultiContentVersion())) {
                            String msg = "执行指令失败！两个话术的交互版本不相同" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        boolean isMultiContentVersion = sourceScript.getMultiContentVersion();

                        // 开始复制
                        long sourceScriptId = sourceScript.getId();
                        targetScriptId = targetScript.getId();

                        // 获取新话术意向分类
                        List<IntentionClass> targetIntentionClassList = client.getIntentionClassList(targetScriptId);
                        Map<String, IntentionClass> targetName2IntentionClass = targetIntentionClassList.stream()
                                .collect(Collectors.toMap(x -> x.getIntentionName(), x -> x));

                        // 获取新话术意向标签
                        Map<String, IntentionLabel> targetName2IntentionLabel = getName2IntentionLabel(client, targetScriptId);

                        List<InfoQueryInfo> targetInfoQueryInfoList = client.getInfoQueryInfoList(targetScriptId);
                        Map<String, InfoQueryInfo> targetFieldName2InfoQueryInfo = targetInfoQueryInfoList.stream()
                                .collect(Collectors.toMap(x -> x.getInfoFieldName(), x -> x));


                        // 获取原话术主流程语料
                        List<ScriptCanvas> allSourceMainScriptCanvasList = client.getMainScriptCanvasList(sourceScriptId);
                        Map<Long, ScriptCanvas> sourceHeadCorpusId2ScriptCanvas = allSourceMainScriptCanvasList.stream()
                                .filter(canvas -> canvas.getHeadCorpusId() != null)
                                .collect(Collectors.toMap(x -> x.getHeadCorpusId(), x -> x));
                        List<ScriptCanvas> sourceMainScriptCanvasList = allSourceMainScriptCanvasList.stream()
                                .filter(canvas -> canvasNameSet.contains(canvas.getName()))
                                .collect(Collectors.toList());
                        if (sourceMainScriptCanvasList.size() != canvasNameSet.size()) {
                            Set<String> filteredCanvasNameSet = sourceMainScriptCanvasList.stream()
                                    .map(canvas -> canvas.getName())
                                    .collect(Collectors.toSet());
                            Set<String> diffCanvasNameSet = new HashSet<>(canvasNameSet);
                            diffCanvasNameSet.removeAll(filteredCanvasNameSet);
                            String msg = "执行指令失败！画布【" + String.join("】、【", diffCanvasNameSet) + "】不存在" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        // 获取新话术现有的所有语料
                        ScriptAudioInfo scriptAudioInfo = client.getScriptAudioInfo(targetScriptId);
                        if (scriptAudioInfo == null) {
                            String msg = "执行指令失败！无法获取新话术现有语料" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }
                        Set<String> targetCorpusNameSet = scriptAudioInfo.getScriptUnitContents().stream()
                                .map(info -> info.getContentName()).collect(Collectors.toSet());

                        // 获取新话术主流程语料
                        List<ScriptCanvas> targetMainScriptCanvasList = client.getMainScriptCanvasList(targetScriptId);
                        Map<String, Long> targetCanvasName2HeadCorpusId = targetMainScriptCanvasList.stream()
                                .filter(x -> x.getHeadCorpusId() != null)
                                .collect(Collectors.toMap(x -> x.getName(), x -> x.getHeadCorpusId()));
                        Set<String> targetCanvasNameSet = targetMainScriptCanvasList.stream()
                                .map(canvas -> canvas.getName())
                                .collect(Collectors.toSet());

                        // 获取识库分组
                        List<KnowledgeGroup> sourceKnowledgeGroupList = client.getKnowledgeBaseGroupList(sourceScriptId);
                        List<KnowledgeGroup> targetKnowledgeGroupList = client.getKnowledgeBaseGroupList(targetScriptId);
                        Map<Long, String> sourceKBGroupId2GroupName = sourceKnowledgeGroupList.stream()
                                .collect(Collectors.toMap(x -> x.getId(), x -> x.getGroupName()));
                        Map<String, Long> targetKBGroupName2GroupId = targetKnowledgeGroupList.stream()
                                .collect(Collectors.toMap(x -> x.getGroupName(), x -> x.getId()));

                        // 检查主流程开放范围知识库分组合法性
                        for (ScriptCanvas sourceScriptCanvas : sourceMainScriptCanvasList) {
                            List<String> failedKBGroupNameList = new ArrayList<>();
                            for (long sourceGroupId : sourceScriptCanvas.getGroupOpenScope()) {
                                String groupName = sourceKBGroupId2GroupName.get(sourceGroupId);
                                Long targetGroupId = targetKBGroupName2GroupId.get(groupName);
                                if (targetGroupId == null) {
                                    failedKBGroupNameList.add(groupName);
                                }
                            }
                            if (!CollectionUtils.isEmpty(failedKBGroupNameList)) {
                                String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】开放范围知识库分组【" + String.join("】、【", failedKBGroupNameList) + "】不存在，请先完成知识库分组配置" + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                        }

                        // 获取原话术功能话术语料
                        Map<Long, ScriptCorpus> sourceCorpusId2PreInterruptCorpus = null;
                        Map<Long, ScriptCorpus> sourceCorpusId2PreContinueCorpus = null;
                        Map<Long, ScriptCorpus> sourceCorpusId2PreUndertakeCorpus = null;
                        Map<String, ScriptCorpus> targetCorpusName2PreInterruptCorpus = null;
                        Map<String, ScriptCorpus> targetCorpusName2PreContinueCorpus = null;
                        Map<String, ScriptCorpus> targetCorpusName2PreUndertakeCorpus = null;
                        if (isMultiContentVersion) {
                            List<ScriptCorpus> sourcePreInterruptCorpusList = client.getPreInterruptCorpusList(sourceScriptId);
                            sourceCorpusId2PreInterruptCorpus = sourcePreInterruptCorpusList.stream()
                                    .collect(Collectors.toMap(x -> x.getId(), x -> x));

                            List<ScriptCorpus> sourcePreContinueCorpusList = client.getPreContinueCorpusList(sourceScriptId);
                            sourceCorpusId2PreContinueCorpus = sourcePreContinueCorpusList.stream()
                                    .collect(Collectors.toMap(x -> x.getId(), x -> x));

                            List<ScriptCorpus> sourcePreUndertakeCorpusList = client.getPreUndertakeCorpusList(sourceScriptId);
                            sourceCorpusId2PreUndertakeCorpus = sourcePreUndertakeCorpusList.stream()
                                    .collect(Collectors.toMap(x -> x.getId(), x -> x));

                            List<ScriptCorpus> targetPreInterruptCorpusList = client.getPreInterruptCorpusList(sourceScriptId);
                            targetCorpusName2PreInterruptCorpus = targetPreInterruptCorpusList.stream()
                                    .collect(Collectors.toMap(x -> x.getName(), x -> x));

                            List<ScriptCorpus> targetPreContinueCorpusList = client.getPreContinueCorpusList(sourceScriptId);
                            targetCorpusName2PreContinueCorpus = targetPreContinueCorpusList.stream()
                                    .collect(Collectors.toMap(x -> x.getName(), x -> x));

                            List<ScriptCorpus> targetPreUndertakeCorpusList = client.getPreUndertakeCorpusList(sourceScriptId);
                            targetCorpusName2PreUndertakeCorpus = targetPreUndertakeCorpusList.stream()
                                    .collect(Collectors.toMap(x -> x.getName(), x -> x));
                        }


                        // 进入编辑状态，锁定话术
                        boolean isLocked = client.lockScript(targetScriptId);
                        if (!isLocked) {
                            String msg = "执行指令失败！无法锁定话术" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        // 处理主流程画布
                        List<String> failedSourceCanvasNameList = new ArrayList<>();
                        Set<String> corpusNameSetWithEventTrigger = new LinkedHashSet<>();
                        for (ScriptCanvas sourceScriptCanvas : sourceMainScriptCanvasList) {
                            ScriptCanvas sourceCanvas = client.getMainScriptCanvas(sourceScriptCanvas.getId());

                            String sourceCanvasName = sourceCanvas.getName();
                            String targetCanvasName = sourceCanvasName;
                            while (targetCanvasNameSet.contains(targetCanvasName)) {
                                targetCanvasName += "-复制";
                            }

                            List<Long> targetGroupIdList = new ArrayList<>();
                            for (long sourceGroupId : sourceScriptCanvas.getGroupOpenScope()) {
                                String groupName = sourceKBGroupId2GroupName.get(sourceGroupId);
                                Long targetGroupId = targetKBGroupName2GroupId.get(groupName);
                                targetGroupIdList.add(targetGroupId);
                            }

                            targetCanvasNameSet.add(targetCanvasName);
                            ScriptCanvas targetCanvas = SerializationUtils.clone(sourceCanvas);
                            targetCanvas.setId(null);
                            targetCanvas.setName(targetCanvasName);
                            targetCanvas.setGroupOpenScope(targetGroupIdList);
                            targetCanvas.setHeadCorpusId(null);
                            targetCanvas.setPreCanvasId(null);
                            targetCanvas.setScriptId(targetScriptId);
                            targetCanvas.setWeight(targetCanvasNameSet.size()); // 将画布加在最后
                            targetCanvas.setCanvasCorpusDataMap(null);
                            targetCanvas.setCanvasBranchDataMap(null);
                            HttpResult<ScriptCanvas> addedCanvasResult = client.createMainScriptCanvas(targetCanvas);
                            boolean isCreateCanvasSuccess = addedCanvasResult.isSuccess();
                            if (!isCreateCanvasSuccess) {
                                failedSourceCanvasNameList.add(sourceCanvasName);
                                continue;
                            }
                            ThreadUtils.sleep(MILLIS);
                            ScriptCanvas addedCanvas = addedCanvasResult.getObject();
                            Long targetCanvasId = addedCanvas.getId();

                            Map<Long, ScriptCorpus> sourceCorpusId2Corpus = new HashMap<>();
                            Map<Long, Long> sourceCorpusId2TargetCorpusId = new HashMap<>();
                            Map<Long, ScriptCorpus> targetCorpusId2Corpus = new HashMap<>();

                            long sourceHeadCorpusId = sourceCanvas.getHeadCorpusId();
                            Map<Long, CanvasCorpus> sourceCanvasCorpusDataMap = sourceCanvas.getCanvasCorpusDataMap();
                            Set<Long> sourceCorpusIdSet = sourceCanvasCorpusDataMap.keySet();
                            List<Long> sourceCorpusIdList = new LinkedList<>(sourceCorpusIdSet);
                            Long targetHeadCorpusId = null;
                            // 头节点语料要第一个创建
                            sourceCorpusIdList.remove(sourceHeadCorpusId);
                            sourceCorpusIdList.add(0, sourceHeadCorpusId);
                            for (long sourceCorpusId : sourceCorpusIdList) {
                                ScriptCorpus sourceCorpus = client.getScriptCorpus(sourceCorpusId);
                                String sourceCorpusName = sourceCorpus.getName();

                                sourceCorpusId2Corpus.put(sourceCorpusId, sourceCorpus);

                                IntentionClass sourceIntentionClass = sourceCorpus.getAiIntentionType();
                                IntentionClass targetIntentionClass = null;
                                if (sourceIntentionClass != null) {
                                    targetIntentionClass = targetName2IntentionClass.get(sourceIntentionClass.getIntentionName());
                                    if (targetIntentionClass == null) {
                                        client.unlockScript(targetScriptId);
                                        String className = sourceIntentionClass.getIntentionName();
                                        String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的意向分类【" + className + "】不存在" + "\n" + this.toDescription();
                                        return new ExecutionResult(false, msg);
                                    }
                                }

                                List<IntentionLabel> sourceLabelList = sourceCorpus.getAiLabels();
                                List<IntentionLabel> targetLabelList = new ArrayList<>();
                                if (!CollectionUtils.isEmpty(sourceLabelList)) {
                                    for (IntentionLabel sourceLabel : sourceLabelList) {
                                        String labelName = sourceLabel.getLabelName();
                                        IntentionLabel targetLabel = targetName2IntentionLabel.get(labelName);
                                        if (targetLabel == null) {
                                            boolean success = client.saveIntentionLabel(targetScriptId, sourceLabel.getLabelName());
                                            if (!success) {
                                                client.unlockScript(targetScriptId);
                                                String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的意向标签【" + labelName + "】不存在" + "\n" + this.toDescription();
                                                return new ExecutionResult(false, msg);
                                            }
                                            ThreadUtils.sleep(MILLIS);

                                            targetName2IntentionLabel = getName2IntentionLabel(client, targetScriptId);
                                            targetLabel = targetName2IntentionLabel.get(labelName);

                                            if (targetLabel == null) {
                                                client.unlockScript(targetScriptId);
                                                String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的意向标签【" + labelName + "】不存在" + "\n" + this.toDescription();
                                                return new ExecutionResult(false, msg);
                                            }
                                        }
                                        targetLabelList.add(targetLabel);
                                    }
                                }

                                ScriptCorpus targetCorpus = SerializationUtils.clone(sourceCorpus);
                                String targetCorpusName = sourceCorpusName;
                                while (targetCorpusNameSet.contains(targetCorpusName)) {
                                    targetCorpusName += "-复制";
                                }

                                targetCorpusNameSet.add(targetCorpusName);

                                targetCorpus.setId(null);
                                targetCorpus.setScriptId(targetScriptId);
                                targetCorpus.setName(targetCorpusName);
                                targetCorpus.setAiIntentionType(targetIntentionClass);
                                targetCorpus.setAiLabels(targetLabelList);

                                // 处理分支
                                Map<Long, String> sourceBranchId2BranchName = new HashMap<>();
                                List<ScriptBranch> branchList = targetCorpus.getBranchList();
                                if (!CollectionUtils.isEmpty(branchList)) {
                                    for (ScriptBranch branch : branchList) {
                                        Long sourceBranchId = branch.getId();
                                        String branchName = branch.getName();
                                        sourceBranchId2BranchName.put(sourceBranchId, branchName);

                                        branch.setId(null);
                                        branch.setScriptId(targetScriptId);
                                        branch.setPreCorpusId(null);
                                        branch.setNextCorpusId(null);

                                        Set<Long> valueIdSet = new HashSet<>();
                                        Map<String, String> queryFieldMap = branch.getQueryField();

                                        if (queryFieldMap != null) {
                                            for (Map.Entry<String, String> entry : queryFieldMap.entrySet()) {
                                                String fieldName = entry.getKey();
                                                String fieldValue = entry.getValue();
                                                InfoQueryInfo infoQueryInfo = targetFieldName2InfoQueryInfo.get(fieldName);
                                                if (infoQueryInfo == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的查询分支【" + branch.getName() + "】信息字段不存在" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                Map<String, InfoQueryValue> value2InfoQueryValue = infoQueryInfo.getInfoQueryValues()
                                                        .stream().collect(Collectors.toMap(x -> x.getValue(), x -> x));
                                                InfoQueryValue infoQueryValue = value2InfoQueryValue.get(fieldValue);
                                                if (infoQueryValue == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的查询分支【" + branch.getName() + "】信息字段不存在" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                valueIdSet.add(infoQueryValue.getId());
                                            }
                                        }

                                        List<Long> infoQueryValueIdList = branch.getInfoQueryValueIds();
                                        if (!CollectionUtils.isEmpty(infoQueryValueIdList)) {
                                            boolean isAllIncluded = infoQueryValueIdList.stream()
                                                    .allMatch(id -> valueIdSet.contains(id));
                                            if (!isAllIncluded) {
                                                client.unlockScript(targetScriptId);
                                                String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的查询分支【" + branch.getName() + "】信息字段不存在" + "\n" + this.toDescription();
                                                return new ExecutionResult(false, msg);
                                            }
                                        }

                                    }
                                }

                                // 处理事件触发
                                if (!CollectionUtils.isEmpty(targetCorpus.getEventTriggerValueIds())) {
                                    corpusNameSetWithEventTrigger.add(sourceCorpusName);
                                    targetCorpus.setEventTriggerValueIds(Collections.emptyList());
                                }
                                if (!CollectionUtils.isEmpty(targetCorpus.getEventTriggerValueList())) {
                                    corpusNameSetWithEventTrigger.add(sourceCorpusName);
                                    targetCorpus.setEventTriggerValueList(Collections.emptyList());
                                }

                                // 处理文本内容
                                for (ScriptMultiContent scriptMultiContent : targetCorpus.getScriptMultiContents()) {
                                    scriptMultiContent.setId(null);
                                    scriptMultiContent.setScriptId(targetScriptId);
                                    scriptMultiContent.setCorpusId(null);
                                    List<ScriptUnitContent> scriptUnitContentList = scriptMultiContent.getScriptUnitContents();
                                    if (!CollectionUtils.isEmpty(scriptUnitContentList)) {
                                        for (ScriptUnitContent scriptUnitContent : scriptMultiContent.getScriptUnitContents()) {
                                            scriptUnitContent.setId(null);
                                            scriptUnitContent.setMultiContentId(null);
                                            scriptUnitContent.setScriptId(targetScriptId);
                                            scriptUnitContent.setCorpusId(null);
                                            scriptUnitContent.setAudioPath(null);

                                            Long preUndertakeCorpusId = scriptUnitContent.getPreUndertakeCorpusId();
                                            if (preUndertakeCorpusId != null) {
                                                ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreUndertakeCorpus.get(preUndertakeCorpusId);
                                                if (sourceScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的承接语料id【" + preUndertakeCorpusId + "】获取失败" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                ScriptCorpus targetScriptCorpus = targetCorpusName2PreUndertakeCorpus.get(sourceScriptCorpus.getName());
                                                if (targetScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的承接语料【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                scriptUnitContent.setPreUndertakeCorpusId(targetScriptCorpus.getId());
                                            }

                                            List<Long> interruptCorpusIdsForEnd = scriptUnitContent.getInterruptCorpusIdsForEnd();
                                            if (!CollectionUtils.isEmpty(interruptCorpusIdsForEnd)) {
                                                List<Long> targetInterruptCorpusIdsForEnd = new ArrayList<>();
                                                for (Long interruptCorpusIdForEnd: interruptCorpusIdsForEnd) {
                                                    ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreInterruptCorpus.get(interruptCorpusIdForEnd);
                                                    if (sourceScriptCorpus == null) {
                                                        client.unlockScript(targetScriptId);
                                                        String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的打断语料id【" + preUndertakeCorpusId + "】获取失败" + "\n" + this.toDescription();
                                                        return new ExecutionResult(false, msg);
                                                    }
                                                    ScriptCorpus targetScriptCorpus = targetCorpusName2PreInterruptCorpus.get(sourceScriptCorpus.getName());
                                                    if (targetScriptCorpus == null) {
                                                        client.unlockScript(targetScriptId);
                                                        String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的打断语料【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                                        return new ExecutionResult(false, msg);
                                                    }
                                                    targetInterruptCorpusIdsForEnd.add(targetScriptCorpus.getId());
                                                }
                                                scriptUnitContent.setInterruptCorpusIdsForEnd(targetInterruptCorpusIdsForEnd);
                                            }

                                            Long preInterruptCorpusId = scriptUnitContent.getPreInterruptCorpusId();
                                            if (preInterruptCorpusId != null) {
                                                ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreInterruptCorpus.get(preInterruptCorpusId);
                                                if (sourceScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的打断垫句语料id【" + preUndertakeCorpusId + "】获取失败" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                ScriptCorpus targetScriptCorpus = targetCorpusName2PreInterruptCorpus.get(sourceScriptCorpus.getName());
                                                if (targetScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的打断垫句语料【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                scriptUnitContent.setPreInterruptCorpusId(targetScriptCorpus.getId());
                                            }

                                            Long preContinueCorpusIdForReturn = scriptUnitContent.getPreContinueCorpusIdForReturn();
                                            if (preContinueCorpusIdForReturn != null) {
                                                ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreContinueCorpus.get(preContinueCorpusIdForReturn);
                                                if (sourceScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的续播垫句(返回)语料id【" + preUndertakeCorpusId + "】获取失败" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                ScriptCorpus targetScriptCorpus = targetCorpusName2PreContinueCorpus.get(sourceScriptCorpus.getName());
                                                if (targetScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的续播垫句(返回)语料【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                scriptUnitContent.setPreContinueCorpusIdForReturn(targetScriptCorpus.getId());
                                            }

                                            Long preContinueCorpusIdForInterrupt = scriptUnitContent.getPreContinueCorpusIdForInterrupt();
                                            if (preContinueCorpusIdForInterrupt != null) {
                                                ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreContinueCorpus.get(preContinueCorpusIdForInterrupt);
                                                if (sourceScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的续播垫句(打断)语料id【" + preUndertakeCorpusId + "】获取失败" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                ScriptCorpus targetScriptCorpus = targetCorpusName2PreContinueCorpus.get(sourceScriptCorpus.getName());
                                                if (targetScriptCorpus == null) {
                                                    client.unlockScript(targetScriptId);
                                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应的续播垫句(打断)语料【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                                    return new ExecutionResult(false, msg);
                                                }
                                                scriptUnitContent.setPreContinueCorpusIdForInterrupt(targetScriptCorpus.getId());
                                            }
                                        }
                                    }
                                }

                                // 检查开放范围知识库分组合法性并设置
                                List<Long> targetKBGroupIdList = new ArrayList<>();
                                List<Long> sourceKBGroupIdList = targetCorpus.getGroupOpenScope();
                                if (!CollectionUtils.isEmpty(sourceKBGroupIdList)) {
                                    for (long sourceGroupId : sourceKBGroupIdList) {
                                        String groupName = sourceKBGroupId2GroupName.get(sourceGroupId);
                                        Long targetGroupId = targetKBGroupName2GroupId.get(groupName);
                                        if (targetGroupId == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】开放范围知识库分组【" + groupName + "】不存在，请先完成知识库分组配置" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        } else {
                                            targetKBGroupIdList.add(targetGroupId);
                                        }
                                    }
                                }
                                targetCorpus.setGroupOpenScope(targetKBGroupIdList);

                                // 设置是否是话术头节点语料
                                if (targetCorpus.getIsHead() != null && targetCorpus.getIsHead() && targetCanvasNameSet.size() == 1) {
                                    targetCorpus.setIsTopHead(true);
                                } else {
                                    targetCorpus.setIsTopHead(false);
                                }

                                if (targetCorpus.getIsHead() != null && targetCorpus.getIsHead()) {
                                    targetCanvasName2HeadCorpusId.put(targetCanvasName, targetCorpus.getId());
                                }

                                // 处理连接语料
                                if (sourceCorpus.getConnectType() == ConnectType.SELECT_MASTER_PROCESS) {
                                    Long sourceConnectCorpusId = sourceCorpus.getConnectCorpusId();
                                    Long targetConnectCorpusId = null;
                                    if (sourceConnectCorpusId != null) {
                                        ScriptCanvas sourceConnectCanvas = sourceHeadCorpusId2ScriptCanvas.get(sourceConnectCorpusId);
                                        if (sourceConnectCanvas != null) {
                                            targetConnectCorpusId = targetCanvasName2HeadCorpusId.get(sourceConnectCanvas.getName());
                                        }
                                    }
                                    if (targetConnectCorpusId == null) {
                                        client.unlockScript(targetScriptId);
                                        String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】连接的流程语料不存在" + "\n" + this.toDescription();
                                        return new ExecutionResult(false, msg);
                                    }
                                    targetCorpus.setConnectCorpusId(targetConnectCorpusId);
                                }

                                // 处理功能语料
                                if (isMultiContentVersion) {
                                    // 打断后的续播垫句
                                    Long preContinueCorpusIdForReturn = sourceCorpus.getPreContinueCorpusIdForReturn();
                                    if (preContinueCorpusIdForReturn != null) {
                                        ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreContinueCorpus.get(preContinueCorpusIdForReturn);
                                        if (sourceScriptCorpus == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应打断后的续播垫句语料id【" + preContinueCorpusIdForReturn + "】获取失败" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        }
                                        ScriptCorpus targetScriptCorpus = targetCorpusName2PreContinueCorpus.get(sourceScriptCorpus.getName());
                                        if (targetScriptCorpus == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应打断后的续播垫句【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        }
                                        targetCorpus.setPreContinueCorpusIdForReturn(targetScriptCorpus.getId());
                                    }

                                    // 返回时的续播垫句
                                    Long preContinueCorpusIdBeforeDefault = sourceCorpus.getPreContinueCorpusIdBeforeDefault();
                                    if (preContinueCorpusIdBeforeDefault != null) {
                                        ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreContinueCorpus.get(preContinueCorpusIdBeforeDefault);
                                        if (sourceScriptCorpus == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应返回时的续播垫句语料id【" + preContinueCorpusIdBeforeDefault + "】获取失败" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        }
                                        ScriptCorpus targetScriptCorpus = targetCorpusName2PreContinueCorpus.get(sourceScriptCorpus.getName());
                                        if (targetScriptCorpus == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应返回时的续播垫句【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        }
                                        targetCorpus.setPreContinueCorpusIdBeforeDefault(targetScriptCorpus.getId());
                                    }

                                    // 返回时的承接语料
                                    Long preUndertakeCorpusId = sourceCorpus.getPreUndertakeCorpusId();
                                    if (preUndertakeCorpusId != null) {
                                        ScriptCorpus sourceScriptCorpus = sourceCorpusId2PreUndertakeCorpus.get(preUndertakeCorpusId);
                                        if (sourceScriptCorpus == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应返回时的承接语料id【" + preUndertakeCorpusId + "】获取失败" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        }
                                        ScriptCorpus targetScriptCorpus = targetCorpusName2PreUndertakeCorpus.get(sourceScriptCorpus.getName());
                                        if (targetScriptCorpus == null) {
                                            client.unlockScript(targetScriptId);
                                            String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】对应返回时的承接语料【" + sourceScriptCorpus.getName() + "】不存在" + "\n" + this.toDescription();
                                            return new ExecutionResult(false, msg);
                                        }
                                        targetCorpus.setPreContinueCorpusIdBeforeDefault(targetScriptCorpus.getId());
                                    }
                                }

                                // 重置分支优先级
                                targetCorpus.setPriorGroup(null);

                                // 保存语料
                                HttpResult<ScriptCorpus> savedCorpusResult = client.saveScriptCorpus(targetCorpus);
                                if (!savedCorpusResult.isSuccess()) {
                                    client.unlockScript(targetScriptId);
                                    String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】保存失败" + "\n" + this.toDescription();
                                    return new ExecutionResult(false, msg);
                                }
                                ThreadUtils.sleep(MILLIS);


                                // 获取保存的语料
                                long savedCorpusId = savedCorpusResult.getObject().getId();
                                ScriptCorpus savedCorpus = client.getScriptCorpus(savedCorpusId);
//                                ScriptCorpus savedCorpus = savedCorpusResult.getObject();
                                targetCorpusId2Corpus.put(savedCorpusId, savedCorpus);
                                sourceCorpusId2TargetCorpusId.put(sourceCorpusId, savedCorpusId);
                                if (targetHeadCorpusId == null) {
                                    targetHeadCorpusId = savedCorpusId;
                                }

                                // 获取新话术分支id与名称映射
                                Map<String, Long> targetBranchName2BranchId = new HashMap<>();
                                List<ScriptBranch> targetBranchList = savedCorpus.getBranchList();
                                if (!CollectionUtils.isEmpty(targetBranchList)) {
                                    for (ScriptBranch targetBranch : targetBranchList) {
                                        targetBranchName2BranchId.put(targetBranch.getName(), targetBranch.getId());
                                    }
                                }

                                // 保存分支优先级
                                PriorGroup sourcePriorGroup = sourceCorpus.getPriorGroup();
                                if (sourcePriorGroup != null) {
                                    List<PriorPojo> sourcePriorPojoList = sourcePriorGroup.getPriorList();
                                    List<PriorPojo> targetPriorPojoList = new ArrayList<>();
                                    if (!CollectionUtils.isEmpty(sourcePriorPojoList)) {
                                        for (PriorPojo sourcePriorPojo : sourcePriorPojoList) {
                                            PriorType priorType = sourcePriorPojo.getType();
                                            Long sourceBranchId = sourcePriorPojo.getId();
                                            String branchName = sourceBranchId2BranchName.get(sourceBranchId);
                                            Long targetBranchId = targetBranchName2BranchId.get(branchName);
                                            if (targetBranchId == null) {
                                                // 可能是链到了知识库分组
                                                String groupName = sourceKBGroupId2GroupName.get(sourceBranchId);
                                                targetBranchId = targetKBGroupName2GroupId.get(groupName);
                                            }
                                            if (targetBranchId == null) {
                                                client.unlockScript(targetScriptId);
                                                String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】分支id【" + sourceBranchId + "】在新话术中没有对应的存在" + "\n" + this.toDescription();
                                                return new ExecutionResult(false, msg);
                                            }
                                            PriorPojo targetPriorPojo = new PriorPojo(targetBranchId, null, priorType);
                                            targetPriorPojoList.add(targetPriorPojo);
                                        }
                                    }

                                    PriorGroup priorGroup = PriorGroup.builder()
                                            .scriptId(savedCorpus.getScriptId())
                                            .corpusId(savedCorpus.getId())
                                            .priorList(targetPriorPojoList)
                                            .build();
                                    boolean success = client.addOrUpdatePriorGroup(priorGroup);
                                    if (!success) {
                                        client.unlockScript(targetScriptId);
                                        String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】语料【" + sourceCorpusName + "】分支优先级保存失败" + "\n" + this.toDescription();
                                        return new ExecutionResult(false, msg);
                                    }
                                    ThreadUtils.sleep(MILLIS);
                                }
                            }

                            // 设置画布
//                            ScriptCanvas targetScriptCanvas = client.getMainScriptCanvas(addedCanvas.getId());
//                            ScriptCanvas targetScriptCanvas = SerializationUtils.clone(sourceCanvas);


                            Map<Long, CanvasBranch> targetCanvasBranchDataMap = new HashMap<>();
                            Map<Long, CanvasCorpus> targetCanvasCorpusDataMap = new HashMap<>();
                            for (Map.Entry<Long, CanvasCorpus> entry : sourceCanvasCorpusDataMap.entrySet()) {
                                Long sourceCorpusId = entry.getKey();
                                CanvasCorpus sourceScriptCorpus = entry.getValue();

                                // 获取 targetCorpusId
                                Long targetCorpusId = sourceCorpusId2TargetCorpusId.get(sourceCorpusId);
                                ScriptCorpus targetCorpus = targetCorpusId2Corpus.get(targetCorpusId);

                                // 更新 targetBranch 的 preCorpusId 和 nextCorpusId
                                List<ScriptBranch> targetBranchList = targetCorpus.getBranchList();
                                if (!CollectionUtils.isEmpty(targetBranchList)) {
                                    ScriptCorpus sourceCorpus = sourceCorpusId2Corpus.get(sourceCorpusId);
                                    List<ScriptBranch> sourceBranchList = sourceCorpus.getBranchList();
                                    Map<String, ScriptBranch> sourceBranchName2Branch = sourceBranchList.stream()
                                            .collect(Collectors.toMap(x -> x.getName(), x -> x));
                                    for (ScriptBranch targetBranch : targetBranchList) {
                                        String branchName = targetBranch.getName();
                                        ScriptBranch sourceBranch = sourceBranchName2Branch.get(branchName);
                                        Long sourcePreCorpusId = sourceBranch.getPreCorpusId();
                                        Long targetPreCorpusId = sourceCorpusId2TargetCorpusId.get(sourcePreCorpusId);
                                        Long sourceNextCorpusId = sourceBranch.getNextCorpusId();
                                        Long targetNextCorpusId = sourceCorpusId2TargetCorpusId.get(sourceNextCorpusId);
                                        targetBranch.setPreCorpusId(targetPreCorpusId);
                                        targetBranch.setNextCorpusId(targetNextCorpusId);

                                        CanvasBranch targetCanvasBranch = CanvasBranch.builder()
                                                .branchId(targetBranch.getId())
                                                .name("")
                                                .preCorpusId(targetPreCorpusId)
                                                .nextCorpusId(targetNextCorpusId)
                                                .build();
                                        targetCanvasBranchDataMap.put(targetBranch.getId(), targetCanvasBranch);
                                    }
                                }

                                // targetCanvasCorpus
                                CanvasCorpus targetCanvasCorpus = SerializationUtils.clone(sourceScriptCorpus);
                                targetCanvasCorpus.setCorpusId(targetCorpusId);
                                targetCanvasCorpus.setBranches(targetBranchList);
                                targetCanvasCorpus.setConnectCorpusId(targetCorpus.getConnectCorpusId());
                                targetCanvasCorpus.setEventTriggerValueIds(targetCorpus.getEventTriggerValueIds());
                                targetCanvasCorpus.setSmsTriggerName(targetCorpus.getSmsTriggerName());

                                targetCanvasCorpusDataMap.put(targetCorpusId, targetCanvasCorpus);
                            }

                            ScriptCanvas canvas = ScriptCanvas.builder()
                                    .id(targetCanvasId)
                                    .scriptId(targetScriptId)
                                    .headCorpusId(targetHeadCorpusId)
                                    .canvasBranchDataMap(targetCanvasBranchDataMap)
                                    .canvasCorpusDataMap(targetCanvasCorpusDataMap)
                                    .build();
                            boolean success = client.saveMainScriptCanvas(canvas);
                            if (!success) {
                                client.unlockScript(targetScriptId);
                                String msg = "执行指令失败！画布【" + sourceScriptCanvas.getName() + "】布局保存失败" + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            ThreadUtils.sleep(MILLIS);
                        }

                        boolean success = true;
                        StringBuilder sbExtraInfo = new StringBuilder();

                        if (!CollectionUtils.isEmpty(failedSourceCanvasNameList)) {
                            sbExtraInfo.append("画布【").append(String.join("】、【", failedSourceCanvasNameList)).append("】复制失败").append("\n");
                            success = false;
                        }
                        if (!CollectionUtils.isEmpty(corpusNameSetWithEventTrigger)) {
                            sbExtraInfo.append("语料【").append(String.join("】、【", corpusNameSetWithEventTrigger)).append("】含有触发事件，请手动配置触发事件").append("\n");
                            success = false;
                        }

                        boolean isUnlocked = client.unlockScript(targetScriptId);

                        String msg;
                        if (success) {
                            msg = "执行指令成功！\n";
                        } else {
                            msg = "执行指令失败！\n";
                        }
                        if (!isUnlocked) {
                            msg += "解锁话术失败！\n";
                        }
                        msg = msg + sbExtraInfo.toString() + this.toDescription();
                        return new ExecutionResult(success, msg);
                    } catch (Exception e) {
                        if (targetScriptId != null) {
                            try {
                                BaizeClientFactory.getBaizeClient().unlockScript(targetScriptId);
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

    private static Map<String, IntentionLabel> getName2IntentionLabel(BaizeClient client, long scriptId) {
        List<IntentionLabel> targetIntentionLabelList = client.getIntentionLabelList(scriptId);
        Map<String, IntentionLabel> targetName2IntentionLabel = targetIntentionLabelList.stream()
                .collect(Collectors.toMap(x -> x.getLabelName(), x -> x));
        return targetName2IntentionLabel;
    }

    public static void main(String[] args) {
        String sourceScriptName = "test002测试变量D6EA2E12BCFE";
        String targetScriptName = "画布复制测试3";
        Set<String> canvasNameSet = Sets.newHashSet("流程2");
        CopyMainScriptCanvasInstructionBean copyMainScriptCanvasInstructionBean = new CopyMainScriptCanvasInstructionBean("instructionId", ChatGroup.CHAT_TEST, "creator", sourceScriptName, targetScriptName, canvasNameSet);
        try {
            ExecutionResult executionResult = copyMainScriptCanvasInstructionBean.getCallableInfo().getCallable().call();
            System.out.println(executionResult.getMsg());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
