package vtc.mathbuddy.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class SobelLineDetection {

	public static SobelLineDetection compute(int[][] image, double thetaStep, double rhoStep, int width, int height, double threshold) {
		final int xMax = width - 2;
		final int yMax = height - 2;

		final double minRho = -width;
		final double maxRho = Math.sqrt((width * width) + (height * height));

		final int thetaCount = (int) Math.ceil(Math.PI / thetaStep);
		final int rhoCount = (int) Math.ceil((maxRho - minRho) / rhoStep);
		int[][] accumulator = new int[thetaCount][rhoCount];

		double[][] phase = new double[height][width];
		double[][] amplitude = new double[height][width];
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

				phase[y][x] = normalize(Math.atan2(gy, gx));
				amplitude[y][x] = Math.sqrt((gx * gx) + (gy * gy));
			}
		}

		final int X_PADDING = width / 5;
		final int Y_PADDING = 4 * height / 5;
		final int LINE_SEARCH_STEP = 1;

		for (int y = 0; y < yMax; y += LINE_SEARCH_STEP) {
			int x = X_PADDING;
			reduceLine(x, y, xMax, yMax, threshold, phase, amplitude);
		}

		for (int x = 0; x < xMax; x += LINE_SEARCH_STEP) {
			int y = Y_PADDING;
			reduceLine(x, y, xMax, yMax, threshold, phase, amplitude);
		}

		for (int y = 1; y <= yMax; ++y) {
			for (int x = 1; x <= xMax; ++x) {
				if (amplitude[y][x] < threshold)
					continue;

				double theta = phase[y][x];
				double rho = (x * Math.cos(theta)) + (y * Math.sin(theta));
				int thetaBin = (int) Math.floor(theta / thetaStep);
				int rhoBin = (int) Math.floor((rho - minRho) / rhoStep);

				if (thetaBin == 180)
					throw new AssertionError();

				++accumulator[thetaBin][rhoBin];
			}
		}

		return new SobelLineDetection(width, accumulator, thetaCount, rhoCount, thetaStep, rhoStep, phase, amplitude);
	}

	private static void reduceLine(int x, int y, int xMax, int yMax, double threshold, double[][] phase, double[][] amplitude) {
		if (amplitude[y][x] < threshold)
			return;

		final double expectedPhase = phase[y][x];

		Set<Coords> coordinates = new HashSet<>();
		Set<Coords> visited = new HashSet<>();
		LinkedList<Coords> toBeVisited = new LinkedList<>();
		toBeVisited.add(new Coords(x, y));
		while (!toBeVisited.isEmpty()) {
			Coords coords = toBeVisited.poll();
			if (!visited.add(coords))
				continue;

			if (Math.abs(phase[coords.y][coords.x] - expectedPhase) > Math.toRadians(3))
				continue;
			
			coordinates.add(coords);
			
			final int k = 10;
			for (int i = -k; i <= k; ++i) {
				for (int j = -k; j <= k; ++j) {
					if (i == j && i == 0)
						continue;

					int x1 = x + j;
					int y1 = y + i;
					toBeVisited.add(new Coords(x1, y1));
				}
			}
		}

		System.out.println("Line from "+x+", "+y+": "+coordinates.size());
		if (coordinates.size() < 100) {
			//return;
		}

		// Remove detected line
		for (Coords coords : coordinates) {
			phase[coords.y][coords.x] = 0;
			amplitude[coords.y][coords.x] = 255;
		}
	}

	private static class Coords {

		final int x, y;

		Coords(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 17 * hash + this.x;
			hash = 17 * hash + this.y;
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

	private static double normalize(double theta) {
		while (theta < 0) {
			theta += Math.PI;
		}

		while (theta >= Math.PI) {
			theta -= Math.PI;
		}

		return theta;
	}

	private final int imageWidth;
	private final int[][] accumulator;
	private final double thetaStep, rhoStep;
	private final double thetaCount, rhoCount;

	private final double[][] sobelPhase;
	private final double[][] sobelAmplitude;

	private SobelLineDetection(int imageWidth, int[][] accumulator, double thetaCount, double rhoCount, double thetaStep, double rhoStep, double[][] sobelPhase, double[][] sobelAmplitude) {
		this.imageWidth = imageWidth;
		this.accumulator = accumulator;
		this.thetaStep = thetaStep;
		this.rhoStep = rhoStep;
		this.thetaCount = thetaCount;
		this.rhoCount = rhoCount;
		this.sobelPhase = sobelPhase;
		this.sobelAmplitude = sobelAmplitude;
	}

	public List<double[]> findKLargest(int k) {
		if (k <= 0)
			throw new IllegalArgumentException();

		Comparator<int[]> comparator = (a, b) -> accumulator[a[0]][a[1]] - accumulator[b[0]][b[1]];
		PriorityQueue<int[]> queue = new PriorityQueue<>(k, comparator);

		// Find K largest entries
		for (int theta = 0; theta < thetaCount; ++theta) {
			for (int rho = 0; rho < rhoCount; ++rho) {
				int[] coords = {theta, rho};
				if (queue.size() < k) {
					queue.add(coords);
					continue;
				}

				int[] min = queue.peek();
				if (comparator.compare(coords, min) > 0) {
					queue.poll();
					queue.add(coords);
				}
			}
		}

		// Convert to (rho, theta) space
		List<double[]> kLargest = new ArrayList<>(queue.size());
		queue.forEach(entry -> {
			double theta = (thetaStep * entry[0]);
			double rho = (rhoStep * entry[1]) - imageWidth;
			kLargest.add(new double[]{theta, rho});
		});

		return kLargest;
	}

	public int[][] getAccumulator() {
		return accumulator;
	}

	public double[][] getSobelPhase() {
		return sobelPhase;
	}

	public double[][] getSobelAmplitude() {
		return sobelAmplitude;
	}

	public double getRhoStep() {
		return rhoStep;
	}

	public double getThetaStep() {
		return thetaStep;
	}

}
