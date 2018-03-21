package vtc.mathbuddy.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import vtc.mathbuddy.util.ColorReducer;

public class ColorReducerTest extends Application {

	private static final Image sourceImage;
	private static final int PALETTE_SIZE = 8;
	private static final WritablePixelFormat<IntBuffer> PIXEL_FORMAT = PixelFormat.getIntArgbInstance();

	static {
		try {
			sourceImage = new Image(new FileInputStream(new File("equation.jpg")), 1000, 800, true, true);
		} catch (FileNotFoundException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		// 1. Read pixels
		int width = (int) sourceImage.getWidth();
		int height = (int) sourceImage.getHeight();
		int[] buffer = new int[width * height];
		sourceImage.getPixelReader().getPixels(0, 0, width, height, PIXEL_FORMAT, buffer, 0, width);

		// 2. Build color palette
		IntStream stream = Arrays.stream(buffer).map(rgbToGrayscale());
		TreeMap<Integer, Integer> colorMap = ColorReducer.reduceColors(PALETTE_SIZE, stream);

		// 3. Convert colors
		final int C = (0xff) << 24;
		WritableImage destImage = new WritableImage(width, height);
		for (int i = 0; i < buffer.length; ++i) {
			int x = buffer[i] & 0xff;
			Entry<Integer, Integer> entry = colorMap.floorEntry(x);
			if (entry == null) {
				entry = colorMap.ceilingEntry(x);
			}

			int val = entry.getValue() & 0xff;
			buffer[i] = val + (val << 8) + (val << 16) + C;
		}

		// 4. Build image
		destImage.getPixelWriter().setPixels(0, 0, width, height, PIXEL_FORMAT, buffer, 0, width);
		ImageView sourceView = new ImageView(sourceImage);
		ImageView destView = new ImageView(destImage);

		Scene scene = new Scene(new HBox(sourceView, destView));
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}

	private static IntUnaryOperator rgbToGrayscale() {
		return x -> {
			int red = (x >>> 16) & 0xff;
			int green = (x >>> 8) & 0xff;
			int blue = (x >>> 0) & 0xff;
			return (red + green + blue) / 3;
		};
	}
}
