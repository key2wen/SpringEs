package com.key.springes.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ElasticsearchQueryService implements InitializingBean {

    private static final Logger logger = Logger
            .getLogger(ElasticsearchService.class);

    @Autowired
    private Client client;

    private String esIndexName = "heros";

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private Client esClient;

    /**
     * 查询 id
     */
    public List<String> queryId(String type, String[] fields, String content,
                                String sortField, SortOrder order, int from, int size) {
        SearchRequestBuilder reqBuilder = client.prepareSearch(esIndexName)
                .setTypes(type).setSearchType(SearchType.DEFAULT)
                .setExplain(true);
        QueryStringQueryBuilder queryString = QueryBuilders.queryString("\""
                + content + "\"");
        for (String k : fields) {
            queryString.field(k);
        }
        queryString.minimumShouldMatch("10");
        reqBuilder.setQuery(QueryBuilders.boolQuery().should(queryString))
                .setExplain(true);
        if (StringUtils.isNotEmpty(sortField) && order != null) {
            reqBuilder.addSort(sortField, order);
        }
        if (from >= 0 && size > 0) {
            reqBuilder.setFrom(from).setSize(size);
        }
        SearchResponse resp = reqBuilder.execute().actionGet();
        SearchHit[] hits = resp.getHits().getHits();
        ArrayList<String> results = new ArrayList<String>();
        for (SearchHit hit : hits) {
            results.add(hit.getId());
        }
        return results;
    }

    /**
     * 查询得到结果为Map集合
     *
     * @param type      表
     * @param fields    字段索引
     * @param content   查询的值
     * @param sortField 排序的字段
     * @param order     排序的規則
     * @param from      分頁
     * @param size
     * @return
     * @author 高国藩
     * @date 2015年6月15日 下午8:46:13
     */
    public List<Map<String, Object>> queryForObject(String type,
                                                    String[] fields, String content, String sortField, SortOrder order,
                                                    int from, int size) {
        SearchRequestBuilder reqBuilder = client.prepareSearch(esIndexName)
                .setTypes(type).setSearchType(SearchType.DEFAULT)
                .setExplain(true);
        QueryStringQueryBuilder queryString = QueryBuilders.queryString("\""
                + content + "\"");
        for (String k : fields) {
            queryString.field(k);
        }
        queryString.minimumShouldMatch("10");
        reqBuilder.setQuery(QueryBuilders.boolQuery().should(queryString))
                .setExplain(true);
        if (StringUtils.isNotEmpty(sortField) && order != null) {
            reqBuilder.addSort(sortField, order);
        }
        if (from >= 0 && size > 0) {
            reqBuilder.setFrom(from).setSize(size);
        }

        SearchResponse resp = reqBuilder.execute().actionGet();
        SearchHit[] hits = resp.getHits().getHits();

        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : hits) {
            results.add(hit.getSource());
        }
        return results;
    }

    /**
     * QueryBuilders 所有查询入口
     */
    public List<Map<String, Object>> queryForObjectEq(String type,
                                                      String[] fields, String content, String sortField, SortOrder order,
                                                      int from, int size) {
        SearchRequestBuilder reqBuilder = client.prepareSearch(esIndexName)
                .setTypes(type).setSearchType(SearchType.DEFAULT)
                .setExplain(true);
        QueryStringQueryBuilder queryString = QueryBuilders.queryString("\""
                + content + "\"");
        for (String k : fields) {
            queryString.field(k);
        }
        queryString.minimumShouldMatch("10");
        reqBuilder.setQuery(QueryBuilders.boolQuery().must(queryString))
                .setExplain(true);
        if (StringUtils.isNotEmpty(sortField) && order != null) {
            reqBuilder.addSort(sortField, order);
        }
        if (from >= 0 && size > 0) {
            reqBuilder.setFrom(from).setSize(size);
        }

        SearchResponse resp = reqBuilder.execute().actionGet();
        SearchHit[] hits = resp.getHits().getHits();

        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : hits) {
            results.add(hit.getSource());
        }
        return results;
    }

    /**
     * 多个文字记不清是那些字,然后放进去查询
     *
     * @param type
     * @param field
     * @param countents
     * @param sortField
     * @param order
     * @param from
     * @param size
     * @return
     * @author 高国藩
     * @date 2015年6月16日 上午9:56:08
     */
    public List<Map<String, Object>> queryForObjectNotEq(String type,
                                                         String field, Collection<String> countents, String sortField,
                                                         SortOrder order, int from, int size) {

        SearchRequestBuilder reqBuilder = client.prepareSearch(esIndexName)
                .setTypes(type).setSearchType(SearchType.DEFAULT)
                .setExplain(true);
        List<String> contents = new ArrayList<String>();
        for (String content : countents) {
            contents.add("\"" + content + "\"");
        }
        TermsQueryBuilder inQuery = QueryBuilders.inQuery(field, contents);
        inQuery.minimumShouldMatch("10");
        reqBuilder.setQuery(QueryBuilders.boolQuery().mustNot(inQuery))
                .setExplain(true);
        if (StringUtils.isNotEmpty(sortField) && order != null) {
            reqBuilder.addSort(sortField, order);
        }
        if (from >= 0 && size > 0) {
            reqBuilder.setFrom(from).setSize(size);
        }

        SearchResponse resp = reqBuilder.execute().actionGet();
        SearchHit[] hits = resp.getHits().getHits();

        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : hits) {
            results.add(hit.getSource());
        }
        return results;
    }

    /**
     * Filters 查询方式
     * <p>
     * 1. 1)QueryBuilders.queryString 获得基本查询
     * 2)FilteredQueryBuilder query = QueryBuilders.filteredQuery(queryString,FilterBuilder)
     * 3)通过上面封装成为查询,将这个query插入到reqBuilder中;完成操作
     * <p>
     * 2.在   reqBuilder.setQuery(query);
     * <p>
     * 3.介绍在2)中的FilterBuilder各种构造方式-参数都可以传String类型即可
     * FilterBuilders.rangeFilter("taskState").lt(20) 小于 、 lte(20) 小于等于
     * FilterBuilders.rangeFilter("taskState").gt(20)) 大于  、 gte(20) 大于等于
     * FilterBuilders.rangeFilter("taskState").from(start).to(end)) 范围,也可以指定日期,用字符串就ok了
     *
     * @param type
     * @param field //     * @param countents
     *              //     * @param sortField
     *              //     * @param order
     *              //     * @param from
     *              //     * @param size
     * @return
     * @author 高国藩
     * @date 2015年6月15日 下午10:06:05
     */
    public List<Map<String, Object>> queryForObjectForElasticSerch(String type,
                                                                   String field, String content, int start, int end) {

        SearchRequestBuilder reqBuilder = client.prepareSearch(esIndexName)
                .setTypes(type).setSearchType(SearchType.DEFAULT)
                .setExplain(true);
        QueryStringQueryBuilder queryString = QueryBuilders.queryString("\""
                + content + "\"");
        queryString.field(field);
        queryString.minimumShouldMatch("10");

        reqBuilder.setQuery(QueryBuilders.filteredQuery(queryString, FilterBuilders.rangeFilter("taskState").from(start).to(end)))
                .setExplain(true);

        SearchResponse resp = reqBuilder.execute().actionGet();
        SearchHit[] hits = resp.getHits().getHits();

        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : hits) {
            results.add(hit.getSource());
        }
        return results;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("init...");

    }

}
