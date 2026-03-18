package com.molandev.framework.spring.tree;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * 节点配置
 */
public class NodeConfig {

    /**
     * id
     */
    public static final String ID = "id";

    /**
     * 父id
     */
    public static final String PARENT_ID = "parentId";

    /**
     * 排序
     */
    public static final String SORT = "sortSeq";

    /**
     * 孩子们
     */
    public static final String CHILDREN = "children";

    /**
     * id的方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method idMethod = null;

    /**
     * id的setter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method idSetterMethod = null;

    /**
     * 父id的方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method parentIdMethod = null;

    /**
     * 父id的setter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method parentIdSetterMethod = null;

    /**
     * 排序方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method sortMethod = null;
    /**
     * 排序setter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method sortSetterMethod = null;

    /**
     * 孩子getter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method childrenGetterMethod = null;

    /**
     * 孩子setter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method childrenSetterMethod = null;

    /**
     * 级别getter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method levelGetterMethod = null;

    /**
     * 级别setter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method levelSetterMethod = null;

    /**
     * 级别码getter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method levelCodeGetterMethod = null;

    /**
     * 级别码setter方法
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    Method levelCodeSetterMethod = null;

    /**
     * id
     */
    @Getter
    @Setter
    private String idFieldName = ID;

    /**
     * 父id
     */
    @Getter
    @Setter
    private String parentIdFieldName = PARENT_ID;

    /**
     * 排序
     */
    @Getter
    @Setter
    private String sortFieldName = SORT;

    /**
     * 孩子们
     */
    @Getter
    @Setter
    private String childrenFieldName = CHILDREN;

}
