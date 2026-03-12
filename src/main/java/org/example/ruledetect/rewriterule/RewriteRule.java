package org.example.ruledetect.rewriterule;

import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewriteRule {

    private final String strRule;
    private final String replacedText;
    private final Pattern pattern;

    public RewriteRule(String strRule) {
        this.strRule = strRule;
        String[] item = strRule.split("=>");
        Preconditions.checkArgument(item.length==2);
        this.pattern = Pattern.compile(item[0].trim().toLowerCase(), Pattern.DOTALL);
        this.replacedText = item[1];
    }

    public String rewrite(String query) {
        String resultText = query;
        Matcher matcher = pattern.matcher(query);
        while(matcher.find()) {
            resultText = replacedText;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                int start = matcher.start(i);
                int end = matcher.end(i);
                String rawValue = query.substring(start, end);
                resultText = resultText.replace("$" + i, rawValue);
            }
            break;
        }
        return resultText;
    }
}
