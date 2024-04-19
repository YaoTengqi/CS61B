package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T> {
    private int size = 0;
    private int itemCount;
    private int firstSentinel = size - 1;
    private int lastSentinel = 0;
    public T items[];

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 8;
        firstSentinel = size - 1;
        lastSentinel = 0;
    }

    @Override
    public void addFirst(T item) {
        if (firstSentinel == lastSentinel) {
            resize();
        }
        items[firstSentinel] = item;
        itemCount = itemCount + 1;
        firstSentinel = firstSentinel - 1;
        if (firstSentinel == -1) {
            firstSentinel = size - 1;
        }
    }

    @Override
    public void addLast(T item) {
        if (firstSentinel == lastSentinel) {
            resize();
        }
        items[lastSentinel] = item;
        if (lastSentinel == size - 1) {
            lastSentinel = 0;
        } else {
            lastSentinel = lastSentinel + 1;
        }
        itemCount = itemCount + 1;
    }

    @Override
    public T removeFirst() {
        T result = null;
        if (lastSentinel != 0 && firstSentinel == size - 1) {
            firstSentinel = -1;
        }
        if (firstSentinel != size - 1) {
            result = items[firstSentinel + 1];
            if (result != null) {
                firstSentinel = firstSentinel + 1;
                itemCount = itemCount - 1;
                items[firstSentinel] = null;
            }
        }
        return result;
    }

    @Override
    public T removeLast() {
        T result = null;
        if (lastSentinel == 0 && firstSentinel != size - 1) {
            lastSentinel = size;
        }
        if (lastSentinel != 0) {
            result = items[lastSentinel - 1];
            if (result != null) {
                lastSentinel = lastSentinel - 1;
                itemCount = itemCount - 1;
                items[lastSentinel] = null;
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        if (itemCount == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return itemCount;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.println(items[i]);
        }
    }

    @Override
    public T get(int index) {
        int mapped_index = firstSentinel + index + 1;
        if (mapped_index > size - 1) {
            mapped_index = mapped_index - size;
        }
        return items[mapped_index];
    }

    public void resize() {
        int newSize = size * 2;
        int first_sentinel_offset = size - firstSentinel;
        T newItems[] = (T[]) new Object[newSize];
        if (firstSentinel >= lastSentinel) {
            System.arraycopy(items, 0, newItems, 0, lastSentinel);
            System.arraycopy(items, firstSentinel, newItems, newSize - first_sentinel_offset, (size - firstSentinel));
        } else {
            System.arraycopy(items, firstSentinel, newItems, lastSentinel, (lastSentinel - firstSentinel));
        }
        size = newSize;
        items = newItems;
        firstSentinel = size - first_sentinel_offset;
    }

    public class ADequeIterator implements Iterator<T> {
        public boolean hasNext() {
            if (itemCount != 0) {
                return true;
            }
            return false;
        }

        public T next() {
            T result = null;
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (lastSentinel == 0) {
                result = items[firstSentinel];
                firstSentinel = firstSentinel + 1;
            } else {
                result = items[lastSentinel];
                lastSentinel = lastSentinel - 1;
            }
            return result;
        }
    }

    public Iterator<T> iterator() {
        return new ADequeIterator();
    }
}
