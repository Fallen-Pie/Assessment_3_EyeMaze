package nz.ac.ara.bd.eyemaze.view;

import static java.lang.Integer.parseInt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import nz.ac.ara.bd.eyemaze.R;
import nz.ac.ara.bd.eyemaze.model.BlankSquare;
import nz.ac.ara.bd.eyemaze.model.Color;
import nz.ac.ara.bd.eyemaze.model.Direction;
import nz.ac.ara.bd.eyemaze.model.Game;
import nz.ac.ara.bd.eyemaze.model.Message;
import nz.ac.ara.bd.eyemaze.model.PlayableSquare;
import nz.ac.ara.bd.eyemaze.model.Shape;

public class GameActivity extends AppCompatActivity {
    private static final String LOG_TAG = GameActivity.class.getSimpleName();
    Game game = new Game();
    boolean movingPlayer = false;
    ImageButton previousButton;
    boolean previousGoal = false;
    int goalTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LinearLayout ll_vert = findViewById(R.id.LinearLayout);
        ll_vert.setOrientation(LinearLayout.VERTICAL);
        setGameState();
        for (int row = 0; row < game.getLevelHeight(); row++) {
            LinearLayout ll_row = new LinearLayout(this);
            ll_row.setOrientation(LinearLayout.HORIZONTAL);
            ll_row.setGravity(Gravity.CENTER_HORIZONTAL);
            ll_vert.addView(ll_row);
            for (int column = 0; column < game.getLevelWidth(); column++) {
                ImageButton new_button = new ImageButton(this);
                setColourShape(new_button, row, column);
                new_button.setTag(row + "_" + column);
                //new_button.getLayoutParams().width = (552/game.getLevelWidth());
                if (game.hasGoalAt(row, column)) {
                    new_button.setColorFilter(R.color.green);
                } else if ((game.getEyeballColumn() == column && game.getEyeballRow() == row)) {
                    new_button.setColorFilter(R.color.orange);
                }
                new_button.setOnClickListener(newOnClickListener);
                ll_row.addView(new_button);
            }
        }
    }



    private void setGameState() {
        Bundle data = getIntent().getExtras();
        assert data != null;
        createGame(data.getInt("LEVEL"));
        goalTotal = game.getGoalCount();
        setGoalText();
    }

    private void setColourShape(ImageButton newButton, int row, int column) {
        Log.d(LOG_TAG, row + " " + column + " " + game.getShapeAt(row, column) + " " + game.getColorAt(row, column));
        switch(game.getShapeAt(row, column)) {
            case DIAMOND:
                newButton.setImageResource(R.drawable.diamond);
                break;
            case CROSS:
                newButton.setImageResource(R.drawable.cross);
                break;
            case STAR:
                newButton.setImageResource(R.drawable.star);
                break;
            case FLOWER:
                newButton.setImageResource(R.drawable.flower);
                break;
            case LIGHTNING:
                newButton.setImageResource(R.drawable.lightning);
                break;
        }
        newButton.setBackgroundColor(0);
        /*switch(game.getColorAt(row, column)) {
            case BLUE:
                newButton.getDrawable().setColorFilter(getResources().getColor(R.color.blue));
                Log.d(LOG_TAG, "Set Blue");
                break;
            case RED:
                newButton.setColorFilter(R.color.red);
                Log.d(LOG_TAG, "Set Red");
                break;
            case YELLOW:
                newButton.setColorFilter(R.color.yellow);
                Log.d(LOG_TAG, "Set Yellow");
                break;
            case GREEN:
                newButton.setColorFilter(R.color.green);
                Log.d(LOG_TAG, "Set Green");
                break;
            case PURPLE:
                newButton.setColorFilter(R.color.purple_700);
                Log.d(LOG_TAG, "Set Purple");
                break;
        }*/
    }

    View.OnClickListener newOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String[] temp = ((String) view.getTag()).split("_");
            int newRow = parseInt(temp[0]);
            int newColumn = parseInt(temp[1]);
            if (((game.getEyeballColumn() == newColumn && game.getEyeballRow() == newRow)) && !movingPlayer) {
                movingPlayer = true;
                previousButton = (ImageButton) view;
                previousButton.setColorFilter(R.color.highlight, PorterDuff.Mode.DARKEN);
            } else if (!((game.getEyeballColumn() == newColumn && game.getEyeballRow() == newRow)) && movingPlayer) {
                if (!game.isDirectionOK(newRow, newColumn)) {
                    setMessage(game.checkDirectionMessage(newRow, newColumn));
                } else if (!game.hasBlankFreePathTo(newRow, newColumn)) {
                    setMessage(game.checkMessageForBlankOnPathTo(newRow, newColumn));
                } else if (!game.canMoveToSquareColor(newRow, newColumn)) {
                    setMessage(game.messageIfMovingTo(newRow, newColumn));
                } else {
                    game.moveTo(newRow,newColumn);
                    if (!hasLegalMoves()) {
                        onLoseDialog();
                    } else {
                        processesMove(newRow, newColumn);
                    }
                }
            }
        }
    };

    private boolean hasLegalMoves() {
        for (int row = 0; row < game.getLevelHeight(); row++) {
            for (int column = 0; column < game.getLevelWidth(); column++) {
                if (!(game.getEyeballColumn() == column && game.getEyeballRow() == row)) {
                    if (game.canMoveTo(row, column) && game.hasBlankFreePathTo(row, column)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void processesMove(int newRow, int newColumn) {
        if (game.hasGoalAt(newRow, newColumn) ) {
            previousGoal = true;
        } else if (previousGoal) {
            previousButton.setColorFilter(getColor(R.color.black));
            previousGoal = false;
        }
        if (game.getCompletedGoalCount() == goalTotal) {
            onWinDialog();
        }
        setGoalText();
        setMessage(Message.OK);
        movingPlayer = false;
        previousButton.setColorFilter(getColor(R.color.purple_700));
    }

    public void onWinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage(R.string.dialogWinGame)
                .setPositiveButton(R.string.continueGame, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Bundle data = getIntent().getExtras();
                        assert data != null;
                        int levelNumber = data.getInt("LEVEL");
                        getIntent().putExtra("LEVEL", levelNumber + 1);
                        recreate();
                    }
                })
                .setNegativeButton(R.string.restartGame, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        recreate();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void onLoseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage(R.string.dialogLoseGame)
                .setNegativeButton(R.string.restartGame, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        recreate();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setGoalText() {
        TextView goalBox = findViewById(R.id.goalText);
        goalBox.setText(getString(R.string.goalMessage, game.getCompletedGoalCount(), goalTotal));
    }
    private void setMessage(Message message) {
        TextView messageBox = findViewById(R.id.messageText);
        switch(message) {
            case OK:
                messageBox.setText(R.string.goodMove);
                break;
            case DIFFERENT_SHAPE_OR_COLOR:
                messageBox.setText(R.string.differentShapeColour);
                break;
            case BACKWARDS_MOVE:
                messageBox.setText(R.string.backwardsMove);
                break;
            case MOVING_OVER_BLANK:
                messageBox.setText(R.string.moveOverBlank);
                break;
            case MOVING_DIAGONALLY:
                messageBox.setText(R.string.moveDiagonally);
                break;
            default:
                messageBox.setText("");
                break;
        }
    }

    protected void createGame(int levelNumber) {
        TextView levelBox = findViewById(R.id.levelText);
        switch(levelNumber) {
            case 2:
                levelBox.setText(getString(R.string.levelMessage, 2));
                levelTwo(game);
                break;
            case 3:
                levelBox.setText(getString(R.string.levelMessage, 3));
                levelThree(game);
                break;
            default:
                levelBox.setText(getString(R.string.levelMessage, 1));
                levelOne(game);
                break;
        }

    }

    private static void levelOne(Game game) {
        game.addLevel(4, 3);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.STAR), 0, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 0, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.DIAMOND), 0, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.CROSS), 1, 0);
        game.addSquare(new PlayableSquare(Color.GREEN, Shape.STAR), 1, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.CROSS), 1, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.LIGHTNING), 2, 0);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.CROSS), 2, 1);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.LIGHTNING), 2, 2);
        game.addSquare(new BlankSquare(), 3, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 3, 1);
        game.addSquare(new BlankSquare(), 3, 2);

        game.addGoal(0, 2);
        game.addGoal(2, 0);

        game.addEyeball(3, 1, Direction.UP);
    }

    private static void levelTwo(Game game) {
        game.addLevel(4, 3);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.STAR), 0, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 0, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.DIAMOND), 0, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.CROSS), 1, 0);
        game.addSquare(new PlayableSquare(Color.GREEN, Shape.STAR), 1, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.CROSS), 1, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.LIGHTNING), 2, 0);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.CROSS), 2, 1);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.LIGHTNING), 2, 2);
        game.addSquare(new BlankSquare(), 3, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 3, 1);
        game.addSquare(new BlankSquare(), 3, 2);

        game.addGoal(0, 2);
        game.addGoal(2, 0);

        game.addEyeball(3, 1, Direction.UP);
    }

    private static void levelThree(Game game) {
        game.addLevel(4, 3);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.STAR), 0, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 0, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.DIAMOND), 0, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.CROSS), 1, 0);
        game.addSquare(new PlayableSquare(Color.GREEN, Shape.STAR), 1, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.CROSS), 1, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.LIGHTNING), 2, 0);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.CROSS), 2, 1);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.LIGHTNING), 2, 2);
        game.addSquare(new BlankSquare(), 3, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 3, 1);
        game.addSquare(new BlankSquare(), 3, 2);

        game.addGoal(0, 2);
        game.addGoal(2, 0);

        game.addEyeball(3, 1, Direction.UP);
    }
}