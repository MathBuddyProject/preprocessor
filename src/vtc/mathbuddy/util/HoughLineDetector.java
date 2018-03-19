package vtc.mathbuddy.util;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.DoublePredicate;

public class HoughLineDetector {

	public static HoughAlgorithmResult houghAlgorithm(double[][] data, int width, int height, int thetaCount, DoublePredicate pixelFilter) {
		HoughAlgorithmResult result = new HoughAlgorithmResult(thetaCount, width, height);

		// 1. Collect data points
		List<int[]> dataPoints = new LinkedList<>();
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (pixelFilter.test(data[y][x])) {
					dataPoints.add(new int[]{x, y});
				}
			}
		}

		// 2. Apply algorithm
		result.accumulate(dataPoints);
		return result;
	}

	public static class HoughAlgorithmResult {

		final int thetaCount;
		final int rhoCount;
		final double thetaStep;
		final double rhoStep;
		final int[][] accumulator;
		final int width, height;
		final double minRho, maxRho;

		HoughAlgorithmResult(int thetaCount, int width, int height) {
			this.width = width;
			this.height = height;

			this.thetaCount = thetaCount;
			this.thetaStep = Math.PI / thetaCount;

			this.rhoStep = 2.0;
			this.minRho = -width;
			this.maxRho = Math.sqrt((width * width) + (height * height));
			this.rhoCount = (int) Math.ceil((maxRho - minRho) / rhoStep);

			this.accumulator = new int[thetaCount][rhoCount];
		}

		void accumulate(List<int[]> dataPoints) {
			for (int t = 0; t < thetaCount; ++t) {
				double theta = t * thetaStep;
				double sine = Math.sin(theta);
				double cosine = Math.cos(theta);
				for (int[] point : dataPoints) {
					double rho = (point[0] * cosine) + (point[1] * sine);
					int bin = bin(rho);
					++accumulator[t][bin];
				};
			}
		}

		int bin(double rho) {
			return Math.max(0, Math.min(rhoCount - 1, (int) Math.floor((rho - minRho) / rhoStep)));
		}

		public double getRhoStep() {
			return rhoStep;
		}

		public int getRhoCount() {
			return rhoCount;
		}

		public double getThetaStep() {
			return thetaStep;
		}

		public int getThetaCount() {
			return thetaCount;
		}

		public int[][] getAccumulator() {
			return accumulator;
		}

		public double getRho(int rhoBin) {
			return minRho + (rhoBin * rhoStep);
		}
		
		public double getTheta(int thetaBin) {
			return (thetaBin * thetaStep);
		}
		
		public PriorityQueue<int[]> findKLargest(int k) {
			if (k <= 0)
				throw new IllegalArgumentException();

			Comparator<int[]> comparator = (a, b) -> accumulator[a[0]][a[1]] - accumulator[b[0]][b[1]];
			PriorityQueue<int[]> queue = new PriorityQueue<>(k, comparator);

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
			
			return queue;
		}
	}
}
