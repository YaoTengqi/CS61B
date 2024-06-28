# BSTMap

## 特征

1. 有序

   左子树的值(key)一定小于右子树

2. 平衡

   在添加和删除时，BST都处于一个相对稳定的状态

## 方法

利用**递归**

put: 从root开始比较node.key与要插入的key，当key < node.key时递归调用put(leftChild, key)，当key > node.key时递归调用put(rightChild, key)，直到key = node.key时更新node.value = value，或者node == null不存在时新建一个node加入到BST当中。

remove: 分为三种情况

```java
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
```



## 总结

1. 利用**递归**
2. BSTMap查找速度快