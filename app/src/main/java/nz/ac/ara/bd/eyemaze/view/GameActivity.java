package nz.ac.ara.bd.eyemaze.view;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import kotlin.NotImplementedError;
import nz.ac.ara.bd.eyemaze.R;
import nz.ac.ara.bd.eyemaze.model.BlankSquare;
import nz.ac.ara.bd.eyemaze.model.Color;
import nz.ac.ara.bd.eyemaze.model.Game;
import nz.ac.ara.bd.eyemaze.model.Level;
import nz.ac.ara.bd.eyemaze.model.PlayableSquare;
import nz.ac.ara.bd.eyemaze.model.Shape;

public class GameActivity extends AppCompatActivity {
    private static final String LOG_TAG = GameActivity.class.getSimpleName();
    Game game = new Game();

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
        Level currentLevel = game.levelList.get(game.currentLevel);
        Log.d(LOG_TAG, "Hello!");
        for (int row = 0; row < game.getLevelHeight(); row++) {
            LinearLayout ll_row = new LinearLayout(this);
            ll_row.setOrientation(LinearLayout.HORIZONTAL);
            ll_row.setGravity(Gravity.CENTER_HORIZONTAL);
            ll_vert.addView(ll_row);
            for (int column = 0; column < game.getLevelWidth(); column++) {
                Button new_button = new Button(this);
                new_button.setText(game.getColorAt(row, column) + " " + game.getShapeAt(row, column));
                new_button.setWidth(ll_vert.getWidth()/game.getLevelWidth());
                if (game.hasGoalAt(row, column)) {
                    new_button.setBackgroundColor(getColor(R.color.green));;
                }
                ll_row.addView(new_button);
            }
        };
    }

    protected void createGame() {
        levelOne(game);
    }

    private static void levelOne(Game game) {
        game.addLevel(4, 3);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.STAR), 0, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 0, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.STAR), 0, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.CROSS), 1, 0);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.STAR), 1, 1);
        game.addSquare(new PlayableSquare(Color.GREEN, Shape.FLOWER), 1, 2);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.LIGHTNING), 2, 0);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.LIGHTNING), 2, 1);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.LIGHTNING), 2, 2);
        game.addSquare(new BlankSquare(), 3, 0);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 3, 1);
        game.addSquare(new BlankSquare(), 3, 2);

        game.addGoal(0, 2);
        game.addGoal(2, 0);
    }
}