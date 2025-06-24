package nz.ac.ara.bd.eyemaze.view;

import static java.lang.Integer.parseInt;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    Button previousButton;

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
        createGame();
        setGoalText();
        for (int row = 0; row < game.getLevelHeight(); row++) {
            LinearLayout ll_row = new LinearLayout(this);
            ll_row.setOrientation(LinearLayout.HORIZONTAL);
            ll_row.setGravity(Gravity.CENTER_HORIZONTAL);
            ll_vert.addView(ll_row);
            for (int column = 0; column < game.getLevelWidth(); column++) {
                Button new_button = new Button(this);
                new_button.setText(game.getColorAt(row, column) + " " + game.getShapeAt(row, column));
                new_button.setTag(row + "_" + column);
                new_button.setWidth(ll_vert.getWidth()/game.getLevelWidth());
                if (game.hasGoalAt(row, column)) {
                    new_button.setBackgroundColor(getColor(R.color.green));
                } else if ((game.getEyeballColumn() == column && game.getEyeballRow() == row)) {
                    new_button.setBackgroundColor(getColor(R.color.orange));
                }
                ll_row.addView(new_button);
                new_button.setOnClickListener(newOnClickListener);
            }
        }
    }

    View.OnClickListener newOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String[] temp = ((String)view.getTag()).split("_");
            int row = parseInt(temp[0]);
            int column = parseInt(temp[1]);
            if (((game.getEyeballColumn() == column && game.getEyeballRow() == row)) && !movingPlayer) {
                movingPlayer = true;
                previousButton = (Button) view;
                previousButton.setBackgroundColor(getColor(R.color.highlight));
            } else if (!((game.getEyeballColumn() == column && game.getEyeballRow() == row)) && movingPlayer) {
                if (!game.isDirectionOK(row, column)) {
                    setMessage(game.checkDirectionMessage(row, column));
                } else if (!game.hasBlankFreePathTo(row, column)) {
                    setMessage(game.checkMessageForBlankOnPathTo(row, column));
                } else if (!game.canMoveToSquareColor(row, column)) {
                    setMessage(game.messageIfMovingTo(row, column));
                } else {
                    if (game.hasGoalAt(row, column)) {
                        Log.d(LOG_TAG, "Why not firing!");
                    }
                    game.moveTo(row,column);
                    setGoalText();
                    setMessage(Message.OK);
                    movingPlayer = false;
                    previousButton.setBackgroundColor(getColor(R.color.purple_700));
                    Log.d(LOG_TAG, "Hello!");
                }

            }
        }
    };

    private void setGoalText() {
        TextView goalBox = findViewById(R.id.goalText);
        goalBox.setText(getString(R.string.goalMessage, game.getCompletedGoalCount(), game.getGoalCount()));
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
        }
    }

    protected void createGame() {
        levelOne(game);
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
}