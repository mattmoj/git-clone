package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    /*
    AListNoResizing<Integer> L = new AListNoResizing<>();
    BuggyAList<Integer> bug = new BuggyAList<>();
    int n = 500;
    for(int i=0; i<=n; i++){
        int operationNumber = StdRandom.uniform(0, 4);
        if(operationNumber == 0){
            //addLast
            int randVal = StdRandom.uniform(0, 100);
            L.addLast(randVal);
            System.out.println("addLast(" + randVal + ")");
        }
        else if (operationNumber == 1) {
            //size
            int size_b = L.size();
            int size_g = bug.size();
            assertEquals("Size buggy not equal", size_g, size_b);
            System.out.println("size: " + size_g);
        }
        else if(operationNumber == 2){
            if(L.size() > 0){
                int last_g = L.getLast();
                int last_b = bug.getLast();
                assertEquals("getLast for buggy incorrect", last_g, last_b);
                System.out.println("getLast" + last_g);
            }
        }
        else if(operationNumber == 3){
            //removeLast
            if(L.size()>0){
                int rlast_g = L.removeLast();
                int rlast_b = bug.removeLast();
                assertEquals("removeLast for buggy incorrect", rlast_g, rlast_b);
                System.out.println("removeLast:" + rlast_g);
            }
        }
    }

     */
    @Test
    public void testThreeAddThreeRemove() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> broken = new BuggyAList<>();

      correct.addLast(5);
      correct.addLast(10);
      correct.addLast(15);

      broken.addLast(5);
      broken.addLast(10);
      broken.addLast(15);

      assertEquals(correct.size(), broken.size());

      assertEquals(correct.removeLast(), broken.removeLast());
      assertEquals(correct.removeLast(), broken.removeLast());
      assertEquals(correct.removeLast(), broken.removeLast());
    }
    @Test
    public void randomizedTest(){
      AListNoResizing<Integer> L = new AListNoResizing<>();
      BuggyAList<Integer> bug = new BuggyAList<>();

      int N = 500;
      for (int i = 0; i < N; i += 1) {
        int operationNumber = StdRandom.uniform(0, 4);
        if (operationNumber == 0) {
          // addLast
          int randVal = StdRandom.uniform(0, 100);
          L.addLast(randVal);
          System.out.println("addLast(" + randVal + ")");
        }
        else if (operationNumber == 1) {
          // size
          int size = L.size();
          System.out.println("size: " + size);
        }
        else if(operationNumber == 2){
          if(L.size() > 0){
            int last_g = L.getLast();
            int last_b = bug.getLast();
            assertEquals("getLast for buggy incorrect", last_g, last_b);
            System.out.println("getLast" + last_g);
          }
        }
        else if(operationNumber == 3){
          //removeLast
          if(L.size()>0){
            int rlast_g = L.removeLast();
            int rlast_b = bug.removeLast();
            assertEquals("removeLast for buggy incorrect", rlast_g, rlast_b);
            System.out.println("removeLast:" + rlast_g);
          }
        }
      }
    }
}
