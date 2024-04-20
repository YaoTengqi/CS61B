package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size = 0;
    private int itemCount;
    private int firstSentinel = size - 1;
    private int lastSentinel = 0;
    private T items[];

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 8;
        firstSentinel = size - 1;
        lastSentinel = 0;
    }

    @Override
    public void addFirst(T item) {
        if (firstSentinel == lastSentinel || itemCount >= size) {
            resizeAdd();
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
        if (firstSentinel == lastSentinel || itemCount >= size) {
            resizeAdd();
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
        if (firstSentinel != size - 1 && firstSentinel > -2 && firstSentinel < size) {
            result = items[firstSentinel + 1];
            if (result != null) {
                firstSentinel = firstSentinel + 1;
                itemCount = itemCount - 1;
                items[firstSentinel] = null;
            }
        }
        if (itemCount < size / 2 && size > 16) { // memory usage control
            resizeShrink();
        }
        return result;
    }

    @Override
    public T removeLast() {
        T result = null;
        if (lastSentinel == 0 && firstSentinel != size - 1) {
            lastSentinel = size;
        }
        if (lastSentinel != 0 && lastSentinel > 0 && lastSentinel <= size) {
            result = items[lastSentinel - 1];
            if (result != null) {
                lastSentinel = lastSentinel - 1;
                itemCount = itemCount - 1;
                items[lastSentinel] = null;
            }
        }
        if (itemCount < size / 2 && size > 16) { // memory usage control
            resizeShrink();
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
        int mappedIndex = firstSentinel + index + 1;
        if (mappedIndex > size - 1) {
            mappedIndex = mappedIndex - size;
        }
        return items[mappedIndex];
    }

    private void resizeAdd() {
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
        if (lastSentinel > size) {
            lastSentinel = size;
        }
    }

    private void resizeShrink() {
        int newSize = size / 2;
        T newItems[] = (T[]) new Object[newSize];
        int first_sentinel_offset = 0;
        if (firstSentinel >= lastSentinel) {
            first_sentinel_offset = size - firstSentinel;
            System.arraycopy(items, 0, newItems, 0, lastSentinel);
            System.arraycopy(items, firstSentinel, newItems, newSize - first_sentinel_offset, (size - firstSentinel));
        } else {
            first_sentinel_offset = lastSentinel - firstSentinel;
            System.arraycopy(items, firstSentinel, newItems, 0, (lastSentinel - firstSentinel));
        }
        size = newSize;
        items = newItems;
        firstSentinel = size - first_sentinel_offset;
        if (lastSentinel > size) {
            lastSentinel = size;
        }
    }

    private class ADequeIterator implements Iterator<T> {
        private int currentIndex = firstSentinel + 1;

        public boolean hasNext() {

            if (((firstSentinel > lastSentinel && currentIndex > firstSentinel)
                    || (firstSentinel > lastSentinel && currentIndex < lastSentinel)
                    || (firstSentinel < lastSentinel && currentIndex > firstSentinel && currentIndex < lastSentinel))
                    && (currentIndex < size && currentIndex > -1)) {
                return true;
            }
            return false;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T result = items[currentIndex];
            if (currentIndex == size - 1) {
                currentIndex = 0;
            } else {
                currentIndex = currentIndex + 1;
            }
            return result;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return (Iterator<T>) new ADequeIterator();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }

        ArrayDeque<T> other = (ArrayDeque<T>) o;

        if (itemCount != other.size()) {
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
