package vtc.mathbuddy.util;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColorReducer {

	public static TreeMap<Integer, Integer> reduceColors(int numColors, IntStream pixels) {
		// 1. Sort pixels
		List<Integer> list = pixels.sorted().boxed().collect(Collectors.toList());

		// 2. Split into colors
		PriorityQueue<ColorGroup> queue = new PriorityQueue<>(Comparator.reverseOrder());
		queue.add(new ColorGroup(list));

		while (queue.size() < numColors) {
			// poll largest group
			ColorGroup group = queue.poll();

			// split at median
			if (group.size() <= 1) {
				queue.add(group);
				break;
			}

			// add back to queue
			queue.add(group.lowHalf());
			queue.add(group.highHalf());
		}
		// 3. Construct mapping tree
		TreeMap<Integer, Integer> map = new TreeMap<>();
		queue.forEach(group -> map.put(group.lowestColor(), group.meanColor()));
		return map;
	}

	private ColorReducer() {
	}

}
