package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class DefaultTeam {
    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        // lancer les tests unitaires 
        UnitTests.unit_tests();

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

    private double malus_angle(ArrayList<Point> points) {
        float result = 0;
        for (int i = 1; i < points.size(); i++) {
            result += malus_angle_3pts(points.get((i - 1) % points.size()), points.get(i), points.get((i + 1) % points.size()));
        }

        return result;
    }

    // a = i-1; b = i; c = i+1
    private double malus_angle_3pts(Point a, Point b, Point c) {
        return (100 / Math.PI) * Math.acos(dot_product(a, b, b, c) / (vector_norm(a, b) * vector_norm(b, c)));
    }

    private double dot_product(Point a, Point b, Point c, Point d) {
        Point vec_ab = new Point(b.x - a.x, b.y - a.y);
        Point vec_cd = new Point(d.x - c.x, d.y - c.y);

        return vec_ab.x * vec_cd.x + vec_ab.y * vec_cd.y;
    }

    private double vector_norm(Point a, Point b) {
        Point vec_ab = new Point(b.x - a.x, b.y - a.y);
        return Math.sqrt(Math.pow(vec_ab.x, 2) + Math.pow(vec_ab.y, 2));
    }

    // local search
    private ArrayList<Point> localSearch(ArrayList<Point> firstSolution, ArrayList<Point> points, int edgeThreshold) {
        ArrayList<Point> current = new ArrayList<>(new HashSet<>(firstSolution)); // pr virer tout doublon

        // TODO : est ce qu'ilne faudrait pas faire des %(current.size() - 1) au lieu de %current.size() ??

        for(int i = 0; i < current.size(); i++) {
            Point a = current.get(i);
            Point b = current.get((i + 1) % current.size());

            for(int j = 0; j < current.size(); j++) {
                Point c = current.get(j);
                Point d = current.get((j + 1) % current.size());

                if(intersect(a, b, c, d)) {
                    // X -> | |
                    //                  w k l
                    // a   c        z r a   c m p
                    //   X      ->  s   | X |   o
                    // d   b        q g d   b n v
                    //                  t u f
                    //
                    // -> Si ça fait 2 boucles, on ne rebrousse jamais chemin

                    boolean success = false;

                    // On rebrousse chemin pour essayer d'aller de c à b
                    for (int k = i; k > (i + current.size()) % current.size(); k--){
                        // Si on retombe sur a avant de tomber sur b ça veut dire qu'on a 2 anneaux
                        if(current.get(k).equals(d)){
                            break;
                        } else if(current.get(k).equals(c)){
                            success = true;
                            break;
                        }
                    }

                    // Si ça forme bien un seul anneau
                    if(success) {
                        // on va parcourir l'anneau comme il se doit
                        ArrayList<Point> next = new ArrayList<>();
                        next.add(a);
                        for(int k = j + 1; k > (j + 1 + current.size()); k--) {
                            next.add(current.get(k % current.size()));
                            if(current.get(k).equals(b)) {
                                break;
                            }
                        }
                        for(int k = j; k < (j + current.size()); k++) {
                            if(current.get(k).equals(a)) {
                                break;
                            }
                            next.add(current.get(k % current.size()));
                        }
                        if()
                    }

                    // X -> =
                    // a   d          a---d
                    //   X      ->
                    // c   b          c---b
                }
            }
        }

    }

    public static boolean ccw(Point a, Point b, Point c) {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x);
    }

    // Return true si ça se croise
    public static boolean intersect(Point a, Point b, Point c, Point d) {
        return ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d);
    }


    private boolean isSolution(ArrayList<Point> candidateIn, ArrayList<Point> pointsIn, int edgeThreshold) {
        ArrayList<Point> candidate = new ArrayList<>(new HashSet<>(candidateIn));
        ArrayList<Point> rest = new ArrayList<>(new HashSet<>(pointsIn));

        rest.removeAll(candidate);
        ArrayList<Point> visited = new ArrayList<Point>();

        while (!rest.isEmpty()) {
            visited.clear();
            visited.add(rest.remove(0));
            for (int i = 0; i < visited.size(); i++) {
                for (Point p : rest) {
                    if (isEdge(visited.get(i), p, edgeThreshold)) {
                        for (Point q : visited) {
                            if (!q.equals(visited.get(i)) && isEdge(p, q, edgeThreshold)) {
                                return false;
                            }
                        }
                        visited.add(p);
                    }
                }
                rest.removeAll(visited);
            }
        }

        return true;
    }
    private boolean isEdge(Point p, Point q, int edgeThreshold) {
        return p.distance(q) < edgeThreshold;
    }




    // tests
    public ArrayList<Point> improve(ArrayList<Point> points) {

        ArrayList<Point> p = new ArrayList<>();
        int n = points.size();

        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j < n; j++) {
                double a = points.get(i % n).distance(points.get((i + 1) % n));
                double b = points.get(j % n).distance(points.get((j + 1) % n));
                double c = points.get(i % n).distance(points.get(j % n));
                double d = points.get((i + 1) % n).distance(points.get((j + 1) % n));

                if (a + b > c + d) {

                    for (int k = 0; k <= i; k++) {
                        p.add(points.get(k));
                    }
                    for (int k = j; k > i; k--) {
                        p.add(points.get(k));
                    }
                    for (int k = j + 1; k < n; k++) {
                        p.add(points.get(k));
                    }

                    return p;
                }
            }
        }

        return points;
    }
}
