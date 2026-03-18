package com.molandev.framework.spring.tree;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("树工具类测试")
class TreeUtilTest {

    @Nested
    @DisplayName("Map树构建测试")
    class MapTreeBuildTest {

        @Test
        @DisplayName("buildMapTree空列表测试")
        void buildMapTree_withEmptyList_shouldReturnEmptyList() {
            List<Map<String, Object>> result = TreeUtil.buildMapTree(new ArrayList<>());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildMapTree null列表测试")
        void buildMapTree_withNullList_shouldReturnEmptyList() {
            List<Map<String, Object>> result = TreeUtil.buildMapTree(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildMapTree正常数据测试")
        void buildMapTree_withValidData_shouldBuildTree() {
            // 准备测试数据
            List<TreeNode> nodes = Arrays.asList(
                    new TreeNode(1L, 0L, "Root"),
                    new TreeNode(2L, 1L, "Child1"),
                    new TreeNode(3L, 1L, "Child2"),
                    new TreeNode(4L, 2L, "Grandchild1")
            );

            List<Map<String, Object>> result = TreeUtil.buildMapTree(nodes);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).get("id"));
            assertEquals(0L, result.get(0).get("parentId"));
            assertEquals("Root", result.get(0).get("name"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) result.get(0).get("children");
            assertNotNull(children);
            assertEquals(2, children.size());

            Map<String, Object> child1 = children.get(0);
            assertEquals(2L, child1.get("id"));
            assertEquals(1L, child1.get("parentId"));
            assertEquals("Child1", child1.get("name"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> grandChildren = (List<Map<String, Object>>) child1.get("children");
            assertNotNull(grandChildren);
            assertEquals(1, grandChildren.size());
            assertEquals(4L, grandChildren.get(0).get("id"));
            assertEquals(2L, grandChildren.get(0).get("parentId"));
            assertEquals("Grandchild1", grandChildren.get(0).get("name"));
        }

        @Test
        @DisplayName("buildMapTree空ID节点测试")
        void buildMapTree_withNullId_shouldThrowException() {
            List<TreeNode> nodes = List.of(new TreeNode(null, 0L, "Node with null id"));

            TreeUtil.TreeException exception = assertThrows(
                    TreeUtil.TreeException.class,
                    () -> TreeUtil.buildMapTree(nodes)
            );

            assertEquals("树节点id不能为空", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("树构建测试")
    class TreeBuildTest {

        @Test
        @DisplayName("buildTree空列表测试")
        void buildTree_withEmptyList_shouldReturnEmptyList() {
            List<TreeNode> result = TreeUtil.buildTree(new ArrayList<>());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildTree null列表测试")
        void buildTree_withNullList_shouldReturnEmptyList() {
            List<TreeNode> result = TreeUtil.buildTree(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("buildTree正常数据测试")
        void buildTree_withValidData_shouldBuildTree() {
            // 准备测试数据
            List<TreeNode> nodes = Arrays.asList(
                    new TreeNode(1L, 0L, "Root"),
                    new TreeNode(2L, 1L, "Child1"),
                    new TreeNode(3L, 1L, "Child2"),
                    new TreeNode(4L, 2L, "Grandchild1")
            );

            List<TreeNode> result = TreeUtil.buildTree(nodes);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals(0L, result.get(0).getParentId());
            assertEquals("Root", result.get(0).getName());

            List<TreeNode> children = result.get(0).getChildren();
            assertNotNull(children);
            assertEquals(2, children.size());

            TreeNode child1 = children.get(0);
            assertEquals(2L, child1.getId());
            assertEquals(1L, child1.getParentId());
            assertEquals("Child1", child1.getName());

            List<TreeNode> grandChildren = child1.getChildren();
            assertNotNull(grandChildren);
            assertEquals(1, grandChildren.size());
            assertEquals(4L, grandChildren.get(0).getId());
            assertEquals(2L, grandChildren.get(0).getParentId());
            assertEquals("Grandchild1", grandChildren.get(0).getName());
        }

        @Test
        @DisplayName("buildTree排序测试")
        void buildTree_withSortSeq_shouldSortTree() {
            // 准备测试数据
            List<TreeNode> nodes = Arrays.asList(
                    new TreeNode(1L, 0L, 2, "Root"),
                    new TreeNode(2L, 1L, 2, "Child2"),
                    new TreeNode(3L, 1L, 1, "Child1"),
                    new TreeNode(4L, 1L, 3, "Child3")
            );

            List<TreeNode> result = TreeUtil.buildTree(nodes);

            assertNotNull(result);
            assertEquals(1, result.size());

            List<TreeNode> children = result.get(0).getChildren();
            assertNotNull(children);
            assertEquals(3, children.size());

            // 验证排序是否正确
            assertEquals("Child1", children.get(0).getName()); // sortSeq = 1
            assertEquals("Child2", children.get(1).getName()); // sortSeq = 2
            assertEquals("Child3", children.get(2).getName()); // sortSeq = 3
        }
    }

    @Nested
    @DisplayName("树展开测试")
    class TreeExpandTest {

        @Test
        @DisplayName("expandTree正常树测试")
        void expandTree_withValidTree_shouldExpandToList() {
            // 构建一个树结构
            TreeNode root = new TreeNode(1L, 0L, "Root");
            TreeNode child1 = new TreeNode(2L, 1L, "Child1");
            TreeNode child2 = new TreeNode(3L, 1L, "Child2");
            TreeNode grandchild1 = new TreeNode(4L, 2L, "Grandchild1");

            child1.setChildren(List.of(grandchild1));
            root.setChildren(Arrays.asList(child1, child2));

            List<TreeNode> treeList = List.of(root);

            List<TreeNode> result = TreeUtil.expandTree(treeList);

            assertNotNull(result);
            assertEquals(4, result.size());

            // 验证所有节点都在展开的列表中
            assertTrue(result.stream().anyMatch(node -> node.getId().equals(1L)));
            assertTrue(result.stream().anyMatch(node -> node.getId().equals(2L)));
            assertTrue(result.stream().anyMatch(node -> node.getId().equals(3L)));
            assertTrue(result.stream().anyMatch(node -> node.getId().equals(4L)));
        }

        @Test
        @DisplayName("expandTree空树测试")
        void expandTree_withEmptyTree_shouldReturnEmptyList() {
            List<TreeNode> result = TreeUtil.expandTree(new ArrayList<>());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("树过滤测试")
    class TreeFilterTest {

        @Test
        @DisplayName("filterTree谓词过滤测试")
        void filterTree_withPredicate_shouldFilterTree() {
            // 构建一个树结构
            TreeNode root = new TreeNode(1L, 0L, "Root");
            TreeNode child1 = new TreeNode(2L, 1L, "Child1");
            TreeNode child2 = new TreeNode(3L, 1L, "Child2");
            TreeNode grandchild1 = new TreeNode(4L, 2L, "Grandchild1");
            TreeNode grandchild2 = new TreeNode(5L, 2L, "Grandchild2");

            child1.setChildren(new ArrayList<>(Arrays.asList(grandchild1, grandchild2)));
            root.setChildren(new ArrayList<>(Arrays.asList(child1, child2)));

            List<TreeNode> treeList = new ArrayList<>(List.of(root));

            // 过滤只保留id为偶数的节点
            TreeUtil.filterTree(treeList, node -> node.getId() % 2 == 0);

            assertEquals(1, treeList.size());
            assertEquals(1, treeList.get(0).getChildren().size());
            TreeNode filteredItem = treeList.get(0).getChildren().get(0);
            assertEquals(2L, filteredItem.getId());
            assertEquals("Child1", filteredItem.getName());

            List<TreeNode> children = filteredItem.getChildren();
            assertNotNull(children);
            assertEquals(1, children.size());
            assertEquals(4L, children.get(0).getId());
        }

        @Test
        @DisplayName("filterTree ID集合过滤测试")
        void filterTree_withIdsCollection_shouldFilterTree() {
            // 构建一个树结构
            TreeNode root = new TreeNode(1L, 0L, "Root");
            TreeNode child1 = new TreeNode(2L, 1L, "Child1");
            TreeNode child2 = new TreeNode(3L, 1L, "Child2");
            TreeNode grandchild1 = new TreeNode(4L, 2L, "Grandchild1");

            child1.setChildren(new ArrayList<>(List.of(grandchild1)));
            root.setChildren(new ArrayList<>(Arrays.asList(child1, child2)));

            List<TreeNode> treeList = new ArrayList<>(List.of(root));

            // 只保留id为1,2,4的节点
            TreeUtil.filterTree(treeList, Arrays.asList(1L, 2L, 4L));

            assertEquals(1, treeList.size());
            TreeNode filteredRoot = treeList.get(0);
            assertEquals(1L, filteredRoot.getId());

            List<TreeNode> children = filteredRoot.getChildren();
            assertNotNull(children);
            assertEquals(1, children.size());
            assertEquals(2L, children.get(0).getId());

            List<TreeNode> grandChildren = children.get(0).getChildren();
            assertNotNull(grandChildren);
            assertEquals(1, grandChildren.size());
            assertEquals(4L, grandChildren.get(0).getId());
        }
    }

    @Nested
    @DisplayName("子节点查找测试")
    class ChildNodeFindTest {

        @Test
        @DisplayName("findChildIds查找子节点ID测试")
        void findChildIds_withValidTree_shouldReturnChildIds() {
            // 构建一个树结构
            TreeNode root = new TreeNode(1L, 0L, "Root");
            TreeNode child1 = new TreeNode(2L, 1L, "Child1");
            TreeNode child2 = new TreeNode(3L, 1L, "Child2");
            TreeNode grandchild1 = new TreeNode(4L, 2L, "Grandchild1");
            TreeNode grandchild2 = new TreeNode(5L, 2L, "Grandchild2");

            child1.setChildren(Arrays.asList(grandchild1, grandchild2));
            root.setChildren(Arrays.asList(child1, child2));

            List<TreeNode> treeList = List.of(root);

            // 查找节点2的所有子节点ID
            List<Long> childIds = TreeUtil.findChildIds(treeList, 2L);

            assertNotNull(childIds);
            assertEquals(3, childIds.size()); // 包括自己
            assertTrue(childIds.contains(2L));
            assertTrue(childIds.contains(4L));
            assertTrue(childIds.contains(5L));
        }

        @Test
        @DisplayName("findChildren查找子节点测试")
        void findChildren_withValidTree_shouldReturnChildren() {
            // 构建一个树结构
            TreeNode root = new TreeNode(1L, 0L, "Root");
            TreeNode child1 = new TreeNode(2L, 1L, "Child1");
            TreeNode child2 = new TreeNode(3L, 1L, "Child2");
            TreeNode grandchild1 = new TreeNode(4L, 2L, "Grandchild1");

            child1.setChildren(Arrays.asList(grandchild1));
            root.setChildren(Arrays.asList(child1, child2));

            List<TreeNode> treeList = List.of(root);

            // 查找节点2及其所有子节点
            TreeNode result = TreeUtil.findChildren(treeList, 2L);

            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals("Child1", result.getName());

            List<TreeNode> children = result.getChildren();
            assertNotNull(children);
            assertEquals(1, children.size());
            assertEquals(4L, children.get(0).getId());
            assertEquals("Grandchild1", children.get(0).getName());
        }
    }

    // 测试用的树节点类
    @Setter
    @Getter
    public static class TreeNode {
        // Getters and setters
        @TreeId
        private Long id;

        @TreeParentId
        private Long parentId;

        @TreeSort
        private Integer sortSeq;

        @TreeChildren
        private List<TreeNode> children;

        private String name;

        public TreeNode() {
        }

        public TreeNode(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        public TreeNode(Long id, Long parentId, Integer sortSeq, String name) {
            this.id = id;
            this.parentId = parentId;
            this.sortSeq = sortSeq;
            this.name = name;
        }

        @Override
        public String toString() {
            return "TreeNode{" +
                    "id=" + id +
                    ", parentId=" + parentId +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}