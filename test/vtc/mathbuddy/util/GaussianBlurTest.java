package vtc.mathbuddy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GaussianBlurTest extends Application {

	private static final Image SOURCE_IMAGE;
	private static final int KERNEL_SIZE = 21;
	private static final double SIGMA = 5;

	static {
		try {
			SOURCE_IMAGE = new Image(new FileInputStream(new File("equation2.jpg")), 1000, 800, true, true);

		} catch (FileNotFoundException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {

		// 1. Read pixels
		int width = (int) SOURCE_IMAGE.getWidth();
		int height = (int) SOURCE_IMAGE.getHeight();
		int[][] image = new int[height][width];
		WritableImage bwImage = new WritableImage(width, height);
		PixelWriter pixelWriter = bwImage.getPixelWriter();
		PixelReader pixelReader = SOURCE_IMAGE.getPixelReader();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				Color color = pixelReader.getColor(x, y).grayscale();
				image[y][x] = (int) (255 * color.getRed());
				pixelWriter.setColor(x, y, color);
			}
		}
		
		// 2. Blur image
		image = GaussianBlur.blur(image, width, height, KERNEL_SIZE, SIGMA);
		WritableImage destImage = new WritableImage(width, height);
		PixelWriter writer = destImage.getPixelWriter();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				writer.setArgb(x, y, toARGB(image[y][x]));
			}
		}

		// 4. Build image
		ImageView sourceView = new ImageView(bwImage);
		ImageView destView = new ImageView(destImage);

		Scene scene = new Scene(new HBox(sourceView, destView));
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}

	private static final int CONST = (0xff) << 24;

	private static int toARGB(int grayscale) {
		return grayscale + (grayscale << 8) + (grayscale << 16) + CONST;
	}

}
