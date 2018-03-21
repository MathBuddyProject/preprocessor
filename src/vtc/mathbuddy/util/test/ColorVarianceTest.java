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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ColorVarianceTest extends Application {

	private static final Image SOURCE_IMAGE;
	private static final double VAR_THRESHOLD = 0.025;
	private static final double DIST_THRESHOLD = 0.05;

	static {
		try {
			SOURCE_IMAGE = new Image(new FileInputStream(new File("equation6.jpg")), 600, 800, true, true);

		} catch (FileNotFoundException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {

		// 1. Read pixels
		int width = (int) SOURCE_IMAGE.getWidth();
		int height = (int) SOURCE_IMAGE.getHeight();
		WritableImage varImage = new WritableImage(width, height);
		WritableImage distImage = new WritableImage(width, height);
		PixelWriter writer1 = varImage.getPixelWriter();
		PixelWriter writer2 = distImage.getPixelWriter();
		PixelReader reader = SOURCE_IMAGE.getPixelReader();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				Color color = reader.getColor(x, y);
				double var = variance(color);
				if (var < VAR_THRESHOLD) {
					writer1.setColor(x, y, Color.BLACK);
				} else {
					double gs = Math.max(0, Math.min(1, 1000 * var));
					writer1.setColor(x, y, new Color(gs, gs, gs, 1));
				}

				double dist = distance(color);
				if (dist < DIST_THRESHOLD) {
					writer2.setColor(x, y, Color.BLACK);
				} else {
					double gs = Math.max(0, Math.min(1, 10*dist));
					writer2.setColor(x, y, new Color(gs, gs, gs, 1));
				}
			}
		}

		// 4. Build image
		ImageView sourceView = new ImageView(SOURCE_IMAGE);
		ImageView varView = new ImageView(varImage);
		ImageView distView = new ImageView(distImage);

		Scene scene = new Scene(new HBox(sourceView, varView, distView));
		stage.setScene(scene);
		stage.show();
	}

	public static double variance(Color c) {
		double r = c.getRed();
		double g = c.getGreen();
		double b = c.getBlue();

		return Math.sqrt(((r * r) + (g * g) + (b * b) - (r * g) - (r * b) - (b * g)) / 2);
	}

	public static double distance(Color c) {
		double r = c.getRed();
		double g = c.getGreen();
		double b = c.getBlue();
		double avg = (r + g + b) / 3;
		return Math.abs(r-avg) + Math.abs(b-avg);
	}

	public static void main(String[] args) {
		launch();
	}

	private static final int CONST = (0xff) << 24;

}
