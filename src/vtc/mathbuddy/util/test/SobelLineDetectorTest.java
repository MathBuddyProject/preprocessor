package vtc.mathbuddy.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
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
import vtc.mathbuddy.util.SobelLineDetection;

public class SobelLineDetectorTest extends Application {

	private static final Image SOURCE_IMAGE;
	private static final double THETA_STEP = Math.toRadians(1);
	private static final double RHO_STEP = 2;
	private static final int PIXEL_THRESHOLD = 40;
	private static final int K = 50;
	
	private static final int BLUR_KERNEL_SIZE = 5;
	private static final double BLUR_SIGMA = 0.8;

	static {
		try {
			SOURCE_IMAGE = new Image(new FileInputStream(new File("equation6.jpg")), 500, 600, true, true);
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

		pixels = GaussianBlur.blur(pixels, width, height, BLUR_KERNEL_SIZE, BLUR_SIGMA);
		
		// 2. Compute sobel operator & hough algorithm
		SobelLineDetection sobelDetection = SobelLineDetection.compute(pixels, THETA_STEP, RHO_STEP, width, height, PIXEL_THRESHOLD);
		double[][] gradient = sobelDetection.getSobelAmplitude();
		double[][] phase = sobelDetection.getSobelPhase();

		// 3. Write difference image
		WritableImage diffImage = new WritableImage(width, height);
		WritableImage linesImage = new WritableImage(width, height);
		PixelWriter diffWriter = diffImage.getPixelWriter();
		PixelWriter linesWriter = linesImage.getPixelWriter();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				double val = Math.max(0, Math.min(255, 2 * gradient[y][x]));
				if (val < PIXEL_THRESHOLD) {
					diffWriter.setColor(x, y, Color.BLACK);
					linesWriter.setColor(x, y, Color.BLACK);
					continue;
				}

				Color c = Color.WHITE.interpolate(Color.GREEN, phase[y][x] / Math.PI).interpolate(Color.BLACK, 1 - (val / 255));
				diffWriter.setColor(x, y, c);
				linesWriter.setColor(x, y, Color.WHITE.interpolate(Color.BLACK, 1 - (val / 255)));
			}
		}

		// 4. Find largest lines
		List<double[]> largest = sobelDetection.findKLargest(K);
		for (double[] entry : largest) {
			double theta = entry[0];
			double rho = entry[1];
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
