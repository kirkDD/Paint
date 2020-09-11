package painter.help;


import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * keeps track of 2D points
 *  use this class to query those points that are close to a given point
 */
public class InterestingPoints {

    int SNAP_RADIUS = 20;

    HashMap<Point, Integer> pointCounts;
    HashMap<Object, Set<Point>> pointStorage;
    public InterestingPoints() {
        pointCounts = new HashMap<>();
        pointStorage = new HashMap<>();
        // big hash map
        // with x // SNAP_R as key, and set of points as value
        // 2. modify the Hashcode of Point to achieve this!!!
        // using a set first
    }

    public Point query(float x, float y) {
        // linear implementation
        for (Point p : pointCounts.keySet()) {
            if (dist(x, y, p.x, p.y) < SNAP_RADIUS) {
                return p;
            }
        }
        return null;
    }

    public void addPoint(Object o, float x, float y) {
        Point n = new Point(x, y);
        if (!pointStorage.containsKey(o)) {
            pointStorage.put(o, new HashSet<>());
        }
        pointStorage.get(o).add(n);
        pointCounts.put(n, pointCounts.getOrDefault(n, 0) + 1);
    }

    public void removePoint(Object o, float x, float y) {
        Point n = new Point(x, y);
        if (pointStorage.containsKey(o)) {
            pointStorage.get(o).remove(n);
            if (pointCounts.containsKey(n)) {
                if (pointCounts.get(n) == 1) {
                    pointCounts.remove(n);
                } else {
                    pointCounts.put(n, pointCounts.get(n) - 1);
                }
            }
        }
    }

    public void removeAllPoints(Object o) {
        if (pointStorage.containsKey(o)) {
            for (Point p : pointStorage.get(o)) {
                if (pointCounts.containsKey(p)) {
                    if (pointCounts.get(p) == 1) {
                        pointCounts.remove(p);
                    } else {
                        pointCounts.put(p, pointCounts.get(p) - 1);
                    }
                } else {
                    Log.d("[][]", "removeAllPoints: nothing!!!");
                }
            }
            pointStorage.remove(o);
        }
    }


    double dist(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }


    // debug
    public Set<Point> allPoints() {
        return pointCounts.keySet();
    }


    /**
     * keep track of a point
     */
    public static class Point {
        public float x;
        public float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return (int) x == (int) point.x &&
                    (int) y == (int) point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash((int) x, (int) y);
        }
    }

}
