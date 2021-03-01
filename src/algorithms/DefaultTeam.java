package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class DefaultTeam {
    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        // lancer les tests unitaires 
        unit_tests();


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



    // --- --- --- Tests unitaires  --- --- --- //

    public void unit_tests() {
        try {
            assertEqualsDouble(vector_norm(new Point(0, 0), new Point(0, 2)), 2.00); // peut rater à cause de la précision
            assertEqualsDouble(vector_norm(new Point(0, 0), new Point(2, 3)), Math.sqrt(13)); // peut rater à cause de la précision

        } catch (Exception e) {
            System.exit(1);
        }
    }

    private void assertEquals(Object a, Object b) throws Exception {
        if(!(a.equals(b))) throw(new Exception());
    }

    // On arrondi les doubles comparés à 2 chiffres après la virgule pour éviter que des tests
    // ne passent pas à cause d'une erreur d'arrondi
    private void assertEqualsDouble(Double a, Double b) throws Exception {
        if(!(round(a, 2) == round(b, 2))) throw(new Exception());
    }

    /**
     * Méthode récupérée sur cette page https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
