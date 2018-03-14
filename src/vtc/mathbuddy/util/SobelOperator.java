package vtc.mathbuddy.util;

public class SobelOperator {

	public static double[][] compute(int[][] image, int width, int height) {
		int xMax = width - 2;
		int yMax = height - 2;

		double[][] sobel = new double[height][width];
		for (int y = 1; y <= yMax; ++y) {
			for (int x = 1; x <= xMax; ++x) {
				int topLeft = image[y - 1][x - 1];
				int topCenter = image[y - 1][x];
				int topRight = image[y - 1][x + 1];
				int centerLeft = image[y][x - 1];
				int centerRight = image[y][x + 1];
				int bottomLeft = image[y + 1][x - 1];
				int bottomCenter = image[y + 1][x];
				int bottomRight = image[y + 1][x + 1];

				double gx = (topLeft - topRight) + 2 * (centerLeft - centerRight) + (bottomLeft - bottomRight);
				double gy = (topLeft - bottomLeft) + 2 * (topCenter - bottomCenter) + (topRight - bottomRight);

				sobel[y][x] = Math.sqrt((gx * gx) + (gy * gy));
			}
		}
		
		return sobel;
	}
}
