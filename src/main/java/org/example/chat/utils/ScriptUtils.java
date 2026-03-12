package org.example.chat.utils;

import org.example.chat.bean.baize.script.ScriptCorpus;
import org.example.chat.bean.baize.script.ScriptMultiContent;
import org.example.chat.bean.baize.script.ScriptUnitContent;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.List;

public class ScriptUtils {

    public static String getContent(ScriptCorpus scriptCorpus) {
        String content = scriptCorpus.getContent();
        if (StringUtils.isEmpty(content)) {
            StringBuilder sb = new StringBuilder();
            List<ScriptMultiContent> multiContentList = scriptCorpus.getScriptMultiContents();
            if (!CollectionUtils.isEmpty(multiContentList)) {
                for (ScriptMultiContent multiContent : multiContentList) {
                    List<ScriptUnitContent> unitContentList = multiContent.getScriptUnitContents();
                    if (!CollectionUtils.isEmpty(unitContentList)) {
                        for (ScriptUnitContent unitContent : unitContentList) {
                            String contentText = unitContent.getContent();
                            if (!StringUtils.isEmpty(contentText)) {
                                sb.append(contentText);
                            }
                        }
                    }
                }
            }
            content = sb.toString();
        }
        return content;
    }
}
