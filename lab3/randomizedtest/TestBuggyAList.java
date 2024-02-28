package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correctList = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();
        correctList.addLast(4);
        buggyList.addLast(4);
        correctList.addLast(5);
        buggyList.addLast(5);
        correctList.addLast(6);
        buggyList.addLast(6);

        assertEquals(correctList.size(), buggyList.size());

        assertEquals(correctList.removeLast(), buggyList.removeLast());
        assertEquals(correctList.removeLast(), buggyList.removeLast());
        assertEquals(correctList.removeLast(), buggyList.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyL = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggyL.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                assertEquals(L.size(), buggyL.size());
            } else if (operationNumber == 2) {
                // getLast
                if (L.size() > 0 && buggyL.size() > 0) {
                    assertEquals(L.getLast(), buggyL.getLast());
                }
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() > 0 && buggyL.size() > 0) {
                    assertEquals(L.removeLast(), buggyL.removeLast());
                }
            }
        }
    }
}
