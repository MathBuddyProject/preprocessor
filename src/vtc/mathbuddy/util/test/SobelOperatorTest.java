package vtc.mathbuddy.util.test;

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
import javafx.stage.Stage;
import vtc.mathbuddy.util.SobelOperator;

public class SobelOperatorTest extends Application {

	private static final Image SOURCE_IMAGE;

	static {
		try {
			SOURCE_IMAGE = new Image(new FileInputStream(new File("equation.jpg")), 1000, 800, true, true);
		} catch (FileNotFoundException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		// 1. Read pixels
		int width = (int) SOURCE_IMAGE.getWidth();
		int height = (int) SOURCE_IMAGE.getHeight();
		int[][] pixels = new int[height][width];
		PixelReader pixelReader = SOURCE_IMAGE.getPixelReader();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				pixels[y][x] = (int) (255 * pixelReader.getColor(x, y).grayscale().getRed());
			}
		}

		// 2. Compute gradients
		double[][] gradient = SobelOperator.compute(pixels, width, height);

		// 3. Draw image
		final int C = (0xff) << 24;
		WritableImage destImage = new WritableImage(width, height);
		PixelWriter pixelWriter = destImage.getPixelWriter();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int val = Math.max(0, Math.min(255, 2 * (int) gradient[y][x]));
				pixelWriter.setArgb(x, y, toARGB(val));
			}
		}

		// 4. Build image
		ImageView sourceView = new ImageView(SOURCE_IMAGE);
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
