package org.example.chat.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.chat.bean.baize.script.*;
import org.example.utils.JsonUtils;
import org.example.utils.ThreadUtils;
import org.example.utils.bean.HttpResponse;
import org.example.utils.bean.HttpResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.example.chat.utils.BaizeClient.BASE_URL;

public interface BaizeScriptClient {

    String getToken();

    String getAccount();
    void update();


    /**
     * 获取语义列表
     *
     * @param secondIndustryId
     * @return
     */
    default List<Semantic> getSemanticList(Integer secondIndustryId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/semantic/findSemanticListByIndustry";
        Map<String, Object> paramMap = ImmutableMap.of("secondIndustryId", secondIndustryId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<Semantic> semanticList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    semanticList = JsonUtils.fromJson(json, new TypeToken<List<Semantic>>() {
//                    });
                    semanticList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<Semantic>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getSemanticList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getSemanticList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getSemanticList error: code " + response.getCode());
            return null;
        }

        return semanticList;
    }


    /**
     * 获取当前账号权限下可见的话术摘要（注意与findAllScriptByStatus接口区分）
     *
     * @param scriptStatus
     * @return
     */
    default List<Script> getScriptList(ScriptStatus scriptStatus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptEditor/findAllScriptInPermission";
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
//                    scriptList = JsonUtils.fromJson(json, new TypeToken<List<Script>>() {
//                    });
                    scriptList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<Script>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptList error: code " + response.getCode());
            return null;
        }

        return scriptList;
    }


    /**
     * 获取话术详情
     *
     * @param scriptId
     * @return
     */
    default Script getScript(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/script/findOneScriptById";
        Map<String, Object> paramMap = ImmutableMap.of("id", scriptId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        Script script = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    script = JsonUtils.fromJson(json, new TypeToken<Script>() {
//                    });
                    script = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<Script>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getScript error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getScript error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getScript error: code " + response.getCode());
            return null;
        }

        return script;
    }


    default boolean stopScript(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptEditor/stopOneScript?scriptId=" + scriptId;
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
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient stopScript error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient stopScript error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient stopScript error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 锁定话术
     *
     * @param scriptId
     * @return
     */
    default boolean lockScript(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptEditor/lockOneScript?scriptId=" + scriptId;
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
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient lockScript error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient lockScript error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient lockScript error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 解锁话术
     *
     * @param scriptId
     * @return
     */
    default boolean unlockScript(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptEditor/unLockOneScript?scriptId=" + scriptId;
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
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient unlockScript error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient unlockScript error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient unlockScript error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 获取事件触发器列表
     *
     * @param scriptId
     * @return
     */
    default List<EventTrigger> getEventTriggerList(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/eventTriggerKey/findAllEventTriggersByScriptId?scriptId=" + scriptId;
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        List<EventTrigger> eventTriggerList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    eventTriggerList = JsonUtils.fromJson(json, new TypeToken<List<EventTrigger>>() {
//                    });
                    eventTriggerList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<EventTrigger>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getEventTriggerList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getEventTriggerList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getEventTriggerList error: code " + response.getCode());
            return null;
        }

        return eventTriggerList;
    }


    /**
     * 获取查询分支键值信息
     *
     * @param scriptId
     * @return
     */
    default List<InfoQueryInfo> getInfoQueryInfoList(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/infoQueryKey/findInfoQueryKeysByScriptLongId";
        Map<String, Object> paramMap = ImmutableMap.of("id", scriptId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<InfoQueryInfo> infoQueryInfoList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    infoQueryInfoList = JsonUtils.fromJson(json, new TypeToken<List<InfoQueryInfo>>() {
//                    });
                    infoQueryInfoList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<InfoQueryInfo>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getInfoQueryInfoList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getInfoQueryInfoList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getInfoQueryInfoList error: code " + response.getCode());
            return null;
        }

        return infoQueryInfoList;
    }


    /**
     * 获取意向分类列表
     *
     * @param scriptId
     * @return
     */
    default List<IntentionClass> getIntentionClassList(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/intentionType/script/" + scriptId;
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<IntentionClass> intentionClassList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    intentionClassList = JsonUtils.fromJson(json, new TypeToken<List<IntentionClass>>() {
//                    });
                    intentionClassList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<IntentionClass>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getIntentionClassList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getIntentionClassList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getIntentionClassList error: code " + response.getCode());
            return null;
        }

        return intentionClassList;
    }


    /**
     * 获取意向标签列表
     *
     * @param scriptId
     * @return
     */
    default List<IntentionLabel> getIntentionLabelList(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/label/script/used/" + scriptId;
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<IntentionLabel> intentionLabelList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    intentionLabelList = JsonUtils.fromJson(json, new TypeToken<List<IntentionLabel>>() {
//                    });
                    intentionLabelList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<IntentionLabel>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getIntentionLabelList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getIntentionLabelList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getIntentionLabelList error: code " + response.getCode());
            return null;
        }

        return intentionLabelList;
    }


    /**
     * 保存意向标签
     *
     * @param scriptId
     * @param  labelName
     * @return
     */
    default boolean saveIntentionLabel(long scriptId, String labelName) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptMasterCanvas/addOneScriptCanvas";
        Map<String, Object> paramMap = ImmutableMap.of("labelName", labelName,"scriptId", scriptId);
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
                    MsgUtils.sendQiweiWarning(account + " BaizeClient saveIntentionLabel error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient saveIntentionLabel error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient saveIntentionLabel error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 获取知识库分组列表
     *
     * @param scriptId
     * @return
     */
    default List<KnowledgeGroup> getKnowledgeBaseGroupList(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/knowledgeGroups/findByScriptId/" + scriptId;
        Map<String, Object> paramMap = Collections.emptyMap();
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<KnowledgeGroup> groupList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    groupList = JsonUtils.fromJson(json, new TypeToken<List<KnowledgeGroup>>() {
//                    });
                    groupList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<KnowledgeGroup>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getKnowledgeBaseGroupList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getKnowledgeBaseGroupList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getKnowledgeBaseGroupList error: code " + response.getCode());
            return null;
        }

        return groupList;
    }

    /**
     * 获取主流程画布摘要列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCanvas> getMainScriptCanvasList(Long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptMasterCanvas/findAllScriptCanvasByScriptId";
        Map<String, Object> paramMap = ImmutableMap.of("scriptId", scriptId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCanvas> scriptCanvasList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCanvasList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCanvas>>() {
//                    });
                    scriptCanvasList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCanvas>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getMainScriptCanvasList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getMainScriptCanvasList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getMainScriptCanvasList error: code " + response.getCode());
            return null;
        }

        return scriptCanvasList;
    }


    /**
     * 获取主流程画布详情
     *
     * @param canvasId
     * @return
     */
    default ScriptCanvas getMainScriptCanvas(Long canvasId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptMasterCanvas/findOneScriptCanvasById";
        Map<String, Object> paramMap = ImmutableMap.of("canvasId", canvasId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        ScriptCanvas scriptCanvas = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCanvas = JsonUtils.fromJson(json, new TypeToken<ScriptCanvas>() {
//                    });
                    scriptCanvas = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCanvas>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptCanvas error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptCanvas error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptCanvas error: code " + response.getCode());
            return null;
        }

        return scriptCanvas;
    }

    /**
     * 创建主流程画布
     *
     * @param scriptCanvas
     * @return
     */
    default HttpResult<ScriptCanvas> createMainScriptCanvas(ScriptCanvas scriptCanvas) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptMasterCanvas/addOneScriptCanvas";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCanvas);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCanvas);
        }

        boolean success;
        String msg;
        ScriptCanvas object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    success = true;
                    msg = "";
//                    scriptCanvas = JsonUtils.fromJson(json, new TypeToken<ScriptCanvas>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCanvas>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient createMainScriptCanvas error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient createMainScriptCanvas error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient createMainScriptCanvas error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }

        return new HttpResult<>(success, msg, object);
    }


    /**
     * 保存主流程画布
     *
     * @param scriptCanvas
     * @return
     */
    default boolean saveMainScriptCanvas(ScriptCanvas scriptCanvas) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptMasterCanvas/saveOneScriptCanvas";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCanvas);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCanvas);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient saveMainScriptCanvas error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient saveMainScriptCanvas error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient saveMainScriptCanvas error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 获取主流程语料
     *
     * @param corpusId
     * @return
     */
    default ScriptCorpus getScriptCorpus(long corpusId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptCorpus/findOneMasterScriptCorpusById";
        Map<String, Object> paramMap = ImmutableMap.of("corpusId", corpusId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        ScriptCorpus scriptCorpus = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCorpus = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    scriptCorpus = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptCorpus error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptCorpus error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptCorpus error: code " + response.getCode());
            return null;
        }

        return scriptCorpus;
    }

    default HttpResult<ScriptCorpus> saveScriptCorpus(ScriptCorpus scriptCorpus) {
        HttpResult<ScriptCorpus> httpResult;
        switch (scriptCorpus.getCorpusType()) {
            case MASTER_ORDINARY:
                httpResult = saveMainOrdinaryScriptCorpus(scriptCorpus);
                break;
            case MASTER_CONNECT:
                httpResult = saveMainConnectScriptCorpus(scriptCorpus);
                break;
            case KNOWLEDGE_BASE_QA:
                httpResult = saveKbQaScriptCorpus(scriptCorpus);
                break;
            case KNOWLEDGE_ORDINARY:
                httpResult = saveKbOrdinaryScriptCorpus(scriptCorpus);
                break;
            case KNOWLEDGE_CONNECT:
                httpResult = saveKbConnectScriptCorpus(scriptCorpus);
                break;
            case FUNC_PRIOR_QA:
                httpResult = saveFuncPriorQaScriptCorpus(scriptCorpus);
                break;
            case FUNC_REPEAT:
                httpResult = saveFuncRepeatScriptCorpus(scriptCorpus);
                break;
            case FUNC_SILENCE:
                httpResult = saveFuncSilenceScriptCorpus(scriptCorpus);
                break;
            case PRE_INTERRUPT:
                httpResult = savePreInterruptCorpus(scriptCorpus);
                break;
            case PRE_CONTINUE:
                httpResult = savePreContinueCorpus(scriptCorpus);
                break;
            case PRE_UNDERTAKE:
                httpResult = savePreUndertakeCorpus(scriptCorpus);
                break;
            default:
                httpResult = new HttpResult<>(false, "未知的语料类型" + scriptCorpus.getCorpusType(), null);
        }
        return httpResult;
    }


    /**
     * 保存主流程普通语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveMainOrdinaryScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptCorpus/saveMasterOrdinaryCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveMainOrdinaryScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveMainOrdinaryScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveMainOrdinaryScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 保存主流程连接语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveMainConnectScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptCorpus/saveMasterConnectCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveMainConnectScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveMainConnectScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveMainConnectScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 获取知识库基本问答语料列表
     *
     * @param groupId
     * @return
     */
    default List<ScriptCorpus> getKbQaScriptCorpusList(long groupId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptKnowledgeBase/findAllKnowledgeBaseQAByGroupId";
        Map<String, Object> paramMap = ImmutableMap.of("groupId", groupId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCorpus> scriptCorpusList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCorpusList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCorpus>>() {
//                    });
                    scriptCorpusList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCorpus>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getKbQaScriptCorpusList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getKbQaScriptCorpusList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getKbQaScriptCorpusList error: code " + response.getCode());
            return null;
        }

        return scriptCorpusList;
    }


    /**
     * 保存知识库基本问答
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveKbQaScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptKnowledgeBase/saveKnowledgeBaseQA";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveKbQaScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveKbQaScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveKbQaScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }



    /**
     * 获取知识库深层沟通摘要列表
     *
     * @param groupId
     * @return
     */
    default List<ScriptCanvas> getKbScriptCanvasList(Long groupId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptKnowledgeCanvas/findAllScriptCanvasByGroupId";
        Map<String, Object> paramMap = ImmutableMap.of("groupId", groupId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCanvas> scriptCanvasList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCanvasList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCanvas>>() {
//                    });
                    scriptCanvasList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCanvas>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getKbScriptCanvasList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getKbScriptCanvasList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getKbScriptCanvasList error: code " + response.getCode());
            return null;
        }

        return scriptCanvasList;
    }

    /**
     * 获取主流程画布摘要列表
     *
     * @param canvasId
     * @return
     */
    default ScriptCanvas getKbMainScriptCanvas(Long canvasId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptKnowledgeCanvas/findOneScriptCanvasById";
        Map<String, Object> paramMap = ImmutableMap.of("canvasId", canvasId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        ScriptCanvas scriptCanvas = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCanvas = JsonUtils.fromJson(json, new TypeToken<ScriptCanvas>() {
//                    });
                    scriptCanvas = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCanvas>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getKbMainScriptCanvas error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getKbMainScriptCanvas error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getKbMainScriptCanvas error: code " + response.getCode());
            return null;
        }

        return scriptCanvas;
    }


    /**
     * 保存基本问答深层沟通普通语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveKbOrdinaryScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptKnowledgeBase/saveKnowledgeOrdinaryCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveKbOrdinaryScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveKbOrdinaryScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveKbOrdinaryScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 保存基本问答深层沟通连接语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveKbConnectScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptKnowledgeBase/saveKnowledgeConnectCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveKbConnectScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveKbConnectScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveKbConnectScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 获取知识库基本问答语料列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getFuncPriorQaScriptCorpusList(long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptFunc/findAllFuncPriorQAByScriptId";
        Map<String, Object> paramMap = ImmutableMap.of("scriptId", scriptId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCorpus> scriptCorpusList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCorpusList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCorpus>>() {
//                    });
                    scriptCorpusList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCorpus>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncPriorQaScriptCorpusList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncPriorQaScriptCorpusList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncPriorQaScriptCorpusList error: code " + response.getCode());
            return null;
        }

        return scriptCorpusList;
    }


    /**
     * 保存最高优先语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveFuncPriorQaScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptFunc/saveFuncPriorQACorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveFuncPriorQaScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveFuncPriorQaScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveFuncPriorQaScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 获取重复语料列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getFuncRepeatScriptCorpusList(long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptFunc/findAllFuncRepeatByScriptId";
        Map<String, Object> paramMap = ImmutableMap.of("scriptId", scriptId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCorpus> scriptCorpusList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCorpusList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCorpus>>() {
//                    });
                    scriptCorpusList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCorpus>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncRepeatScriptCorpusList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncRepeatScriptCorpusList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncRepeatScriptCorpusList error: code " + response.getCode());
            return null;
        }

        return scriptCorpusList;
    }


    /**
     * 保存重复语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveFuncRepeatScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptFunc/saveFuncRepeatCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveFuncRepeatScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveFuncRepeatScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveFuncRepeatScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 获取沉默语料列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getFuncSilenceScriptCorpusList(long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptFunc/findAllFuncSilenceByScriptId";
        Map<String, Object> paramMap = ImmutableMap.of("scriptId", scriptId);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCorpus> scriptCorpusList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCorpusList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCorpus>>() {
//                    });
                    scriptCorpusList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCorpus>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncSilenceScriptCorpusList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncSilenceScriptCorpusList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getFuncSilenceScriptCorpusList error: code " + response.getCode());
            return null;
        }

        return scriptCorpusList;
    }


    /**
     * 保存沉默语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> saveFuncSilenceScriptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptFunc/saveFuncSilenceCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient saveFuncSilenceScriptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient saveFuncSilenceScriptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient saveFuncSilenceScriptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }

    /**
     * 获取打断垫句列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getPreInterruptCorpusList(long scriptId) {
        return getPreCorpusList(scriptId, CorpusType.PRE_INTERRUPT);
    }

    /**
     * 获取续播垫句列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getPreContinueCorpusList(long scriptId) {
        return getPreCorpusList(scriptId, CorpusType.PRE_CONTINUE);
    }


    /**
     * 获取承接语料列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getPreUndertakeCorpusList(long scriptId) {
        return getPreCorpusList(scriptId, CorpusType.PRE_UNDERTAKE);
    }


    /**
     * 获取垫句列表
     *
     * @param scriptId
     * @return
     */
    default List<ScriptCorpus> getPreCorpusList(long scriptId, CorpusType corpusType) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptPreCorpus/findPreCorpusListByCorpusType";
        Map<String, Object> paramMap = ImmutableMap.of("scriptId", scriptId, "type", corpusType);
        HttpResponse response = BaizeUtils.doGet(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doGet(url, token, paramMap);
        }

        List<ScriptCorpus> scriptCorpusList = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
//                    scriptCorpusList = JsonUtils.fromJson(json, new TypeToken<List<ScriptCorpus>>() {
//                    });
                    scriptCorpusList = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<List<ScriptCorpus>>() {
                    });
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getPreCorpusList error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getPreCorpusList error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getPreCorpusList error: code " + response.getCode());
            return null;
        }

        return scriptCorpusList;
    }


    /**
     * 保存打断垫句语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> savePreInterruptCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptPreCorpus/savePreInterruptCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient savePreInterruptCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient savePreInterruptCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient savePreInterruptCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 保存续播垫句语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> savePreContinueCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptPreCorpus/savePreContinueCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient savePreContinueCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient savePreContinueCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient savePreContinueCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 保存承接语料
     *
     * @param scriptCorpus
     * @return
     */
    default HttpResult<ScriptCorpus> savePreUndertakeCorpus(ScriptCorpus scriptCorpus) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptPreCorpus/savePreUndertakeCorpus";
        HttpResponse response = BaizeUtils.doPost(url, token, scriptCorpus);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, scriptCorpus);
        }

        boolean success;
        String msg;
        ScriptCorpus object = null;
        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    success = true;
                    msg = "";
                    String json = baseObj.get("data").toString();
//                    object = JsonUtils.fromJson(json, new TypeToken<ScriptCorpus>() {
//                    });
                    object = JsonUtils.fromJsonAndFailOnUnknownProperties(json, new TypeReference<ScriptCorpus>() {
                    });
                } else {
                    success = false;
                    msg = account + " BaizeClient savePreUndertakeCorpus error: " + response.getText();
                    MsgUtils.sendQiweiWarning(msg);
                }
            } catch (Exception e) {
                success = false;
                msg = account + " BaizeClient savePreUndertakeCorpus error: " + e;
                MsgUtils.sendQiweiWarning(msg);
            }
        } else {
            success = false;
            msg = account + " BaizeClient savePreUndertakeCorpus error: code " + response.getCode();
            MsgUtils.sendQiweiWarning(msg);
        }
        return new HttpResult<>(success, msg, object);
    }


    /**
     * 保存承接语料
     *
     * @param priorGroup
     * @return
     */
    default boolean addOrUpdatePriorGroup(PriorGroup priorGroup) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptCorpus/addOrUpdatePriorGroup";
        HttpResponse response = BaizeUtils.doPost(url, token, priorGroup);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, priorGroup);
        }

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    return true;
                } else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient addOrUpdatePriorGroup error: " + response.getText());
                    return false;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient addOrUpdatePriorGroup error: " + e);
                return false;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient addOrUpdatePriorGroup error: code " + response.getCode());
            return false;
        }
    }


    /**
     * 获取话术的音频列表
     *
     * @param scriptId
     * @return
     */
    default ScriptAudioInfo getScriptAudioInfo(long scriptId) {
        String token = getToken();
        String account = getAccount();

        String url = BASE_URL + "/AiSpeech/scriptCorpus/audioList";

        Map<String, Object> paramMap = ImmutableMap.of("scriptId", scriptId);
        HttpResponse response = BaizeUtils.doPost(url, token, paramMap);
        if (response.getCode() == 403) {
            update();
            response = BaizeUtils.doPost(url, token, paramMap);
        }

        ScriptAudioInfo scriptAudioInfo = null;

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                if ("2000".equals(baseObj.get("code").getAsString())) {
                    String json = baseObj.get("data").toString();
                    scriptAudioInfo = JsonUtils.fromJson(json, new TypeToken<ScriptAudioInfo>() {
                    });
                }else {
                    MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptAudioInfo error: " + response.getText());
                    return null;
                }
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptAudioInfo error: " + e);
                return null;
            }
        } else {
            MsgUtils.sendQiweiWarning(account + " BaizeClient getScriptAudioInfo error: code " + response.getCode());
            return null;
        }

        return scriptAudioInfo;
    }
}
