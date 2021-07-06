import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Chanmoey
 */
public class ReplaceImageBackgroundColor {

    private int[][][] rgbMatrix;
    private int width;
    private int height;
    private final File file;
    private final int[][] d = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
    private int newColor = 0x00BFFF;
    private boolean[][] visited;
    private int[] backgroundColor;
    private BufferedImage bufferedImage;

    public ReplaceImageBackgroundColor(File file) {
        this.file = file;
    }

    public ReplaceImageBackgroundColor(File file, int newColor) {
        this.file = file;
        this.newColor = newColor;
    }

    public void getImageColorMatrix() throws IOException {

        this.bufferedImage = ImageIO.read(this.file);

        this.width = this.bufferedImage.getWidth();
        this.height = this.bufferedImage.getHeight();
        int minX = this.bufferedImage.getMinTileX();
        int minY = this.bufferedImage.getMinTileY();
        this.rgbMatrix = new int[this.width][this.height][3];

        for (int i = minX; i < this.width; i++) {
            for (int j = minY; j < this.height; j++) {
                int pixel = bufferedImage.getRGB(i, j);
                int[] rgb = new int[3];
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                this.rgbMatrix[i][j] = rgb;
            }
        }
    }

    private void floodFill() {

        Deque<int[]> rgbStack = new ArrayDeque<>();
        Deque<int[]> indexStack = new ArrayDeque<>();
        if (this.isBackground(this.rgbMatrix[0][0])) {
            rgbStack.addLast(this.rgbMatrix[0][0]);
            indexStack.addLast(new int[]{0, 0});
            this.visited[0][0] = true;
        }
        while (!rgbStack.isEmpty()) {
            rgbStack.removeLast();
            int[] xy = indexStack.removeLast();
            int x = xy[0];
            int y = xy[1];

            for (int i = 0; i < 4; i++) {
                int newX = x + this.d[i][0];
                int newY = y + this.d[i][1];
                if (this.inArea(newX, newY) && !this.visited[newX][newY] && this.isBackground(this.rgbMatrix[newX][newY])) {
                    rgbStack.addLast(this.rgbMatrix[newX][newY]);
                    indexStack.addLast(new int[]{newX, newY});
                    this.visited[newX][newY] = true;
                }
            }
        }
    }

    private boolean isBackground(int[] rgb) {
        for (int i = 0; i < rgb.length; i++) {
            if (Math.abs(rgb[i] - this.backgroundColor[i]) > 36) {
                return false;
            }
        }
        return true;
    }

    private boolean inArea(int x, int y) {
        return x >= 0 && x < this.width && y >= 0 && y < this.height;
    }

    private void creatNewImage(File file) throws IOException {
        for (int i = 0; i < this.visited.length; i++) {
            for (int j = 0; j < this.visited[i].length; j++) {
                if (this.visited[i][j]) {
                    this.bufferedImage.setRGB(i, j, this.newColor);
                }
            }
        }

        FileOutputStream ops = new FileOutputStream(file);
        ImageIO.write(this.bufferedImage, "jpg", ops);
        ops.flush();
        ops.close();
    }

    private void getBackGroundColor() {
        int x = 0, y = 0, z = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                x += this.rgbMatrix[i][j][0];
                y += this.rgbMatrix[i][j][1];
                z += this.rgbMatrix[i][j][2];
            }
        }

        x /= 16;
        y /= 16;
        z /= 16;

        this.backgroundColor = new int[]{x, y, z};
    }

    public void go(File file) throws IOException {
        this.getImageColorMatrix();
        this.visited = new boolean[this.width][this.height];
        this.getBackGroundColor();
        this.floodFill();
        this.creatNewImage(file);
    }

    public static void main(String[] args) throws IOException {

        File originFile = new File("D:\\JavaProgram\\replace-image-background-color\\image\\origin1.jpg");
        File newFile = new File("D:\\JavaProgram\\replace-image-background-color\\image\\newFile1.jpg");
        ReplaceImageBackgroundColor replaceImageBackgroundColor = new ReplaceImageBackgroundColor(originFile);
        replaceImageBackgroundColor.go(newFile);
    }
}
