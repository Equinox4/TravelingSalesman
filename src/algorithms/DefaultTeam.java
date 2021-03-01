package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class DefaultTeam {
    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {

        ArrayList<Point> result = new ArrayList();
        ArrayList<Point> rest = new ArrayList<>();
        rest.addAll(hitPoints);

        // Greedy
        Random r = new Random();
        int start = r.nextInt(rest.size() - 1);

        result.add(rest.remove(start));
        while (!rest.isEmpty()) {
            Point last = result.get(result.size() - 1);
            Point next = rest.get(0);

            for (Point p : rest) {
                if (p.distance(last) < next.distance(last)) {
                    next = p;
                }
            }

            result.add(next);
            rest.remove(next);
        }

        return result;
    }
}
