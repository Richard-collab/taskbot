package org.example.ruledetect.bean;

import org.example.preporcess.Preprocessor;
import org.example.utils.JsonUtils;

import java.io.Serializable;

public class QueryInfo implements Serializable {

    private String domain;
    private String rawQuery; //原始query
//    private String normalizedQuery; //规范化后的query
//    private String normalizedQueryWithoutPunc; //规范化后的去除标点的query
//    private List<String> charTokenList; //按字分隔后的query
//    private List<String> wordTokenList; //分词后的query
//    private List<String> bertTokenList; //按bert的tokenizer分隔后的query
    private String maskedQuery; //mask实体后的query
//    private String maskedQueryWithoutPunc; //mask实体后的去除标点的query
//
//    private List<Slot> slotList;

    public QueryInfo(String domain, String rawQuery) {
        this.domain = domain;
        this.rawQuery = rawQuery;
        this.maskedQuery = Preprocessor.getMaskedQuery(rawQuery);
    }

    public QueryInfo(String domain, String rawQuery, String maskedQuery) {
        this.domain = domain;
        this.rawQuery = rawQuery;
        this.maskedQuery = maskedQuery;
    }

    public static QueryInfo createQueryInfo(String domain, String rawQuery) {
        return new QueryInfo(domain, rawQuery);
    }

    public static QueryInfo createQueryInfo(String domain, String rawQuery, String maskedQuery) {
        return new QueryInfo(domain, rawQuery, maskedQuery);
    }

    public String getQuery(QueryType queryType) {
        String query;
        switch (queryType) {
            case RAW_QUERY:
                query = this.getRawQuery();
                break;
//            case NORMALIZED_QUERY:
//                query = this.getNormalizedQuery();
//                break;
//            case NORMALIZED_QUERY_WITHOUT_PUNC:
//                query = this.getNormalizedQueryWithoutPunc();
//                break;
            case MASKED_QUERY:
                query = this.getMaskedQuery();
                break;
//            case MASKED_QUERY_WITHOUT_PUNC:
//                query = this.getMaskedQueryWithoutPunc();
//                break;
            default:
                query = this.getRawQuery();
                break;
        }
        return query;
    }

    public String getDomain() {
        return domain;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public String getMaskedQuery() {
        return maskedQuery;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
