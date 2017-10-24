package com.centit.framework.jdbc.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.framework.core.dao.CodeBook;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.utils.PageDesc;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.orm.OrmDaoUtils;
import com.centit.support.database.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 意图将BaseDao中公共的部分独立出来，减少类的函数数量，
 * 因为每一个继承BaseDaoImpl的类都有这些函数，而这些行数基本上都是一样的
 */
@SuppressWarnings("unused")
public abstract class DatabaseOptUtils {

    protected static Logger logger = LoggerFactory.getLogger(DatabaseOptUtils.class);

    public static Object callFunction(BaseDaoImpl<?, ?> baseDao , String procName,
                                            int sqlType, Object... paramObjs){
        try {
            return baseDao.getJdbcTemplate().execute(
                    (ConnectionCallback<Object>) conn ->
                            DatabaseAccess.callFunction(conn, procName, sqlType, paramObjs));
        } catch (DataAccessException e){
            throw new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION, e);
        }
    }

    public final static boolean callProcedure(BaseDaoImpl<?, ?> baseDao , String procName, Object... paramObjs){
        try {
            return baseDao.getJdbcTemplate().execute(
                    (ConnectionCallback<Boolean>) conn ->
                            DatabaseAccess.callProcedure(conn, procName, paramObjs));
        } catch (DataAccessException e){
            throw new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION, e);
        }
    }

    public final static boolean doExecuteSql(BaseDaoImpl<?, ?> baseDao , String sSql) throws SQLException {
        baseDao.getJdbcTemplate().execute(sSql);
        return true;
        /*try {
            return baseDao.getJdbcTemplate().execute(
                    (ConnectionCallback<Boolean>) conn ->
                            DatabaseAccess.doExecuteSql(conn,sSql));
        } catch (DataAccessException e){
            throw new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION, e);
        }*/
    }

    /*
     * 直接运行行带参数的 SQL,update delete insert
     */
    public final static int doExecuteSql(BaseDaoImpl<?, ?> baseDao , String sSql, Object[] values) throws SQLException {

        return baseDao.getJdbcTemplate().update(sSql,values );
        /*try {
            return baseDao.getJdbcTemplate().execute(
                    (ConnectionCallback<Integer>) conn ->
                            DatabaseAccess.doExecuteSql(conn, sSql, values));
        } catch (DataAccessException e){
            throw new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION, e);
        }*/
    }

    /*
     * 执行一个带命名参数的sql语句
     */
    public final static int doExecuteNamedSql(BaseDaoImpl<?, ?> baseDao , String sSql, Map<String, Object> values)
            throws SQLException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return doExecuteSql(baseDao, qap.getQuery(), qap.getParams());
    }

    /**
     * 在sql语句中找到属性对应的字段语句
     * @param querySql sql语句
     * @param fieldName 属性
     * @return 返回的对应这个属性的语句，如果找不到返回 null
     */
    public static String mapFieldToColumnPiece(String querySql, String fieldName){
        List<Pair<String,String>> fields = QueryUtils.getSqlFieldNamePieceMap(querySql);
        for(Pair<String,String> field : fields ){
            if(fieldName.equalsIgnoreCase(field.getLeft()) ||
                    fieldName.equals(DatabaseAccess.mapColumnNameToField(field.getKey())) ||
                    fieldName.equalsIgnoreCase(field.getRight())){
                return  field.getRight();
            }
        }
        return null;
    }

    /* 下面所有的查询都返回 jsonArray */

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                            String querySql, String[] fieldNames , String queryCountSql,
                                            Map<String, Object> namedParams, PageDesc pageDesc /*,
                                      Map<String,KeyValuePair<String,String>> dictionaryMapInfo*/ ) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        pageDesc.setTotalRows(NumberBaseOpt.castObjectToInteger(
                                DatabaseAccess.getScalarObjectQuery(
                                        conn, queryCountSql, namedParams)));
                        return DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, querySql,
                                namedParams, fieldNames, pageDesc.getPageNo(), pageDesc.getPageSize());
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                            String querySql,  String[] fieldNames ,
                                            Map<String, Object> namedParams,  PageDesc pageDesc) {

        return listObjectsBySqlAsJson(baseDao, querySql, fieldNames ,
                QueryUtils.buildGetCountSQLByReplaceFields( querySql ), namedParams,   pageDesc  );
    }

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                                   String querySql,  String[] fieldNames ,
                                                   Map<String, Object> namedParams) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        return DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, querySql,
                                namedParams, fieldNames);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }


    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                            String querySql,  String queryCountSql,
                                            Map<String, Object> namedParams,  PageDesc pageDesc ) {
        return listObjectsBySqlAsJson(baseDao, querySql, null ,  queryCountSql, namedParams,   pageDesc  );
    }


    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,  Map<String,Object> params ) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        return DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, querySql, params);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }




    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                            Map<String, Object> namedParams,  PageDesc pageDesc  ) {
        if(pageDesc!=null && pageDesc.getPageSize()>0) {
            return DatabaseOptUtils.listObjectsBySqlAsJson(baseDao, querySql, null ,
                    QueryUtils.buildGetCountSQLByReplaceFields( querySql ), namedParams,   pageDesc  );
        }else{
            return DatabaseOptUtils.listObjectsBySqlAsJson(baseDao, querySql, namedParams);
        }
    }

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql, String[] fieldNames,
                                                   String queryCountSql, Object[] params,  PageDesc pageDesc ) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        pageDesc.setTotalRows(NumberBaseOpt.castObjectToInteger(
                                DatabaseAccess.getScalarObjectQuery(
                                        conn, queryCountSql, params)));
                        return DatabaseAccess.findObjectsAsJSON(conn, querySql ,
                                params, fieldNames, pageDesc.getPageNo(), pageDesc.getPageSize());
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                                   String querySql,  String[] fieldNames ,
                                                   Object[] params) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        return DatabaseAccess.findObjectsAsJSON(conn, querySql,
                                params, fieldNames);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }


    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,  String queryCountSql,
                                            Object[] params,  PageDesc pageDesc ) {

        return listObjectsBySqlAsJson(baseDao,  querySql, null,  queryCountSql, params,   pageDesc );
    }

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,  Object[] params, String[] fieldnames) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        return DatabaseAccess.findObjectsAsJSON(conn, querySql, params, fieldnames);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,  Object[] params ) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONArray>) conn -> {
                    try {
                        return DatabaseAccess.findObjectsAsJSON(conn, querySql, params);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }
 


    public static JSONArray listObjectsBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql, Object[] params,  PageDesc pageDesc  ) {
        if(pageDesc!=null && pageDesc.getPageSize()>0) {
            return DatabaseOptUtils.listObjectsBySqlAsJson(baseDao, querySql,
                    QueryUtils.buildGetCountSQLByReplaceFields( querySql ), params,   pageDesc  );
        }else{
            return DatabaseOptUtils.listObjectsBySqlAsJson(baseDao, querySql, params);
        }
    }


    public static JSONArray listObjectsByParamsDriverSqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                                        String querySql, String[] fieldNames , String queryCountSql,
                                                        Map<String, Object> namedParams, PageDesc pageDesc  ) {
        QueryAndNamedParams qap = QueryUtils.translateQuery( querySql, namedParams);
        Map<String, Object> paramsMap = qap.getParams();
        QueryAndNamedParams countQap = QueryUtils.translateQuery( queryCountSql, namedParams);
        paramsMap.putAll(countQap.getParams());

        return listObjectsBySqlAsJson(baseDao, qap.getQuery(), fieldNames , countQap.getQuery(),
                paramsMap, pageDesc);

    }


    public static JSONArray listObjectsByParamsDriverSqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                                   String querySql,  String[] fieldNames ,
                                                   Map<String, Object> namedParams,  PageDesc pageDesc) {
        QueryAndNamedParams qap = QueryUtils.translateQuery( querySql, namedParams);
        
        return listObjectsBySqlAsJson(baseDao,  qap.getQuery(), fieldNames ,
                QueryUtils.buildGetCountSQLByReplaceFields( qap.getQuery() ), qap.getParams(),   pageDesc  );
    }

    public static JSONArray listObjectsByParamsDriverSqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                                   String querySql,  String[] fieldNames ,
                                                   Map<String, Object> namedParams) {
        
        QueryAndNamedParams qap = QueryUtils.translateQuery( querySql, namedParams);

        return listObjectsBySqlAsJson( baseDao,
                qap.getQuery(),  fieldNames, qap.getParams());
    }


    public static JSONArray listObjectsByParamsDriverSqlAsJson(BaseDaoImpl<?, ?> baseDao,
                                                   String querySql,  String queryCountSql,
                                                   Map<String, Object> namedParams,  PageDesc pageDesc ) {
        
        return listObjectsByParamsDriverSqlAsJson(baseDao, querySql, 
                null ,  queryCountSql, namedParams,   pageDesc  );
    }


    public static JSONArray listObjectsByParamsDriverSqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                                               Map<String,Object> namedParams ) {

        QueryAndNamedParams qap = QueryUtils.translateQuery( querySql, namedParams);

        return listObjectsBySqlAsJson( baseDao, qap.getQuery(), qap.getParams());
    }


    public static JSONArray listObjectsByParamsDriverSqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                            Map<String, Object> namedParams,  PageDesc pageDesc  ) {
        QueryAndNamedParams qap = QueryUtils.translateQuery( querySql, namedParams);

        return listObjectsBySqlAsJson(baseDao, qap.getQuery(),
                                        qap.getParams(), pageDesc);
    }


    public static JSONObject getObjectBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                                  Object[] params, String [] fieldName) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONObject>) conn -> {
                    try {
                        return DatabaseAccess.getObjectAsJSON(conn, querySql, params,fieldName);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static JSONObject getObjectBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                                  Object[] params) {
        return getObjectBySqlAsJson( baseDao,  querySql, params, null);
    }

    public static JSONObject getObjectBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                                  Map<String, Object> params, String [] fieldName) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONObject>) conn -> {
                    try {
                        return DatabaseAccess.getObjectAsJSON(conn, querySql, params, fieldName);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static JSONObject getObjectBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql,
                                                  Map<String, Object>  params) {
        return getObjectBySqlAsJson( baseDao,  querySql, params, null);
    }

    public static JSONObject getObjectBySqlAsJson(BaseDaoImpl<?, ?> baseDao, String querySql) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<JSONObject>) conn -> {
                    try {
                        return DatabaseAccess.getObjectAsJSON(conn, querySql);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static Object getScalarObjectQuery(BaseDaoImpl<?, ?> baseDao, String sSql,
                                                    Map<String,Object> values){
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Object>) conn -> {
                    try {
                        return DatabaseAccess.getScalarObjectQuery(conn, sSql,values);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }
    /*
     * * 执行一个标量查询
     */
    public static Object getScalarObjectQuery(BaseDaoImpl<?, ?> baseDao,
                                                    String sSql, Object[] values) {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Object>) conn -> {
                    try {
                        return DatabaseAccess.getScalarObjectQuery(conn, sSql,values);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    /*
     * * 执行一个标量查询
     */
    public static Object getScalarObjectQuery(BaseDaoImpl<?, ?> baseDao, String sSql)
            throws SQLException, IOException {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Object>) conn -> {
                    try {
                        return DatabaseAccess.getScalarObjectQuery(conn, sSql);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    /*
     * * 执行一个标量查询
     */
    public static Object getScalarObjectQuery(BaseDaoImpl<?, ?> baseDao, String sSql,Object value)
            throws SQLException, IOException {
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Object>) conn -> {
                    try {
                        return DatabaseAccess.getScalarObjectQuery(conn, sSql, value);
                    } catch (SQLException | IOException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    public static Long getSequenceNextValue(BaseDaoImpl<?, ?> baseDao, String sequenceName){
        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Long>) conn ->
                        OrmDaoUtils.getSequenceNextValue(conn, sequenceName));
    }


    /**
     * 保存任意对象
     * @param baseDao BaseDaoImpl
     * @param objects Collection objects
     * @return 保存任意对象数量
     */
    public static int batchSaveNewObjects(BaseDaoImpl<?, ?> baseDao,
                                             Collection<? extends Object> objects) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Integer>) conn -> {
                    int successSaved=0;
                    for(Object o : objects) {
                        successSaved += OrmDaoUtils.saveNewObject(conn, o);
                    }
                    return successSaved;
                });
    }

    /**
     * 更新任意对象
     * @param baseDao BaseDaoImpl
     * @param objects Collection objects
     * @return 更新对象数量
     */
    public static int batchUpdateObjects(BaseDaoImpl<?, ?> baseDao,
                                                Collection<? extends Object> objects) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Integer>) conn -> {
                    int successUpdated=0;
                    for(Object o : objects) {
                        successUpdated += OrmDaoUtils.updateObject(conn, o);
                    }
                    return successUpdated;
                });
    }

    /**
     * 保存或者更新任意对象 ，每次都先判断是否存在
     * @param baseDao BaseDaoImpl
     * @param objects Collection objects
     * @return merge对象数量
     */
    public static int batchMergeObjects(BaseDaoImpl<?, ?> baseDao,
                                               Collection<? extends Object> objects) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Integer>) conn -> {
                    int successMerged=0;
                    for(Object o : objects) {
                        successMerged += OrmDaoUtils.mergeObject(conn, o);
                    }
                    return successMerged;
                });
    }

    /**
     * 批量删除对象
     * @param baseDao BaseDaoImpl
     * @param objects Collection objects
     * @return 批量删除对象数量
     */
    public static int batchDeleteObjects(BaseDaoImpl<?, ?> baseDao,
                                              Collection<? extends Object> objects) {

        return baseDao.getJdbcTemplate().execute(
                (ConnectionCallback<Integer>) conn -> {
                    int successDeleted=0;
                    for(Object o : objects) {
                        successDeleted += OrmDaoUtils.deleteObject(conn, o);
                    }
                    return successDeleted;
                });
    }
}
