package com.centit.framework.core.dao;

/**
 * @Deprecated  这个类已经迁移到 com.centit.support.database.utils.PageDesc
 * @See com.centit.support.database.utils.PageDesc
 */
@Deprecated
public class PageDesc extends com.centit.support.database.utils.PageDesc {
    public PageDesc() {
        super();
    }

    public PageDesc(int pn, int ps) {
        super(pn,ps);
    }

    public PageDesc(int pn, int ps, int tr) {
        super(pn,ps,tr);
    }
}
