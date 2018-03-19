package vtc.mathbuddy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.PriorityQueue;
import java.util.function.DoublePredicate;
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
import vtc.mathbuddy.util.HoughLineDetector.HoughAlgorithmResult;

public class HoughLineDetectorTest extends Application {

	private static final Image SOURCE_IMAGE;
	private static final int THETA_COUNT = 360; // intervals of 0.5 degree
	private static final int PIXEL_THRESHOLD = 40;

	
	static {
		try {
			SOURCE_IMAGE = new Image(new FileInputStream(new File("equation5.jpg")), 500, 600, true, true);
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

		// 2. Draw difference image
		double[][] gradient = SobelOperator.compute(pixels, width, height);
		WritableImage diffImage = new WritableImage(width, height);
		WritableImage linesImage = new WritableImage(width, height);
		PixelWriter diffWriter = diffImage.getPixelWriter();
		PixelWriter linesWriter = linesImage.getPixelWriter();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int val = Math.max(0, Math.min(255, 2 * (int) gradient[y][x]));
				val = toARGB(val >= PIXEL_THRESHOLD ? val : 0);

				diffWriter.setArgb(x, y, val);
				linesWriter.setArgb(x, y, val);
			}
		}

		// 3. Detect lines
		DoublePredicate filter = x -> (x >= PIXEL_THRESHOLD);
		HoughAlgorithmResult result = HoughLineDetector.houghAlgorithm(gradient, width - 2, height - 2, THETA_COUNT, filter);

		// 4. Find largest lines
		PriorityQueue<int[]> largest = result.findKLargest(50);
		for (int[] coords : largest) {
			double theta = result.getTheta(coords[0]);
			double rho = result.getRho(coords[1]);
			drawLine(linesWriter, width, height, theta, rho);
		}

		ImageView sourceView = new ImageView(SOURCE_IMAGE);
		ImageView diffView = new ImageView(diffImage);
		ImageView linesView = new ImageView(linesImage);

		HBox box = new HBox(sourceView, diffView, linesView);
		box.setSpacing(10);
		Scene scene = new Scene(box);
		scene.setFill(Color.BEIGE);
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

	private static void drawLine(PixelWriter writer, int width, int height, double theta, double rho) {
		// equation: y = (p - xcosT) / sinT

		double cos = Math.cos(theta);
		double sin = Math.sin(theta);

		// Vertical line
		if (Math.abs(sin) <= 0.001) {
			if (rho >= 0 && rho < width) {
				int x = (int) rho;
				for (int y = 0; y < height; ++y) {
					writer.setColor(x, y, Color.RED);
				}
			}
			return;
		}

		for (int x = 0; x < width; ++x) {
			int y = (int) ((rho - (x * cos)) / sin);
			if (y < 0 || y >= height)
				continue;

			writer.setColor(x, y, Color.RED);
		}
	}
}
