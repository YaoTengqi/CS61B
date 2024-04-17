package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T> {
    public int size = 0;
    public int item_count;
    public int first_sentinel = 0;
    public int last_sentinel = 1;
    public T items[];

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 8;
        first_sentinel = size - 1;
        last_sentinel = 0;
    }

    @Override
    public void addFirst(T item) {
        if (first_sentinel == last_sentinel) {
            resize();
        }
        items[first_sentinel] = item;
        item_count = item_count + 1;
        first_sentinel = first_sentinel - 1;
    }

    @Override
    public void addLast(T item) {
        if (first_sentinel == last_sentinel) {
            resize();
        }
        items[last_sentinel] = item;
        if (last_sentinel == size - 1) {
            last_sentinel = 0;
        } else {
            last_sentinel = last_sentinel + 1;
        }
        item_count = item_count + 1;
    }

    @Override
    public T removeFirst() {
        T result = null;
        if (last_sentinel != 0 && first_sentinel == size - 1) {
            first_sentinel = last_sentinel - 2;
        }
        if (first_sentinel != size - 1) {
            result = items[first_sentinel + 1];
            if (result != null) {
                first_sentinel = first_sentinel + 1;
                item_count = item_count - 1;
                items[first_sentinel] = null;
            }
        }
        return result;
    }

    @Override
    public T removeLast() {
        T result = null;
        if (last_sentinel == 0 && first_sentinel != size - 1) {
            last_sentinel = first_sentinel;
        }
        if (last_sentinel != 0) {
            result = items[last_sentinel - 1];
            if (result != null) {
                last_sentinel = last_sentinel - 1;
                item_count = item_count - 1;
                items[last_sentinel] = null;
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        if (item_count == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return item_count;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.println(items[i]);
        }
    }

    @Override
    public T get(int i) {
        return items[i];
    }

    public void resize() {
        int new_size = size * 2;
        int first_sentinel_offset = size - first_sentinel;
        T new_items[] = (T[]) new Object[new_size];
        System.arraycopy(items, 0, new_items, 0, size);
        size = new_size;
        items = new_items;
        first_sentinel = size - first_sentinel_offset;
    }

    public class ADequeIterator implements Iterator<T> {
        @Override
        public boolean hasNext() {
            if (item_count != 0) {
                return true;
            }
            return false;
        }

        @Override
        public T next() {
            T result = null;
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (last_sentinel == 0) {
                result = items[first_sentinel];
                first_sentinel = first_sentinel + 1;
            } else {
                result = items[last_sentinel];
                last_sentinel = last_sentinel - 1;
            }
            return result;
        }
    }
}
