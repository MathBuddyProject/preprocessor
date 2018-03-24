package vtc.mathbuddy.util;

public class GaussianBlur {

	public static int[][] blur(int[][] image, int width, int height, int kernelSize, double sigma) {
		if (kernelSize % 2 == 0)
			throw new IllegalArgumentException("Kernel size must be odd");

		double[][] kernel = makeKernel(kernelSize, sigma);
		int[][] dest = new int[height][width];
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				applyKernel(image, dest, x, y, width, height, kernel);
			}
		}

		return dest;
	}

	public static double[][] blur(double[][] image, int width, int height, int kernelSize, double sigma) {
		if (kernelSize % 2 == 0)
			throw new IllegalArgumentException("Kernel size must be odd");

		double[][] kernel = makeKernel(kernelSize, sigma);
		double[][] dest = new double[height][width];
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				applyKernel(image, dest, x, y, width, height, kernel);
			}
		}

		return dest;
	}

	private static double[][] makeKernel(int size, double sigma) {
		final int halfSize = size / 2;
		double[][] kernel = new double[size][size];
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				int x = j - halfSize;
				int y = i - halfSize;
				double val = Math.exp(-((x * x) + (y * y)) / (2 * sigma * sigma)) / (2 * sigma * sigma * Math.PI);
				kernel[i][j] = val;
			}
		}

		double sum = 0;
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				sum += kernel[i][j];
			}
		}

		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				kernel[i][j] /= sum;
			}
		}

		return kernel;
	}

	private static void applyKernel(int[][] image, int[][] dest, int x, int y, int width, int height, double[][] kernel) {
		double sum = 0;
		int halfSize = kernel.length / 2;
		for (int i = 0; i < kernel.length; ++i) {
			for (int j = 0; j < kernel.length; ++j) {
				int y1 = Math.max(0, Math.min(height - 1, y + i - halfSize));
				int x1 = Math.max(0, Math.min(width - 1, x + j - halfSize));
				sum += image[y1][x1] * kernel[i][j];
			}
		}

		dest[y][x] = (int) sum;
	}

	private static void applyKernel(double[][] image, double[][] dest, int x, int y, int width, int height, double[][] kernel) {
		double sum = 0;
		int halfSize = kernel.length / 2;
		for (int i = 0; i < kernel.length; ++i) {
			for (int j = 0; j < kernel.length; ++j) {
				int y1 = Math.max(0, Math.min(height - 1, y + i - halfSize));
				int x1 = Math.max(0, Math.min(width - 1, x + j - halfSize));
				sum += image[y1][x1] * kernel[i][j];
			}
		}

		dest[y][x] = sum;
	}

}
