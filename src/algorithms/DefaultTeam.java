package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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
        ArrayList<Point> rest = new ArrayList<>();
        rest.addAll(hitPoints);

        // Greedy
        // Random r = new Random();
        int start = 5; // r.nextInt(rest.size() - 1);

        result.add(rest.remove(start));
        //Point last = null;
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
            //last = next;
        }

/*
        System.out.println(hitPoints);
        System.out.println(hitPoints.size());
        System.out.println(new ArrayList<>(new HashSet<>(hitPoints)).size());
        System.out.println("---------------");
        System.out.println(result.size());
        System.out.println(new ArrayList<>(new HashSet<>(result)).size());
        System.out.println(result);
*/
        // localsearch
        ArrayList<Point> result2 = localSearch(result, edgeThreshold);
/*
        System.out.println("---------------");
        System.out.println(result2.size());
        System.out.println(new ArrayList<>(new HashSet<>(result2)).size());
        System.out.println(result2);
*/
        for (int i = 0; i < 100; i++){
            result2 = localSearch(result2, edgeThreshold);
        }

        ArrayList<Point> adapted_result = new ArrayList<>();
        ArrayList<Integer> tmp_list = new ArrayList<>();
        for (int i = 0; i < result2.size(); i++) {
            tmp_list.add(result.indexOf(result2.get(i)));
            adapted_result.addAll(getShortestPaths(result2.get(i), result2.get((i + 1) % result2.size())));
        }

//        System.out.println("Comparaison index finaux : " + tmp_list);

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
        //ArrayList<Point> current = new ArrayList<>(new HashSet<>(firstSolution)); // pr virer tout doublon

        // TODO : est ce qu'il ne faudrait pas faire des %(current.size() - 1) au lieu de %current.size() ??

        for (int i = 0; i < current.size() - 1; i++) {
            Point a = current.get(i);
            Point b = current.get((i + 1));

            for (int j = i + 2; j < current.size() - 1; j++) {
                Point c = current.get(j % current.size());
                Point d = current.get((j + 1) % current.size());

                if (intersect(a, b, c, d)) {
/*
                    System.out.println("a : " + a);
                    System.out.println("b : " + b);
                    System.out.println("c : " + c);
                    System.out.println("d : " + d);
*/
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
                    int tmp = 0;
                    ArrayList<Integer> tmp_list = new ArrayList<Integer>();
                    // Si ça forme bien un seul anneau
                    if(success) {
                        // on va parcourir l'anneau comme il se doit
                        ArrayList<Point> next = new ArrayList<>();
                        next.add(a);
                        tmp_list.add(i);
                        for (int k = j + 1; k > (j + 1 - current.size()); k--) {
                            tmp++;
                            next.add(current.get(k % current.size()));
                            tmp_list.add(k % current.size());
                            if(current.get(k % current.size()).equals(b)) {
//                                System.out.println(current.get(k % current.size()));
                                break;
                            }
                        }
                        System.out.println("success boucle 1 : " + tmp);
                        tmp = 0;

                        System.out.println(current.get((j + 1) % current.size()));

                        for (int k = j; k < (j + current.size()); k++) {
                            tmp++;
                            if (current.get(k % current.size()).equals(a)) {
                                break;
                            }
                            tmp_list.add(k % current.size());
                            next.add(current.get(k % current.size()));
                        }
//                        System.out.println("success boucle 2 : " + tmp);
                        tmp = 0;

                        return next;
                    }

                    // X -> =
                    // a   d           z r a---c m p
                    //   X      ->     s     X     o
                    // c   b           q g d---b n v

                    // on va parcourir l'anneau comme il se doit
                    ArrayList<Point> next = new ArrayList<>();
                    next.add(a);
                    tmp_list.add(i);
                    for (int k = j ; k > (j - current.size()); k--) {
                        tmp++;
                        next.add(current.get(k % current.size()));
                        tmp_list.add(k % current.size());
                        if (current.get(k % current.size()).equals(b)) {
//                            System.out.println(current.get(k % current.size()));
                            break;
                        }
                        
                    }
//                    System.out.println("non success boucle 1 : " + tmp);
                    tmp = 0;

//                    System.out.println(current.get((j + 1) % current.size()));

                    for (int k = j + 1; k < (j + 1 + current.size()); k++) {
                        tmp++;
                        if (current.get(k % current.size()).equals(a)) {
                            break;
                        }
                        next.add(current.get(k % current.size()));
                        tmp_list.add(k % current.size());
                    }
//                    System.out.println("non success boucle 2 : " + tmp);
                    tmp = 0;
//                    System.out.println("indexes : " + tmp_list);

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

        //result.add(points.get(current));

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
}
