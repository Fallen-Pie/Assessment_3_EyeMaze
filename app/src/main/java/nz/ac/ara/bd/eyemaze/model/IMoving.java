package nz.ac.ara.bd.eyemaze.model;

public interface IMoving {
    public boolean canMoveTo( int destinationRow, int destinationColumn);
    public Message messageIfMovingTo(int destinationRow, int destinationColumn);
    public boolean isDirectionOK(int destinationRow, int destinationColumn);
    public Message checkDirectionMessage(int destinationRow, int destinationColumn);
    public boolean hasBlankFreePathTo(int destinationRow, int destinationColumn);
    public Message checkMessageForBlankOnPathTo(int destinationRow, int destinationColumn);
    public void moveTo(int destinationRow, int destinationColumn);
}
