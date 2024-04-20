package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.comparator = c;
    }

    public T max() {
        T max = get(0);
        if (this.isEmpty()) {
            return null;
        } else {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i) != null && comparator.compare(this.get(i), max) > 0) {
                    max = this.get(i);
                }
            }
        }
        return max;
    }

    public T max(Comparator<T> c) {
        T max = get(0);
        if (this.isEmpty()) {
            return null;
        } else {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i) != null && c.compare(this.get(i), max) > 0) {
                    max = this.get(i);
                }
            }
        }
        return max;
    }
}
