package org.example.instruction.bean;

public enum InstructionType {
    ACTION_START_TASK("操作类型_开始任务", StartTaskInstructionBean.class),
    ACTION_RESTART_TASK("操作类型_重启任务", StartTaskInstructionBean.class),
    ACTION_RECALL_TASK("操作类型_复呼任务", StartTaskInstructionBean.class),
    ACTION_STOP_TASK("操作类型_暂停任务", StopTaskInstructionBean.class),
    ACTION_RESUME_TASK("操作类型_恢复任务", ResumeTaskInstructionBean.class),
    ACTION_FORBID_DISTRICT("操作类型_设置屏蔽", ForbidDistrictInstructionBean.class),
    ACTION_ALLOW_DISTRICT("操作类型_放开屏蔽", ForbidDistrictInstructionBean.class),
    ACTION_REPORT_FORBID_DISTRICT_ALL_DAY("操作类型_查看全天屏蔽", ExecuteInstructionBean.class),
    ACTION_CHANGE_CONCURRENCY("操作类型_调整并发", ChangeTenantLineInstructionBean.class),
    ACTION_CHANGE_TENANT_LINE("操作类型_切换线路", ChangeTenantLineInstructionBean.class),
    ACTION_ADD_TASK("操作类型_追加任务", ChangeTenantLineInstructionBean.class),
    ACTION_SET_LINE_RATIO("操作类型_调集线比", SetLineRatioInstructionBean.class),
//    ACTION_REALLOCATE_LOCKED_CONCURRENCY("操作类型_重新分配锁定并发", ReallocateLockedConcurrencyInstructionBean.class),
    ACTION_REPORT_ACCOUNT_LOCKED_CONCURRENCY("操作类型_展示账号锁定并发", ReportAccountLockedConcurrencyInstructionBean.class),
    ACTION_EDIT_SUPPLY_LINE("操作类型_编辑供应线路", EditSupplyLineInstructionBean.class),
    ACTION_EDIT_TENANT_LINE("操作类型_编辑商户线路", EditTenantLineInstructionBean.class),
    ACTION_EDIT_SINGLE_TENANT_LINE("操作类型_编辑单条商户线路", EditSingleTenantLineInstructionBean.class),
//    ACTION_CHANGE_SCRIPT("操作类型_切换话术"),
    ACTION_CHECK_TASK_DATA("操作类型_核对任务", CheckDataInstructionBean.class),
    ACTION_EXECUTE_INSTRUCTION("操作类型_执行指令", ExecuteInstructionBean.class),
    ACTION_REMOVE_INSTRUCTION("操作类型_删除指令", ExecuteInstructionBean.class),
    ACTION_REPORT_INSTRUCTION("操作类型_展示指令", ReportInstructionInstructionBean.class),
    ACTION_REPORT_FINISHED_INSTRUCTION("操作类型_展示已完成指令", ReportFinishedInstructionInstructionBean.class),
    ACTION_DOWNLOAD_AUDIO("操作类型_下载录音", DownloadAudioInstructionBean.class),
    ACTION_REPLACE_PHRASE_IN_SCRIPT("操作类型_替换语料", ReplacePhraseInScriptInstructionBean.class),
    ACTION_COPY_MAIN_SCRIPT_CANVAS("操作类型_复制主流程画布", CopyMainScriptCanvasInstructionBean.class),
    ACTION_CREATE_MAIN_ACCOUNT("操作类型_新建主账号", CreateMainAccountInstructionBean.class),
    ACTION_CREATE_SUB_ACCOUNT("操作类型_新建子账号", CreateSubAccountInstructionBean.class),
    ACTION_CREATE_ROLE("操作类型_新建角色", CreateRoleInstructionBean.class),
    ACTION_ADD_ROLE_IP("操作类型_添加角色IP", AddRoleIpInstructionBean.class),
    ACTION_SET_ACCOUNT_OPERATOR_PARAM("操作类型_配置回调地址", SetAccountOperatorParamInstructionBean.class),
    ACTION_REPORT_CALLED_TASK_TEMPLATE("操作类型_任务模板汇报", ReportCalledTaskTemplateInstructionBean.class),
    ACTION_GET_TASK_TEMPLATE("操作类型_查看任务模板", GetTaskTemplateInstructionBean.class),
    ACTION_CREATE_TASK_TEMPLATE("操作类型_新建任务模板", CreateTaskTemplateInstructionBean.class),
    ACTION_STOP_SCRIPT("操作类型_停用话术", StopScriptInstructionBean.class),
    ACTION_REPORT_SCRIPT_FOR_LINE("操作类型_获取线路报备话术", ReportScriptForLineInstructionBean.class),
    ACTION_REPORT_SCRIPT_FOR_JIAFANG("操作类型_获取甲方报备话术", ReportScriptForLineInstructionBean.class),
    ACTION_REPORT_TASK_STATISTIC("操作类型_任务数据汇报", ReportTaskStatisticInstructionBean.class),
    ACTION_REPORT_TASK_SCRIPT_STATISTIC("操作类型_任务话术数据汇报", ReportTaskScriptStatisticInstructionBean.class),
    ACTION_REPORT_RECENTLY_UPDATED_SCRIPT_STATISTIC("操作类型_新话术数据汇报", ReportRecentlyUpdatedScriptStatisticInstructionBean.class),
    ACTION_GET_PHONE_RECORD("操作类型_查询通话记录", GetPhoneRecordInstructionBean.class),
    ;

    private String name;
    private Class classType;

    InstructionType(String name, Class classType) {
        this.name = name;
        this.classType = classType;
    }

    public String getName() {
        return name;
    }

    public Class getClassType() {
        return classType;
    }
}
