package algorithms;

import java.awt.Point;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class GraphUtils {
    /**
     *  permet d'obtenir une matrice donnant pour deux points d'indice i et j l'indice k
     *  du prochain sommet dans un plus court chemin de i à j
     *
     * @param points
     * @param edgeThreshold
     * @return une matrice à deux dimensions
     */
    public static int[][] calculShortestPaths(ArrayList<Point> points, int edgeThreshold) {
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

    public static double [][] calculDistances(ArrayList<Point> points, ArrayList<Point> hitPoints, int [][] shortestPaths){
        double[][] distances = new double[hitPoints.size()][hitPoints.size()];
        for (int i= 0; i < hitPoints.size(); i++) {
            for (int j = 0; j < hitPoints.size(); j++) {
                distances[i][j] = real_distance(shortestPaths, points, hitPoints.get(i), hitPoints.get(j));
            }
        }
        return distances;
    }
    public static double [][] classementDistances(ArrayList<Point> points, ArrayList<Point> hitPoints, int [][] shortestPaths){
        double[][] distances = new double[hitPoints.size()][hitPoints.size()];
        for (int i= 0; i < hitPoints.size(); i++) {
            for (int j = 0; j < hitPoints.size(); j++) {
                distances[i][j] = real_distance(shortestPaths, points, hitPoints.get(i), hitPoints.get(j));
            }
            for (int j = 0; j < hitPoints.size(); j++) {
                distances[i][j] = real_distance(shortestPaths, points, hitPoints.get(i), hitPoints.get(j));
            }
        }
        return distances;
    }

    public static ArrayList<Point> getShortestPaths(int [][] shortestPaths, ArrayList<Point> liste_points, Point p1, Point p2) {
        ArrayList<Point> result = new ArrayList<>();

        int goal = liste_points.indexOf(p2);
        int current = liste_points.indexOf(p1);

        while(current != goal) {
            result.add(liste_points.get(current));
            current = shortestPaths[current][goal];
        }

        return result;
    }

    public static double real_distance(int [][] shortestPaths, ArrayList<Point> points, Point p1, Point p2) {
        int result = 0;

        ArrayList<Point> chemin = getShortestPaths(shortestPaths, points, p1, p2);
        chemin.add(p2);

        for (int i = 0; i < chemin.size() - 1; i++) {
            result += chemin.get(i).distance(chemin.get(i + 1));
        }

        return result;
    }

    // local search
    public static ArrayList<Point> localSearch(ArrayList<Point> current, int edgeThreshold) {
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
                        if (current.get(k % current.size()).equals(d)) {
                            break;
                        }
                        else if (current.get(k % current.size()).equals(c)) {
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

    // Return true si ça se croise
    public static boolean intersect(Point a, Point b, Point c, Point d) {
        return ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d);
    }

    public static boolean ccw(Point a, Point b, Point c) {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x);
    }

    public static ArrayList<Point> adapt_result(int [][] shortestPaths, ArrayList<Point> points, ArrayList<Point> list){
        ArrayList<Point> adapted_result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            adapted_result.addAll(getShortestPaths(shortestPaths, points, list.get(i), list.get((i + 1) % list.size())));
        }
        return adapted_result;
    }

    public static ArrayList<Point> smartMerge(Graph a, Graph b, int edgeThreshold, int [][] shortestPaths){
        double score_a = Evaluator.score(GraphUtils.adapt_result(shortestPaths, a.points, a.solution));
        double score_b = Evaluator.score(GraphUtils.adapt_result(shortestPaths, b.points, b.solution));


        ArrayList<Point> result = new ArrayList<>(score_a < score_b ? a.solution : b.solution);

        for(int frame_size = 4; frame_size < (result.size()/2); frame_size++){
            for(int i = 0; i < a.solution.size(); i++){
                for(int j = 0; j < b.solution.size(); j++){
                    if(subList(a.solution, i, i + frame_size).containsAll(subList(b.solution, j, j + frame_size))){
                        ArrayList<Point> tmp_a = new ArrayList<>(a.solution);
                        for(int k = i; k < i+frame_size; k++){
                            tmp_a.set(k % a.solution.size(), b.solution.get((k-i+j) % b.solution.size()));
                            if(k % a.solution.size() > 50){
                                System.out.println("erreur : " + k + " - " + a.solution.size() + " - " + (k % a.solution.size()));
                            }
                        }

                        ArrayList<Point> tmp_b = new ArrayList<>(b.solution);
                        for(int k = j; k < j+frame_size; k++){
                            tmp_b.set(k % b.solution.size(), a.solution.get((k-j+i) % a.solution.size()));
                            if(k % b.solution.size() > 50){
                                System.out.println("erreur : " + k + " - " + b.solution.size() + " - " + (k % b.solution.size()));
                            }
                        }
                        double new_score_a = Evaluator.score(GraphUtils.adapt_result(shortestPaths, a.points, tmp_a));
                        double new_score_b = Evaluator.score(GraphUtils.adapt_result(shortestPaths, b.points, tmp_b));

                        if(new_score_a < score_a){
                            a.solution = tmp_a;
                            score_a = new_score_a;
                        }
                        if(new_score_b < score_b){
                            b.solution = tmp_b;
                            score_b = new_score_b;
                        }

                    }
                }
            }
        }


        score_a = Evaluator.score(GraphUtils.adapt_result(shortestPaths, a.points, a.solution));
        score_b = Evaluator.score(GraphUtils.adapt_result(shortestPaths, b.points, b.solution));

        return score_a < score_b ? a.solution : b.solution;
    }

    private static ArrayList<Point> subList(ArrayList<Point> liste, int depart, int fin){
        if(fin != liste.size()) fin = fin % liste.size();
        ArrayList<Point> result = new ArrayList<Point>();

        if(depart <= fin) {
            result.addAll(liste.subList(depart, fin));
        } else {
            result.addAll(liste.subList(depart, liste.size()));
            result.addAll(liste.subList(0, fin));
        }
        return result;
    }


}
