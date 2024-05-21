import java.awt.*;

class MyRectangle extends Rectangle {
    private boolean isSquare;

    public MyRectangle(int x, int y, int width, int height, boolean isSquare) {
        super(x, y, width, height);
        this.isSquare = isSquare;
    }

    public boolean isSquare() {
        return isSquare;
    }
}
