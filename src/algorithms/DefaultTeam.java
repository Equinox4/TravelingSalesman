package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Collections;

public class DefaultTeam {
    protected int[][] shortestPaths;
    protected ArrayList<Point> points;

    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        // lancer les tests unitaires 
        UnitTests.unit_tests();

        // calculer tous les plus courts chemins
        this.points = points;
        this.shortestPaths = calculShortestPaths(edgeThreshold);

        ArrayList<Point> result = new ArrayList<>();
        ArrayList<Point> adapted_result = new ArrayList<>();
        ArrayList<Point> rest = new ArrayList<>();
        rest.addAll(hitPoints);

        // Greedy
        int start = 5;

        result.add(rest.remove(start));
        while (!rest.isEmpty()) {
            Point last = result.get(result.size() - 1);
            Point next = rest.get(0);

            for (Point p : rest) {
                if (real_distance(p, last) < real_distance(next, last)) {
                    next = p;
                }
            }

            result.add(next);
            rest.remove(next);
        }

        // localsearch
        for (int i = 0; i < 100; i++){
            result = localSearch(result, edgeThreshold);
        }
        adapted_result = adapt_result(result);

        // bruteforce
        System.out.println("Score : " + Evaluator.score(adapted_result));
        for (int i = 0; i < 100000; i++){
            result = bruteForce_window(result, 3);
        }
        adapted_result = adapt_result(result);
        System.out.println("Score : " + Evaluator.score(adapted_result));

        for (int i = 0; i < 30000; i++){
            result = bruteForce_window(result, 5);
        }
        adapted_result = adapt_result(result);
        System.out.println("Score : " + Evaluator.score(adapted_result));

        for (int i = 0; i < 300; i++){
            result = bruteForce_window(result, 8);
        }
        adapted_result = adapt_result(result);
        System.out.println("Score : " + Evaluator.score(adapted_result));

        for (int i = 0; i < 90; i++){
            result = bruteForce_window(result, 10);
        }
        adapted_result = adapt_result(result);
        System.out.println("Score : " + Evaluator.score(adapted_result));

        return adapted_result;
    }

    private ArrayList<Point> adapt_result(ArrayList<Point> list){
        ArrayList<Point> adapted_result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            adapted_result.addAll(getShortestPaths(list.get(i), list.get((i + 1) % list.size())));
        }
        return adapted_result;
    }

    private double malus_angle() {
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

    protected static double vector_norm(Point a, Point b) {
        Point vec_ab = new Point(b.x - a.x, b.y - a.y);
        return Math.sqrt(Math.pow(vec_ab.x, 2) + Math.pow(vec_ab.y, 2));
    }



    // local search
    private ArrayList<Point> localSearch(ArrayList<Point> current, int edgeThreshold) {
        for (int i = 0; i < current.size() - 1; i++) {
            Point a = current.get(i);
            Point b = current.get((i + 1));

            for (int j = i + 2; j < current.size() - 1; j++) {
                Point c = current.get(j % current.size());
                Point d = current.get((j + 1) % current.size());

                if (intersect(a, b, c, d)) {
                    //                  w k l
                    // a   c        z r a   c m p
                    //   X      ->  s   | X |   o
                    // d   b        q g d   b n v
                    //                  t u f
                    //
                    // -> Si ça fait 2 boucles, on ne rebrousse jamais chemin

                    boolean success = false;

                    // On rebrousse chemin pour essayer d'aller de c à b
                    for (int k = i; k > (i + current.size()); k--) {
                        // Si on retombe sur a avant de tomber sur b ça veut dire qu'on a 2 anneaux
                        if(current.get(k % current.size()).equals(d)) {
                            break;
                        }
                        else if(current.get(k % current.size()).equals(c)) {
                            success = true;
                            break;
                        }
                    }

                    // Si ça forme bien un seul anneau
                    if(success) {
                        // on va parcourir l'anneau comme il se doit
                        ArrayList<Point> next = new ArrayList<>();
                        next.add(a);
                        for (int k = j + 1; k > (j + 1 - current.size()); k--) {
                            next.add(current.get(k % current.size()));
                            if(current.get(k % current.size()).equals(b)) {
                                break;
                            }
                        }

                        for (int k = j; k < (j + current.size()); k++) {
                            if (current.get(k % current.size()).equals(a)) {
                                break;
                            }
                            next.add(current.get(k % current.size()));
                        }

                        return next;
                    }

                    // a   d           z r a---c m p
                    //   X      ->     s     X     o
                    // c   b           q g d---b n v

                    // on va parcourir l'anneau comme il se doit
                    ArrayList<Point> next = new ArrayList<>();
                    next.add(a);
                    for (int k = j ; k > (j - current.size()); k--) {
                        next.add(current.get(k % current.size()));
                        if (current.get(k % current.size()).equals(b)) {
                            break;
                        }
                        
                    }

                    for (int k = j + 1; k < (j + 1 + current.size()); k++) {
                        if (current.get(k % current.size()).equals(a)) {
                            break;
                        }
                        next.add(current.get(k % current.size()));
                    }

                    return next;
                }
            }
        }

        return current;
    }

    public static boolean ccw(Point a, Point b, Point c) {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x);
    }

    // Return true si ça se croise
    public static boolean intersect(Point a, Point b, Point c, Point d) {
        return ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d);
    }

    private ArrayList<Point> getShortestPaths(Point p1, Point p2){
        ArrayList<Point> result = new ArrayList<>();

        int goal = points.indexOf(p2);
        int current = points.indexOf(p1);

        while(current != goal) {
            result.add(points.get(current));
            current = shortestPaths[current][goal];
        }

        return result;
    }

    private double real_distance(Point p1, Point p2) {
        int result = 0;

        ArrayList<Point> chemin = getShortestPaths(p1, p2);
        chemin.add(p2);

        for (int i = 0; i < chemin.size() - 1; i++) {
            result += chemin.get(i).distance(chemin.get(i + 1));
        }

        return result;
    }

    /**
     *  permet d'obtenir une matrice donnant pour deux points d'indice i et j l'indice k
     *  du prochain sommet dans un plus court chemin de i à j
     *
     * @param points
     * @param edgeThreshold
     * @return une matrice à deux dimensions
     */
    public int[][] calculShortestPaths(int edgeThreshold) {
        int[][] paths = new int[points.size()][points.size()];
        for (int i= 0; i < paths.length; i++) {
            for (int j = 0; j < paths.length; j++) {
                paths[i][j] = i;
            }
        }

        double[][] dist = new double[points.size()][points.size()];

        for (int i = 0; i < paths.length; i++) {
            for (int j = 0; j < paths.length; j++) {
                if (i == j) {
                    dist[i][i] = 0;
                    continue;
                }
                if (points.get(i).distance(points.get(j)) <= edgeThreshold) {
                    dist[i][j] = points.get(i).distance(points.get(j));
                }
                else {
                    dist[i][j] = Double.POSITIVE_INFINITY;
                }
                paths[i][j] = j;
            }
        }

        for (int k = 0; k < paths.length; k++) {
            for (int i = 0; i < paths.length; i++) {
                for (int j = 0; j < paths.length; j++) {
                    if (dist[i][j] > dist[i][k] + dist[k][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        paths[i][j] = paths[i][k];
                    }
                }
            }
        }

        return paths;
    }

    protected ArrayList<Point> bruteForce_window(ArrayList<Point> current_list, int window) {
        if (window >= current_list.size()) return current_list;

        Random random_generator = new Random();
        int r_num = random_generator.nextInt(current_list.size() - 1);

        double current_score = Evaluator.score(current_list);
        double new_score = Integer.MAX_VALUE;

        ArrayList<Point> liste = current_list;

        // mettre un for avec toutes les permutations possibles
        while (current_score < new_score) {
            liste = new ArrayList<>(current_list);
            ArrayList<Point> temp_list = new ArrayList<>();
            for (int i = r_num; i < r_num + window; i++) {
                temp_list.add(liste.get(i % liste.size()));
            }
            Collections.shuffle(temp_list);

            for (int i = r_num; i < r_num + window; i++) {
                liste.set(i % liste.size(), temp_list.remove(0));
            }

            new_score = Evaluator.score(liste);
        }

        return liste;
    }
}
