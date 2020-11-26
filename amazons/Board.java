package amazons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static amazons.Piece.*;


/** The state of an Amazons Game.
 *  @author Eric Huang
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;
    /** Decrement for toString function. */
    static final int DECREMENT = 20;
    /** Starting value for toString function. */
    static final int STARTINGVAL = 90;

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        this._nummoves = model.numMoves();
        this._moves = model.moves();
        this._turn = model.turn();
        this._winner = model.winner();
    }

    /** Clears the board to the initial position. */
    void init() {
        for (int i = 0; i < 100; i++) {
            Square.sq(i).setpiece(EMPTY);
        }
        _whites = new ArrayList<Square>();
        _blacks = new ArrayList<Square>();

        Square.sq("a4").setpiece(WHITE);
        _whites.add(Square.sq("a4"));
        Square.sq("d1").setpiece(WHITE);
        _whites.add(Square.sq("d1"));
        Square.sq("g1").setpiece(WHITE);
        _whites.add(Square.sq("g1"));
        Square.sq("j4").setpiece(WHITE);
        _whites.add(Square.sq("j4"));
        Square.sq("a7").setpiece(BLACK);
        _blacks.add(Square.sq("a7"));
        Square.sq("d10").setpiece(BLACK);
        _blacks.add(Square.sq("d10"));
        Square.sq("g10").setpiece(BLACK);
        _blacks.add(Square.sq("g10"));
        Square.sq("j7").setpiece(BLACK);
        _blacks.add(Square.sq("j7"));

        _turn = WHITE;
        _winner = EMPTY;
        _nummoves = 0;
        _moves = new ArrayList<Move>();
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _nummoves;
    }

    /** Returns arraylist with positions of all white queens. */
    ArrayList<Square> whites() {
        return _whites;
    }

    /** Returns arraylist with positions of all black queens. */
    ArrayList<Square> blacks() {
        return _blacks;
    }

    /** Return the array of moves that have been made. */
    ArrayList<Move> moves() {
        return _moves;
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        return _winner;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return s.getpiece();
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return Square.sq(row, col).getpiece();
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        int row = s.col();
        int col = s.row();
        Square.sq(col, row).setpiece(p);
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        Square.sq(col, row).setpiece(p);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - '1', row - 'a');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        if (!from.isQueenMove(to)) {
            return false;
        }
        int direction = from.direction(to);
        int col = from.col();
        int row = from.row();
        Square place = from;
        if (!to.getpiece().equals(Piece.EMPTY)) {
            return false;
        }
        while (!place.equals(to)) {
            if (direction == 7 || direction < 2) {
                col++;
            } else if (direction > 2 && direction < 6) {
                col--;
            }
            if (direction > 0 && direction < 4) {
                row++;
            } else if (direction > 4 && direction < 8) {
                row--;
            }
            place = Square.sq(col, row);
            if (!place.getpiece().equals(Piece.EMPTY)
                    && !place.equals(asEmpty)) {
                return false;
            }
        }
        return true;
    }

    /** Returns number of free spaces around FROM. */
    public int free(Square from) {
        int oldcol = from.col();
        int oldrow = from.row();
        int result = 0;
        for (int col = oldcol - 1; col < oldcol + 2; col++) {
            for (int row = oldrow - 1; row < (oldrow + 2); row++) {
                if (Square.exists(col, row)
                        && Square.sq(col, row) != Square.sq(oldcol, oldrow)
                        && Square.sq(col, row).getpiece().equals(Piece.EMPTY)) {
                    result++;
                }
            }
        }
        return result;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        int oldcol = from.col();
        int oldrow = from.row();
        for (int col = oldcol - 1; col < oldcol + 2; col++) {
            for (int row = oldrow - 1; row < (oldrow + 2); row++) {
                if (Square.exists(col, row)
                        && Square.sq(col, row) != Square.sq(oldcol, oldrow)
                        && Square.sq(col, row).getpiece().equals(Piece.EMPTY)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        return isUnblockedMove(from, to, null);
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        if (isLegal(from, to) && from.getpiece().equals(_turn)
                && _winner.equals(Piece.EMPTY)) {
            if (spear.equals(from)) {
                return true;
            } else if (isUnblockedMove(to, spear, from)) {
                return true;
            }
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        Move move = Move.mv(from, to, spear);
        makeMove(move);
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        if (isLegal(move.from(), move.to(), move.spear())) {
            move.to().setpiece(move.from().getpiece());
            move.from().setpiece(Piece.EMPTY);
            move.spear().setpiece(Piece.SPEAR);
            _nummoves++;
            _moves.add(move);
            if (_turn.equals(Piece.WHITE)) {
                _whites.remove(move.from());
                _whites.add(move.to());
            } else if (_turn.equals(Piece.BLACK)) {
                _blacks.remove(move.from());
                _blacks.add(move.to());
            }
            if (!legalMoves(_turn.opponent()).hasNext()) {
                _winner = _turn;
            }
            _turn = _turn.opponent();
        } else {
            return;
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moves.isEmpty()) {
            return;
        }
        Move move = _moves.get(_moves.size() - 1);
        _moves.remove(_moves.size() - 1);
        _nummoves--;
        _winner = Piece.EMPTY;
        _turn = _turn.opponent();
        move.spear().setpiece(Piece.EMPTY);
        move.from().setpiece(move.to().getpiece());
        move.to().setpiece(Piece.EMPTY);
        if (_turn.equals(Piece.WHITE)) {
            _whites.remove(move.to());
            _whites.add(move.from());
        } else if (_turn.equals(Piece.BLACK)) {
            _blacks.remove(move.to());
            _blacks.add(move.from());
        }
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = 0;
            _steps = 1;
            _asEmpty = asEmpty;
        }

        @Override
        public boolean hasNext() {
            while (_dir < 8) {
                int col = _from.nextCol(_dir, _steps);
                int row = _from.nextRow(_dir, _steps);
                if (Square.exists(col, row)
                        && (Square.sq(col, row).getpiece().equals(Piece.EMPTY)
                        || Square.sq(col, row).equals(_asEmpty))
                ) {
                    return true;
                }
                _dir++;
                _steps = 1;
            }
            return false;
        }

        @Override
        public Square next() {
            Square next = null;
            while (next == null) {
                try {
                    next = _from.queenMove(_dir, _steps);
                    toNext();
                } catch (IllegalArgumentException exc) {
                    toNext();
                }
            }

            if (!next.getpiece().equals(Piece.EMPTY)
                    && !next.equals(_asEmpty)) {
                _dir++;
                _steps = 1;
                return next();
            }
            return next;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            _steps++;
            if (_steps > 10) {
                _dir++;
                _steps = 1;
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            _start = null;
            _queens = new ArrayList<Square>();
            while (_startingSquares.hasNext()) {
                Square sqr = _startingSquares.next();
                if (sqr.getpiece().equals(_fromPiece) && isLegal(sqr)) {
                    _queens.add(sqr);
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (_queens.isEmpty() && !_pieceMoves.hasNext()
                    && !_spearThrows.hasNext()) {
                return false;
            }
            return true;
        }

        @Override
        public Move next() {
            toNext();
            Move result = Move.mv(_start, _nextSquare, _spearThrows.next());
            return result;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            if (!_pieceMoves.hasNext() && !_spearThrows.hasNext()) {
                _start = _queens.get(0);
                _pieceMoves = new ReachableFromIterator(_start, null);
                _queens.remove(0);
            }
            if (!_spearThrows.hasNext()) {
                _nextSquare = _pieceMoves.next();
                _spearThrows = new ReachableFromIterator(_nextSquare, _start);
            }
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
        /** Numbers of queens counted. */
        private ArrayList<Square> _queens;
    }

    @Override
    public String toString() {
        String result = "  ";
        for (int i = STARTINGVAL; i >= 0; i++) {
            result += " " + Square.sq(i).getpiece().toString();
            if ((i % 10) == 9) {
                result += "\n";
                if (i != 9) {
                    result += "  ";
                }
                i = i - DECREMENT;
            }
        }
        return result;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
        Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;

    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;

    /** Number of moves that have been made. */
    private int _nummoves;

    /** List of moves in order. */
    private ArrayList<Move> _moves;

    /** Positions of all white queens. */
    private ArrayList<Square> _whites;

    /** Positions of all black queens. */
    private ArrayList<Square> _blacks;
}
