package vtc.mathbuddy.util;

public class ImageMoments {

	public static double computeMoment(double[][] image, int width, int height, boolean multX, boolean multY) {
		double moment = 0;
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				double val = image[y][x];
				if (multX)
					val *= x;
				
				if (multY)
					val *= y;
				
				moment += val;
			}
		}
		
		return moment;
	}
	
	public static double[] computeCentroid(double[][] image, int width, int height) {
		double energy = computeMoment(image, width, height, false, false);
		double centerX = computeMoment(image, width, height, true, false);
		double centerY = computeMoment(image, width, height, false, true);
		
		return new double[]{(centerX / energy), (centerY / energy)};
	}
}
