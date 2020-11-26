package amazons;

import org.junit.Test;
import ucb.junit.textui;

import java.util.Iterator;

import static org.junit.Assert.*;

public class BoardTest {

    public static void main(String[] ignored) {
        textui.runClasses(BoardTest.class);
    }

    /** Tests for proper initialization of square. **/
    @Test
    public void squareInitTest() {
        String[] board = new String[100];
        int k = 0;
        for (int j = 1; j < 11; j++) {
            for (char i = 'a'; i < 'k'; i++) {
                board[k] = Character.toString(i) + Integer.toString(j);
                k++;
            }
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(board[i], Square.sq(i).toString());
        }
    }

    /** Tests correct direction from square. **/
    @Test
    public void squareDirectionTest() {
        assertEquals(0, Square.sq("d2").direction(Square.sq("d7")));
        assertEquals(1, Square.sq("d2").direction(Square.sq("g5")));
        assertEquals(2, Square.sq("d2").direction(Square.sq("h2")));
        assertEquals(3, Square.sq("d2").direction(Square.sq("e1")));
        assertEquals(4, Square.sq("d2").direction(Square.sq("d1")));
        assertEquals(5, Square.sq("d2").direction(Square.sq("c1")));
        assertEquals(6, Square.sq("d2").direction(Square.sq("a2")));
        assertEquals(7, Square.sq("d2").direction(Square.sq("a5")));
    }

    /** Tests if move from square to another square is legal. Checks if
     * any pieces are in the way. **/
    @Test
    public void isLegalTest() {
        Board b = new Board();
        assertFalse(b.isLegal(Square.sq("j1"), Square.sq("a1")));

        assertTrue(b.isLegal(Square.sq("d1"), Square.sq("b3")));
        assertTrue(b.isLegal(Square.sq("j2"), Square.sq("c2")));
        b.put(Piece.WHITE, '2', 'b');
        assertFalse(b.isLegal(Square.sq("a3"), Square.sq("c1")));
        assertFalse(b.isLegal(Square.sq("b9"), Square.sq("b2")));

        b = new Board();
        b.makeMove(Square.sq("d1"), Square.sq("d2"), Square.sq("d3"));
        assertTrue(b.isLegal(Square.sq("a7"), Square.sq("a8"),
                Square.sq("a6")));
    }

    /** Tests if piece can move from square. **/
    @Test
    public void fromIsLegalTest() {
        Board b = new Board();
        for (int i = 0; i < 100; i++) {
            assertTrue(b.isLegal(Square.sq(i)));
        }
        b.put(Piece.WHITE, Square.sq("a2"));
        b.put(Piece.WHITE, Square.sq("b1"));
        assertTrue(b.isLegal(Square.sq(0)));
        b.put(Piece.WHITE, Square.sq("b2"));
        assertFalse(b.isLegal(Square.sq(0)));

        assertTrue(b.isLegal(Square.sq("d4")));
        b.put(Piece.WHITE, Square.sq("c3"));
        b.put(Piece.WHITE, Square.sq("c4"));
        b.put(Piece.WHITE, Square.sq("c5"));
        b.put(Piece.WHITE, Square.sq("e3"));
        b.put(Piece.WHITE, Square.sq("e4"));
        b.put(Piece.WHITE, Square.sq("e5"));
        b.put(Piece.WHITE, Square.sq("d5"));
        assertTrue(b.isLegal(Square.sq("d4")));
        b.put(Piece.WHITE, Square.sq("d3"));
        assertFalse(b.isLegal(Square.sq("d4")));
    }

    /** Checks for legal move that is unblocked. **/
    @Test
    public void spearLegalMoveTest() {
        Board b = new Board();

        assertFalse(b.isLegal(Square.sq("d1"), Square.sq("d4"),
                Square.sq("d10")));
        assertTrue(b.isLegal(Square.sq("d1"), Square.sq("c1"),
                Square.sq("e1")));
        assertTrue(b.isLegal(Square.sq("d1"), Square.sq("d4"),
                Square.sq("f6")));
        assertTrue(b.isLegal(Square.sq("d1"), Square.sq("d4"),
                Square.sq("d1")));
    }

    /** Checks ReachableFrom has working next function. **/
    @Test
    public void reachableFromTest() {
        Board b = new Board();
        Iterator<Square> iter =
                b.reachableFrom(Square.sq("c4"), Square.sq("i4"));
        b.put(Piece.WHITE, Square.sq("h7"));
        b.put(Piece.WHITE, Square.sq("d9"));
        while (iter.hasNext()) {
            iter.next().setpiece(Piece.SPEAR);
        }
        String map =
                "   - - S B - - B - - -\n"
              + "   - - S - - - - - - -\n"
              + "   - - S - - - W - - -\n"
              + "   B - S - - S - - - B\n"
              + "   S - S - S - - - - -\n"
              + "   - S S S - - - - - -\n"
              + "   W S - S S S S S S W\n"
              + "   - S S S - - - - - -\n"
              + "   S - S - S - - - - -\n"
              + "   - - S W - S W - - -\n";
        assertEquals(b.toString(), map);

        b = new Board();
        iter = b.reachableFrom(Square.sq("a1"), null);
        while (iter.hasNext()) {
            iter.next().setpiece(Piece.SPEAR);
        }

        map =   "   - - - B - - B - - S\n"
              + "   - - - - - - - - S -\n"
              + "   - - - - - - - S - -\n"
              + "   B - - - - - S - - B\n"
              + "   - - - - - S - - - -\n"
              + "   - - - - S - - - - -\n"
              + "   W - - S - - - - - W\n"
              + "   S - S - - - - - - -\n"
              + "   S S - - - - - - - -\n"
              + "   - S S W - - W - - -\n";
        assertEquals(b.toString(), map);

        b = new Board();
        b.makeMove(Square.sq("g1"), Square.sq("g5"), Square.sq("g6"));
        b.makeMove(Square.sq("g10"), Square.sq("e10"), Square.sq("a6"));
        iter = b.reachableFrom(Square.sq("g5"), null);
        while (iter.hasNext()) {
            iter.next().setpiece(Piece.SPEAR);
        }

        map =   "   - S - B B - - - - -\n"
              + "   - - S - - - - - - -\n"
              + "   - - - S - - - - - S\n"
              + "   B - - - S - - - S B\n"
              + "   S - - - - S S S - -\n"
              + "   S S S S S S W S S S\n"
              + "   W - - - - S S S - W\n"
              + "   - - - - S - S - S -\n"
              + "   - - - S - - S - - S\n"
              + "   - - S W - - S - - -\n";
        assertEquals(b.toString(), map);
    }

    @Test
    public void isLegalTestSpear() {
        Board b = new Board();
        b.makeMove(Square.sq("d1"), Square.sq("d2"), Square.sq("g2"));
        assertFalse(b.isLegal(Square.sq("g1"), Square.sq("f1"),
                Square.sq("g2")));

        b = new Board();
        assertFalse(b.isLegal(Square.sq("d1"), Square.sq("d2"),
                Square.sq("d10")));
    }

    @Test
    public void queenReturnTest() {
        Board b = new Board();
        b.makeMove(Square.sq("d1"), Square.sq("d4"), Square.sq("b4"));
        b.makeMove(Square.sq("a7"), Square.sq("a6"), Square.sq("a5"));
        assertEquals("[d10, g10, j7, a6]", b.blacks().toString());
        assertEquals("[a4, g1, j4, d4]", b.whites().toString());
        b.undo();
        assertEquals("[d10, g10, j7, a7]", b.blacks().toString());
        assertEquals("[a4, g1, j4, d4]", b.whites().toString());
        b.undo();
        assertEquals("[d10, g10, j7, a7]", b.blacks().toString());
        assertEquals("[a4, g1, j4, d1]", b.whites().toString());
    }
}
