package com.centit.framework.core.dao;

import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableReference;
import com.centit.support.database.utils.DBType;
import com.centit.support.xml.IgnoreDTDEntityResolver;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by codefan on 17-8-27.
 */
@SuppressWarnings("unused")
public abstract class ExtendedQueryPool {
    /**
     * 通过XML文件加载
     */
    private static final Map<String,String> EXTENDED_SQL_MAP=new HashMap<>();

    public static final void loadExtendedSqlMap(InputStream extendedSqlXmlFile, DBType dbtype)
            throws DocumentException,IOException {

        SAXReader builder = new SAXReader(false);
        builder.setValidation(false);
        builder.setEntityResolver(new IgnoreDTDEntityResolver());
        Document doc = builder.read(extendedSqlXmlFile);
        Element root = doc.getRootElement();//获取根元素
        for(Object element : root.elements()){
            String strDbType = ((Element)element).attributeValue("dbtype");
            if(StringUtils.isBlank(strDbType) || dbtype == DBType.valueOf(strDbType) ) {
                EXTENDED_SQL_MAP.put(
                        ((Element) element).attributeValue("id"),
                        ((Element) element).getStringValue());
            }
        }
    }

    public static final void loadExtendedSqlMap(String extendedSqlXmlFile, DBType dbtype)
            throws DocumentException,IOException {
        loadExtendedSqlMap(ExtendedQueryPool.class.getResourceAsStream(extendedSqlXmlFile), dbtype);
    }

    public static final String getExtendedSql(String extendedSqlId){
        return EXTENDED_SQL_MAP.get(extendedSqlId);
    }

}
