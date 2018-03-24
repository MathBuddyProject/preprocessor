package vtc.mathbuddy.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
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
import vtc.mathbuddy.util.GaussianBlur;
import vtc.mathbuddy.util.HoughLineDetector;
import vtc.mathbuddy.util.HoughLineDetector.HoughAlgorithmResult;
import vtc.mathbuddy.util.SobelOperator;

public class HoughLineDetectorTest extends Application {

	private static final Image SOURCE_IMAGE;
	private static final int THETA_COUNT = 360; // intervals of 0.5 degree
	private static final int PIXEL_THRESHOLD = 50;

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

		pixels = GaussianBlur.blur(pixels, width, height, 5, 1);

		// 2. Draw difference image
		double[][] gradient = SobelOperator.compute(pixels, width, height);
		for (int i = 0; i < 3; ++i) {
			gradient = GaussianBlur.blur(gradient, width, height, 3, 1);
			applyThreshold(gradient, width, height, PIXEL_THRESHOLD);
		}

		WritableImage diffImage = new WritableImage(width, height);
		WritableImage linesImage = new WritableImage(width, height);
		PixelWriter diffWriter = diffImage.getPixelWriter();
		PixelWriter linesWriter = linesImage.getPixelWriter();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int val = Math.max(0, Math.min(255, 2 * (int) gradient[y][x]));
				val = toARGB(val);

				diffWriter.setArgb(x, y, val);
				linesWriter.setArgb(x, y, val);
			}
		}

		// 3. Detect lines
		DoublePredicate filter = x -> (x >= PIXEL_THRESHOLD);
		HoughAlgorithmResult result = HoughLineDetector.houghAlgorithm(gradient, width - 2, height - 2, THETA_COUNT, filter);

		// 4. Find largest lines
		BiConsumer<Integer, Integer> cons = (x, y) -> {
			//gradient[y][x] = 0;
			linesWriter.setColor(x, y, Color.BLACK);
		};

		PriorityQueue<int[]> largest = result.findKLargest(500);
		for (int[] coords : largest) {
			double theta = result.getTheta(coords[0]);
			double rho = result.getRho(coords[1]);
			removeLine(pixelReader, cons, width, height, theta, rho);
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

	private static void applyThreshold(double[][] image, int width, int height, double threshold) {
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (image[y][x] < threshold)
					image[y][x] = 0;
			}
		}
	}

	private static void oyoy(double[][] arr) {
		int max = 0;
		Map<Integer, Integer> count = new HashMap<>();
		for (int i = 0; i < arr.length; ++i) {
			double[] ar = arr[i];
			for (int j = 0; j < ar.length; ++j) {
				int x = (int) ar[j];
				count.merge(x, 1, Integer::sum);

				if (x > max)
					max = x;
			}
		}

		System.out.println("intensity\tcount");
		for (int i = 0; i <= max; ++i) {
			Integer c = count.get(i);
			if (c != null && c != 0)
				System.out.println(i + "\t" + c);
		}

	}

	private static final int CONST = (0xff) << 24;

	private static int toARGB(int grayscale) {
		return grayscale + (grayscale << 8) + (grayscale << 16) + CONST;
	}

	private static void removeLine(PixelReader reader, BiConsumer<Integer, Integer> writer, int width, int height, double theta, double rho) {
		// 1. Collect pixels
		List<PixelColor> list = collectLinePixels(reader, width, height, theta, rho);

		// 2. Compute mean & std dev
		double meanR = 0, meanG = 0, meanB = 0;
		double stdDevR = 0, stdDevG = 0, stdDevB = 0;

		for (PixelColor pixel : list) {
			Color c = pixel.color;
			meanR += c.getRed();
			meanG += c.getGreen();
			meanB += c.getBlue();
		}

		meanR /= list.size();
		meanG /= list.size();
		meanB /= list.size();
		final double meanThreshold = 0.1;
		final double devThreshold = 0.5;
		for (Iterator<PixelColor> it = list.iterator(); it.hasNext();) {
			PixelColor pixel = it.next();
			Color c = pixel.color;

			double diffR = c.getRed() - meanR;
			double diffG = c.getGreen() - meanG;
			double diffB = c.getBlue() - meanB;
			stdDevR += diffR * diffR;
			stdDevG += diffG * diffG;
			stdDevB += diffB * diffB;

			if (Math.abs(diffR) > meanThreshold || Math.abs(diffG) > meanThreshold || Math.abs(diffB) > meanThreshold)
				it.remove();
		}

		stdDevR = Math.sqrt(stdDevR / (list.size() - 1));
		stdDevG = Math.sqrt(stdDevG / (list.size() - 1));
		stdDevB = Math.sqrt(stdDevB / (list.size() - 1));

		if (stdDevR > devThreshold || stdDevG > devThreshold || stdDevB > devThreshold)
			return;

		for (PixelColor pixel : list) {
			writer.accept(pixel.x, pixel.y);
		}
	}

	private static List<PixelColor> collectLinePixels(PixelReader reader, int width, int height, double theta, double rho) {
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		List<PixelColor> list = new LinkedList<>();

		// compute x using y
		if (theta < 0.25 * Math.PI || theta > 0.75 * Math.PI) {
			for (int y = 0; y < height; ++y) {
				int x = (int) ((rho - (y * sin)) / cos);
				if (x < 0 || x >= width)
					continue;

				list.add(new PixelColor(x, y, reader.getColor(x, y)));
			}

			return list;
		}

		// compute y using x
		for (int x = 0; x < width; ++x) {
			int y = (int) ((rho - (x * cos)) / sin);
			if (y < 0 || y >= height)
				continue;

			list.add(new PixelColor(x, y, reader.getColor(x, y)));
		}
		return list;
	}

	static class PixelColor {

		final int x, y;
		final Color color;

		PixelColor(int x, int y, Color color) {
			this.x = x;
			this.y = y;
			this.color = color;
		}
	}
}
