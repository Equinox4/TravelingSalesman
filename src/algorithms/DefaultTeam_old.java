package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import algorithms.Evaluator;

import java.util.Collections;

public class DefaultTeam_old {
    protected static int[][] shortestPaths;
    protected static int[][] shortestPathsHitPoints;
    protected static ArrayList<Point> points;
    protected static ArrayList<Point> hitPoints;

    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        // lancer les tests unitaires 
        UnitTests.unit_tests();

        // calculer tous les plus courts chemins
        this.points = points;
        this.shortestPaths = calculShortestPaths(points, edgeThreshold);
        this.shortestPathsHitPoints = calculShortestPaths(hitPoints, 1000);
        

        ArrayList<Point> result = new ArrayList<>();
        ArrayList<Point> adapted_result = adapt_result(result);
        

        
        
        

        // Greedy
        //int start = 5;
        result = geedy(hitPoints);
        ArrayList<Point> best_result = result;
        

        for(int i = 0; i < 100; i++){
            result = geedy(result);

            adapted_result = adapt_result(result);
            System.out.println("score : " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(adapt_result(best_result))) best_result = result;
        }

        System.out.println("Best score : " + Evaluator.score(adapt_result(best_result)));


        

        
        long startTime = System.currentTimeMillis();

        
        double tmp_score = Evaluator.score(adapted_result);
        adapted_result = adapt_result(result);
        String gains = "-" + tmp_score;
        
        

        for (int i = 0; i < 50000; i++){
            result = improve_random_segment(best_result, 10);
            adapted_result = adapt_result(result);
            if(Evaluator.score(adapted_result) < Evaluator.score(adapt_result(best_result))) best_result = result;
        }
        System.out.println("Score geedy_segment : [" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapt_result(best_result)));

        // localsearch
        for (int i = 0; i < 2000; i++){
            result = localSearch(best_result, edgeThreshold);
            adapted_result = adapt_result(result);
            if(Evaluator.score(adapted_result) < Evaluator.score(adapt_result(best_result))) best_result = result;
        }
        System.out.println("Score localSearch : [" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapt_result(best_result)));

        for (int i = 0; i < 50000; i++){
            result = improve_random_segment(best_result, 10);
            adapted_result = adapt_result(result);
            if(Evaluator.score(adapted_result) < Evaluator.score(adapt_result(best_result))) best_result = result;
        }
        System.out.println("Score geedy_segment2 : [" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapt_result(best_result)));



        if(true) return adapt_result(best_result);


        for (int i = 0; i < 2000; i++){
            result = all_cores_bruteForce(result, 10);
            /*
            if(Evaluator.score(adapt_result(result)) > Evaluator.score(adapt_result(tmp_result))){
                result = tmp_result;
            }
            */
            //System.out.println("1");
            adapted_result = adapt_result(best_result);
            //if(((System.currentTimeMillis() - startTime)/1000) > 100) break;
            System.out.println("Score multi : [" + 10 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(best_result)) best_result = adapted_result;
            if(((System.currentTimeMillis() - startTime)/1000) > 250) break;
        }

        for (int i = 0; i < 2000; i++){
            result = bruteForce_window(result, 9);
            //System.out.println("1");
            adapted_result = adapt_result(result);
            System.out.println("Score multi : [" + 9 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(best_result)) best_result = adapted_result;
            if(((System.currentTimeMillis() - startTime)/1000) > 275) break;
        }

        /*
        gains += "\nb" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);
        for (int i = 0; i < 10; i++){
            result = all_cores_bruteForce(result, 9);
            //System.out.println("1");
            adapted_result = adapt_result(result);
            //if(((System.currentTimeMillis() - startTime)/1000) > 100) break;
            System.out.println("Score multi : [" + 9 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
        }
        



        gains += "\nc" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);
        for (int i = 0; i < 1000; i++){
            result = bruteForce_window(result, 7);
            //System.out.println("1");
            adapted_result = adapt_result(result);
            //if(((System.currentTimeMillis() - startTime)/1000) > 100) break;
            System.out.println("Score : [" + 7 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
        }
        gains += "\nd" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);


        for (int i = 0; i < 30000; i++){
            result = bruteForce_window(result, 5);
            //System.out.println("1");
            adapted_result = adapt_result(result);
            //if(((System.currentTimeMillis() - startTime)/1000) > 100) break;
            System.out.println("Score : [" + 7 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
        }
        gains += "\ne" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);
        */
        

        // localsearch
        for (int i = 0; i < 2000; i++){
            result = localSearch(result, edgeThreshold);
        }
        adapted_result = adapt_result(result);
        if(Evaluator.score(adapted_result) < Evaluator.score(best_result)) best_result = adapted_result;

        gains += "\nf" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);


        // bruteforce
        // 7846.960937699307 -> sans brute avant decroisement
        //System.out.println("Score [" + ((System.currentTimeMillis() - startTime)/1000) + "]: " + Evaluator.score(adapted_result));
        /*
        for (int i = 0; i < 10000; i++){
            result = bruteForce_window(result, 3);
        }
        adapted_result = adapt_result(result);
        //System.out.println("Score [" + ((System.currentTimeMillis() - startTime)/1000) + "]: " + Evaluator.score(adapted_result));
        gains += "\ng" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);

        for (int i = 0; i < 30000; i++){
            result = bruteForce_window(result, 5);
        }
        adapted_result = adapt_result(result);
        //System.out.println("Score [" + ((System.currentTimeMillis() - startTime)/1000) + "]: " + Evaluator.score(adapted_result));
        gains += "\nh" + (tmp_score - Evaluator.score(adapted_result));
        tmp_score = Evaluator.score(adapted_result);
        System.out.println(gains);
        */

        /*
        for (int i = 0; i < 10000; i++){
            result = bruteForce_window(result, 6);
            adapted_result = adapt_result(result);

            System.out.println("Score : [" + 7 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
        }
        */

        for (int i = 0; i < 2000; i++){
            result = all_cores_bruteForce(result, 10);
            //System.out.println("1");
            adapted_result = adapt_result(result);
            //if(((System.currentTimeMillis() - startTime)/1000) > 100) break;
            System.out.println("Score multi post : [" + 10 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(best_result)) best_result = adapted_result;
            if(((System.currentTimeMillis() - startTime)/1000) > 330) break;
        }
       
        for (int i = 0; i < 2000; i++){
            result = bruteForce_window(result, 7);
            adapted_result = adapt_result(result);
            //if(((System.currentTimeMillis() - startTime)/1000) > 100) break;
            System.out.println("Score post : [" + 7 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(best_result)) best_result = adapted_result;
            if(((System.currentTimeMillis() - startTime)/1000) > 350) break;
        }
        
        
        /*

        for (int i = 0; i < 300; i++){
            result = bruteForce_window(result, 8);
            adapted_result = adapt_result(result);
            System.out.println("Score : [" + 8 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] " + Evaluator.score(adapted_result));
        }
        adapted_result = adapt_result(result);
        System.out.println("Score : " + Evaluator.score(adapted_result));

        for (int i = 0; i < 90; i++){
            result = bruteForce_window(result, 10);
            adapted_result = adapt_result(result);
            System.out.println("Score [" + 10 + "][" + ((System.currentTimeMillis() - startTime)/1000) + "] : " + Evaluator.score(adapted_result));
        }
        adapted_result = adapt_result(result);
        System.out.println("Score : " + Evaluator.score(adapted_result));
        */

        return best_result;

        //  [java] Score : 7846.960937699307
    }

    protected static ArrayList<Point> adapt_result(ArrayList<Point> list){
        ArrayList<Point> adapted_result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            adapted_result.addAll(getShortestPaths(shortestPaths, points, list.get(i), list.get((i + 1) % list.size())));
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

    public static boolean ccw(Point a, Point b, Point c) {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x);
    }

    // Return true si ça se croise
    public static boolean intersect(Point a, Point b, Point c, Point d) {
        return ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d);
    }

    private static ArrayList<Point> getShortestPaths(int [][] shortestPaths, ArrayList<Point> liste_points, Point p1, Point p2){
        ArrayList<Point> result = new ArrayList<>();

        int goal = liste_points.indexOf(p2);
        int current = liste_points.indexOf(p1);

        while(current != goal) {
            result.add(liste_points.get(current));
            current = shortestPaths[current][goal];
        }

        return result;
    }

    private static double real_distance(Point p1, Point p2) {
        int result = 0;

        ArrayList<Point> chemin = getShortestPaths(shortestPaths, points, p1, p2);
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
     * @param liste_points
     * @param edgeThreshold
     * @return une matrice à deux dimensions
     */
    public static int[][] calculShortestPaths(ArrayList<Point> liste_points, int edgeThreshold) {
        int[][] paths = new int[liste_points.size()][liste_points.size()];
        for (int i= 0; i < paths.length; i++) {
            for (int j = 0; j < paths.length; j++) {
                paths[i][j] = i;
            }
        }

        double[][] dist = new double[liste_points.size()][liste_points.size()];

        for (int i = 0; i < paths.length; i++) {
            for (int j = 0; j < paths.length; j++) {
                if (i == j) {
                    dist[i][i] = 0;
                    continue;
                }
                if (liste_points.get(i).distance(liste_points.get(j)) <= edgeThreshold) {
                    dist[i][j] = liste_points.get(i).distance(liste_points.get(j));
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

    protected static ArrayList<Point> bruteForce_window(ArrayList<Point> current_list, int window) {
        if (window >= current_list.size()) return current_list;

        Random random_generator = new Random();
        int r_num = random_generator.nextInt(current_list.size() - 1);

        double current_score = Evaluator.score(current_list);
        double new_score = Integer.MAX_VALUE;

        ArrayList<Point> liste = current_list;

        //System.out.print("a");
        long startTime = System.currentTimeMillis();

        // mettre un for avec toutes les permutations possibles
        while (current_score < new_score) {
            liste = new ArrayList<>(current_list);
            ArrayList<Point> temp_list = new ArrayList<>();
            for (int i = r_num; i < r_num + window; i++) {
                temp_list.add(liste.get(i % liste.size()));
            }
            Collections.shuffle(temp_list);

            //System.out.print("-");

            for (int i = r_num; i < r_num + window; i++) {
                liste.set(i % liste.size(), temp_list.remove(0));
            }

            if(((System.currentTimeMillis() - startTime)/1000) > 15) return current_list;

            //System.out.print("+");
            new_score = Evaluator.score(liste);

            //System.out.print("\r ");
            // On ne calcul le nouveau score que si l'approximation semble donner un meilleur score
            /*
            if(current_score > new_score){
                System.out.print("\r.");
                ArrayList<Point> adapted_result = adapt_result(liste);
                new_score = Evaluator.score(adapted_result);
                if(current_score < new_score) {
                    System.out.print("\r+");
                    return current_list;
                }
            }
            */
        }

        return liste;
    }

    // Held–Karp algorithm
    public ArrayList<Point> hka(ArrayList<Point> window) {
/*
function algorithm TSP (G, n) is
    for k := 2 to n do
        C({k}, k) := d1,k
    end for

    for s := 2 to n−1 do
        for all S ⊆ {2, . . . , n}, |S| = s do
            for all k ∈ S do
                C(S, k) := minm≠k,m∈S [C(S\{k}, m) + dm,k]
            end for
        end for
    end for

    opt := mink≠1 [C({2, 3, . . . , n}, k) + dk, 1]
    return (opt)
end function
*/
        return null;
    }


    private static ArrayList<Point> all_cores_bruteForce(ArrayList<Point> liste, int window){
        ArrayList<Point> adapted_result = adapt_result(liste);
        double score_liste = Evaluator.score(adapted_result);


        ArrayList<Point> result = liste;

        ThreadGroup tg = new ThreadGroup("main");
        int np = Runtime.getRuntime().availableProcessors();

        ArrayList<MultiCPUProcess> sims = new ArrayList<MultiCPUProcess>();
        //for (int i=0;i<np;i++) sims.add(new MultiCPUProcess(tg, "proces"+i, liste, window));

        int i=0;
        while (i<sims.size()){
            if (tg.activeCount()<np){ // do we have available CPUs?
                MultiCPUProcess sim = sims.get(i);
                sim.start();
                i++;
            } else {
                try {Thread.sleep(100);} /*wait 0.1 second before checking again*/
                catch (InterruptedException e) {e.printStackTrace();}
            }

        }

    

        while(tg.activeCount()>0){
            for (i=0;i<sims.size();i++) {
                MultiCPUProcess sim = sims.get(i);
                double score_tmp = sim.getScore();
                if((score_tmp != -1) && (score_tmp < score_liste)){
                    result = sim.getListe();
                    for(MultiCPUProcess sim_to_stop : sims){
                        sim_to_stop.stop();
                    }
                }
            }
        }
        return result;
    }

    protected static ArrayList<Point> geedy(ArrayList<Point> hitPoints){
        Random random_generator = new Random();
        int start = random_generator.nextInt(hitPoints.size() - 1);

        ArrayList<Point> result = new ArrayList<>();
        ArrayList<Point> rest = new ArrayList<>();
        rest.addAll(hitPoints);

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

        return result;
    }

    protected static ArrayList<Point> improve_random_segment(ArrayList<Point> hitPoints, int window){
        Random random_generator = new Random();
        int r_num = random_generator.nextInt(hitPoints.size() - 1);

        ArrayList<Point> temp_list = getShortestPaths(
            shortestPathsHitPoints, 
            hitPoints, 
            hitPoints.get(r_num % hitPoints.size()), 
            hitPoints.get((r_num + window) % hitPoints.size())
        );
        ArrayList<Point> result = new ArrayList<>(hitPoints);

        System.out.println("temp_list : " + temp_list.size());
        System.out.println("window : " + window);

        for (int i = r_num; i < r_num + window; i++) {
            result.set(i % hitPoints.size(), temp_list.remove(0));
        }

        return result;
    }
    
}
