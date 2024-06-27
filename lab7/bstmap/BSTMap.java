package bstmap;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private Node root = null;              // 根节点
    private int size = 0;               // 整体的大小

    public BSTMap() {
    }

    private class Node {
        private K key;              // 节点的索引
        private V value;            // 节点的值
        private Node leftChild;     // 左子树的头结点
        private Node rightChild;    // 右子树的头结点
        private int size;           // 子树的大小

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.size = 1;
            this.leftChild = null;
            this.rightChild = null;
        }

        public void setLeftChild(Node leftChild) {
            this.leftChild = leftChild;
        }

        public void setRightChild(Node rightChild) {
            this.rightChild = rightChild;
        }

        public K getKey() {
            return key;
        }

    }

    /**
     * 将BSTMap清空
     * size清0，root根节点设为null
     */
    @Override
    public void clear() {
        this.size = 0;
        this.root = null;
    }

    /**
     * @param key
     * @return
     */
    @Override
    public boolean containsKey(K key) {
        V value = get(key);
        if (value != null) {
            return true;
        }
        return false;
    }

    @Override
    public V get(K key) {
        V value = get(this.root, key);
        return value;
    }

    private V get(Node node, K key) {
        V value = null;
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            value = get(node.leftChild, key);
        } else if (cmp > 0) {
            value = get(node.rightChild, key);
        } else {
            value = node.value;
        }
        return value;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * 将键值对插入到二叉搜索树当中，首先需要搜索是否包含此key
     *
     * @param key
     * @param value
     */
    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node put(Node node, K key, V value) {
        if (value == null) {    // 若value为null给其一个默认值"default"
            value = (V) "default";
        }
        if (node == null) {
            size++;
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {    // 递归插入左子树
            node.leftChild = put(node.leftChild, key, value);
        } else if (cmp > 0) {   // 递归插入右子树
            node.rightChild = put(node.rightChild, key, value);
        } else {
            node.value = value;
        }
        node.size = getSize(node);
        return node;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        keys = addKeyInOrder(root, keys);
        return keys;
    }

    private Set<K> addKeyInOrder(Node node, Set<K> keys) {
        if (node == null) {
            return keys;
        }
        addKeyInOrder(node.leftChild, keys);
        keys.add(node.key);
        addKeyInOrder(node.rightChild, keys);
        return keys;
    }

    @Override
    public V remove(K key) {
        V value = get(key);
        if (value != null) {
            root = remove(root, key, value);
            size--;
        }
        return value;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    private Node remove(Node node, K key, V value) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.leftChild = remove(node.leftChild, key, value);
        } else if (cmp > 0) {
            node.rightChild = remove(node.rightChild, key, value);
        } else {
            // 当前node就是要被remove的node
            // 1.没有child
            // 直接删除
            // 2. 一个child
            // 将child代替当前节点的位置，并将此节点删除
            if (node.leftChild == null) {
                return node.rightChild;
            }
            if (node.rightChild == null) {
                return node.leftChild;
            }
            // 3.两个child
            // 找出左子树的最大节点或者右子树的最小节点替换当前节点
            Node temp = node;
            node = min(node.rightChild); // 找出右子树的最小节点
            node.rightChild = deleteMin(temp.rightChild);   // 删除右子树的最小节点
            node.leftChild = temp.leftChild;    // 左子树不变
        }
        node.size = getSize(node);
        return node;
    }

    private Node min(Node node) {
        // 直到没有比该节点小的节点就可以，既找到一个没有左子树的node
        if (node.leftChild == null) {
            return node;
        }
        return min(node.leftChild);
    }

    private Node deleteMin(Node node) {
        if (node.leftChild == null) {
            return node.rightChild;
        }
        node.leftChild = deleteMin(node.leftChild);   // 先找到最小的节点
        node.size = getSize(node);
        return node;
    }

    @Override
    public Iterator<K> iterator() {
        return null;
    }

    private int getSize(Node node) {
        int leftSize = 0;
        int rightSize = 0;
        if (node.leftChild != null) {
            leftSize = node.leftChild.size;
        }
        if (node.rightChild != null) {
            rightSize = node.rightChild.size;
        }
        int size = 1 + leftSize + rightSize;
        return size;
    }
}
