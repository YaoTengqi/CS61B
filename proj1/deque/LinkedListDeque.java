package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private static class LinkedListNode<T> {
        private LinkedListNode prev; //前指针
        private T item; //Deque的值
        private LinkedListNode next; //后指针

        private LinkedListNode(LinkedListNode p, T i, LinkedListNode n) {
            prev = p;
            item = i;
            next = n;
        }
    }

    private LinkedListNode<T> sentinel;
    private int size = 0;

    /**
     * 创建一个空的双端链表
     */
    public LinkedListDeque() {
        sentinel = new LinkedListNode<T>(null, null, null); //这是一个循环哨兵
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }

    /**
     * 在列表头添加数据
     *
     * @param item
     */
    public void addFirst(T item) {
        LinkedListNode newNode = new LinkedListNode(sentinel, item, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        if (size == 0) {
            sentinel.prev = newNode;
        }

        size = size + 1;
    }

    /**
     * 在列表尾添加数据
     *
     * @param item
     */
    public void addLast(T item) {
        LinkedListNode endNode = sentinel.prev;
        LinkedListNode newNode = new LinkedListNode(endNode, item, sentinel);
        endNode.next = newNode;
        sentinel.prev = newNode;
        size = size + 1;
    }

    /**
     * Removes and returns the item at the front of the deque. If no such item exists, returns null.
     *
     * @return
     */
    public T removeFirst() {
        LinkedListNode<T> removeNode = sentinel.next;
        if (removeNode == sentinel) {
            return null;
        } else if (size == 1) {
            size = size - 1;
            sentinel.next = sentinel;
            sentinel.prev = sentinel;
            removeNode.prev = null; // save memory usage
            removeNode.next = null; // save memory usage
            return removeNode.item;
        } else {
            size = size - 1;
            sentinel.next.next.prev = sentinel;
            sentinel.next = sentinel.next.next;
        }
        removeNode.prev = null; // save memory usage
        removeNode.next = null; // save memory usage
        return removeNode.item;
    }

    /**
     * Removes and returns the item at the back of the deque. If no such item exists, returns null.
     *
     * @return
     */
    public T removeLast() {
        LinkedListNode<T> removeNode = sentinel.prev;
        if (removeNode == sentinel) {
            return null;
        } else if (size == 1) {
            size = size - 1;
            sentinel.next = sentinel;
            sentinel.prev = sentinel;
            removeNode.prev = null; // save memory usage
            removeNode.next = null; // save memory usage
            return removeNode.item;
        } else {
            size = size - 1;
            sentinel.prev = sentinel.prev.prev;
            sentinel.prev.next = sentinel;
        }
        removeNode.prev = null; // save memory usage
        removeNode.next = null; // save memory usage
        return removeNode.item;
    }


    /**
     * Returns true if deque is empty, false otherwise.
     *
     * @return
     */
    public boolean isEmpty() {
        if (sentinel.next == sentinel) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the number of items in the deque.
     *
     * @return size
     */
    public int size() {
        return size;
    }

    /**
     * Prints the items in the deque from first to last, separated by a space. Once all the items have been printed, print out a new line.
     */
    public void printDeque() {
        LinkedListNode p = sentinel.next;
        int dequeSize = this.size();
        for (int i = 0; i < dequeSize; i++) {
            System.out.println(p.item);
            p = p.next;
        }
        System.out.println();
    }

    /**
     * 获得第i个LLD的数据
     *
     * @param i
     * @return
     */
    public T get(int i) {
        LinkedListNode p = sentinel.next;
        T result = (T) p.item;
        while (i > 0) {
            p = p.next;
            i = i - 1;
            result = (T) p.item;
        }
        return result;
    }

    private class LLDIterator implements Iterator<T> {

        private LinkedListNode<T> currentNode = sentinel;

        public boolean hasNext() {
            if (currentNode.next != sentinel) {
                return true;
            } else {
                return false;
            }
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            currentNode = currentNode.next;
            T item = currentNode.item;
            return item;
        }
    }

    /**
     * The Deque objects we’ll make are iterable (i.e. Iterable<T>) so we must provide this method to return an iterator.
     *
     * @return
     */
    @Override
    public Iterator<T> iterator() {
        return (Iterator<T>) new LLDIterator();
    }

    public T getRecursive(int index) {
        LinkedListNode p = sentinel.next;
        return getRecursiveHelper(index, p);
    }

    private T getRecursiveHelper(int index, LinkedListNode p) {
        if (p == null) {
            return null;
        }
        if (index == 0) {
            return (T) p.item;
        } else {
            return getRecursiveHelper(index - 1, p.next);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }

        LinkedListDeque<T> other = (LinkedListDeque<T>) o;

        if (size != other.size()) {
            return false;
        }

        Iterator<T> thisIterator = this.iterator();
        Iterator<T> otherIterator = other.iterator();
        while (thisIterator.hasNext()) {
            T thisItem = thisIterator.next();
            T otherItem = otherIterator.next();
            if (thisItem != null && otherItem != null && thisItem != otherItem) {
                return false;
            }
        }
        return true;
    }
}
