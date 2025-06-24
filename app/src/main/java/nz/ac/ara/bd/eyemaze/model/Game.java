package nz.ac.ara.bd.eyemaze.model;

import java.util.*;

public class Game implements IGoalHolder, ILevelHolder, IMoving, ISquareHolder, IEyeballHolder {
    List<Level> levelList = new ArrayList<>();
    int currentLevel = 0;

    @Override
    public void addGoal(int row, int column) {
        try {
            Square goalSquare = levelList.get(currentLevel).gridLevel[row][column];
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        finally {
            levelList.get(currentLevel).setGoal(new Position(row, column));
        }
    }

    @Override
    public int getGoalCount() {
        return levelList.get(currentLevel).goalList.size();
    }

    @Override
    public boolean hasGoalAt(int targetRow, int targetColumn) {
        for (Position goal : levelList.get(currentLevel).goalList) {
            if (goal.row == targetRow && goal.column == targetColumn) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getCompletedGoalCount() {
        return levelList.get(currentLevel).goalCompletedCount;
    }

    @Override
    public void addLevel(int height, int width) {
        levelList.add(new Level(height, width));
        setLevel(getLevelCount() - 1);
    }

    @Override
    public int getLevelWidth() {
        return levelList.get(currentLevel).gridLevel[0].length;
    }

    @Override
    public int getLevelHeight() {
        return levelList.get(currentLevel).gridLevel.length;
    }

    @Override
    public void setLevel(int levelNumber) {
        try {
            levelList.get(levelNumber);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        finally {
            currentLevel = levelNumber;
        }
    }

    @Override
    public int getLevelCount() {
        return levelList.size();
    }

    @Override
    public boolean canMoveTo(int destinationRow, int destinationColumn) {
        Position currentPosition = levelList.get(currentLevel).currentEyeball.position;
        if (currentPosition.row == destinationRow || currentPosition.column == destinationColumn) {
            return canMoveToSquareColor(destinationRow, destinationColumn) && isDirectionOK(destinationRow, destinationColumn);
        }
        return false;
    }

    public boolean canMoveToSquareColor(int destinationRow, int destinationColumn) {
        Position newCurrentPosition = levelList.get(currentLevel).currentEyeball.position;
        Square newSquare = levelList.get(currentLevel).gridLevel[destinationRow][destinationColumn];
        Square oldSquare = levelList.get(currentLevel).gridLevel[newCurrentPosition.row][newCurrentPosition.column];
        return oldSquare.color == newSquare.color || oldSquare.shape == newSquare.shape;
    }

    @Override
    public Message messageIfMovingTo(int destinationRow, int destinationColumn) {
        if (!canMoveTo(destinationRow, destinationColumn)) {
            return Message.DIFFERENT_SHAPE_OR_COLOR;
        }
        return Message.OK;
    }

    @Override
    public boolean isDirectionOK(int destinationRow, int destinationColumn) {
        Direction currentDirection = levelList.get(currentLevel).currentEyeball.direction;
        Position currentPosition = levelList.get(currentLevel).currentEyeball.position;
        if (currentPosition.row != destinationRow && currentPosition.column != destinationColumn) {
            return false;
        }
        else if (currentPosition.row - destinationRow > 0 && currentDirection == Direction.DOWN) {
            return false;
        }
        else if (currentPosition.row - destinationRow < 0 && currentDirection == Direction.UP) {
            return false;
        }
        else if (currentPosition.column - destinationColumn > 0 && currentDirection == Direction.RIGHT) {
            return false;
        }
        else if (currentPosition.column - destinationColumn < 0 && currentDirection == Direction.LEFT) {
            return false;
        }
        return true;
    }

    @Override
    public Message checkDirectionMessage(int destinationRow, int destinationColumn) {
        Position currentPosition = levelList.get(currentLevel).currentEyeball.position;
        if ((currentPosition.row == destinationRow || currentPosition.column == destinationColumn) && !isDirectionOK(destinationRow, destinationColumn)) {
            return Message.BACKWARDS_MOVE;
        }
        if (!canMoveTo(destinationRow, destinationColumn)) {
            return Message.MOVING_DIAGONALLY;
        }
        return Message.OK;
    }

    @Override
    public boolean hasBlankFreePathTo(int destinationRow, int destinationColumn) {
        Position currentPosition = levelList.get(currentLevel).currentEyeball.position;
        Square[][] currentGrid = levelList.get(currentLevel).gridLevel;
        int startGrid, endGrid;
        if (destinationRow != currentPosition.row && destinationColumn == currentPosition.column) {
            startGrid = destinationRow;
            endGrid = currentPosition.row;
            if (startGrid > endGrid) {
                int temp = startGrid;
                startGrid = endGrid;
                endGrid = temp;
            }
            for (int gridNumber = startGrid; gridNumber < endGrid; gridNumber++) {
                if (currentGrid[gridNumber][destinationColumn] instanceof BlankSquare) {
                    return false;
                }
            }
        }
        else if (destinationColumn != currentPosition.column && destinationRow == currentPosition.row) {
            startGrid = destinationColumn;
            endGrid = currentPosition.column;
            if (startGrid > endGrid) {
                int temp = startGrid;
                startGrid = endGrid;
                endGrid = temp;
            }
            for (int gridNumber = startGrid; gridNumber < endGrid; gridNumber++) {
                if (currentGrid[destinationRow][gridNumber] instanceof BlankSquare) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Message checkMessageForBlankOnPathTo(int destinationRow, int destinationColumn) {
        if (!hasBlankFreePathTo(destinationRow, destinationColumn)) {
            return Message.MOVING_OVER_BLANK;
        }
        return Message.OK;
    }

    @Override
    public void moveTo(int destinationRow, int destinationColumn) {
        Position currentPosition = levelList.get(currentLevel).currentEyeball.position;
        Direction newDirection;
        if (currentPosition.row - destinationRow > 0) {
            newDirection = Direction.UP;
        }
        else if (currentPosition.row - destinationRow < 0) {
            newDirection = Direction.DOWN;
        }
        else if (currentPosition.column - destinationColumn > 0) {
            newDirection = Direction.LEFT;
        }
        else{
            newDirection = Direction.RIGHT;
        }
        if (levelList.get(currentLevel).currentGoalSquare != null) {
            levelList.get(currentLevel).changeAwayFromGoal();
        }
        levelList.get(currentLevel).currentEyeball.move(new Position(destinationRow, destinationColumn), newDirection);
        Iterator<Position> goalList = levelList.get(currentLevel).goalList.iterator();
        while (goalList.hasNext()) {
            Position goal = goalList.next();
            if (goal.row == destinationRow && goal.column == destinationColumn) {
                goalList.remove();
                levelList.get(currentLevel).completeGoal(goal);
            }
        }
    }

    @Override
    public void addSquare(Square square, int row, int column) {
        try {
            levelList.get(currentLevel).gridLevel[row][column] = square;
        }
        catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Color getColorAt(int row, int column) {
        return levelList.get(currentLevel).gridLevel[row][column].color;
    }

    @Override
    public Shape getShapeAt(int row, int column) {
        return levelList.get(currentLevel).gridLevel[row][column].shape;
    }

    @Override
    public void addEyeball(int row, int column, Direction direction) {
        try {
            Square eyeSquare = levelList.get(currentLevel).gridLevel[row][column];
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        finally {
            levelList.get(currentLevel).currentEyeball = new Eyeball(row, column, direction);
        }
    }

    @Override
    public int getEyeballRow() {
        return levelList.get(currentLevel).currentEyeball.position.row;
    }

    @Override
    public int getEyeballColumn() {
        return levelList.get(currentLevel).currentEyeball.position.column;
    }

    @Override
    public Direction getEyeballDirection() {
        return levelList.get(currentLevel).currentEyeball.direction;
    }
}
