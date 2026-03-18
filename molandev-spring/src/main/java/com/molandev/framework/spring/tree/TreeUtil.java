package com.molandev.framework.spring.tree;

import com.molandev.framework.util.ClassUtils;
import com.molandev.framework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 树工具
 */
@SuppressWarnings({"unchecked", "rawtypes", "null"})
public class TreeUtil {

    /**
     * 根节点id
     */
    public static final String ROOT_ID = "0";

    /**
     * get方法前缀
     */
    public static final String GET = "get";

    /**
     * set方法前缀
     */
    public static final String SET = "set";


    /**
     * 配置缓存
     */
    private static final Map<Class<?>, NodeConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 构建树map 构建Map形式的tree
     *
     * @param list 列表
     * @return {@link List<Map>}
     */
    public static <T> List<Map<String, Object>> buildMapTree(List<T> list) {
        return buildMapTree(list, new NodeConfig());
    }

    /**
     * 构建树map 构建Map形式的tree
     *
     * @param list       列表
     * @param nodeConfig 节点配置
     * @return {@link List<Map>}
     */

    public static <T> List<Map<String, Object>> buildMapTree(List<T> list, NodeConfig nodeConfig) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map> rlist = new ArrayList<>();
        boolean hasSortProperty = false;

        // 构建id与节点map，方面查询父节点
        Map<Object, Map<String, Object>> idMap = new HashMap<>();
        for (T t : list) {
            BeanMap m = BeanMap.create(t);
            Map<String, Object> r = new HashMap<String, Object>(m);
            rlist.add(r);
            Object id = r.get(nodeConfig.getIdFieldName());
            if (id == null) {
                throw new TreeException("树节点id不能为空");
            }
            idMap.put(id, r);
            if (!hasSortProperty) {
                Object sortValue = r.get(nodeConfig.getSortFieldName());
                if (sortValue != null) {
                    hasSortProperty = true;
                    if (!Comparable.class.isAssignableFrom(sortValue.getClass())) {
                        throw new TreeException("树节点排序字段应是可排序类型");
                    }
                }
            }
        }

        List<Map<String, Object>> treeList = new ArrayList<>();
        for (Map<String, Object> r : rlist) {
            Object pid = r.get(nodeConfig.getParentIdFieldName());
            Map<String, Object> parent = idMap.get(pid);
            if (parent == null) {
                // 没有父节点，说明是根；
                treeList.add(r);
            } else {
                // 有父节点，挂到父节点的children上
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get(nodeConfig.getChildrenFieldName());
                if (children == null) {
                    children = new ArrayList<>();
                    parent.put(nodeConfig.getChildrenFieldName(), children);
                }
                children.add(r);
            }
        }
        if (hasSortProperty) {
            Comparator<Map<String, Object>> comparator = (o1, o2) -> {
                Object x1 = o1.get(nodeConfig.getSortFieldName());
                Object x2 = o2.get(nodeConfig.getSortFieldName());
                return compareValue(x1, x2);
            };
            // 根节点排序
            treeList.sort(comparator);
            // 子集节点排序
            for (Map<String, Object> value : idMap.values()) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) value.get(nodeConfig.getChildrenFieldName());
                if (children != null && !children.isEmpty()) {
                    children.sort(comparator);
                }
            }
        }
        return treeList;
    }

    /**
     * 构建树,保持员对象
     *
     * @param list 列表
     * @return {@link List<T>}
     */
    public static <T> List<T> buildTree(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Function<T, T> objectCopyMethod = t -> t;
        return (List<T>) buildTree(list, (Class<T>) list.get(0).getClass(), objectCopyMethod);
    }

    /**
     * 构建树
     *
     * @param list  列表
     * @param rType r型
     * @return {@link List<R>}
     */
    public static <T, R> List<R> buildTree(List<T> list, Class<R> rType) {
        return buildTree(list, rType, t -> {
            R r = ClassUtils.newInstance(rType);
            BeanUtils.copyProperties(t, r);
            return r;
        });
    }

    /**
     * 构建树
     *
     * @param list  列表
     * @param rType r型
     * @return {@link List<R>}
     */
    public static <T, R> List<R> buildTree(List<T> list, Class<R> rType, Function<T, R> objectCopyMethod) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        NodeConfig nodeConfig = getNodeConfig(rType);

        List<R> rlist = new ArrayList<>();

        // 构建id与节点map，方面查询父节点
        Map<Object, R> idMap = new HashMap<>();
        for (T t : list) {
            R r;
            // 类型一致，使用原对象即可
            if (rType == t.getClass()) {
                r = (R) t;
            } else {
                r = objectCopyMethod.apply(t);
            }
            rlist.add(r);
            Object id = ReflectionUtils.invokeMethod(nodeConfig.getIdMethod(), r);
            idMap.put(id, r);
        }

        List<R> treeList = new ArrayList<>();
        for (R r : rlist) {
            Object pid = ReflectionUtils.invokeMethod(nodeConfig.getParentIdMethod(), r);
            R parent = idMap.get(pid);
            if (parent == null) {
                // 没有父节点，说明是根；
                treeList.add(r);
            } else {
                // 有父节点，挂到父节点的children上
                List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), parent);
                if (children == null) {
                    children = new ArrayList<>();
                    ReflectionUtils.invokeMethod(nodeConfig.getChildrenSetterMethod(), parent, children);
                }
                children.add(r);
            }
        }
        if (nodeConfig.getSortMethod() != null) {
            Class<?> returnType = nodeConfig.getSortMethod().getReturnType();
            if (!Comparable.class.isAssignableFrom(returnType)) {
                throw new TreeException("树节点排序字段有误");
            }

            Comparator<R> comparator = (o1, o2) -> {
                Object x1 = ReflectionUtils.invokeMethod(nodeConfig.getSortMethod(), o1);
                Object x2 = ReflectionUtils.invokeMethod(nodeConfig.getSortMethod(), o2);
                return compareValue(x1, x2);
            };
            treeList.sort(comparator);
            // 子集节点排序
            for (R r : idMap.values()) {
                List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), r);
                if (children != null && !children.isEmpty()) {
                    children.sort(comparator);
                }
            }
        }
        return treeList;
    }

    /**
     * 展开树
     *
     * @param treeList 树列表
     */
    public static <R> List<R> expandTree(List<R> treeList) {
        if (treeList != null && !treeList.isEmpty()) {
            List<R> list = new ArrayList<>();
            NodeConfig nodeConfig = getNodeConfig(treeList.get(0).getClass());
            for (R t : treeList) {
                list.add(t);
                List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), t);
                if (children != null && !children.isEmpty()) {
                    list.addAll(expandTree(children));
                }
            }
            return list;
        }
        return treeList;
    }

    /**
     * 过滤树，根据给定的id集合
     * 此处的List不能是不可变的
     *
     * @param treeList 树列表
     */
    public static <R> void filterTree(List<R> treeList, Predicate<R> filterPredicate) {
        if (treeList != null && !treeList.isEmpty()) {
            NodeConfig nodeConfig = getNodeConfig(treeList.get(0).getClass());
            Iterator<R> iterator = treeList.iterator();
            while (iterator.hasNext()) {
                R t = iterator.next();
                List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), t);
                if (children != null && !children.isEmpty()) {
                    filterTree(children, filterPredicate);
                    // 只要包含任意一个子节点，则当前节点保留,否则根据条件判断
                    if (children.isEmpty()) {
                        if (!filterPredicate.test(t)) {
                            iterator.remove();
                        }
                    }
                } else {
                    // 没有children，当前已经是末端节点
                    if (!filterPredicate.test(t)) {
                        iterator.remove();
                    }
                }

            }
        }
    }

    /**
     * 过滤树，根据给定的id集合
     *
     * @param treeList 树列表
     */
    public static <R> void filterTree(List<R> treeList, Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            treeList.clear();
        } else {
            if (treeList == null || treeList.isEmpty()) {
                return;
            }
            NodeConfig nodeConfig = getNodeConfig(treeList.get(0).getClass());
            filterTree(treeList, r ->
                    ids.contains(ReflectionUtils.invokeMethod(nodeConfig.getIdMethod(), r))
            );
        }

    }

    /**
     * 比较value
     *
     * @param x1 x1
     * @param x2 x2
     * @return int
     */
    private static int compareValue(Object x1, Object x2) {
        if (x1 == null && x2 == null) {
            return 0;
        }
        if (x1 == null) {
            return -1;
        }
        if (x2 == null) {
            return 1;
        }
        return ((Comparable) x1).compareTo(x2);
    }

    /**
     * 获取节点配置
     *
     * @param rType r型
     * @return {@link NodeConfig}
     */
    private static <R> NodeConfig getNodeConfig(Class<R> rType) {
        // 使用ConcurrentHashMap的computeIfAbsent方法替代synchronized块
        return configCache.computeIfAbsent(rType, TreeUtil::getNodeConfigFromClass);
    }

    /**
     * 获取节点配置类
     *
     * @param rType r型
     * @return {@link NodeConfig}
     */

    private static <R> NodeConfig getNodeConfigFromClass(Class<R> rType) {
        NodeConfig nodeConfig = new NodeConfig();
        ReflectionUtils.doWithFields(rType, field -> {
            if (field.getAnnotation(TreeId.class) != null) {
                nodeConfig.setIdFieldName(field.getName());
                return;
            }
            if (field.getAnnotation(TreeParentId.class) != null) {
                nodeConfig.setParentIdFieldName(field.getName());
                return;
            }
            if (field.getAnnotation(TreeSort.class) != null) {
                nodeConfig.setSortFieldName(field.getName());
                return;
            }
            if (field.getAnnotation(TreeChildren.class) != null) {
                nodeConfig.setChildrenFieldName(field.getName());
                return;
            }
        });
        nodeConfig.setIdMethod(
                ReflectionUtils.findMethod(rType, GET + StringUtils.upperFirst(nodeConfig.getIdFieldName())));
        nodeConfig.setParentIdMethod(
                ReflectionUtils.findMethod(rType, GET + StringUtils.upperFirst(nodeConfig.getParentIdFieldName())));
        nodeConfig.setSortMethod(
                ReflectionUtils.findMethod(rType, GET + StringUtils.upperFirst(nodeConfig.getSortFieldName())));
        Method childrenGetter = ReflectionUtils.findMethod(rType,
                GET + StringUtils.upperFirst(nodeConfig.getChildrenFieldName()));
        nodeConfig.setChildrenGetterMethod(childrenGetter);
        nodeConfig.setChildrenSetterMethod(ReflectionUtils.findMethod(rType,
                SET + StringUtils.upperFirst(nodeConfig.getChildrenFieldName()), childrenGetter.getReturnType()));

        if (nodeConfig.getIdMethod() == null) {
            throw new TreeException("树节点id字段标识未知");
        }
        if (nodeConfig.getParentIdMethod() == null) {
            throw new TreeException("树节点parentId字段标识未知");
        }
        if (nodeConfig.getChildrenGetterMethod() == null || nodeConfig.getChildrenSetterMethod() == null) {
            throw new TreeException("树节点children字段标识未知");
        }
        return nodeConfig;
    }

    public static <R, T> List<T> findChildIds(List<R> treeList, T pid) {
        return findChildIds(treeList, pid, false);
    }

    public static <R> R findChildren(List<R> treeList, Object pid) {
        // 找到当前节点以及下级节点
        List<R> children = findChildren(treeList, pid, false);
        if (!children.isEmpty()) {
            return children.get(0);
        }
        return null;
    }

    private static <R> List<R> findChildren(List<R> treeList, Object pid, boolean matched) {
        if (treeList != null && !treeList.isEmpty()) {
            List<R> rList = new ArrayList<>();
            NodeConfig nodeConfig = getNodeConfig(treeList.get(0).getClass());
            for (R r : treeList) {
                Object id = ReflectionUtils.invokeMethod(nodeConfig.getIdMethod(), r);
                if (matched || pid.equals(id)) {
                    rList.add(r);
                    List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), r);
                    if (children != null && !children.isEmpty()) {
                        rList.addAll(findChildren(children, pid, true));
                    }
                    // 第一次匹配到，不再向下匹配
                    if (!matched) {
                        break;
                    }
                }
                // 没有匹配到，递归子节点
                if (!matched) {
                    List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), r);
                    if (children != null && !children.isEmpty()) {
                        List<R> childIds = findChildren(children, pid, matched);
                        matched = !childIds.isEmpty();
                        if (matched) {
                            rList.addAll(childIds);
                            // 已经匹配到了，不再向下匹配
                            break;
                        }
                    }
                }
            }
            return rList;
        }
        return new ArrayList<>();
    }

    private static <R, T> List<T> findChildIds(List<R> treeList, T pid, boolean matched) {
        if (treeList != null && !treeList.isEmpty()) {
            List<T> rList = new ArrayList<>();
            NodeConfig nodeConfig = getNodeConfig(treeList.get(0).getClass());
            for (R r : treeList) {
                T id = (T) ReflectionUtils.invokeMethod(nodeConfig.getIdMethod(), r);
                if (matched || pid.equals(id)) {
                    rList.add(id);
                    List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), r);
                    if (children != null && !children.isEmpty()) {
                        rList.addAll(findChildIds(children, pid, true));
                    }
                    if (!matched) {
                        break;
                    }
                }
                if (!matched) {
                    List<R> children = (List<R>) ReflectionUtils.invokeMethod(nodeConfig.getChildrenGetterMethod(), r);
                    if (children != null && !children.isEmpty()) {
                        List<T> childIds = findChildIds(children, pid, matched);
                        matched = !childIds.isEmpty();
                        if (matched) {
                            rList.addAll(childIds);
                            break;
                        }
                    }
                }
            }
            return rList;
        }
        return new ArrayList<>();
    }

    /**
     * 转换树结构的异常
     */
    public static class TreeException extends RuntimeException {

        public TreeException(String message) {
            super(message);
        }

    }

}
