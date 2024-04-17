package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;


    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.comparator = c;
    }

    public T max() {
        T max = items[0];
        if (this.isEmpty()) {
            return null;
        } else {
            for (int i = 0; i < this.size(); i++) {
                if (this.items[i] != null && comparator.compare(this.items[i], max) > 0) {
                    max = this.items[i];
                }
            }
        }
        return max;
    }

    public T max(Comparator c) {
        T max = items[0];
        if (this.isEmpty()) {
            return null;
        } else {
            for (int i = 0; i < this.size(); i++) {
                if (this.items[i] != null && comparator.compare(this.items[i], max) > 0) {
                    max = this.items[i];
                }
            }
        }
        return max;
    }
}
