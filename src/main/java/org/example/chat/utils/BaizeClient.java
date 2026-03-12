package org.example.chat.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.chat.bean.baize.*;
import org.example.chat.bean.baize.script.Script;
import org.example.chat.bean.baize.script.ScriptCorpus;
import org.example.utils.*;
import org.example.utils.bean.HttpResponse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaizeClient implements BaizeScriptClient {
    static final String BASE_URL = getBaseUrl();
    private static String getBaseUrl() {
        if (BaseUtils.isLocal()) {
            return "http://192.168.23.85:8860/market";
//            return "http://ai.api.bountech.com/aiprod";
        } else if (BaseUtils.isNewVM()) {
            return "http://ai.api.bountech.cn/aiprod"; // 新虚机
        } else {
            return "http://192.168.215.73:8333/aiprod"; // 小助手虚机
        }
    }

    private static final Map<Role, Map<String, List<String>>> ROLE_AUTHORITY_MAP;

    static {
        try {
            ROLE_AUTHORITY_MAP = JsonUtils.fromJsonFile(ResourceUtils.getAbsolutePath("/role_authority_map.json"), new TypeToken<Map<Role, Map<String, List<String>>>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SimpleDateFormat SDF_DAY_START = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
    private static final SimpleDateFormat SDF_DAY_END = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
    private final String account;
    private final String password;
    private volatile String token;
    private volatile int accountId;
    private volatile int tenantId;
    private volatile String groupId;

    BaizeClient(String account, String password) {
        this.account = account;
        this.password = password;
        update();
    }

    public void update() {
        LoginResult loginResult = getLoginResult(this.account, this.password);
        this.token = loginResult.getToken();
        this.accountId = loginResult.getId();
        this.tenantId = loginResult.getTenantId();
        this.groupId = loginResult.getGroupId();
    }

    public String getToken() {
        return token;
    }

    public String getAccount() {
        return account;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getTenantId() {
        return tenantId;
    }

    private static LoginResult getLoginResult(String account, String password) {
        String url = BASE_URL + "/AiSpeech/admin/login";
        return BaizeUtils.getLoginResult(url, account, password);
    }

    /**
     * 获取系统并发.
     *
     * @return
     */
    public SystemConcurrency adminGetSystemConcurrency() {
        String url = BASE_URL + "/AiSpeech/surveillance/individualConcurrent";
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        SystemConcurrency systemConcurrency = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    systemConcurrency = JsonUtils.fromJson(json, new TypeToken<SystemConcurrency>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getSystemConcurrency error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getSystemConcurrency error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getSystemConcurrency error: code " + response.getCode());
            return null;
        }

        return systemConcurrency;
    }

    public Map<String, String> adminGetMainAccount2GroupId() {
        Map<String, String> account2GroupId = new HashMap<>();

        String url = BASE_URL + "/AiSpeech/surveillance/operationMainAdminList";
        HttpResponse response = BaizeUtils.doPost(url, token, "");
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, "");
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    List<JsonElement> objList = baseObj.getAsJsonArray("data").asList();
                    for (JsonElement obj : objList) {
                        String account = obj.getAsJsonObject().get("account").getAsString();
                        String groupId = obj.getAsJsonObject().get("groupId").getAsString();
                        account2GroupId.put(account, groupId);
                    }
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetMainAccount2GroupId error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetMainAccount2GroupId error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetMainAccount2GroupId error: code " + response.getCode());
            return null;
        }
        return account2GroupId;
    }


    /**
     * 获取供应线路
     *
     * @param supplyLineNumber 供应线路No
     * @param supplyLineNameContain 供应线路名称包含
     * @param lineType 供应线路类型
     * @param enableStatus 供应线路状态
     * @param masterCallNumber 供应线路主叫
     * @param prefix 供应线路前缀
     * @param callLineSupplierId 供应商id
     * @param callLineSupplierNumber 供应商No
     * @param callLineSupplierName 供应商名称
     * @return
     */
    public List<SupplyLine> adminFindSupplyLinesByConditions(
            String supplyLineNumber, String supplyLineNameContain, Collection<String> secondIndustries,
            LineType lineType, Boolean isForEncryptionPhones, EnableStatus enableStatus,
            String masterCallNumber, String prefix, Integer callLineSupplierId, String callLineSupplierNumber,
            String callLineSupplierName) {
        String url = BASE_URL + "/AiMonitor/callLineSupplyManager/findSupplyLinesByConditions";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(supplyLineNumber)) {
            paramMap.put("supplyLineNumber", supplyLineNumber);
        }
        if (!StringUtils.isEmpty(supplyLineNameContain)) {
            paramMap.put("supplyLineName", supplyLineNameContain);
        }
        if (!CollectionUtils.isEmpty(secondIndustries)) {
            paramMap.put("secondIndustries", secondIndustries);
        }
        if (lineType != null) {
            paramMap.put("supplyLineType", lineType);
        }
        if (isForEncryptionPhones != null) {
            paramMap.put("isForEncryptionPhones", isForEncryptionPhones);
        }
        if (enableStatus != null) {
            paramMap.put("enableStatus", enableStatus);
        }
        if (masterCallNumber != null) { // 主叫可以是空字符串
            paramMap.put("masterCallNumber", masterCallNumber);
        }
        if (prefix != null) { // 被叫前缀可以是空字符串
            paramMap.put("prefix", prefix);
        }
        if (callLineSupplierId != null) {
            paramMap.put("callLineSupplierId", callLineSupplierId);
        }
        if (!StringUtils.isEmpty(callLineSupplierNumber)) {
            paramMap.put("callLineSupplierNumber", callLineSupplierNumber);
        }
        if (!StringUtils.isEmpty(callLineSupplierName)) {
            paramMap.put("callLineSupplierName", callLineSupplierName);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<SupplyLine> supplyLineList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    supplyLineList = JsonUtils.fromJson(json, new TypeToken<List<SupplyLine>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindSupplyLinesByConditions error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindSupplyLinesByConditions error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindSupplyLinesByConditions error: code " + response.getCode());
            return null;
        }

        return supplyLineList;
    }


    public boolean adminEditSupplyLine(SupplyLine supplyLine) {
        String url = BASE_URL + "/AiSpeech/callLineSupply/updateOneSupplyLine";

        supplyLine = supplyLine.deepClone();
        supplyLine.setCreateTime(null);
        supplyLine.setUpdateTime(null);
        supplyLine.setCallLineSupplierName(null);
        supplyLine.setTenantLines(null);
        supplyLine.setCityCodes(null);
        supplyLine.getCityCodeGroups().forEach(group -> {
            group.setCreateTime(null);
            group.setUpdateTime(null);
        });

        SupplyLine finalSupplyLine = supplyLine;

        HttpResponse response = BaizeUtils.doPost(url, token, finalSupplyLine);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, finalSupplyLine);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminEditSupplyLine error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminEditSupplyLine error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminEditSupplyLine error: code " + response.getCode());
            return false;
        }
    }


    public boolean adminEditSupplyLineConcurrencyInTenantLine(List<LinePairConcurrencyInfo> linePairConcurrencyInfoList) {
        String url = BASE_URL + "/AiSpeech/callLineTenant/changeTenantSupplyLineLimitBatch";
        Map<String, Object> paramMap = new HashMap<>();
        for (LinePairConcurrencyInfo info: linePairConcurrencyInfoList) {
            String key = info.getTenantLineNumber() + "_" + info.getSupplyLineNumber();
            paramMap.put(key, info.getSupplyLineConcurrencyInTenantLine());
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminEditSupplyLineConcurrencyInTenantLine error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminEditSupplyLineConcurrencyInTenantLine error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminEditSupplyLineConcurrencyInTenantLine error: code " + response.getCode());
            return false;
        }
    }


    @Deprecated
    public List<TenantLine> findTenantLinesByConditions() {
        return findTenantLinesByConditions(null);
    }

    @Deprecated
    public List<TenantLine> findTenantLinesByConditions(EnableStatus enableStatus) {
        String url = BASE_URL + "/AiSpeech/callLineTenant/findTenantLinesByConditions";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("groupId", groupId);
        if (enableStatus != null) {
            paramMap.put("enableStatus", enableStatus);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<TenantLine> tenantLineList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    tenantLineList = JsonUtils.fromJson(json, new TypeToken<List<TenantLine>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient findTenantLinesByConditions error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient findTenantLinesByConditions error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient findTenantLinesByConditions error: code " + response.getCode());
            return null;
        }

        return tenantLineList;
    }

    public List<TenantLine> adminFindTenantLinesByConditions(String tenantLineNumber, String groupId) {
        String url = BASE_URL + "/AiSpeech/callLineTenant/findTenantLinesByConditionsForOperation";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(tenantLineNumber)) {
            paramMap.put("lineNumber", tenantLineNumber);
        }
        if (!StringUtils.isEmpty(groupId)) {
            paramMap.put("groupId", groupId);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<TenantLine> tenantLineList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    tenantLineList = JsonUtils.fromJson(json, new TypeToken<List<TenantLine>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindTenantLinesByConditions error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindTenantLinesByConditions error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindTenantLinesByConditions error: code " + response.getCode());
            return null;
        }

        return tenantLineList;
    }


    public List<TenantLine> findActiveTenantLinesByGroupId(Long scriptId, LineType lineType) {
        String url = BASE_URL + "/AiSpeech/callLineTenant/findActiveTenantLinesByGroupId";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(groupId)) {
            paramMap.put("groupId", groupId);
        }
        if (scriptId != null) {
            paramMap.put("scriptId", scriptId);
        }
        if (lineType != null) {
            paramMap.put("lineType", lineType);
        }
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<TenantLine> tenantLineList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    tenantLineList = JsonUtils.fromJson(json, new TypeToken<List<TenantLine>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient findActiveTenantLinesByGroupId error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient findActiveTenantLinesByGroupId error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient findActiveTenantLinesByGroupId error: code " + response.getCode());
            return null;
        }

        return tenantLineList;
    }

    public boolean adminAddTenantLine(int tenantId, int accountId, String groupId, LineType lineType, String tenantLineName, EnableStatus enableStatus, int maxConcurrency, Set<String> secondIndustrySet, List<SupplyLineGroup> supplyLineGroups, String notes) {
        return adminConfigTenantLine(ChangeType.ADD, tenantId, accountId, groupId, lineType, null, null, tenantLineName, enableStatus, maxConcurrency, maxConcurrency, secondIndustrySet, supplyLineGroups, notes);
    }

    public boolean adminEditTenantLine(TenantLine tenantLine, int maxConcurrency) {
        return adminConfigTenantLine(ChangeType.SET, tenantLine.getTenantId(), tenantLine.getAdminId(), tenantLine.getGroupId(), tenantLine.getLineType(), tenantLine.getId(), tenantLine.getLineNumber(), tenantLine.getLineName(), tenantLine.getEnableStatus(), maxConcurrency, null, tenantLine.getSecondIndustries(), tenantLine.getSupplyLineGroups(), tenantLine.getNotes());
    }

    public boolean adminEditTenantLine(TenantLine tenantLine, String tenantLineName, EnableStatus enableStatus, int maxConcurrency, Set<String> secondIndustrySet, List<SupplyLineGroup> supplyLineGroups, String notes) {
        return adminConfigTenantLine(ChangeType.SET, tenantLine.getTenantId(), tenantLine.getAdminId(), tenantLine.getGroupId(), tenantLine.getLineType(), tenantLine.getId(), tenantLine.getLineNumber(), tenantLineName, enableStatus, maxConcurrency, null, secondIndustrySet, supplyLineGroups, notes);
    }

    public boolean adminEditTenantLine(int tenantId, int accountId, String groupId, LineType lineType, int tenantLineId, String tenantLineNo, String tenantLineName, EnableStatus enableStatus, int maxConcurrency, Set<String> secondIndustrySet, List<SupplyLineGroup> supplyLineGroups, String notes) {
        return adminConfigTenantLine(ChangeType.SET, tenantId, accountId, groupId, lineType, tenantLineId, tenantLineNo, tenantLineName, enableStatus, maxConcurrency, null, secondIndustrySet, supplyLineGroups, notes);
    }

    private boolean adminConfigTenantLine(ChangeType changeType, int tenantId, int accountId, String groupId, LineType lineType, Integer tenantLineId, String tenantLineNo, String tenantLineName, EnableStatus enableStatus, int concurrentLimit, Integer lineRemainConcurrent, Collection<String> secondIndustrySet, List<SupplyLineGroup> supplyLineGroups, String notes) {
        String url;
        switch (changeType) {
            case ADD: {
                url = BASE_URL + "/AiSpeech/callLineTenant/addOneTenantLine";
                break;
            }
            case SET: {
                url = BASE_URL + "/AiSpeech/callLineTenant/editOneTenantLine";
                break;
            }
            default: {
                url = null;
                break;
            }
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tenantId", tenantId);
        paramMap.put("adminId", accountId);
        paramMap.put("groupId", groupId);
        paramMap.put("lineType", lineType);
        if (tenantLineId != null) {
            paramMap.put("id", tenantLineId);
        }
        if (tenantLineNo != null) {
            paramMap.put("lineNumber", tenantLineNo);
            paramMap.put("tenantLineNumber", tenantLineNo);
        }
        paramMap.put("lineName", tenantLineName);
        paramMap.put("enableStatus", enableStatus);
        paramMap.put("concurrentLimit", concurrentLimit);
        if (lineRemainConcurrent != null) {
            paramMap.put("lineRemainConcurrent", lineRemainConcurrent);
        }
        if (secondIndustrySet != null) {
            secondIndustrySet = new LinkedHashSet<>(secondIndustrySet);
        }
        paramMap.put("secondIndustries", secondIndustrySet);
        paramMap.put("supplyLineGroups", supplyLineGroups);
        paramMap.put("notes", notes);


        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminChangeTenantLine error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminChangeTenantLine error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminChangeTenantLine error: code " + response.getCode());
            return false;
        }
    }


    public List<CallTeam> adminGetCallTeamList(String groupId) {
        String url = BASE_URL + "/AiSpeech/callSeatManager/findAllCallTeamsByGroupId";
        Map<String, Object> paramMap = ImmutableMap.of("groupId", groupId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<CallTeam> callTeamList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    callTeamList = JsonUtils.fromJson(json, new TypeToken<List<CallTeam>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetCallTeamList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetCallTeamList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetCallTeamList error: code " + response.getCode());
            return null;
        }

        return callTeamList;
    }


    public List<BlacklistInfo> adminGetTenantBlacklistInfoList(String groupId) {
        String url = BASE_URL + "/AiSpeech/tenantBlack/findListForOperator";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(groupId)) {
            paramMap.put("groupId", groupId);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<BlacklistInfo> blacklistInfoList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    blacklistInfoList = JsonUtils.fromJson(json, new TypeToken<List<BlacklistInfo>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantBlacklistInfoList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantBlacklistInfoList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantBlacklistInfoList error: code " + response.getCode());
            return null;
        }

        return blacklistInfoList;
    }


    public List<ScriptSmsTemplate> adminGetSmsTemplateList(String groupId, EnableStatus templateStatus) {
        String url = BASE_URL + "/AiSpeech/tenantSmsTemplate/findSmsTemplate";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(groupId)) {
            paramMap.put("groupId", groupId);
        }
        if (templateStatus != null) {
            paramMap.put("templateStatus", templateStatus);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<ScriptSmsTemplate> scriptSmsTemplateList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    scriptSmsTemplateList = JsonUtils.fromJson(json, new TypeToken<List<ScriptSmsTemplate>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetSmsTemplateList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetSmsTemplateList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetSmsTemplateList error: code " + response.getCode());
            return null;
        }

        return scriptSmsTemplateList;
    }


    public List<TaskTemplate> adminGetTaskTemplateList(@Nonnull String groupId) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTaskTemplate/operatorFindByName";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(groupId)) {
            paramMap.put("groupId", groupId);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<TaskTemplate> taskTemplateList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    taskTemplateList = JsonUtils.fromJson(json, new TypeToken<List<TaskTemplate>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateList error: code " + response.getCode());
            return null;
        }

        return taskTemplateList;
    }


    public List<TaskTemplate> adminGetTaskTemplateListByScriptStringId(String scriptStringId) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTaskTemplate/findTemplateByScriptStringId";
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(scriptStringId)) {
            paramMap.put("scriptStringId", scriptStringId);
        }
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<TaskTemplate> taskTemplateList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    taskTemplateList = JsonUtils.fromJson(json, new TypeToken<List<TaskTemplate>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateListByScriptStringId error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateListByScriptStringId error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateListByScriptStringId error: code " + response.getCode());
            return null;
        }

        return taskTemplateList;
    }


    public TaskTemplate adminGetTaskTemplateById(int templateId) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTaskTemplate/findTemplatePositionById?id=" + templateId;
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        TaskTemplate taskTemplate = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    taskTemplate = JsonUtils.fromJson(json, new TypeToken<TaskTemplate>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateById error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateById error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTaskTemplateById error: code " + response.getCode());
            return null;
        }

        return taskTemplate;
    }


    public boolean adminSaveTaskTemplate(TaskTemplate taskTemplate) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTaskTemplate/save";

        HttpResponse response = BaizeUtils.doPost(url, token, taskTemplate);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, taskTemplate);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveTaskTemplate error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveTaskTemplate error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveTaskTemplate error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 账号解除绑定话术
     *
     * @param groupId
     * @param scriptId
     * @param scriptStringId
     * @return
     */
    public boolean adminRemoveRelatedScript(String groupId, Long scriptId, String scriptStringId) {
        String url = BASE_URL + "/AiSpeech/tenant/remove-related-script";
        String[] items = groupId.split("_");
        Long tenantId = Long.valueOf(items[1]);
        Long id = Long.valueOf(items[2]);
        Map<String,Object> paramMap = ImmutableMap.of(
                "scriptId", scriptId,
                "scriptStringId", scriptStringId,
                "tenantId", tenantId,
                "groupId", groupId,
                "id", id
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminRemoveRelatedScript error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminRemoveRelatedScript error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminRemoveRelatedScript error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 获取所有话术（注意与findAllScriptInPermission接口区分）
     *
     * @param scriptStatus
     * @return
     */
    public List<Script> adminGetScriptList(ScriptStatus scriptStatus) {
        String url = BASE_URL + "/AiSpeech/script/findAllScriptByStatus";
        Map<String, Object> paramMap = ImmutableMap.of("status", scriptStatus);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<Script> scriptList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    scriptList = JsonUtils.fromJson(json, new TypeToken<List<Script>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptList error: code " + response.getCode());
            return null;
        }

        return scriptList;
    }


    /**
     * 查找账号所绑定的话术
     *
     * @param tenantId
     * @param groupId
     * @return
     */
    public AccountScriptInfo adminGetAccountScriptInfo(int tenantId, String groupId) {
        String url = BASE_URL + "/AiSpeech/tenant/related-script";
        Map<String, Object> paramMap = ImmutableMap.of("id", tenantId, "groupId", groupId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        AccountScriptInfo accountScriptInfo = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    accountScriptInfo = JsonUtils.fromJson(json, new TypeToken<AccountScriptInfo>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountScriptInfo error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountScriptInfo error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountScriptInfo error: code " + response.getCode());
            return null;
        }

        return accountScriptInfo;
    }

    public boolean adminBindAccountScript(long scriptId, int tenantId, String groupId, String scriptStringId) {
        final String url = BASE_URL + "/AiSpeech/tenant/create-related-script";
        Map<String, Object> paramMap = ImmutableMap.of(
                "scriptId", scriptId,
                "tenantId", tenantId,
                "groupId", groupId,
                "scriptStringId", scriptStringId
        );
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient bindAccountScript error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient bindAccountScript error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient bindAccountScript error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 账号外呼统计
     *
     * @return
     */
    public List<SmsSupplier> adminFindSmsSuppliers(EnableStatus enableStatus) {
        String url = BASE_URL + "/AiSpeech/smsSupplier/findSmsSuppliersByStatus";
        Map<String, Object> paramMap = ImmutableMap.of("status", enableStatus);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<SmsSupplier> infoList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    infoList = JsonUtils.fromJson(json, new TypeToken<List<SmsSupplier>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindSmsSuppliers error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindSmsSuppliers error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminFindSmsSuppliers error: code " + response.getCode());
            return null;
        }

        return infoList;
    }


    /**
     * 创建短信对接账号
     *
     * @param smsAccount
     * @return
     */
    private boolean adminAddSmsAccount(SmsAccount smsAccount) {
        String url = BASE_URL + "/AiSpeech/smsAccount/addOneSmsAccount";
        HttpResponse response = BaizeUtils.doPost(url, token, smsAccount);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, smsAccount);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminAddSmsAccount error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminAddSmsAccount error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminAddSmsAccount error: code " + response.getCode());
            return false;
        }
    }



    /**
     * 账号外呼统计
     *
     * @return
     */
    public List<AccountOutboundStatisticInfo> adminGetAccountOutboundStatistic() {
        String url = BASE_URL + "/AiSpeech/surveillance/groupStatus";
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<AccountOutboundStatisticInfo> infoList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    infoList = JsonUtils.fromJson(json, new TypeToken<List<AccountOutboundStatisticInfo>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountOutboundStatistic error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountOutboundStatistic error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountOutboundStatistic error: code " + response.getCode());
            return null;
        }

        return infoList;
    }


    public List<AiTask> adminFindAiOutboundTasks(String account) {
        Date date = new Date();
        String strStartTime = SDF_DAY_START.format(date);
        String strEndTime = SDF_DAY_END.format(date);
        return adminFindAiOutboundTasks(account, strStartTime, strEndTime);
    }


    public List<AiTask> adminFindAiOutboundTasks(String account, String strStartTime, String strEndTime) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTask/findList";

        Date date = new Date();
        if (StringUtils.isEmpty(strStartTime)) {
            strStartTime = SDF_DAY_START.format(date);
        }
        if (StringUtils.isEmpty(strEndTime)) {
            strEndTime = SDF_DAY_END.format(date);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startTime", strStartTime);
        paramMap.put("endTime", strEndTime);
        paramMap.put("ifFindAll", "1");
        if (!StringUtils.isEmpty(account)) {
            paramMap.put("account", account);
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<AiTask> infoList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    infoList = JsonUtils.fromJson(json, new TypeToken<List<AiTask>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient findAiOutboundTasks error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient findAiOutboundTasks error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient findAiOutboundTasks error: code " + response.getCode());
            return null;
        }

        return infoList;
    }


    public List<AiTask> findAiOutboundTasks() {
        Date date = new Date();
        String strStartTime = SDF_DAY_START.format(date);
        String strEndTime = SDF_DAY_END.format(date);
        return findAiOutboundTasks(strStartTime, strEndTime);
    }

    public List<AiTask> findAiOutboundTasks(String strStartTime, String strEndTime) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTask/findList";
        Map<String, String> paramMap = ImmutableMap.of("startTime", strStartTime, "endTime", strEndTime);
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<AiTask> aiTaskList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    aiTaskList = JsonUtils.fromJson(json, new TypeToken<List<AiTask>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient findAiOutboundTasks error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient findAiOutboundTasks error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient findAiOutboundTasks error: code " + response.getCode());
            return null;
        }

        return aiTaskList;
    }
//
//
//    public List<Integer> findTaskIdList(List<String> callStatusList, List<Integer> taskIdList) {
//        String url = BASE_URL + "/AiSpeech/aiOutboundTask/findTaskListByAITool";
//        Date date = new Date();
//        String strStartTime = SDF_DAY_START.format(date);
//        String strEndTime = SDF_DAY_END.format(date);
//        Map<String, Object> paramMap = ImmutableMap.of(
//                "startTime",strStartTime,
//                "endTime", strEndTime,
//                "callStatus", callStatusList,
//                "taskIds", taskIdList
//        );
//        HttpResponse rsp = BaizeUtils.doPost(url, token, paramMap);
//
//        List<Integer> filteredTaskIdList = null;
//
//        if (rsp.getCode() == 200) {
//            try {
//                JsonObject baseObj = JsonUtils.parseJson(rsp.getText()).getAsJsonObject();
//                if ("2000".equals(baseObj.get("code").getAsString())) {
//                    String json = baseObj.get("data").toString();
//                    filteredTaskIdList = JsonUtils.fromJson(json, new TypeToken<List<Integer>>(){});
//                } else {
//                    MsgUtils.sendQiweiWarning(account + " BaizeClient findTaskIdList error: " + response.getText());
//                    return null;
//                }
//            } catch (Exception e) {
//                MsgUtils.sendQiweiWarning(account + " BaizeClient findTaskIdList error: " + e);
//                return null;
//            }
//        } else {
//            MsgUtils.sendQiweiWarning(account + " BaizeClient findTaskIdList error: code " + rsp.getCode());
//            return null;
//        }
//
//        return filteredTaskIdList;
//    }

    public boolean batchPreProcess(Set<Long> taskIdSet) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTask/batchPreProcess";
        Map<String, Object> paramMap = ImmutableMap.of(
                "taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","))
        );
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient patchPreProcess error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient patchPreProcess error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient patchPreProcess error: code " + response.getCode());
            return false;
        }
    }


    public boolean changeRestrictArea(ChangeType changeType, Set<Long> taskIdSet,
                                      Set<String> allProvinceCodeSet, Set<String> allCityCodeSet,
                                      Set<String> ydProvinceCodeSet, Set<String> ydCityCodeSet,
                                      Set<String> ltProvinceCodeSet, Set<String> ltCityCodeSet,
                                      Set<String> dxProvinceCodeSet, Set<String> dxCityCodeSet,
                                      Set<String> virtualProvinceCodeSet, Set<String> virtualCityCodeSet,
                                      Set<String> unknownProvinceCodeSet, Set<String> unknownCityCodeSet) {
        final String url;
        switch (changeType) {
            case SET: {
                url = BASE_URL + "/AiSpeech/aiOutboundTask/setRestrictAreaBatch";
                break;
            }
            case ADD: {
                url = BASE_URL + "/AiSpeech/aiOutboundTask/setRestrictAreaAppendBatch";
                break;
            }
            case REMOVE: {
                url = BASE_URL + "/AiSpeech/aiOutboundTask/setRestrictAreaReduceBatch";
                break;
            }
            default: {
                url = null;
                break;
            }
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")));
        paramMap.put("allRestrictProvince", (CollectionUtils.isEmpty(allProvinceCodeSet)) ? null : String.join(",", allProvinceCodeSet));
        paramMap.put("allRestrictCity", (CollectionUtils.isEmpty(allCityCodeSet)) ? null : String.join(",", allCityCodeSet));
        paramMap.put("ydRestrictProvince", (CollectionUtils.isEmpty(ydProvinceCodeSet)) ? null : String.join(",", ydProvinceCodeSet));
        paramMap.put("ydRestrictCity", (CollectionUtils.isEmpty(ydCityCodeSet)) ? null : String.join(",", ydCityCodeSet));
        paramMap.put("ltRestrictProvince", (CollectionUtils.isEmpty(ltProvinceCodeSet)) ? null : String.join(",", ltProvinceCodeSet));
        paramMap.put("ltRestrictCity", (CollectionUtils.isEmpty(ltCityCodeSet)) ? null : String.join(",", ltCityCodeSet));
        paramMap.put("dxRestrictCity", (CollectionUtils.isEmpty(dxProvinceCodeSet)) ? null : String.join(",", dxProvinceCodeSet));
        paramMap.put("dxRestrictProvince", (CollectionUtils.isEmpty(dxCityCodeSet)) ? null : String.join(",", dxCityCodeSet));
        paramMap.put("virtualRestrictCity", (CollectionUtils.isEmpty(virtualProvinceCodeSet)) ? null : String.join(",", virtualProvinceCodeSet));
        paramMap.put("virtualRestrictProvince", (CollectionUtils.isEmpty(virtualCityCodeSet)) ? null : String.join(",", virtualCityCodeSet));
        paramMap.put("unknownRestrictCity", (CollectionUtils.isEmpty(unknownProvinceCodeSet)) ? null : String.join(",", unknownProvinceCodeSet));
        paramMap.put("unknownRestrictProvince", (CollectionUtils.isEmpty(unknownCityCodeSet)) ? null : String.join(",", unknownCityCodeSet));
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient setRestrictAreaBatch error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient setRestrictAreaBatch error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient setRestrictAreaBatch error: code " + response.getCode());
            return false;
        }
    }


    public int getEstimateConcurrency(Set<Long> taskIdSet, Date expectedEndTime) {
        final String url = BASE_URL + "/AiSpeech/aiOutboundTask/neededConcurrentTotal";
        Map<String, String> paramMap = ImmutableMap.of(
                "taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")),
                "expectedFinishTime", DatetimeUtils.getStrDatetime(expectedEndTime)
        );
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return baseObj.getAsJsonObject("data").getAsInt();
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getEstimateConcurrency error: " + response.getText());
                    return -1;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getEstimateConcurrency error: " + e);
                return -1;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getEstimateConcurrency error: code " + response.getCode());
            return -1;
        }
    }


    public boolean startAiAutoTask(Set<Long> taskIdSet, TenantLine tenantLine, int concurrency, boolean isIncludeAutoStop) {
        final String url = BASE_URL + "/AiSpeech/aiOutboundTask/startTaskBatch";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")));
        paramMap.put("callStatus", TaskStatus.ONGONIG.getCaption());
        paramMap.put("lineId", tenantLine.getId());
        paramMap.put("lineName", tenantLine.getLineName());
        paramMap.put("lineCode", tenantLine.getLineNumber());
        paramMap.put("lineRemainConcurrent", tenantLine.getLineRemainConcurrent());
        paramMap.put("totalConcurrent", concurrency);
        if (isIncludeAutoStop) {
            paramMap.put("includeAutoStop", "1");
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient startAiAutoTask error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient startAiAutoTask error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient startAiAutoTask error: code " + response.getCode());
            return false;
        }
    }

    public boolean startAiAutoTask(Set<Long> taskIdSet, TenantLine tenantLine, Date expectedEndTime, boolean isIncludeAutoStop) {
        final String url = BASE_URL + "/AiSpeech/aiOutboundTask/startTaskBatch";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")));
        paramMap.put("callStatus", TaskStatus.ONGONIG.getCaption());
        paramMap.put("lineId", tenantLine.getId());
        paramMap.put("lineName", tenantLine.getLineName());
        paramMap.put("lineCode", tenantLine.getLineNumber());
        paramMap.put("lineRemainConcurrent", tenantLine.getLineRemainConcurrent());
        paramMap.put("expectedFinishTime", DatetimeUtils.getStrDatetime(expectedEndTime));
        if (isIncludeAutoStop) {
            paramMap.put("includeAutoStop", "1");
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient startAiAutoTask error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient startAiAutoTask error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient startAiAutoTask error: code " + response.getCode());
            return false;
        }
    }


    public boolean startAiManualTask(Set<Long> taskIdSet, TenantLine tenantLine, boolean isIncludeAutoStop) {
        final String url = BASE_URL + "/AiSpeech/aiOutboundManualTask/startAiManualTaskBatch";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")));
        paramMap.put("callStatus", TaskStatus.ONGONIG.getCaption());
        paramMap.put("lineId", tenantLine.getId());
        paramMap.put("lineName", tenantLine.getLineName());
        paramMap.put("lineCode", tenantLine.getLineNumber());
        paramMap.put("lineRemainConcurrent", tenantLine.getLineRemainConcurrent());
        paramMap.put("concurrent", 0);
        if (isIncludeAutoStop) {
            paramMap.put("includeAutoStop", "1");
        }
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient startAiManualTask error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient startAiManualTask error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient startAiManualTask error: code " + response.getCode());
            return false;
        }
    }


    public boolean stopTask(TaskType taskType, Set<Long> taskIdSet) {
        String url;
        if (taskType == TaskType.AI_AUTO) {
            url = BASE_URL + "/AiSpeech/aiOutboundTask/stopTaskBatch";
        } else if (taskType == TaskType.AI_MANUAL) {
            url = BASE_URL + "/AiSpeech/aiOutboundManualTask/stopAiManualTaskBatch";
        } else {
            url = null;
        }
        Map<String, Object> paramMap = ImmutableMap.of(
                "taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")),
                "callStatus", "已停止"
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient stopTask error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient stopTask error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient stopTask error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 恢复执行任务
     *
     * @param taskType
     * @param taskIdSet
     * @param includeAutoStop
     * @return
     */
    public boolean resumeTask(TaskType taskType, Set<Long> taskIdSet, boolean includeAutoStop) {
        String url;
        if (taskType == TaskType.AI_AUTO) {
            url = BASE_URL + "/AiSpeech/aiOutboundTask/recoveryTaskBatch";
        } else if (taskType == TaskType.AI_MANUAL) {
            url = BASE_URL + "/AiSpeech/aiOutboundManualTask/recoveryAiManualTaskBatch";
        } else {
            url = null;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")));
        if (includeAutoStop) {
            paramMap.put("includeAutoStop", "1");
        }

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient resumeTask error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient resumeTask error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient resumeTask error: code " + response.getCode());
            return false;
        }
    }



    public boolean editConcurrencyAndStartTask(
            Set<Long> taskIdSet, TaskStatus taskStatus, long lineId, String lineCode, String lineName, int concurrency) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTask/editConcurrencyAndStartTaskBatch";
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("taskIds", String.join(",", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","))));
        paramMap.put("lineId", lineId);
        paramMap.put("lineCode", lineCode);
        paramMap.put("lineName", lineName);
        paramMap.put("totalConcurrent", concurrency);
        paramMap.put("includeAutoStop", "1");
        if (taskStatus != null) {
            paramMap.put("callStatus", taskStatus.getCaption());
        }
        List<Map<String, Object>> paramList = Lists.newArrayList(paramMap);

        HttpResponse response = BaizeUtils.doPost(url, token, paramList);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramList);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient editConcurrencyAndStartTask error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient editConcurrencyAndStartTask error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient editConcurrencyAndStartTask error: code " + response.getCode());
            return false;
        }
    }


    public int findFinishedPhoneCount(TaskType taskType, Set<Long> taskIdSet) {
        Date date = new Date();
        String strStartTime = SDF_DAY_START.format(date);
        String strEndTime = SDF_DAY_END.format(date);
        return findFinishedPhoneCount(taskType, taskIdSet, strStartTime, strEndTime);
    }


    public int findFinishedPhoneCount(TaskType taskType, Set<Long> taskIdSet, String strStartTime, String strEndTime) {
        String url;
        if (taskType == TaskType.AI_AUTO) {
            url = BASE_URL + "/AiSpeech/callRecord/findFinishedPhoneNum";
        } else if (taskType == TaskType.AI_MANUAL) {
            url = BASE_URL + "/AiSpeech/callRecordAiManual/findAiManualFinishedPhoneNum";
        } else {
            url = null;
        }
        Map<String, Object> paramMap = ImmutableMap.of(
                "taskId", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")),
                "putThroughNumLeft", 0,
                "putThroughNumRight", 0,
                "addStartTime", strStartTime,
                "addEndTime", strEndTime
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return baseObj.get("total").getAsInt();
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient findFinishedPhoneCount error: " + response.getText());
                    return -1;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient findFinishedPhoneCount error: " + e);
                return -1;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient findFinishedPhoneCount error: code " + response.getCode());
            return -1;
        }
    }

    /**
     * 批量添加呼叫
     *
     * @param taskType
     * @param taskIdSet
     * @return
     */
    public boolean addFinishedPhoneList(TaskType taskType, Set<Long> taskIdSet) {
        String url;
        if (taskType == TaskType.AI_AUTO) {
            url = BASE_URL + "/AiSpeech/callRecord/addFinishedPhoneListBatch";
        } else if (taskType == TaskType.AI_MANUAL) {
            url = BASE_URL + "/AiSpeech/callRecordAiManual/addAiManualFinishedPhoneListBatch";
        } else {
            url = null;
        }
        Date date = new Date();
        String strStartTime = SDF_DAY_START.format(date);
        String strEndTime = SDF_DAY_END.format(date);
        Map<String, Object> paramMap = ImmutableMap.of(
                "taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")),
                "putThroughNumLeft", 0,
                "putThroughNumRight", 0,
                "addStartTime", strStartTime,
                "addEndTime", strEndTime
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap, 30 * 1000, 45 * 60 * 1000);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap, 30 * 1000, 45 * 60 * 1000);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    System.out.println(account + " BaizeClient addFinishedPhoneList error: " + response.getText());
                    MsgUtils.sendQiweiWarning(account + " BaizeClient addFinishedPhoneList error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                System.out.println(account + " BaizeClient addFinishedPhoneList error: " + e);
                MsgUtils.sendQiweiWarning(account + " BaizeClient addFinishedPhoneList error: " + e);
                return false;
            }
        } else {
            System.out.println(account + " BaizeClient addFinishedPhoneList error: code " + response.getCode());
            MsgUtils.sendQiweiWarning(account + " BaizeClient addFinishedPhoneList error: code " + response.getCode());
            return false;
        }
    }


    public boolean setLineRatio(Set<Long> taskIdSet, int lineRatio) {
        String url = BASE_URL + "/AiSpeech/aiOutboundTask/editBatch";
        Map<String, Object> paramMap = ImmutableMap.of(
                "taskType", "AI_MANUAL",
                "taskIds", taskIdSet.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")),
                "lineRatio", lineRatio
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient setLineRatio error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient setLineRatio error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient setLineRatio error: code " + response.getCode());
            return false;
        }
    }


    public List<Tenant> adminGetTenantList(String account) {
        String url = BASE_URL + "/AiSpeech/tenant/findTenantList";
        if (!StringUtils.isEmpty(account)) {
            url += "?account=" + account;
        }
        Map<String, Object> paramMap = Collections.emptyMap();

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    List<Tenant> tenantList = JsonUtils.fromJson(json, new TypeToken<List<Tenant>>(){});
                    return tenantList;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantList error: code " + response.getCode());
            return null;
        }
    }

    public TenantInfoTool adminGetTenantInfoTool() {
        String url = BASE_URL + "/AiSpeech/tenant/findTenantList";
        Map<String, Object> paramMap = Collections.emptyMap();

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    Map<Integer, String> tenantId2tenantNo = new HashMap<>();
                    Map<Integer, String> tenantId2tenantName = new HashMap<>();
                    List<JsonElement> tenantList = baseObj.get("data").getAsJsonArray().asList();
                    for (JsonElement ele : tenantList) {
                        JsonObject obj = ele.getAsJsonObject();
                        int tenantId = obj.get("id").getAsInt();
                        String tenantNo = obj.get("tenantNo").getAsString();
                        String tenantName = obj.get("tenantName").getAsString();
                        tenantId2tenantNo.put(tenantId, tenantNo);
                        tenantId2tenantName.put(tenantId, tenantName);
                    }
                    return new TenantInfoTool(tenantId2tenantNo, tenantId2tenantName);
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantInfoTool error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantInfoTool error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetTenantInfoTool error: code " + response.getCode());
            return null;
        }
    }


    public IndustryInfoTool adminGetIndustryInfoTool() {
        String url = BASE_URL + "/AiSpeech/industryField/findAllIndustryField";
        Map<String, Object> paramMap = Collections.emptyMap();

        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    Map<Integer, String> firstIndustryId2industryName = new HashMap<>();
                    Map<Integer, String> secondIndustryId2industryName = new HashMap<>();
                    Map<Integer, Integer> secondIndustryId2firstIndustryId = new HashMap<>();
                    List<JsonElement> firstEleList = baseObj.get("data").getAsJsonArray().asList();
                    for (JsonElement firstEle : firstEleList) {
                        JsonObject firstObj = firstEle.getAsJsonObject();
                        int firstIndustryId = firstObj.get("id").getAsInt();
                        String firstIndustryName = firstObj.get("primaryIndustry").getAsString();
                        firstIndustryId2industryName.put(firstIndustryId, firstIndustryName);
                        JsonObject secondObj = firstObj.get("secondaryIndustriesIdMap").getAsJsonObject();
                        List<JsonElement> secondEleList = firstObj.get("secondaryIndustries").getAsJsonArray().asList();
                        for (JsonElement secondEle : secondEleList) {
                            String secondIndustryName = secondEle.getAsString();
                            Integer secondIndustryId = secondObj.get(secondIndustryName).getAsInt();
                            secondIndustryId2industryName.put(secondIndustryId, secondIndustryName);
                            secondIndustryId2firstIndustryId.put(secondIndustryId, firstIndustryId);
                        }
                    }
                    return new IndustryInfoTool(firstIndustryId2industryName, secondIndustryId2industryName, secondIndustryId2firstIndustryId);
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetIndustryInfoTool error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetIndustryInfoTool error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetIndustryInfoTool error: code " + response.getCode());
            return null;
        }
    }

    /**
     * 创建主账号，注意：只有管理员账号才能创建主账号
     *
     * @param account
     * @param password
     * @param name
     * @param tenantId
     * @param isForEncryptionPhones
     * @return
     */
    public boolean adminAddMainUser(String account, String password, String name, int tenantId, Boolean isForEncryptionPhones, Boolean isForEncryptionAgain) {
        return addUser(account, password, null, name, tenantId, isForEncryptionPhones, isForEncryptionAgain, null);
    }

    /**
     * 创建子账号，注意：只有主账号才能创建子账号
     *
     * @param account
     * @param password
     * @param password2
     * @param name
     * @param roleId
     * @return
     */
    public boolean addSubUser(String account, String password, String password2, String name, int roleId) {
        return addUser(account, password, password2, name, this.tenantId, null, null, roleId);
    }

    private boolean addUser(String account, String password, String password2, String name, int tenantId, Boolean isForEncryptionPhones, Boolean isForEncryptionAgain, Integer roleId) {
        String url = BASE_URL + "/AiSpeech/admin/addUser";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("account", account);
        paramMap.put("password", password);
        paramMap.put("name", name);
        paramMap.put("phone", "");
        paramMap.put("email", "");
        paramMap.put("address", "");
        paramMap.put("accountEnable", true);
        paramMap.put("note", "");
        paramMap.put("tenantId", tenantId);
        paramMap.put("accountType", 1);
        paramMap.put("gender", "MALE");
        paramMap.put("department", "");
        if (isForEncryptionPhones != null) {
            paramMap.put("isForEncryptionPhones", isForEncryptionPhones);
        }
        if (isForEncryptionAgain != null) {
            paramMap.put("isForEncryptionAgain", isForEncryptionAgain);
        }
        if (roleId != null) {
            paramMap.put("roleId", roleId);
        }
        if (password2 != null) {
            paramMap.put("password2", password2);
        }

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient addUser error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient addUser error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient addUser error: code " + response.getCode());
            return false;
        }
    }

    /**
     * 获取账号列表
     *
     * @return
     */
    public List<UserInfo> getUserInfoList() {
        String url = BASE_URL + "/AiSpeech/admin/getUserListByConditions";
        Map<String, Object> paramMap = ImmutableMap.of();

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    List<UserInfo> userInfoList = JsonUtils.fromJson(json, new TypeToken<List<UserInfo>>(){});
                    return userInfoList;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getUserInfoList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getUserInfoList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getUserInfoList error: code " + response.getCode());
            return null;
        }
    }


    /**
     * 创建角色
     *
     * @param role
     * @return 创建成功返回roleId，失败返回-1
     */
    public int addRole(Role role) {
        String roleName = role.getCaption();
        Map<String, List<String>> authorityMap = ROLE_AUTHORITY_MAP.get(role);
        if (authorityMap == null) {
            return -1;
        } else {
            return addRole(roleName, authorityMap, 1, this.tenantId, "");
        }
    }

    /**
     * 创建角色
     *
     * @param roleName 角色名称
     * @param sourceRole 来源角色
     * @return 创建成功返回roleId，失败返回-1
     */
    public int addRole(String roleName, Role sourceRole) {
        Map<String, List<String>> authorityMap = ROLE_AUTHORITY_MAP.get(sourceRole);
        if (authorityMap == null) {
            return -1;
        } else {
            return addRole(roleName, authorityMap, 1, this.tenantId, "");
        }
    }

    /**
     * 创建角色
     *
     * @param roleName
     * @param authorityMap
     * @param accountType
     * @param tenantId
     * @param note
     * @return 创建成功返回roleId，失败返回-1
     */
    private int addRole(String roleName, Map<String, List<String>> authorityMap, int accountType, int tenantId, String note) {
        String url = BASE_URL + "/AiSpeech/adminRole/addOneRole";
        Map<String, Object> paramMap = ImmutableMap.of(
                "roleName", roleName,
                "authorityMap", authorityMap,
                "accountType", accountType,
                "tenantId", tenantId,
                "note", note
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return baseObj.get("data").getAsJsonObject().get("id").getAsInt();
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient addRole error: " + response.getText());
                    return -1;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient addRole error: " + e);
                return -1;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient addRole error: code " + response.getCode());
            return -1;
        }
    }

    public List<RoleInfo> getRoleInfoList() {
        String url = BASE_URL + "/AiSpeech/adminRole/findAdminRoles";
        Map<String, Object> paramMap = ImmutableMap.of();

        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    List<RoleInfo> roleInfoList = JsonUtils.fromJson(json, new TypeToken<List<RoleInfo>>(){});
                    return roleInfoList;
//                    return roleInfoList.stream().collect(Collectors.toMap(roleInfo -> Role.fromName(roleInfo.getRoleName()), roleInfo -> roleInfo, (oldValue, newValue) -> newValue));
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getRoleInfoList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getRoleInfoList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getRoleInfoList error: code " + response.getCode());
            return null;
        }
    }

    public boolean setIpList(int roleId, Collection<String> ips) {
        String url = BASE_URL + "/AiSpeech/adminRole/updateIpListById";
        Map<String, Object> paramMap = ImmutableMap.of(
                "id", roleId,
                "ipList", new ArrayList<>(ips)
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient setIpList error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient setIpList error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient setIpList error: code " + response.getCode());
            return false;
        }
    }

    public List<String> getIpList(int roleId) {
        String url = BASE_URL + "/AiSpeech/adminRole/findIpListByRoleId";
        Map<String, Object> paramMap = ImmutableMap.of("id", roleId);

        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    List<String> ipList = JsonUtils.fromJson(json, new TypeToken<List<String>>(){});
                    Set<String> ipSet = new LinkedHashSet<>(ipList);
                    ipSet.remove(null);
                    return new ArrayList<>(ipSet);
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getRoleMap error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getRoleMap error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getRoleMap error: code " + response.getCode());
            return null;
        }
    }

    public AccountOperatorParam adminGetAccountOperatorParam(int accountId) {
        String url = BASE_URL + "/AiSpeech/admin/findAccountOperatorParamByAccountId";
        Map<String, Object> paramMap = ImmutableMap.of(
                "accountId", accountId
        );

        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    return JsonUtils.fromJson(json, new TypeToken<AccountOperatorParam>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountOperatorParam error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountOperatorParam error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAccountOperatorParam error: code " + response.getCode());
            return null;
        }
    }


    /**
     * 配置回调地址等信息
     *
     * @param accountId accountId
     * @param dataStatisticOutboundTypeSet 【数据统计】查询范围
     * @param taskCallbackUrl 【任务回调】回调地址
     * @param callbackOutboundTypeSet 【通话回调】回调范围
     * @param callbackStatusSet 【通话回调】回调状态
     * @param outboundCallbackFieldSet 【通话回调】回调字段
     * @param quicklyCallbackUrl 【通话回调】快速回调接口
     * @param newCallbackUrl 【通话回调】话后回调接口（新）
     * @param txtUpdateCallbackUrl 【通话回调】文本补推接口
     * @param mSmsCallbackUrl 【其他回调】M短信接口
     * @param oldCallBackUrl 【其他回调】通话回调接口（旧）
     * @param smsCallbackFieldSet 【短信回调】回调字段
     * @param smsCallbackUrl 【短信回调】回调地址
     * @param upSmsCallbackUrl 【短信回调】上行短信回调地址
     * @param ipSet 【IP配置】IP地址
     * @return
     */
    public boolean adminSetAccountOperatorParam(
            int accountId, Set<OutboundType> dataStatisticOutboundTypeSet, String taskCallbackUrl,
            Set<OutboundType> callbackOutboundTypeSet, Set<CallbackStatus> callbackStatusSet,
            Set<OutboundCallbackField> outboundCallbackFieldSet, String quicklyCallbackUrl, String newCallbackUrl,
            String txtUpdateCallbackUrl, String mSmsCallbackUrl, String oldCallBackUrl,
            Set<SmsCallbackField> smsCallbackFieldSet, String smsCallbackUrl, String upSmsCallbackUrl, Set<String> ipSet) {
        String url = BASE_URL + "/AiSpeech/admin/updateAccountOperatorParam";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("accountId", accountId);
        paramMap.put("dataStatisticRange", dataStatisticOutboundTypeSet);
        if (!StringUtils.isEmpty(taskCallbackUrl)) {
            paramMap.put("taskCallbackUrl", taskCallbackUrl);
        }
        if (!CollectionUtils.isEmpty(callbackOutboundTypeSet)) {
            paramMap.put("callBackRange", callbackOutboundTypeSet);
        }
        if (!StringUtils.isEmpty(oldCallBackUrl)) {
            paramMap.put("callBackUrl", oldCallBackUrl);
        }
        if (!StringUtils.isEmpty(quicklyCallbackUrl)) {
            paramMap.put("callSmsCallbackUrl", quicklyCallbackUrl);
        }
        if (!StringUtils.isEmpty(mSmsCallbackUrl)) {
            paramMap.put("callMCallbackUrl", mSmsCallbackUrl);
        }
        if (!StringUtils.isEmpty(newCallbackUrl)) {
            paramMap.put("callDataCallbackUrl", newCallbackUrl);
        }
        if (!StringUtils.isEmpty(txtUpdateCallbackUrl)) {
            paramMap.put("callUpdateCallbackUrl", txtUpdateCallbackUrl);
        }
        if (!StringUtils.isEmpty(smsCallbackUrl)) {
            paramMap.put("smsCallbackUrl", smsCallbackUrl);
        }
        if (!StringUtils.isEmpty(upSmsCallbackUrl)) {
            paramMap.put("smsMoCallbackUrl", upSmsCallbackUrl);
        }
        paramMap.put("callbackFieldConfig", Stream.concat(outboundCallbackFieldSet.stream(), smsCallbackFieldSet.stream()).collect(Collectors.toSet()));
        paramMap.put("callbackStatusConfig", CallbackStatus.getValueSet(callbackStatusSet));
        if (!CollectionUtils.isEmpty(ipSet)) {
            paramMap.put("whiteIps", ipSet);
        }

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminSetAccountOperatorParam error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminSetAccountOperatorParam error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminSetAccountOperatorParam error: code " + response.getCode());
            return false;
        }
    }

    public List<Product> adminGetProductList(String productNameContain) {
        String url = BASE_URL + "/AiSpeech/productAdmin/findProductByName";
        Map<String, Object> paramMap = ImmutableMap.of(
                "name", productNameContain
        );

        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    return JsonUtils.fromJson(json, new TypeToken<List<Product>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetProductList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetProductList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetProductList error: code " + response.getCode());
            return null;
        }
    }

    public boolean adminSaveProduct(String productName, int secondIndustryId, String secondIndustryName) {
        String url = BASE_URL + "/AiSpeech/productAdmin/saveProduct";
        Map<String, Object> paramMap = ImmutableMap.of(
                "productName", productName,
                "industrySecondFieldId", secondIndustryId,
                "industrySecondFieldName", secondIndustryName
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveProduct error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveProduct error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveProduct error: code " + response.getCode());
            return false;
        }
    }

    public boolean adminSaveProgram(String programName, int productId, String productName, int secondIndustryId, String secondIndustryName, int tenantId, String groupId) {
        String url = BASE_URL + "/AiSpeech/tenantProgramAdmin/saveProgram";
        Map<String, Object> paramMap = ImmutableMap.of(
                "programName", programName,
                "productId", productId,
                "productName", productName,
                "secondIndustryId", secondIndustryId,
                "secondIndustryName", secondIndustryName,
                "tenantId", tenantId,
                "groupId", groupId
        );

        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveProgram error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveProgram error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminSaveProgram error: code " + response.getCode());
            return false;
        }
    }

    @Deprecated
    public Long adminGetCallRecordCount (
            String phone, String account, Set<TaskType> taskTypeSet, String startDatetime, String endDateTime,
            List<String> scriptStringIdList) {
        if (CollectionUtils.isEmpty(taskTypeSet)) {
            taskTypeSet = Arrays.stream(TaskType.values()).collect(Collectors.toSet());
        }
        List<String> urlList = new ArrayList<>();
        for (TaskType taskType: taskTypeSet) {
            String url = null;
            if (taskType == TaskType.AI_AUTO) {
                url = BASE_URL + "/AiSpeech/callRecord/callRecordNum";
            } else if (taskType == TaskType.AI_MANUAL) {
                url = BASE_URL + "/AiSpeech/callRecordAiManual/callRecordAiManualNum";
            } else if (taskType == TaskType.MANUAL_DIRECT) {
                url = BASE_URL + "/AiSpeech/callRecordManual/callRecordManualNum";
            }
            urlList.add(url);
        }
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(phone)) {
            paramMap.put("phone", phone);
        }
        paramMap.put("needOrder", "1");
        paramMap.put("startPage", 0);
        paramMap.put("pageNum", 100);
        paramMap.put("adminFlag", "0");
        if (!StringUtils.isEmpty(account)) {
            paramMap.put("account", account);
        }
        if (!StringUtils.isEmpty(startDatetime)) {
            paramMap.put("calloutStartTime", startDatetime);
        }
        if (!StringUtils.isEmpty(endDateTime)) {
            paramMap.put("calloutEndTime", endDateTime);
        }
        if (!CollectionUtils.isEmpty(scriptStringIdList)) {
            paramMap.put("scriptStringIdList", String.join(",", scriptStringIdList));
        }
        long totalCount = 0;
        for (String url: urlList) {
            HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
            if (response.getCode() == 403) {
                update();
                response = BaizeUtils.doPost(url, token, paramMap);
            }

            if (response.getCode() == 200) {
                try {
                    JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                    if ("2000".equals(baseObj.get("code").getAsString())) {
                        long count = baseObj.get("total").getAsLong();
                        totalCount += count;
                    } else {
                        MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetCallRecordCount error: " + response.getText());
                        return null;
                    }
                } catch (Exception e) {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetCallRecordCount error: " + e);
                    return null;
                }
            } else {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetCallRecordCount error: code " + response.getCode());
                return null;
            }
        }
        return totalCount;
    }


    public List<ScriptCallStatistic> adminGetScriptCallStatisticList(String strStartDate, String strEndDate) {
        String url;
        if (BaseUtils.isLocal()) {
            url = "http://192.168.23.85:8267/assistant/getScriptCallStatistic?startDate=" + strStartDate + "&endDate=" + strEndDate;
        } else {
            url = BASE_URL + "/AiMonitor/assistant/getScriptCallStatistic?startDate=" + strStartDate + "&endDate=" + strEndDate;
        }
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    return JsonUtils.fromJson(json, new TypeToken<List<ScriptCallStatistic>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptCallStatistic error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptCallStatistic error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptCallStatistic error: code " + response.getCode());
            return null;
        }
    }


    public List<AudioInfo> adminGetAudioUrlList(
            String phone, String account, Set<TaskType> taskTypeSet, String startDatetime, String endDateTime,
            List<String> scriptStringIdList) {
        if (CollectionUtils.isEmpty(taskTypeSet)) {
            taskTypeSet = Arrays.stream(TaskType.values()).collect(Collectors.toSet());
        }
        List<String> urlList = new ArrayList<>();
        for (TaskType taskType: taskTypeSet) {
            String url = null;
            if (taskType == TaskType.AI_AUTO) {
                url = BASE_URL + "/AiSpeech/callRecord/callRecordListForOperation";
            } else if (taskType == TaskType.AI_MANUAL) {
                url = BASE_URL + "/AiSpeech/callRecordAiManual/callRecordAiManualListForOperation";
            } else if (taskType == TaskType.MANUAL_DIRECT) {
                url = BASE_URL + "/AiSpeech/callRecordManual/callRecordManualListForOperation";
            }
            urlList.add(url);
        }
        Map<String, Object> paramMap = new HashMap<>();
        if (!StringUtils.isEmpty(phone)) {
            paramMap.put("phone", phone);
        }
        paramMap.put("needOrder", "1");
        paramMap.put("startPage", 0);
        paramMap.put("pageNum", 100);
        paramMap.put("adminFlag", "0");
        if (!StringUtils.isEmpty(account)) {
            paramMap.put("account", account);
        }
        if (!StringUtils.isEmpty(startDatetime)) {
            paramMap.put("calloutStartTime", startDatetime);
        }
        if (!StringUtils.isEmpty(endDateTime)) {
            paramMap.put("calloutEndTime", endDateTime);
        }
        if (!CollectionUtils.isEmpty(scriptStringIdList)) {
            paramMap.put("scriptStringIdList", String.join(",", scriptStringIdList));
        }
        List<AudioInfo> allAudioInfoList = new ArrayList<>();
        for (String url: urlList) {
            HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
            if (response.getCode() == 403) {
                update();
                response = BaizeUtils.doPost(url, token, paramMap);
            }

            if (response.getCode() == 200) {
                try {
                    JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                    if ("2000".equals(baseObj.get("code").getAsString())) {
                        List<AudioInfo> audioInfoList = baseObj.get("data").getAsJsonArray().asList().stream()
                                .filter(item -> item.getAsJsonObject().get("callDurationSec").getAsInt() > 0)
                                .map(item -> new AudioInfo(
                                        item.getAsJsonObject().get("wholeAudioFileUrl").getAsString(),
                                        item.getAsJsonObject().get("lineCode").getAsString(),
                                        item.getAsJsonObject().get("callOutTime").getAsString(),
                                        item.getAsJsonObject().get("callDurationSec").getAsInt()
                                        ))
                                .filter(audioInfo -> !StringUtils.isEmpty(audioInfo.getAudioUrl()))
                                .collect(Collectors.toList());
                        if (!CollectionUtils.isEmpty(audioInfoList)) {
                            allAudioInfoList.addAll(audioInfoList);
                        }
                    } else {
                        MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAudioUrlList error: " + response.getText());
//                        return null;
                    }
                } catch (Exception e) {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAudioUrlList error: " + e);
//                    return null;
                }
            } else {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetAudioUrlList error: code " + response.getCode());
//                return null;
            }
        }
        return allAudioInfoList;
    }

    public Set<String> adminGetScriptNamesByDateAndAccount(String strDate, String account) {
        String url = BASE_URL + "/AiMonitor/assistant/getScriptNamesByDateAndAccount";
        Map<String, Object> paramMap = ImmutableMap.of(
                "date", strDate,
                "account", account
        );
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    return JsonUtils.fromJson(json, new TypeToken<Set<String>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptNamesByDateAndAccount error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptNamesByDateAndAccount error: " + e);
                    return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient adminGetScriptNamesByDateAndAccount error: code " + response.getCode());
                return null;
        }
    }


    public boolean addTraceInfo(String page, Object params) {
        String url = BASE_URL + "/AiMonitor/traceLog/addTraceInfo";
        Map<String, Object> paramMap = ImmutableMap.of(
                "page", page,
                "params", (params instanceof String)? params : JsonUtils.toJson(params, false),
                "submitTime", DatetimeUtils.getStrDatetime(new Date())
        );
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient addTraceInfo error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient addTraceInfo error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient addTraceInfo error: code " + response.getCode());
            return false;
        }
    }

    public boolean download(String url, String pathOutput) {
        return BaizeUtils.download(url, token, pathOutput);
    }

    public enum ChangeType {
        SET,
        ADD,
        REMOVE,
        ;
    }


    public static void main(String[] args) {
        BaizeClient client = BaizeClientFactory.getBaizeClient("chen001");
//        BaizeClient client = BaizeClientFactory.getBaizeClient();

//        client.getMainAccount2GroupId();
//        List<TenantLine> result = client.findTenantLinesByConditions();

//        List<AiTask> taskList = client.findAiOutboundTasks();
//        Set<Integer> taskIdSet = taskList.stream().map(task -> task.getId()).collect(Collectors.toSet());
//        int phoneCount = client.findFinishedPhoneCount(TaskType.AI_AUTO, taskIdSet);
//        System.out.println(phoneCount);

//        List<TenantLine> tenantLineList = BaizeClientFactory.getBaizeClient("chen001").findActiveTenantLinesByGroupId("1_53_322", 1107, TenantLineType.AI_OUTBOUND_CALL);
//        System.out.println(JsonUtils.toJson(taskList, false));

//        IndustryInfo industryInfo = client.getIndustryInfo();
//        System.out.println(industryInfo);

//        TenantInfo tenantInfo = client.getTenantInfo();
//        System.out.println(tenantInfo);

//        AccountOperatorParam info = client.getAccountOperatorParam(322);
//        System.out.println(info);

//        System.out.println(client.saveProduct("70众安保险", 7, "保险"));

//        List<Product> productList = client.getProductList("zw验收产品");
//        System.out.println(productList);

//        Map<Role, RoleInfo> roleInfoMap = client.getRoleMap();
//        System.out.println(roleInfoMap);

//        System.out.println(client.getIpList(187));

//        boolean rsp = client.adminSetAccountOperatorParam(
//                606,
//                Sets.newHashSet(OutbountType.PURE_AI, OutbountType.MANUAL_DIRECT),
//                "http://taskCallbackUrl.com",
//                Sets.newHashSet(OutbountType.PURE_AI, OutbountType.HUMAN_MACHINE),
//                Sets.newHashSet(CallbackStatus.GET_THROUGH, CallbackStatus.NOT_CONNECTED),
//                Sets.newHashSet(OutboundCallbackField.company, OutboundCallbackField.name),
//                "http://quicklyCallbackUrl.com",
//                "http://newCallbackUrl.com",
//                "http://txtUpdateCallbackUrl.com",
//                "http://mSmsCallbackUrl.com",
//                "http://oldCallBackUrl.com",
//                Sets.newHashSet(SmsCallbackField.smsCompany, SmsCallbackField.smsFullName),
//                "http://smsCallbackUrl.com",
//                "http://upSmsCallbackUrl.com",
//                Sets.newHashSet("192.168.1.1", "192.168.1.2")
//        );
//        System.out.println(rsp);
//        System.out.println(Paths.get("data/1.gz").toAbsolutePath());
//        client.download("https://mirrors.tuna.tsinghua.edu.cn/deepin/apricot/dists/apricot/Contents-amd64.gz", Paths.get("data/1.gz").toString());
//        List<TenantLine> tenantLineList = client.adminFindTenantLinesByConditions("1_53_322");
//        System.out.println(tenantLineList);

//        List<SupplyLine> supplyLineList = client.adminFindSupplyLinesByConditions("GYXL9E59CE0817E", null, null, null, null, null, null, null, null, null, null);
//        System.out.println(supplyLineList);

//        List<AccountOutboundStatisticInfo> infoList = client.adminGetAccountOutboundStatistic();
//        System.out.println(infoList);

//        TaskTemplate taskTemplate = client.adminGetTaskTemplateById(343);
//        System.out.println(taskTemplate);

//        List<Tenant> tenantList = client.adminGetTenantList("chen001");
//        System.out.println(tenantList);

//        AccountScriptInfo accountScriptInfo = client.adminGetAccountScriptInfo(53, "1_53_322");
//        System.out.println(accountScriptInfo);

//        List<String> scriptNameList = client.adminGetScriptNamesByDateAndAccount("2025-11-05", "baotai88");
//        System.out.println(scriptNameList);

//        List<SmsSupplier> smsSupplierList = client.adminFindSmsSuppliers(EnableStatus.ENABLE);
//        System.out.println(smsSupplierList);

//        SystemConcurrency systemConcurrency = client.adminGetSystemConcurrency();
//        System.out.println(systemConcurrency);

//        Script script = client.getScript(2384);
//        System.out.println(script);

//        List<ScriptCanvas> scriptCanvasList = client.getScriptCanvasList(2513);
//        System.out.println(scriptCanvasList);

//        ScriptCanvas scriptCanvas = client.getScriptCanvas(7726);
//        System.out.println(scriptCanvas);

//        ScriptCorpus scriptCorpus = client.getScriptCorpus(106857);
//        System.out.println(scriptCorpus);

//        List<TaskTemplate> taskTemplateList = client.adminGetTaskTemplateListByScriptStringId("4c63597d-c40d-4a44-9295-c83149d36a58");
//        System.out.println(taskTemplateList);

//        List<ScriptCallStatistic> scriptCallStatisticList = client.adminGetScriptCallStatisticList("2026-01-26", "2026-01-26");
//        System.out.println(scriptCallStatisticList);

//        client.resumeTask(TaskType.AI_AUTO, Sets.newHashSet(26726L), false);

        client.editConcurrencyAndStartTask(Sets.newHashSet(26771L, 26772L), null, 370, "SHXL057FB716B9A", "【保险】chen001测试1", 7);
    }
}
