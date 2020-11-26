package amazons;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static amazons.Utils.*;

/** Represents a position on an Amazons board.  Positions are numbered
 *  from 0 (lower-left corner) to 99 (upper-right corner).  Squares
 *  are immutable and unique: there is precisely one square created for
 *  each distinct position.  Clients create squares using the factory method
 *  sq, not the constructor.  Because there is a unique Square object for each
 *  position, you can freely use the cheap == operator (rather than the
 *  .equals method) to compare Squares, and the program does not waste time
 *  creating the same square over and over again.
 *  @author Eric Huang
 */
final class Square {

    /** The regular expression for a square designation (e.g.,
     *  a3). For convenience, it is in parentheses to make it a
     *  group.  This subpattern is intended to be incorporated into
     *  other pattern that contain square designations (such as
     *  patterns for moves). */
    static final String SQ = "([a-j](?:[0-9]|10))";

    /** Number for converting to char. */
    static final int DECREMENT = 97;

    /** Number for converting char. */
    static final int CONVERT = 97;

    /** Return my row position, where 0 is the bottom row. */
    int row() {
        return _row;
    }

    /** Return my column position, where 0 is the leftmost column. */
    int col() {
        return _col;
    }

    /** Return my index position (0-99).  0 represents square a1, and 99
     *  is square j10. */
    int index() {
        return _index;
    }

    /** Places piece P into this square. */
    public void setpiece(Piece p) {
        this._piece = p;
    }

    /** Returns the piece on this square. */
    public Piece getpiece() {
        return this._piece;
    }

    /** Return true iff THIS - TO is a valid queen move. */
    boolean isQueenMove(Square to) {
        int row1 = to.row();
        int row2 = this.row();
        int col1 = to.col();
        int col2 = this.col();
        if (to == this) {
            return false;
        }
        if (row1 == row2 || col1 == col2
                || java.lang.Math.abs(row2 - row1)
                == java.lang.Math.abs(col2 - col1)) {
            return true;
        }
        return false;
    }

    /** Definitions of direction for queenMove.  DIR[k] = (dcol, drow)
     *  means that to going one step from (col, row) in direction k,
     *  brings us to (col + dcol, row + drow). */
    private static final int[][] DIR = {
        { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
        { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };


    /** Return the Square that is STEPS>0 squares away from me in direction
     *  DIR, or null if there is no such square.
     *  DIR = 0 for north, 1 for northeast, 2 for east, etc., up to 7 for
     *  northwest. If DIR has another value, return null. Thus, unless the
     *  result is null the resulting square is a queen move away from me. */
    Square queenMove(int dir, int steps) {
        int col = this.col();
        int row = this.row();
        if (dir == 7 || dir < 2) {
            col += steps;
        } else if (dir > 2 && dir < 6) {
            col -= steps;
        }
        if (dir > 0 && dir < 4) {
            row += steps;
        } else if (dir > 4 && dir < 8) {
            row -= steps;
        }
        return Square.sq(col, row);
    }


    /** Return the direction (an int as defined in the documentation
     *  for queenMove) of the queen move THIS-TO. */
    int direction(Square to) {
        assert isQueenMove(to);
        int rowdif = to.row() - this.row();
        int coldif = to.col() - this.col();

        if (rowdif == 0) {
            if (coldif > 0) {
                return 0;
            }
            return 4;
        } else if (coldif == 0) {
            if (rowdif > 0) {
                return 2;
            }
            return 6;
        } else if ((rowdif * coldif) > 0) {
            if (rowdif > 0) {
                return 1;
            }
            return 5;
        } else {
            if (rowdif > 0) {
                return 3;
            }
            return 7;
        }

    }

    @Override
    public String toString() {
        return _str;
    }

    /** Return true iff COL ROW is a legal square. */
    static boolean exists(int col, int row) {
        return row >= 0 && col >= 0 && row < Board.SIZE && col < Board.SIZE;
    }

    /** Return the (unique) Square denoting COL ROW. */
    static Square sq(int col, int row) {
        if (!exists(row, col)) {
            throw error("row or column out of bounds");
        }
        return Square.sq((col * 10) + row);
    }

    /** Return the (unique) Square denoting the position with index INDEX. */
    static Square sq(int index) {
        return SQUARES[index];
    }

    /** Return the (unique) Square denoting the position COL ROW, where
     *  COL ROW is the standard text format for a square (e.g., a4). */
    static Square sq(String col, String row) {
        int rowcode = row.charAt(0);
        int icol = Integer.parseInt(col) - 1;
        int irow = rowcode - DECREMENT;
        return sq((icol * 10) + irow);
    }

    /** Return the (unique) Square denoting the position in POSN, in the
     *  standard text format for a square (e.g. a4). POSN must be a
     *  valid square designation. */
    static Square sq(String posn) {
        assert posn.matches(SQ);
        return sq(posn.substring(1), posn.substring(0, 1));
    }

    /** Return an iterator over all Squares. */
    static Iterator<Square> iterator() {
        return SQUARE_LIST.iterator();
    }

    /** Return the Square with index INDEX. */
    private Square(int index) {
        _index = index;
        _row = index % 10;
        _col = index / 10;
        char lrow = (char) (_row + CONVERT);
        int lcol = _col + 1;
        _str = Character.toString(lrow) + lcol;
        _piece = Piece.EMPTY;
    }

    /** The cache of all created squares, by index. */
    private static final Square[] SQUARES =
        new Square[Board.SIZE * Board.SIZE];

    /** SQUARES viewed as a List. */
    private static final List<Square> SQUARE_LIST = Arrays.asList(SQUARES);

    static {
        for (int i = Board.SIZE * Board.SIZE - 1; i >= 0; i -= 1) {
            SQUARES[i] = new Square(i);
        }
    }

    /** Return next col STEPS in DIR. **/
    public int nextCol(int dir, int steps) {
        int col = this.col();
        if (dir == 7 || dir < 2) {
            col += steps;
        } else if (dir > 2 && dir < 6) {
            col -= steps;
        }
        return col;
    }

    /** Return next row STEPS in DIR. **/
    public int nextRow(int dir, int steps) {
        int row = this.row();
        if (dir > 0 && dir < 4) {
            row += steps;
        } else if (dir > 4 && dir < 8) {
            row -= steps;
        }
        return row;
    }

    /** My index position. */
    private final int _index;

    /** My row and column (redundant, since these are determined by _index). */
    private final int _row, _col;

    /** My String denotation. */
    private final String _str;

    /** My piece. */
    private Piece _piece;
}
