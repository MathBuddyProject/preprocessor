package vtc.mathbuddy.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class ColorClusters {

	private static final int BLACK = (0xff << 24);

	public static void removeClusterAround(int x, int y, PixelReader reader, PixelWriter writer, int width, int height, double threshold, Set<Coords> ignore) {
		Coords start = new Coords(x, y);
		if (ignore.contains(start))
			return;

		Set<Coords> visited = new HashSet<>();
		LinkedList<Coords> queue = new LinkedList<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			Coords coords = queue.poll();
			if (ignore.contains(coords))
				continue;

			ignore.add(coords);
			writer.setArgb(x, y, BLACK);

			final int k = 5;
			for (int i = -k; i <= k; ++i) {
				for (int j = -k; j <= k; ++j) {
					if (i == j && i == 0)
						continue;

					int x1 = x + j;
					int y1 = y + i;
					if (x1 < 0 || x1 >= width || y1 < 0 || y1 >= height)
						continue;

					Coords neighbor = new Coords(x1, y1);
					if (!checkSimilar(coords, neighbor, threshold, reader))
						continue;

					queue.add(neighbor);
				}
			}
		}

	}

	private static boolean checkSimilar(Coords c1, Coords c2, double threshold, PixelReader reader) {
		Color color1 = reader.getColor(c1.x, c1.y);
		Color color2 = reader.getColor(c2.x, c2.y);
		return (Math.abs(color1.getRed() - color2.getRed()) <= threshold)
				&& (Math.abs(color1.getGreen() - color2.getGreen()) <= threshold)
				&& (Math.abs(color1.getBlue() - color2.getBlue()) <= threshold);
	}

	public static class Coords {

		final int x, y;

		public Coords(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 23 * hash + this.x;
			hash = 23 * hash + this.y;
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (!(obj instanceof Coords))
				return false;

			Coords other = (Coords) obj;
			return this.x == other.x && this.y == other.y;
		}

	}
}
