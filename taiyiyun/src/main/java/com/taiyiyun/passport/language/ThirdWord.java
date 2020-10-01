package com.taiyiyun.passport.language;

/**
 * Created by okdos on 2017/9/6.
 */
public class ThirdWord {

    private String expression;
    private MatchType matchType;
    private String langKey;


    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public static enum MatchType {
        CONTAIN,
        REGULAR
    }
}
