package org.example.ruledetect.bean;

import java.util.regex.Pattern;

public enum RegexMode {

    DEFAULT(0),
    UNIX_LINES(Pattern.UNIX_LINES),
    CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),
    COMMENTS(Pattern.COMMENTS),
    MULTILINE(Pattern.MULTILINE),
    LITERAL(Pattern.LITERAL),
    DOTALL(Pattern.DOTALL),
    UNICODE_CASE(Pattern.UNICODE_CASE),
    CANON_EQ(Pattern.CANON_EQ),
    UNICODE_CHARACTER_CLASS(Pattern.UNICODE_CHARACTER_CLASS),
    ;

    private int flag;

    RegexMode(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
