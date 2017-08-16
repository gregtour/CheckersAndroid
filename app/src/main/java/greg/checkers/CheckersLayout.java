package greg.checkers;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.util.DisplayMetrics;

import greg.checkers.game.CheckersGame;
import greg.checkers.game.Board;
import greg.checkers.game.Piece;
import greg.checkers.game.Position;

public class CheckersLayout extends TableLayout {

    public class CheckerImageView extends AppCompatImageView {
        public int x;
        public int y;
        public CheckerImageView(Activity activity) {
            super(activity);
        }
    }

    protected MyCheckersActivity myActivity;
    protected CheckersGame myGame;
    protected CheckerImageView cells[][];

    private final OnClickListener CellClick = new OnClickListener() {
        @Override
        public void onClick(View _view) {
            CheckerImageView view = (CheckerImageView)_view;
            myActivity.onClick(view.x, view.y);
        }
    };

    public void refresh() {
        Board myBoard = myGame.getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++)
                if (myBoard.isGameSquare(x, y)) {
                    CheckerImageView cell = cells[x][y];
                    Piece piece = myBoard.getPiece(x, y);
                    if (piece != null) {
                        int color = piece.getColor();
                        boolean king = piece.isKing();
                        // set the correct image
                        if (color == CheckersGame.RED) {
                            if (king) {
                                cell.setImageResource(R.drawable.redking);
                            } else {
                                cell.setImageResource(R.drawable.red);
                            }
                        } else if (color == CheckersGame.BLACK) {
                            if (king) {
                                cell.setImageResource(R.drawable.blackking);
                            } else {
                                cell.setImageResource(R.drawable.black);
                            }
                        }
                        // set the background color
                        if (myActivity.isSelected(piece)) {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellSelect));
                        } else {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellBlack));
                        }
                    } else {
                        // clear the image
                        cell.setImageDrawable(null);
                        Position curPos = new Position(x, y);
                        if (myActivity.isOption(curPos) /* && highlightsEnabled */) {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellOption));
                        } else {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellBlack));
                        }
                    }
                }
        }
    }

    public CheckersLayout(CheckersGame game, MyCheckersActivity activity) {
        super(activity);
        myActivity = activity;

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dimension = displayMetrics.widthPixels;
        if (displayMetrics.heightPixels < dimension) {
            dimension = displayMetrics.heightPixels;
        }
        int cellDimension = ((dimension - 2) / 8) - 2;

        LayoutParams params;

        myGame = game;
        Board myBoard = myGame.getBoard();

        params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(1, 1, 1, 1);
        setLayoutParams(params);
        setBackgroundColor(Color.rgb(48, 48, 48));

        // add table of image views
        cells = new CheckerImageView[8][8];
        for (int y = 0; y < 8; y++) {
            TableRow row = new TableRow(activity);
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tableParams.setMargins(0, 0, 0, 0);
            row.setLayoutParams(tableParams);

            for (int x = 0; x < 8; x++) {
                CheckerImageView cell;
                cells[x][y] = cell = new CheckerImageView(activity);
                cell.x = x;
                cell.y = y;

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                rowParams.setMargins(1, 1, 1, 1);
                rowParams.width = cellDimension;
                rowParams.height = cellDimension;
                cell.setLayoutParams(rowParams);

                int bgColor;
                if (myBoard.isGameSquare(x,y)) {
                    // add click handler
                    cell.setOnClickListener(CellClick);
                    bgColor = getResources().getColor(R.color.cellBlack);
                }
                else {
                    bgColor = getResources().getColor(R.color.cellRed);
                }

                cell.setBackgroundColor(bgColor);
                row.addView(cell);
            }
            addView(row);
        }
    }
}
