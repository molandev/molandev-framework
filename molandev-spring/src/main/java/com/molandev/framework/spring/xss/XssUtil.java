package com.molandev.framework.spring.xss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.molandev.framework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.Set;

/**
 * xss clean
 */
public class XssUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * xss 清理
     *
     * @param html html
     * @return 清理后的 html
     */
    public static String escape(String html) {
        if (StringUtils.isNotEmpty(html)) {
            return HtmlUtils.htmlEscape(html, "UTF-8");
        }
        return html;
    }

    /**
     * 对JSON字符串进行XSS清理
     *
     * @param json JSON字符串
     * @return 清理后的JSON字符串
     */
    public static String escapeJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            escapeJsonNode(jsonNode);
            return jsonNode.toString();
        } catch (Exception e) {
            // 如果不是有效的JSON，使用普通的HTML转义
            return escape(json);
        }
    }

    /**
     * 递归处理JSON节点
     *
     * @param node JSON节点
     */
    private static void escapeJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Set<Map.Entry<String, JsonNode>> properties = objectNode.properties();
            for (Map.Entry<String, JsonNode> field : properties) {
                JsonNode value = field.getValue();
                if (value.isTextual()) {
                    String escapedText = HtmlUtils.htmlEscape(value.asText(), "UTF-8");
                    objectNode.set(field.getKey(), new TextNode(escapedText));
                } else {
                    escapeJsonNode(value);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode element = arrayNode.get(i);
                if (element.isTextual()) {
                    String escapedText = HtmlUtils.htmlEscape(element.asText(), "UTF-8");
                    arrayNode.set(i, new TextNode(escapedText));
                } else {
                    escapeJsonNode(element);
                }
            }
        }
    }
}