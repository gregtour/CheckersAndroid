package greg.checkers.game;

import java.util.ArrayList;

public class CheckersGame {
    public static final int NONE = 0;
    public static final int BLACK = 1;
    public static final int RED = 2;
    public static final int KINGED = 3;

    // checkers game state
    private Board gameBoard;
    private int turn;
    private boolean over;
    private int winner;

    // checkers game holds board state and current turn
    public CheckersGame() {
        gameBoard = new Board(this);
        turn = CheckersGame.BLACK;
        over = false;
        winner = CheckersGame.NONE;
    }

    // check whose turn it is
    public int whoseTurn() {
        return turn;
    }

    // get the board data
    public Board getBoard() {
        return this.gameBoard;
    }

    public Move getLongestMove(Position start, Position end) {
        Move longest = null;
        Move moveset[] = getMoves();
        for (Move move : moveset) {
            if (move.start().equals(start) && move.end().equals(end)) {
                if (longest == null ||
                        longest.captures.size() < move.captures.size())
                    longest = move;
            }
        }
        return longest;
    }

    // get possible moves for current player
    public Move[] getMoves() {
        ArrayList<Move> finalMoves = new ArrayList<>();
        ArrayList<Move> potentialMoves = new ArrayList<>();
        ArrayList<Position> startingPositions = new ArrayList<>();

        // add moves for each matching piece
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = gameBoard.getPiece(x, y);
                if (piece != null && piece.getColor() == turn) {
                    Position start = new Position(x, y);
                    potentialMoves.addAll(
                        gameBoard.getMoves(start)
                    );
                }
            }
        }

//        // check if there have been captures
//        boolean noCaptures = true;
//        for (Move sequence : potentialMoves) {
//            if (sequence.captures.size() > 0) {
//                noCaptures = false;
//            }
//        }
//
//        // filter appropriate moves
//        for (Move move : potentialMoves) {
//            if (move.captures.size() > 0 || noCaptures) {
//                finalMoves.add(move);
//            }
//        }

        // return choices as a sequence of positions
        // return finalMoves.toArray(new Move[finalMoves.size()]);
        return potentialMoves.toArray(new Move[potentialMoves.size()]);
    }

    // make a move
    public void makeMove(Move choice) {
        gameBoard.makeMove(choice);
        advanceTurn();
    }

    // switch turns
    private void advanceTurn() {
        if (turn == CheckersGame.RED) {
            turn = CheckersGame.BLACK;
        } else {
            turn = CheckersGame.RED;
        }
    }
}
