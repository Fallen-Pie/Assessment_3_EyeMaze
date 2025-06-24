package nz.ac.ara.bd.eyemaze.model;

import java.util.*;

public class Level {
    public Square[][] gridLevel;
    List<Position> goalList = new ArrayList<>();
    Position currentGoalSquare;
    int goalCompletedCount = 0;
    Eyeball currentEyeball;

    public Level(int height, int width) {
        gridLevel = new Square[height][width];
    }

    public void setGoal(Position currentPosition) {
        goalList.add(currentPosition);
    }

    public void changeAwayFromGoal() {
        gridLevel[currentGoalSquare.row][currentGoalSquare.column] = new BlankSquare();
    }

    public void completeGoal(Position currentPosition) {
        currentGoalSquare = currentPosition;
        goalCompletedCount++;
    }
}
