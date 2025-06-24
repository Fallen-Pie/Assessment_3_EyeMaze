package nz.ac.ara.bd.eyemaze.model;

public class Position {
    int row, column;

    public Position(int newRow, int newColumn) {
        row = newRow;
        column = newColumn;
    }

    public int getRow() {
        return row;
    }
    public int getColumn() {
        return column;
    }
}
