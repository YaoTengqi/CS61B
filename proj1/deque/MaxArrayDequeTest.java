package deque;

import jh61b.junit.In;
import org.junit.Test;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {
    Comparator<Integer> c = new Comparator<Integer>() {
        @Override
        public int compare(Integer integer, Integer t1) {
            if (integer > t1) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    @Test
    public void createMADeque() {

        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        mad.addFirst(1);
        mad.addLast(2);
        mad.addLast(3);
        assertEquals(3, mad.size());
    }

    @Test
    public void testMax() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        mad.addFirst(1);
        mad.addLast(2);
        mad.addLast(3);
        Integer max = mad.max();
        assertEquals(new Integer(3), max);
    }

    @Test
    public void testMaxComparator() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        mad.addFirst(1);
        mad.addLast(2);
        mad.addLast(3);
        mad.addLast(3);
        mad.addLast(3);
        mad.addLast(100);
        Integer max = mad.max(c);
        assertEquals(new Integer(100), max);
    }

    @Test
    public void randomTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        Random random = new Random();
        int mode = random.nextInt(4);
        for (int i = 0; i < 100; i++) {
            mode = random.nextInt(4);
            switch (mode) {
                case 1:
                    mad.addFirst(mode);
                    break;
                case 2:
                    mad.addLast(mode);
                    break;
                case 3:
                    mad.removeFirst();
                    break;
                case 4:
                    mad.removeLast();
                    break;
            }
        }
    }

    @Test
    public void removeEdgeTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        mad.addLast(0);
        mad.addLast(1);
        int result = mad.removeFirst();
        assertEquals(0, result);
    }

    @Test
    public void removeTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        mad.isEmpty();
        mad.addFirst(1);
        mad.addFirst(2);
        mad.addFirst(3);
        mad.isEmpty();
        int result = mad.removeLast();
        assertEquals(1, result);
    }

    @Test
    public void getTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<Integer>(c);
        int result = 0;
        mad.addLast(0);
        mad.removeFirst();
        mad.addLast(2);
        mad.addLast(3);
        mad.get(0);
        mad.addFirst(5);
        mad.removeFirst();
        mad.addLast(7);
        mad.removeLast();
        mad.get(0);
        mad.removeFirst();
        mad.addLast(11);
        mad.removeFirst();
        mad.addLast(13);
        mad.addFirst(14);
        mad.addLast(15);
        mad.addFirst(16);
        mad.addLast(17);
        mad.addLast(18);
        mad.addFirst(19);
        mad.removeLast();
        result = mad.get(5);
        assertEquals(15, result);
    }
}
