package nz.ac.ara.bd.eyemaze.model;

public class Eyeball {
    Position position;
    Direction direction;
    public Eyeball(int row, int column, Direction newDirection) {
        position = new Position(row, column);
        direction = newDirection;
    }
    public void move(Position newPosition, Direction newDirection) {
        position = newPosition;
        direction = newDirection;
    }
}
