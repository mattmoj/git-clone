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
        // TODO: YOUR CODE HERE
        AList n = new AList();
        AList ops = new AList();
        AList times = new AList();

        SLList oop = new SLList();
        for(int i=1000; i<=64000; i*=2){
            int m = 10000;
            for(int j=0; j<i; j++){
                oop.addLast(i);
            }
            Stopwatch stoppy = new Stopwatch();
            for(int k=0; k<=m; k++){
                //this is the only one that needs to be an SLList
                oop.getLast();
            }
            double timey = stoppy.elapsedTime();
            n.addLast(i);
            times.addLast(timey);
            ops.addLast(m);
        }
        printTimingTable(n, times, ops);
    }

}
