package greg.checkers.game;

import java.util.ArrayList;

/**
 * Created by Greg on 8/6/2017.
 */ // data for complete board state
public class Board {
    private CheckersGame checkersGame;
    private Piece board[][];
    private int numPieces[];

    public boolean isGameSquare(int x, int y) {
        // within 8x8 dimensions and is odd-square
        return (x >= 0 && y >= 0 && x < 8 && y < 8 && (x + y) % 2 > 0);
    }

    public boolean isGameSquare(Position pos) {
        return isGameSquare(pos.x, pos.y);
    }


    //
    private Position[] RED_DIRECTIONS = new Position[]{new Position(-1, 1), new Position(1, 1)};
    private Position[] BLACK_DIRECTIONS = new Position[]{new Position(-1, -1), new Position(1, -1)};
    private Position[] BOTH_DIRECTIONS = new Position[]{new Position(-1, 1), new Position(1, 1), new Position(-1, -1), new Position(1, -1)};
    private Position[] NO_DIRECTIONS = new Position[]{};

    private Position[] getNeighbors(int color, boolean king) {
        if (king) {
            return BOTH_DIRECTIONS;
        } else if (color == CheckersGame.RED) {
            return RED_DIRECTIONS;
        } else if (color == CheckersGame.BLACK) {
            return BLACK_DIRECTIONS;
        } else {
            return BOTH_DIRECTIONS;
        }
    }


    // create new board
    public Board(CheckersGame checkersGame) {
        this.checkersGame = checkersGame;
        numPieces = new int[]{0, 0};
        board = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int side = (y < 3) ? CheckersGame.RED : (y > 4) ? CheckersGame.BLACK : 0;
                boolean validSquare = this.isGameSquare(x, y);
                if (side != CheckersGame.NONE && validSquare) {
                    board[x][y] = new Piece(side, false);
                    numPieces[side - 1]++;
                } else {
                    board[x][y] = null;
                }
            }
        }
    }

    // create from existing positions
    public Board(CheckersGame checkersGame, int[][] positions) {
        this.checkersGame = checkersGame;
        numPieces = new int[]{0, 0};
        board = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (positions[x][y] > CheckersGame.NONE) {
                    int side = positions[x][y] % CheckersGame.KINGED;
                    boolean kinged = positions[x][y] > CheckersGame.KINGED;
                    board[x][y] = new Piece(side, kinged);
                    numPieces[side - 1]++;
                } else {
                    board[x][y] = null;
                }
            }
        }
    }

    // save positions as int[][]
    public int[][] saveBoard() {
        int result[][] = new int[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (board[x][y] != null) {
                    Piece piece = board[x][y];
                    result[x][y] = piece.getColor();
                    if (piece.isKing()) {
                        result[x][y] += CheckersGame.KINGED;
                    }
                } else {
                    result[x][y] = CheckersGame.NONE;
                }
            }
        }
        return result;
    }

    // get a piece on the board
    public Piece getPiece(int x, int y) {
        return (isGameSquare(x, y) ? board[x][y] : null);
    }
    public Piece getPiece(Position pos) {
        return getPiece(pos.x, pos.y);
    }

    // find a piece on the board
    public Position getPosition(Piece piece) {
        int x = 0, y = 0;
        for (; x < 8; x++) {
            for (; y < 8; y++) {
                if (getPiece(x, y) == piece) {
                    return new Position(x, y);
                }
            }
        }
        return null;
    }

    //
    public ArrayList<Move> getCaptures(Position start)
    {
        ArrayList<Move> base = new ArrayList<>();
        Piece piece = getPiece(start);
        int color = piece.getColor();
        boolean isKing = piece.isKing();

        // add jumps in each direction
        Position[] directions = getNeighbors(color, isKing);
        for (Position dir : directions) {
            Position target = start.plus(dir);
            Position dest = target.plus(dir);
            Piece targetPiece = getPiece(target);
            Piece destPiece = getPiece(dest);

            // look for a valid landing space with an opposing piece in-between
            if (isGameSquare(dest) && destPiece == null &&
                    targetPiece != null &&
                    targetPiece.getColor() != color)
            {
                Move newMove = new Move(start);
                newMove.add(dest);
                base.add(newMove);
            }
        }

        // find longest for each jump choice
        return getCaptures(start, base);
    }

    //
    public ArrayList<Move> getCaptures(Position start, ArrayList<Move> expand) {
        ArrayList<Move> finalCaptures = new ArrayList<>();
        ArrayList<Move> furtherCaptures = new ArrayList<>();

        Piece piece = getPiece(start);
        int color = piece.getColor();
        boolean isKing = piece.isKing();

        // create longer moves from existing ones
        for (Move move : expand) {
            Position[] directions = getNeighbors(color, isKing || move.kings);
            Position current = move.end();
            boolean continues = false;
            for (Position dir : directions)
            {
                Position target = current.plus(dir);
                Position dest = target.plus(dir);
                Piece targetPiece = getPiece(target);
                Piece destPiece = getPiece(dest);

                // look for a valid landing space with an opposing piece in-between
                if (isGameSquare(dest) && destPiece == null &&
                        targetPiece != null &&
                        targetPiece.getColor() != color) {
                    // check that the 'opposing piece' hasn't been captured in this move sequence yet
                    boolean valid = true;
                    for (Position captured : move.captures) {
                        if (captured.equals(target)) {
                            valid = false;
                            break;
                        }
                    }
                    // valid piece to capture
                    if (valid) {
                        Move newMove = new Move(move);
                        newMove.add(dest);
                        furtherCaptures.add(newMove);
                        continues = true;
                    }
                }
            }

            // only add this move if there are no longer alternatives
            if (!continues) {
                finalCaptures.add(move);
            }
        }

        if (furtherCaptures.size() > 0) {
            furtherCaptures = getCaptures(start, furtherCaptures);
        }
        finalCaptures.addAll(furtherCaptures);

        return finalCaptures;
    }

    // get a set of possible moves from a place on the board
    public ArrayList<Move> getMoves(Position start) {
        Piece piece = getPiece(start);

        ArrayList<Move> immediateMoves = new ArrayList<>();

        // check neighboring positions
        Position[] neighbors = getNeighbors(piece.getColor(), piece.isKing());
        for (Position pos : neighbors) {
            Position dest = start.plus(pos);
            Piece destPiece = getPiece(dest);

            if (isGameSquare(dest) && destPiece == null) {
                Move newMove = new Move(start);
                newMove.add(dest);
                immediateMoves.add(newMove);
            }
        }

        // check for captures
        ArrayList<Move> captures = getCaptures(start);

        immediateMoves.addAll(captures);

//        if (captures.size() > 0) {
//            return captures;
//        } else {
        return immediateMoves;
        //}
    }

    // carry out a move sequence
    public void makeMove(Move move) {
        Position start = move.start();
        Position end = move.end();
        Piece piece = getPiece(start);
        int otherColor = (piece.getColor() == CheckersGame.RED) ? CheckersGame.BLACK : CheckersGame.RED;
        // clear visited positions
        for (Position pos : move.positions) {
            board[pos.x][pos.y] = null;
        }
        // clear captured positions and decrease piece count
        for (Position cap : move.captures) {
            board[cap.x][cap.y] = null;
            numPieces[otherColor - 1]--;
        }
        // place at end position
        board[end.x][end.y] = piece;
        // check if piece was kinged
        if (move.kings) {
            piece.makeKing();
        }
    }
}
