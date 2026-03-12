package org.example.utils;

import org.example.chat.bean.ChatGroup;

import java.nio.file.Paths;

public class ConstUtils {

    public static final String PULL_WECHAT_MSG_URL = "http://192.168.215.171:8266/wecom/message/query";
    public static final String SEND_QIWEI_MSG_URL = "http://192.168.215.171:8266/wecom/message/rebot/send";
    public static final String SEND_QIWEI_FILE_URL = "http://192.168.215.171:8266/wecom/message/rebot/send-file";
    public static final String QIWEI_TOKEN_ERROR = ChatGroup.CHAT_TEST.getRobotToken();

    public static final String SEND_LOCAL_MSG_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send";
    public static final String LOCAL_QIWEI_TOKEN =  "f9d06226-f43e-4931-85f2-ad4d27bceda8";

    public static final String PATH_LAST_TIME_PULL_WECHAT_MSG = Paths.get("data/last_time_pull_wechat_msg.txt").toString();
    public static final String PATH_LAST_MSG_PULL_WECHAT_MSG = Paths.get("data/last_msg_pull_wechat_msg.txt").toString();
    public static final String PATH_INSTRUCTION_ID_TO_CREATOR_SET = Paths.get("data/instruction_id_to_creator_set.txt").toString();
    public static final String PATH_UNEXECUTED_INSTRUCTION_INFO = Paths.get("data/unexecuted_instruction_info.txt").toString();
    public static final String PATH_ONGOING_INSTRUCTION_INFO = Paths.get("data/ongoing_instruction_info.txt").toString();
    public static final String PATH_FINISHED_INSTRUCTION_INFO = Paths.get("data/finished_instruction_info.txt").toString();
    public static final String PATH_FORBIDDEN_DISTRICT_INFO = Paths.get("data/forbidden_district_info.txt").toString();

    public static final int WAIT_MS_FOR_CHECK = 2000;

    public static final String ERROR_NOTE = "请处理";

    public static final String MASKED_SLOT_LEFT_SIDE = "〖";
    public static final String MASKED_SLOT_RIGHT_SIDE = "〗";
    public static final String STR_DOMAIN_SYSTEM = "system";
    public static final String STR_DOMAIN_MASK = "mask";

    public static final String STR_YES = "是";
    public static final String STR_NO = "否";
    public static final String STR_PHONE = "手机号";
    public static final String STR_DATE = "日期";
    public static final String STR_YEAR = "年";
    public static final String STR_MONTH = "月";
    public static final String STR_DAY = "日";
    public static final String STR_HOUR = "小时";
    public static final String STR_MINUTE = "分钟";
    public static final String STR_TIME = "时间";
    public static final String STR_MORNING = "上午";
    public static final String STR_AFTERNOON = "下午";
    public static final String STR_INSTRUCTION_ID = "指令ID";
    public static final String STR_EXECUTE_INSTRUCTION_ID = "执行指令ID";
    public static final String STR_ACCOUNT = "账号";
    public static final String STR_TENANT_LINE = "线路方";
    public static final String STR_CALL_NUMBER = "主叫号码";
    public static final String STR_FILTERED_TENANT_LINE = "筛选线路方";
    public static final String STR_FILTERED_CALLING_NUMBER = "筛选主叫号码";
    public static final String STR_CONCURRENCY = "并发数";
    public static final String STR_TASK_CREATE_TIME_BOUND_PERIOD = "任务创建时间范畴";
    public static final String STR_TASK_CREATE_TIME_BOUND_START = "任务创建时间上限";
    public static final String STR_TASK_CREATE_TIME_BOUND_END = "任务创建时间下限";
    public static final String STR_START_DATE = "开始日期";
    public static final String STR_END_DATE = "结束日期";
    public static final String STR_EXPECTED_START_TIME = "预期开始时间";
    public static final String STR_EXPECTED_END_TIME = "预期完成时间";
    public static final String STR_EXPECTED_CONNECTED_CALL_COUNT = "预期呼通数";
    public static final String STR_TASK_NAME_LIST = "任务名称列表";
    public static final String STR_TASK_NAME_EQUAL = "任务名称";
    public static final String STR_TASK_NAME_CONTAIN = "任务名称包含";
    public static final String STR_TASK_NAME_NOT_CONTAIN = "任务名称不包含";
    public static final String STR_TASK_NAME_SUFFIX = "任务名称后缀";
    public static final String STR_TASK_TYPE = "任务类型";
    public static final String STR_TASK_STATUS = "任务状态";
    public static final String STR_CITY = "城市";
    public static final String STR_PROVINCE = "省份";
    public static final String STR_FORBIDDEN_CITY = "屏蔽城市";
    public static final String STR_FORBIDDEN_PROVINCE = "屏蔽省份";
    public static final String STR_EFFECTIVE_ALL_DAY = "全天生效";
    public static final String STR_ORIGINAL_CONCURRENCY = "原并发";
    public static final String STR_PRODUCT = "产品名称";
    public static final String STR_ENTRANCE_MODE = "入口模式";
    public static final String STR_ACCOUNT_SUFFIX = "账号后缀";
    public static final String STR_LINE_RATIO = "集线比";
    public static final String STR_PASSWORD = "密码";
    public static final String STR_TENANT_NAME = "商户名称";
    public static final String STR_CONTACTS = "联系人";
    public static final String STR_ROLE_NAME = "角色名称";
    public static final String STR_SOURCE_ROLE_NAME = "来源角色名称";
    public static final String STR_PHONE_NUM_TYPE = "号码类型";
    public static final String STR_SECOND_INDUSTRY_NAME = "二级行业";
    public static final String STR_IP = "IP";
    public static final String STR_IP_LIST = "IP列表";
    public static final String STR_BAIZE_IP = "白泽IP";
    public static final String STR_BAIZE_IP_LIST = "白泽IP列表";
    public static final String STR_JIAFANG_IP = "甲方IP";
    public static final String STR_JIAFANG_IP_LIST = "甲方IP列表";
    public static final String STR_ACCOUNT_INFO = "账号信息";
    public static final String STR_ACCOUNT_INFO_LIST = "账号信息列表";
    public static final String STR_CONTENT_CONTAIN = "文本包含";

    public static final String STR_INTENT_LIST = "意图列表";
    public static final String STR_INTENT = "意图";
//    public static final String STR_BAIZE_YUNYING_ACCOUNT_INFO = "白泽运营账号信息";
//    public static final String STR_BAIZE_YUNYING_ACCOUNT_INFO_LIST = "白泽运营账号信息列表";
//    public static final String STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO = "甲方管理员账号信息";
//    public static final String STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO_LIST = "甲方管理员账号信息列表";
//    public static final String STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO = "外部坐席组长账号信息";
//    public static final String STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO_LIST = "外部坐席组长账号信息列表";
//    public static final String STR_WAIBU_ZUOXI_ACCOUNT_INFO = "外部坐席账号信息";
//    public static final String STR_WAIBU_ZUOXI_ACCOUNT_INFO_LIST = "外部坐席账号信息列表";
    public static final String STR_DATA_STATISTIC_OUTBOUND_TYPE_LIST = "查询范围列表";
    public static final String STR_DATA_STATISTIC_OUTBOUND_TYPE = "查询范围";
    public static final String STR_TASK_CALLBACK_URL = "任务回调地址";
    public static final String STR_CALLBACK_OUTBOUND_TYPE_LIST = "通话回调范围列表";
    public static final String STR_CALLBACK_OUTBOUND_TYPE = "通话回调范围";
    public static final String STR_CALLBACK_STATUS_LIST = "回调状态列表";
    public static final String STR_CALLBACK_STATUS = "回调状态";
    public static final String STR_OUTBOUND_CALLBACK_FIELD_LIST = "回调字段列表";
    public static final String STR_OUTBOUND_CALLBACK_FIELD = "回调字段";
    public static final String STR_QUICKLY_CALLBACK_URL = "快速回调接口";
    public static final String STR_NEW_CALLBACK_URL = "话后回调接口（新）";
    public static final String STR_TXT_UPDATE_CALLBACK_URL = "文本补推接口";
    public static final String STR_M_SMS_CALLBACK_URL = "M短信接口";
    public static final String STR_OLD_CALLBACK_URL = "通话回调接口（旧）";
    public static final String STR_SMS_CALLBACK_FIELD_LIST = "短信回调字段列表";
    public static final String STR_SMS_CALLBACK_FIELD = "短信回调字段";
    public static final String STR_SMS_CALLBACK_URL = "短信回调地址";
    public static final String STR_UP_SMS_CALLBACK_URL = "上行短信回调地址";
    public static final String STR_TEMPLATE_ID = "模板编号";
    public static final String STR_TEMPLATE_NAME = "模板名称";
    public static final String STR_INTENTION_CLASS = "意向分类";
    public static final String STR_INTENTION_CLASS_LIST = "意向分类列表";
    public static final String STR_IS_OUTPUT_AVG_CALL_DURATION = "是否输出平均通时";
    public static final String STR_IS_OUTPUT_TOTAL_CALL_DURATION = "是否输出总通时";
    public static final String STR_IS_NEXT_DAY_CALL = "是否隔日续呼";
    public static final String STR_SCRIPT_NAME = "话术名称";
    public static final String STR_SCRIPT_NAME_LIST = "话术名称列表";
    public static final String STR_CALL_TEAM_NAME = "坐席组名称";
    public static final String STR_CALL_TEAM_NAME_LIST = "坐席组名称列表";
    public static final String STR_HANDLE_TYPE = "处理方式";
    public static final String STR_OCCUPY_RATE = "占用等级";
    public static final String STR_VIRTUAL_SEAT_RATIO = "虚拟坐席系数";
    public static final String STR_IS_INCLUDE_AUTO_STOP = "是否包含止损任务";
    public static final String STR_REPLACE_TYPE = "替换类型";
    public static final String STR_OLD_PHRASE = "原文本";
    public static final String STR_NEW_PHRASE = "新文本";

}
