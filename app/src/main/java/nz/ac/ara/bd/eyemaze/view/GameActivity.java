package nz.ac.ara.bd.eyemaze.view;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import nz.ac.ara.bd.eyemaze.R;
import nz.ac.ara.bd.eyemaze.model.BlankSquare;
import nz.ac.ara.bd.eyemaze.model.Color;
import nz.ac.ara.bd.eyemaze.model.Direction;
import nz.ac.ara.bd.eyemaze.model.Game;
import nz.ac.ara.bd.eyemaze.model.Message;
import nz.ac.ara.bd.eyemaze.model.PlayableSquare;
import nz.ac.ara.bd.eyemaze.model.Position;
import nz.ac.ara.bd.eyemaze.model.Shape;

public class GameActivity extends AppCompatActivity {
    private static final String LOG_TAG = GameActivity.class.getSimpleName();
    Game game = new Game();
    MediaPlayer mediaPlayer;
    AudioManager audioControl;
    boolean muted = false;
    boolean movingPlayer = false;
    ImageButton previousButton;
    Position previousLocation;
    Position previousGoal;
    int goalTotal;
    int totalMoves;

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
        LinearLayout verticalSquares = findViewById(R.id.LinearLayout);
        verticalSquares.setOrientation(LinearLayout.VERTICAL);
        setGameState();
        for (int row = 0; row < game.getLevelHeight(); row++) {
            LinearLayout horizontalSquares = new LinearLayout(this);
            horizontalSquares.setOrientation(LinearLayout.HORIZONTAL);
            horizontalSquares.setGravity(Gravity.CENTER_HORIZONTAL);
            verticalSquares.addView(horizontalSquares);
            for (int column = 0; column < game.getLevelWidth(); column++) {
                ImageButton new_button = new ImageButton(this);
                new_button.setTag(row + "_" + column);
                horizontalSquares.addView(new_button);
                setColourShape(new_button, row, column, game.getEyeballColumn() == column && game.getEyeballRow() == row);
                new_button.setOnClickListener(gameClickListener);
            }
        }
        findViewById(R.id.resetButton).setOnClickListener(resetClickListener);
        findViewById(R.id.pauseButton).setOnClickListener(pauseClickListener);
        findViewById(R.id.muteButton).setOnClickListener(muteClickListener);
        audioControl = (AudioManager)getSystemService(AUDIO_SERVICE);
        audioControl.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND);
    }

    View.OnClickListener resetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            recreate();
        }
    };

    View.OnClickListener muteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MaterialButton thisButton = (MaterialButton) view;
            audioControl.adjustVolume(AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_PLAY_SOUND);
            if (!muted) {
                thisButton.setIconResource(R.drawable.baseline_volume_off_24);
                muted = true;
            } else {
                thisButton.setIconResource(R.drawable.baseline_volume_up_24);
                muted = false;
            }
        }
    };


    View.OnClickListener pauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage(R.string.pauseGame)
                    .setNegativeButton(R.string.resumeGame, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    };

    private void setGameState() {
        Bundle data = getIntent().getExtras();
        assert data != null;
        createGame(data.getInt("LEVEL"));
        goalTotal = game.getGoalCount();
        setText();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setColourShape(ImageButton newButton, int row, int column ,boolean player) {
        int white = getColor(R.color.white);
        Drawable shape = switch (game.getShapeAt(row, column)) {
            case DIAMOND -> getDrawable(R.drawable.diamond);
            case CROSS -> getDrawable(R.drawable.cross);
            case STAR -> getDrawable(R.drawable.star);
            case FLOWER -> getDrawable(R.drawable.flower);
            case LIGHTNING -> getDrawable(R.drawable.lightning);
            default -> getDrawable(R.drawable.blank);
        };
        int colour = switch (game.getColorAt(row, column)) {
            case BLUE -> getColor(R.color.blue);
            case RED -> getColor(R.color.red);
            case YELLOW -> getColor(R.color.yellow);
            case GREEN -> getColor(R.color.green);
            case PURPLE -> getColor(R.color.purple_700);
            default -> white;
        };
        assert shape != null;
        shape.setColorFilter(colour, PorterDuff.Mode.MULTIPLY);
        List<Drawable> drawableList = new ArrayList<>();
        drawableList.add(shape);
        if (player) drawableList.add(playerCharacter());
        else if (game.hasGoalAt(row, column)) drawableList.add(getDrawable(R.drawable.goal));
        LayerDrawable finalDrawable = new LayerDrawable(drawableList.toArray(new Drawable[0]));
        Bitmap bitmap = Bitmap.createBitmap(finalDrawable.getIntrinsicWidth(), finalDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        finalDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        finalDrawable.draw(canvas);
        newButton.setImageBitmap(bitmap);
        newButton.setBackgroundColor(white);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable playerCharacter() {
        int rotation = switch (game.getEyeballDirection()) {
            case UP -> 0;
            case DOWN -> 180;
            case LEFT -> 270;
            case RIGHT -> 90;
        };
        Bitmap playerBitmapInitial = BitmapFactory.decodeResource(this.getResources(), R.drawable.player);
        Bitmap playerBitmap = Bitmap.createBitmap(playerBitmapInitial.getWidth(), playerBitmapInitial.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas rotationCanvas = new Canvas(playerBitmap);
        rotationCanvas.rotate(rotation, (float) playerBitmapInitial.getWidth()/2, (float) playerBitmapInitial.getHeight()/2);
        rotationCanvas.drawBitmap(playerBitmapInitial, 0, 0, null);
        Drawable playerBitmapFinal = new BitmapDrawable(getResources(), playerBitmap);
        if (!movingPlayer) playerBitmapFinal.setColorFilter(getColor(R.color.orange), PorterDuff.Mode.MULTIPLY);
        else playerBitmapFinal.setColorFilter(getColor(R.color.highlight), PorterDuff.Mode.MULTIPLY);
        return playerBitmapFinal;
    }

    View.OnClickListener gameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String[] temp = ((String) view.getTag()).split("_");
            int newRow = parseInt(temp[0]);
            int newColumn = parseInt(temp[1]);
            if (((game.getEyeballColumn() == newColumn && game.getEyeballRow() == newRow)) && !movingPlayer) {
                movingPlayer = true;
                previousButton = (ImageButton) view;
                previousLocation = new Position(newRow, newColumn);
                setColourShape(previousButton, previousLocation.getRow(), previousLocation.getColumn() ,true);
            } else if (!((game.getEyeballColumn() == newColumn && game.getEyeballRow() == newRow)) && movingPlayer) {
                if (!game.isDirectionOK(newRow, newColumn)) {
                    playAudio(R.raw.error);
                    setMessage(game.checkDirectionMessage(newRow, newColumn));
                } else if (!game.hasBlankFreePathTo(newRow, newColumn)) {
                    playAudio(R.raw.error);
                    setMessage(game.checkMessageForBlankOnPathTo(newRow, newColumn));
                } else if (!game.canMoveToSquareColor(newRow, newColumn)) {
                    playAudio(R.raw.error);
                    setMessage(game.messageIfMovingTo(newRow, newColumn));
                } else {
                    setColourShape(previousButton, previousLocation.getRow(), previousLocation.getColumn() ,false);
                    processesMove(newRow, newColumn);
                    setColourShape((ImageButton) view, newRow, newColumn ,true);
                    if (!hasLegalMoves() && game.getCompletedGoalCount() != goalTotal) {
                        onLoseDialog();
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
        boolean movedToGoal = false;
        if (game.hasGoalAt(newRow, newColumn) ) {
            previousGoal = new Position(newRow, newColumn);
            movedToGoal = true;
        }
        game.moveTo(newRow,newColumn);
        if (previousGoal != null && !movedToGoal) {
            setColourShape(previousButton, previousGoal.getRow(), previousGoal.getColumn(), false);
            previousGoal = null;
        }
        if (game.getCompletedGoalCount() == goalTotal) {
            playAudio(R.raw.success);
            onWinDialog();
        }
        totalMoves += 1;
        setText();
        setMessage(Message.OK);
        movingPlayer = false;
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

    private void playAudio(int audioTrack) {
        mediaPlayer = MediaPlayer.create(this, audioTrack);
        mediaPlayer.start();
    }

    private void setText() {
        TextView goalBox = findViewById(R.id.goalText);
        goalBox.setText(getString(R.string.goalMessage, game.getCompletedGoalCount(), goalTotal));
        TextView moveBox = findViewById(R.id.moveText);
        moveBox.setText(getString(R.string.moveMessage, totalMoves));
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
        levelNumber = ((levelNumber - 1) % 3) + 1;
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
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.DIAMOND), 0, 0);
        game.addSquare(new PlayableSquare(Color.RED, Shape.FLOWER), 0, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.DIAMOND), 0, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.DIAMOND), 1, 0);
        game.addSquare(new BlankSquare(), 1, 1);
        game.addSquare(new PlayableSquare(Color.BLUE, Shape.CROSS), 1, 2);
        game.addSquare(new PlayableSquare(Color.RED, Shape.LIGHTNING), 2, 0);
        game.addSquare(new PlayableSquare(Color.RED, Shape.DIAMOND), 2, 1);
        game.addSquare(new PlayableSquare(Color.YELLOW, Shape.LIGHTNING), 2, 2);
        game.addSquare(new BlankSquare(), 3, 0);
        game.addSquare(new PlayableSquare(Color.PURPLE, Shape.DIAMOND), 3, 1);
        game.addSquare(new BlankSquare(), 3, 2);

        game.addGoal(2, 0);
        game.addGoal(2, 2);

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

