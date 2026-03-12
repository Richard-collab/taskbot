package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StopScriptInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_STOP_SCRIPT;

    private List<String> scriptNameList;

    public StopScriptInstructionBean(String instructionId, ChatGroup chatGroup, String creator, List<String> scriptNameList) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (scriptNameList != null) {
            scriptNameList = new ArrayList<>(new LinkedHashSet<>(scriptNameList));
        }
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
        return IntStream.rangeClosed(1, scriptNameList.size()).boxed()
                .map(idx -> new SubStopScriptInstructionBean(this.getInstructionId() + "_" + idx, getChatGroup(), getCreator(), scriptNameList.get(idx - 1)))
                .collect(Collectors.toList());
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
        return null;
    }
}
