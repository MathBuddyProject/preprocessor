package vtc.mathbuddy.util;

import java.util.List;

public class ColorGroup implements Comparable<ColorGroup> {

	private List<Integer> colorValues;

	public ColorGroup(List<Integer> colorValues) {
		this.colorValues = colorValues;
	}

	public int size() {
		return colorValues.size();
	}

	public int lowestColor() {
		return colorValues.get(0);
	}

	public int highestColor() {
		return colorValues.get(colorValues.size() - 1);
	}

	public int meanColor() {
		return (int) colorValues.stream().mapToInt(x -> x).average().getAsDouble();
	}

	public ColorGroup lowHalf() {
		return new ColorGroup(colorValues.subList(0, colorValues.size() / 2));
	}

	public ColorGroup highHalf() {
		return new ColorGroup(colorValues.subList(colorValues.size() / 2, colorValues.size()));
	}

	@Override
	public int compareTo(ColorGroup o) {
		return this.size() - o.size();
	}

}
