package amazons;

import static java.lang.Math.*;
import java.util.Iterator;

import static amazons.Piece.*;

/** A Player that automatically generates moves.
 *  @author Eric Huang
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** Step one for depth search. */
    private static final int STEP1 = 20;
    /** Step two for depth search. */
    private static final int STEP2 = 55;
    /** Step one for depth search. */
    private static final int STEP3 = 75;
    /** Value for trapping an opponent queen. */
    private static final int TRAP = 65;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _lastFoundMove = null;
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = board();
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != Piece.EMPTY) {
            return staticScore(board);
        }

        if (sense == 1) {
            Iterator<Move> white = board.legalMoves(Piece.WHITE);
            int max = staticScore(board);
            while (white.hasNext()) {
                Move move = white.next();
                board.makeMove(move);
                int eval = findMove(board, depth - 1, false, -1, alpha, beta);
                board.undo();
                max = Math.max(max, eval);
                if (saveMove && !white.hasNext() && _lastFoundMove == null) {
                    _lastFoundMove = move;
                }
                if (saveMove && (Math.max(eval, max) == eval)) {
                    _lastFoundMove = move;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return max;

        } else {
            Iterator<Move> black = board.legalMoves(Piece.BLACK);
            int min = staticScore(board);
            while (black.hasNext()) {
                Move move = black.next();
                board.makeMove(move);
                int eval = findMove(board, depth - 1, false, 1, alpha, beta);
                board.undo();
                min = Math.min(staticScore(board), eval);
                if (saveMove && !black.hasNext() && _lastFoundMove == null) {
                    _lastFoundMove = move;
                }
                if (saveMove && (Math.min(eval, min) == eval))  {
                    _lastFoundMove = move;
                }
                beta = Math.max(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return min;
        }
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        int size = board.whites().size() + board.blacks().size();
        if (N < STEP1) {
            return 1;
        } else if (N < STEP2) {
            return 2;
        } else if (N < STEP3) {
            return 3 + ((10 - size) / (STEP1 * 10 / N));
        } else {
            return N / 15;
        }
    }


    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }
        int n = 0;
        for (Square white : board.whites()) {
            n += Math.pow(board.free(white), 2);
            if (!board.isLegal(white)) {
                n -= TRAP;
            }
        }
        for (Square black : board.blacks()) {
            n -= Math.pow(board.free(black), 2);
            if (board.isLegal(black)) {
                n += TRAP;
            }
        }
        return n;
    }
}
