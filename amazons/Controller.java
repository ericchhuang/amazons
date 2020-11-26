package amazons;

import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Consumer;

import static amazons.Utils.*;
import static amazons.Piece.*;

/** The input/output and GUI controller for play of Amazons.
 *  @author Eric Huang */
final class Controller {

    /** Controller for one or more games of Amazons, using
     *  MANUALPLAYERTEMPLATE as an exemplar for manual players
     *  (see the Player.create method) and AUTOPLAYERTEMPLATE
     *  as an exemplar for automated players.  Reports
     *  board changes to VIEW at appropriate points.  Uses REPORTER
     *  to report moves, wins, and errors to user. If LOGFILE is
     *  non-null, copies all commands to it. If STRICT, exits the
     *  program with non-zero code on receiving an erroneous move from a
     *  player. */
    Controller(View view, PrintStream logFile, Reporter reporter,
               Player manualPlayerTemplate, Player autoPlayerTemplate) {
        _view = view;
        _playing = false;
        _logFile = logFile;
        _input = new Scanner(System.in);
        _autoPlayerTemplate = autoPlayerTemplate;
        _manualPlayerTemplate = manualPlayerTemplate;
        _nonPlayer = manualPlayerTemplate.create(EMPTY, this);
        _reporter = reporter;
    }

    /** Play Amazons. */
    void play() {
        _playing = true;
        _winner = null;
        _board.init();
        _white = _manualPlayerTemplate.create(WHITE, this);
        _black = _autoPlayerTemplate.create(BLACK, this);
        while (_playing) {
            _view.update(_board);
            String command;
            if (_winner == null) {
                if (_board.turn() == WHITE) {
                    command = _white.myMove();
                } else {
                    command = _black.myMove();
                }
            } else {
                command = _nonPlayer.myMove();
                if (command == null) {
                    command = "quit";
                }
            }
            try {
                executeCommand(command);
            } catch (IllegalArgumentException excp) {
                reportError("Error: %s%n", excp.getMessage());
            }
        }
        if (_logFile != null) {
            _logFile.close();
        }
    }

    /** Return the current board.  The value returned should not be
     *  modified by the caller. */
    Board board() {
        return _board;
    }

    /** Return a random integer in the range 0 inclusive to U, exclusive.
     *  Available for use by AIs that use random selections in some cases.
     *  Once setRandomSeed is called with a particular value, this method
     *  will always return the same sequence of values. */
    int randInt(int U) {
        return _randGen.nextInt(U);
    }

    /** Re-seed the pseudo-random number generator (PRNG) that supplies randInt
     *  with the value SEED. Identical seeds produce identical sequences.
     *  Initially, the PRNG is randomly seeded. */
    void setSeed(long seed) {
        _randGen.setSeed(seed);
    }

    /** Return the next line of input, or null if there is no more. First
     *  prompts for the line.  Trims the returned line (if any) of all
     *  leading and trailing whitespace. */
    String readLine() {
        System.out.print("> ");
        System.out.flush();
        if (_input.hasNextLine()) {
            return _input.nextLine().trim();
        } else {
            return null;
        }
    }

    /** Report error by calling reportError(FORMAT, ARGS) on my reporter. */
    void reportError(String format, Object... args) {
        _reporter.reportError(format, args);
    }

    /** Report note by calling reportNote(FORMAT, ARGS) on my reporter. */
    void reportNote(String format, Object... args) {
        _reporter.reportNote(format, args);
    }

    /** Report move by calling reportMove(MOVE) on my reporter. */
    void reportMove(Move move) {
        if (_board.winner().equals(Piece.EMPTY)) {
            _reporter.reportMove(move);
        }
    }

    /** A Command is pair (<pattern>, <processor>), where <pattern> is a
     *  Matcher that matches instances of a particular command, and
     *  <processor> is a functional object whose .accept method takes a
     *  successfully matched Matcher and performs some operation. */
    private static class Command {
        /** A new Command that matches PATN (a regular expression) and uses
         *  PROCESSOR to process commands that match the pattern. */
        Command(String patn, Consumer<Matcher> processor) {
            _matcher = Pattern.compile(patn).matcher("");
            _processor = processor;
        }

        /** A Matcher matching my pattern. */
        protected final Matcher _matcher;
        /** The function object that implements my command. */
        protected final Consumer<Matcher> _processor;
    }

    /** A list of Commands describing the valid textual commands to the
     *  Amazons program and the methods to process them. */
    private Command[] _commands = {
        new Command("quit$", this::doQuit),
        new Command("seed\\s+(\\d+)$", this::doSeed),
        new Command("dump$", this::doDump),
        new Command("new$", this::doNew),
        new Command("undo$", this::doUndo),
        new Command("(manual\\s(((b|B)(l|L)(a|A)(c|C)(k|K))|"
                + "((w|W)(h|H)(i|I)(t|T)(e|E))))$", this::doManual),
        new Command("(auto\\s(((b|B)(l|L)(a|A)(c|C)(k|K))|"
                + "((w|W)(h|H)(i|I)(t|T)(e|E))))$", this::doAuto),
        new Command("([a-j](10|[1-9])\\s){2}[a-j](10|[1-9])$", this::doMove),
        new Command("[a-j](10|[1-9])-[a-j](10|[1-9])\\([a-j]"
                + "(10|[1-9])\\)", this::doMoveOther),
    };

    /** A Matcher whose Pattern matches comments. */
    private final Matcher _comment = Pattern.compile("#.*").matcher("");

    /** Check that CMND is one of the valid Amazons commands and execute it, if
     *  so, raising an IllegalArgumentException otherwise. */
    private void executeCommand(String cmnd) {
        if (_logFile != null) {
            _logFile.println(cmnd);
            _logFile.flush();
        }

        _comment.reset(cmnd);
        cmnd = _comment.replaceFirst("").trim().toLowerCase();

        if (cmnd.isEmpty()) {
            return;
        }
        for (Command parser : _commands) {
            parser._matcher.reset(cmnd);
            if (parser._matcher.matches()) {
                parser._processor.accept(parser._matcher);
                return;
            }
        }
        throw error("Bad command: %s", cmnd);
    }

    /** Command "new". */
    private void doNew(Matcher unused) {
        _board.init();
        _winner = null;
    }

    /** Command "quit". */
    private void doQuit(Matcher unused) {
        _playing = false;
    }

    /** Command "seed N" where N is the first group of MAT. */
    private void doSeed(Matcher mat) {
        try {
            setSeed(Long.parseLong(mat.group(1)));
        } catch (NumberFormatException excp) {
            throw error("number too large");
        }
    }

    /** Dump the contents of the board on standard output. */
    private void doDump(Matcher unused) {
        System.out.printf("===%n%s===%n", _board);
    }

    /** Changes player to manual according to MAT. **/
    private void doManual(Matcher mat) {
        String player = mat.group().substring(7);
        if (player.equals("black")) {
            _black = _manualPlayerTemplate.create(BLACK, this);
        } else {
            _white = _manualPlayerTemplate.create(WHITE, this);
        }
    }

    /** Changes player to auto according to MAT. **/
    private void doAuto(Matcher mat) {
        String player = mat.group().substring(5);
        if (player.equals("black")) {
            _black = _autoPlayerTemplate.create(BLACK, this);
        } else {
            _white = _autoPlayerTemplate.create(WHITE, this);
        }
    }

    /** Makes MOVE on the board. **/
    private void doMove(Matcher move) {
        String command = move.group();
        int firstspace = command.indexOf(" ", 0);
        int secondspace = command.indexOf(" ", firstspace + 1);
        Square from = Square.sq(command.substring(0, firstspace));
        Square to = Square.sq(command.substring(firstspace + 1, secondspace));
        Square spear = Square.sq(command.substring(secondspace + 1));
        _board.makeMove(from, to, spear);
        if (!_board.winner().equals(Piece.EMPTY)) {
            _winner = _board.winner();
            _reporter.reportNote(_board.winner().toName() + " wins.");
        }
    }

    /** Makes MOVE on the board, but based on different pattern. */
    private void doMoveOther(Matcher move) {
        String command = move.group();
        int hyphen = command.indexOf('-');
        int pths = command.indexOf('(');
        Square from = Square.sq(command.substring(0, hyphen));
        Square to = Square.sq(command.substring(hyphen + 1, pths));
        Square spear = Square.sq(command.substring(pths + 1,
                command.length() - 1));
        _board.makeMove(from, to, spear);
        if (!_board.winner().equals(Piece.EMPTY)) {
            _winner = _board.winner();
            _reporter.reportNote(_board.winner().toName() + " wins.");
        }
    }

    /** Undoes player and AI move. */
    private void doUndo(Matcher unused) {
        _board.undo();
        _board.undo();
    }

    /** The board. */
    private Board _board = new Board();

    /** The winning side of the current game. */
    private Piece _winner;

    /** True while game is still active. */
    private boolean _playing;

    /** The object that is displaying the current game. */
    private View _view;

    /** My pseudo-random number generator. */
    private Random _randGen = new Random();

    /** Log file, or null if absent. */
    private PrintStream _logFile;

    /** Input source. */
    private Scanner _input;

    /** The current White and Black players, each created from
     *  _autoPlayerTemplate or _manualPlayerTemplate. */
    private Player _white, _black;

    /** A dummy Player used to return commands but not moves when no
     *  game is in progress. */
    private Player _nonPlayer;

    /** The current templates for manual and automated players. */
    private Player _autoPlayerTemplate, _manualPlayerTemplate;

    /** Reporter for messages and errors. */
    private Reporter _reporter;
}
