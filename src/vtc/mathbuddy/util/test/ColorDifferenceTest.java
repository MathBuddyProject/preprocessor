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

public class ColorDifferenceTest extends Application {

	private static final Image SOURCE_IMAGE;

	static {
		try {
			SOURCE_IMAGE = new Image(new FileInputStream(new File("equation6.jpg")), 1000, 800, true, true);
		} catch (FileNotFoundException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		// 1. Read pixels
		// 1. Read pixels
		int width = (int) SOURCE_IMAGE.getWidth();
		int height = (int) SOURCE_IMAGE.getHeight();
		WritableImage diffImage = new WritableImage(width, height);
		PixelWriter writer = diffImage.getPixelWriter();
		PixelReader reader = SOURCE_IMAGE.getPixelReader();
		writer.setPixels(0, 0, width, height, reader, 0, 0);

		ImageView sourceView = new ImageView(SOURCE_IMAGE);
		ImageView diffView = new ImageView(diffImage);

		diffView.setOnMousePressed(e -> {
			int x = (int) e.getX();
			int y = (int) e.getY();
			Color ref = reader.getColor(x, y);

			for (int i = 0; i < height; ++i) {
				for (int j = 0; j < width; ++j) {
					Color col = reader.getColor(j, i);
					double dr = Math.abs(col.getRed() - ref.getRed());
					double dg = Math.abs(col.getGreen() - ref.getGreen());
					double db = Math.abs(col.getBlue() - ref.getBlue());
					writer.setColor(j, i, new Color(dr, dg, db, 1));
				}
			}
		});

		Scene scene = new Scene(new HBox(sourceView, diffView));
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}

}
