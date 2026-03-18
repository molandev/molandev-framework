package com.molandev.framework.util;


import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("unchecked")
public class ClassUtils {

    /**
     * 接口的前缀
     */
    public static final String INTERFACE_PREFIX = "interface ";

    /**
     * 类前缀
     */
    public static final String CLASS_PREFIX = "class ";

    /**
     * 类前缀长度
     */
    public static final int CLASS_PREFIX_LENGTH = CLASS_PREFIX.length();

    /**
     * int
     */
    public static final String INT = "int";

    /**
     * 双精度浮点数
     */
    public static final String DOUBLE = "double";

    /**
     * 短整型
     */
    public static final String SHORT = "short";

    /**
     * 长整型
     */
    public static final String LONG = "long";

    /**
     * 浮动
     */
    public static final String FLOAT = "float";

    /**
     * 布尔
     */
    public static final String BOOLEAN = "boolean";

    /**
     * 字符
     */
    public static final String CHAR = "char";

    /**
     * 字节
     */
    public static final String BYTE = "byte";

    /**
     * 泛型标记 左侧
     */
    public static final String GENERIC_TYPE_LEFT = "<";

    /**
     * 泛型标记 右侧
     */
    public static final String GENERIC_TYPE_RIGHT = ">";

    public static final String ARRAY_CLASS_PREFIX = "class [L";

    public static final String DOT = ",";

    /**
     * 反射创建对象，支持创建内部类
     */
    @SuppressWarnings("all")
    public static <T> T newInstance(Class<T> type) {
        try {
            // 如果不是成员类（内部类），直接创建实例
            boolean isMemberClass = type.isMemberClass();
            if (!isMemberClass) {
                return type.getConstructor().newInstance();
            }

            // 区分静态内部类和非静态内部类
            boolean isStatic = Modifier.isStatic(type.getModifiers());
            if (isStatic) {
                // 静态内部类不需要外部类实例
                return type.getConstructor().newInstance();
            }

            // 非静态内部类需要外部类实例
            List<Class<T>> cs = new ArrayList<>();
            cs.add(type);
            Class cc = type;
            while (cc.isMemberClass() && !Modifier.isStatic(cc.getModifiers())) {
                cs.add(0, cc.getEnclosingClass());
                cc = cc.getEnclosingClass();
            }

            Object current = null;
            for (Class c : cs) {
                if (current == null) {
                    current = c.getDeclaredConstructor().newInstance();
                } else {
                    current = c.getDeclaredConstructor(current.getClass()).newInstance(current);
                }
            }
            return (T) current;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断是不是接口
     */
    public static boolean isInterface(Type type) {
        return type.toString().indexOf(INTERFACE_PREFIX) == 0;
    }

    /**
     * 判断类是不是一个class
     */
    public static boolean isClass(Type type) {
        return type.toString().indexOf(CLASS_PREFIX) == 0;
    }

    /**
     * 判断是不是基本类型
     */
    public static boolean isPrimitive(Type type) {

        return type.toString().equals(INT) || type.toString().equals(DOUBLE) || type.toString().equals(SHORT)
                || type.toString().equals(LONG) || type.toString().equals(FLOAT) || type.toString().equals(BOOLEAN)
                || type.toString().equals(CHAR) || type.toString().equals(BYTE);
    }

    /**
     * 判断是否为泛型
     */
    public static boolean isGenericType(Type type) {
        return type.toString().indexOf(GENERIC_TYPE_LEFT) > 0;
    }

    /**
     * 根据名字获取对应的类型 name 来自于type.toString int 基本类型 class [Ljava.lang.String; 数组 class [Z
     * 基本类型数组 class java.lang.String 正常class java.util.List<java.lang.Exception[]> 泛型
     * java.util.Map<java.util.List<java.lang.String>, java.lang.Exception>
     */
    public static Type getType(String name) {
        switch (name) {
            case INT:
                return int.class;
            case BOOLEAN:
                return boolean.class;
            case BYTE:
                return byte.class;
            case SHORT:
                return short.class;
            case LONG:
                return long.class;
            case CHAR:
                return char.class;
            case FLOAT:
                return float.class;
            case DOUBLE:
                return double.class;
            default:

        }

        if (name.startsWith(CLASS_PREFIX) && name.length() > CLASS_PREFIX_LENGTH) {
            return forName(name.substring(CLASS_PREFIX_LENGTH).trim());
        }

        if (name.contains(GENERIC_TYPE_LEFT)) {
            return getParameterizedType(name);
        }

        throw new IllegalArgumentException("类:" + name + "无法找到");
    }

    /**
     * @param clazz1 主类
     * @param types  泛型类
     */
    public static Type getType(Class<?> clazz1, Type... types) {
        if (types.length == 0) {
            return clazz1;
        }
        StringBuilder sb = new StringBuilder();
        for (Type type : types) {
            sb.append(type.getTypeName()).append(DOT);
        }
        sb.deleteCharAt(sb.length() - 1);
        return getParameterizedType(clazz1.getName() + GENERIC_TYPE_LEFT + sb.toString() + GENERIC_TYPE_RIGHT);
    }


    /**
     * 获取泛型type
     */
    public static ParameterizedType getParameterizedType(String name) {
        int begin = name.indexOf(GENERIC_TYPE_LEFT);
        int end = name.lastIndexOf(GENERIC_TYPE_RIGHT);
        Class<?> aClass = forName(name.substring(0, begin));
        String paraTypes = name.substring(begin + 1, end);
//        String[] split = paraTypes.split(",");
        String[] split = splitType(paraTypes);

        Type[] innerTypes = new Type[split.length];
        for (int i = 0; i < split.length; i++) {
            String s = split[i];

            if (s.contains(GENERIC_TYPE_LEFT)) {
                innerTypes[i] = getParameterizedType(s);
            } else {
                if (s.endsWith("[]")) {
                    s = ARRAY_CLASS_PREFIX + s.substring(0, s.length() - 2).trim() + ";";
                } else {
                    s = CLASS_PREFIX + s;
                }
                innerTypes[i] = getType(s);
            }
        }
        return ParameterizedTypeImpl.make(aClass, innerTypes, null);
    }


    /**
     * 用于支持多层嵌套，此时传进来的字符串是内部泛型，
     * 1. java.lang.String
     * 2. java.lang.String,java.lang.String
     * 3. java.lang.String,java.lang.Map<String,String>
     * 4. java.lang.String,java.lang.List<java.lang.Map<java.lang.String,java.lang.Map<java.lang.String,java.lang.Object>>>
     */
    private static String[] splitType(String paraTypes) {
        List<String> split = new ArrayList<>();
        if (paraTypes.contains(GENERIC_TYPE_LEFT)) {
            StringBuilder sb = new StringBuilder();
            int depth = 0;
            for (char c : paraTypes.toCharArray()) {
                if (c == ' ') {
                    continue;
                }
                if (c == ',' && depth == 0) {
                    if (sb.length() != 0) {
                        split.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    continue;
                }
                sb.append(c);
                if (c == GENERIC_TYPE_LEFT.charAt(0)) {
                    depth++;
                }
                if (c == GENERIC_TYPE_RIGHT.charAt(0)) {
                    depth--;
                    if (depth == 0) {
                        split.add(sb.toString());
                        sb = new StringBuilder();
                    }
                }
            }
            if (sb.length() != 0) {
                split.add(sb.toString());
            }
            return split.toArray(new String[0]);
        } else {
            return paraTypes.split(",");
        }
    }

    /**
     * 加载类 不能传数组类，基本类型
     *
     * @param className 类名
     */
    public static Class<?> forName(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取类型的泛型的类型，只支持一级
     *
     * @param genericSuperclass 一般的超类
     * @return {@link Class[]}
     */
    public static Class<?>[] getGenericTypes(Type genericSuperclass) {
        if (isGenericType(genericSuperclass)) {
            if (genericSuperclass instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
                Class<?>[] classes = new Class[actualTypeArguments.length];
                boolean matched = true;
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if (actualTypeArguments[i] instanceof Class) {
                        classes[i] = (Class<?>) actualTypeArguments[i];
                    } else {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    return classes;
                }
            }
            String typeName = genericSuperclass.getTypeName();
            String substring = typeName.substring(typeName.indexOf(GENERIC_TYPE_LEFT) + 1,
                    typeName.lastIndexOf(GENERIC_TYPE_RIGHT));
            if (substring.contains(GENERIC_TYPE_LEFT)) {
                throw new IllegalArgumentException("仅支持一级泛型");
            }
            String[] split = substring.split(DOT);
            Class<?>[] classes = new Class[split.length];
            for (int i = 0; i < split.length; i++) {
                classes[i] = forName(split[i].trim());
            }
            return classes;
        }
        return null;
    }

}