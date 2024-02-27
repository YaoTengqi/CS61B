package timingtest;

import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        System.out.println("Timing table for getLast");
        int size_power = 8;
        int SLList_size = 1000;
        int getLast_times = 10000;
        SLList<Integer> SLLists = new SLList<>();
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        for (int i = 0; i < size_power; i++) {
            for (int j = 0; j < SLList_size; j++) {
                SLLists.addLast(j);
            }
            Stopwatch sw = new Stopwatch();
            Ns.addLast(SLList_size);
            for(int k = 0; k < getLast_times; k++) {
                SLLists.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
            opCounts.addLast(getLast_times);
            SLList_size = SLList_size * 2;
        }
        printTimingTable(Ns, times, opCounts);
    }

}
