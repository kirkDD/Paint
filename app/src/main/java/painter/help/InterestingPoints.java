package painter.help;


import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * keeps track of 2D points
 *  use this class to query those points that are close to a given point
 */
public class InterestingPoints {

    int SNAP_RADIUS = 20;

    HashMap<Point, Integer> pointCounts;
    public InterestingPoints(int xMin, int xMax, int yMin, int yMax) {
        pointCounts = new HashMap<>();
        // big hash map
        // with x // SNAP_R as key, and set of points as value
        // 2. modify the Hashcode of Point to achieve this!!!
        // todo
    }

    public Point query(float x, float y) {
        return null;
    }

    public void addPoint(float x, float y) {
        Point n = new Point(x, y);
        pointCounts.put(n, pointCounts.getOrDefault(n, 1));
    }

    public void removePoint(float x, float y) {
        Point n = new Point(x, y);
        if (pointCounts.containsKey(n)) {
            if (pointCounts.get(n) == 1) {
                pointCounts.remove(n);
            } else {
                pointCounts.put(n, pointCounts.get(n) - 1);
            }
        }
    }


    /**
     * keep track of a point
     */
    public static class Point {
        public int x;
        public int y;
        public Point(float x, float y) {
            this.x = (int) x;
            this.y = (int) y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x &&
                    y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

}
