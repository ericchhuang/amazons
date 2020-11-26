package amazons;

import org.junit.Test;
import ucb.junit.textui;

import static org.junit.Assert.*;

public class AITest {
    public static void main(String[] ignored) {
        textui.runClasses(AITest.class);
    }

    private int testFunction(Board board) {
        int n = 0;
        for (Square white : board.whites()) {
            if (!board.isLegal(white)) {
                n += 5;
            }
        }
        for (Square black : board.blacks()) {
            if (!board.isLegal(black)) {
                n -= 5;
            }
        }
        return n;
    }


    @Test
    public void staticScoreTest() {
        Board b = new Board();
        long startTime = System.nanoTime();
        testFunction(b);
        long endTime = System.nanoTime();
        assertTrue((endTime - startTime) / 100000 < 10);
    }
}
