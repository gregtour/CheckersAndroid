package greg.checkers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import greg.checkers.game.Board;
import greg.checkers.game.CheckersGame;
import greg.checkers.game.Move;
import greg.checkers.game.Piece;
import greg.checkers.game.Position;

import static android.content.ContentValues.TAG;

public class MyCheckersActivity extends Activity {
    private CheckersGame gamelogic;
    private CheckersLayout checkersView;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        createGameBoard();
    }

    private void createGameBoard()
    {
        gamelogic = new CheckersGame();

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        TextView topText = new TextView(this);
        topText.setText("Play Checkers");

        statusText = new TextView(this);
        statusText.setText("status");

        checkersView = new CheckersLayout(gamelogic, this);
        checkersView.refresh();

        rootLayout.addView(topText);
        rootLayout.addView(checkersView);
        rootLayout.addView(statusText);

        setContentView(rootLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        prepTurn();
    }

    Piece selectedPiece;
    Position selectedPosition;
    Piece selectablePieces[];
    Position moveOptions[];

    Handler turnHandler = new Handler();
    Runnable computerTurn = new Runnable() {
        @Override
        public void run() {
            makeComputerTurn();
        }
    };

    // prepare a human or computer turn
    private void prepTurn() {
        Board board = gamelogic.getBoard();

        selectedPiece = null;
        selectedPosition = null;
        selectablePieces = null;
        moveOptions = null;

        int turn = gamelogic.whoseTurn();

        if (turn == CheckersGame.RED) {
            statusText.setText("Red's (computer's) turn.");
            // set timeout for computer turn
            turnHandler.removeCallbacks(computerTurn);
            turnHandler.postDelayed(computerTurn, 800);

        } else if (turn == CheckersGame.BLACK) {
            statusText.setText("Black's (player's) turn.");

            // prep for human player turn
            ArrayList<Piece> selectablePieces = new ArrayList<>();
            Move moves[] = gamelogic.getMoves();

            // find pieces which can be moved
            for (Move move : moves) {
                Piece newPiece = board.getPiece(move.start());
                if (!selectablePieces.contains(newPiece)) {
                    selectablePieces.add(newPiece);
                }
            }

            // convert to array
            this.selectablePieces = selectablePieces.toArray(
                    new Piece[selectablePieces.size()]
            );

            if (selectablePieces.size() == 0) {
                statusText.setText("You lost!");
            }
        }

        checkersView.refresh();
    }

    // difficulty easy: randomly pick a move
    private void makeComputerTurn() {
        if (gamelogic.whoseTurn() == CheckersGame.RED) {
            Move moves[] = gamelogic.getMoves();
            if (moves.length > 0) {
                int choice = (int)(moves.length * Math.random());
                Move move = moves[choice];
                gamelogic.makeMove(move);
                prepTurn();
            } else {
                // player wins
                statusText.setText("You won!");
            }
        }
    }

    // check which piece is selected
    public boolean isSelected(Piece piece) {
        return (piece != null && piece == selectedPiece);
    }

    // check which squares are options
    public boolean isOption(Position checkPosition) {
        if (moveOptions == null) {
            return false;
        }
        for (Position position : moveOptions) {
            if (position.equals(checkPosition)) {
                return true;
            }
        }
        return false;
    }

    public void selectPiece(Piece piece, Position location)
    {
        selectedPiece = null;
        selectedPosition = null;
        moveOptions = null;

        if (piece != null && selectablePieces != null
                && piece.getColor() == gamelogic.whoseTurn())
        {
            boolean isSelectable = false;
            for (Piece selectablePiece : selectablePieces) {
                if (selectablePiece == piece) {
                    isSelectable = true;
                }
            }

            if (isSelectable) {
                selectedPiece = piece;
                selectedPosition = location;

                // fill move options

                ArrayList<Position> moveOptionsArr = new ArrayList<>();

                Move allMoves[] = gamelogic.getMoves();

                // iterate through moves
                for (Move checkMove : allMoves) {
                    Position start = checkMove.start();
                    Position end = checkMove.end();

                    if (start.equals(location)) {
                        if (!moveOptionsArr.contains(end)) {
                            moveOptionsArr.add(end);
                        }
                    }
                }

                // save list results
                moveOptions = moveOptionsArr.toArray(new Position[moveOptionsArr.size()]);
            }
        }

        checkersView.refresh();
    }

    // player made a move
    public void makeMove(Position destination)
    {
        // make longest move available
        Move move = gamelogic.getLongestMove(selectedPosition, destination);
        if (move != null) {
            gamelogic.makeMove(move);
            prepTurn();
        }
    }

    // player makes a click
    public void onClick(int x, int y) {
        // check if its player's turn
        if (gamelogic.whoseTurn() != CheckersGame.BLACK) {
            return;
        }

        Position location = new Position(x, y);
        Piece targetPiece = gamelogic.getBoard().getPiece(x, y);

        // attempting to make a move
        if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
            makeMove(location);
        }
        else
        {
            selectPiece(targetPiece, location);
        }
    }
}
