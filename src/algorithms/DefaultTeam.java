package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class DefaultTeam {
    public static final int TOP_TO_KEEP = 10;
    private static final int MAX_GREEDY_RANDOMNESS = 60; // en %
    // Pas de variable statique pour rendre possible l'utilisation sur plusieurs CPUs
    protected static final String MODE = "CREATE_SOLUTION";
    protected static final Integer MAIN_TIMEOUT = 20000000;
    protected static final Integer IMPROVE_TIMEOUT = 300;
    protected Integer NB_GRAPHS_TO_IMPROVE = 5;
    StorageUtils storage;
    int edgeThreshold = -1;

    public static void main(String [] args){
        long startTime = System.currentTimeMillis();
        DefaultTeam dt = new DefaultTeam();
        int edgeThreshold = 55; // c'est ce que j'ai vu en faisant un print
        while(true){
            dt.calculAngularTSP(new ArrayList<>(), edgeThreshold, new ArrayList<>());
            if(((System.currentTimeMillis() - startTime)/1000) > MAIN_TIMEOUT) break;
        }
    }

    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        if(storage == null) this.storage = new StorageUtils();
        if(this.edgeThreshold == -1) this.edgeThreshold = edgeThreshold;

        System.out.println("edgeThreshold : " + edgeThreshold);

        ArrayList<Point> result = new ArrayList<>();


        switch (MODE) {
            case "INIT" :
                try {
                    storage.saveGraph(points, hitPoints);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                break;
            case "CREATE_SOLUTION":
                // reception graphe
                Graph graph = storage.getOneGraphWithNoSolution();
                if(graph == null){
                    System.out.println("Aucun graphe sans solution n'a été trouvé, création d'une nouvelle solution pour un graphe existant");
                    graph = storage.getOneRandomGraphWithSolution();
                    if(graph == null || graph.points.isEmpty() || graph.hitPoints.isEmpty()){
                        System.out.println("Aucun (graphe + hitpoints) disponibles, veuillez vous connecter à internet");
                        System.exit(-1);
                    }
                }

                // traitement graphe
                int [][] shortestPaths = GraphUtils.calculShortestPaths(graph.points, edgeThreshold);
                ArrayList<Point> best_result = GraphUtils.adapt_result(shortestPaths, graph.points, graph.hitPoints); // pas une bonne solution parce que ça pourrait empecher la sauvegarde de la recherche si les hitpoints sont en fait le résultat d'un calcul précédent (peut arriver si on est en offline)

                for (int i = 0; i < 200; i++){
                    result = start_solution(graph.points, graph.hitPoints);

                    System.out.println("Score [it:" + i + "][id:" + graph.id + "] : " + Evaluator.score(result) + " (best:" + Evaluator.score(best_result) + ")");
                    if(Evaluator.score(result) < Evaluator.score(best_result)) best_result = result;
                }
                System.out.println("MEILLEUR SCORE : " + Evaluator.score(best_result));


                // sauvegarde résultat
                try {
                    storage.saveSolution(graph.id, best_result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(true) return hitPoints;

                break;
            case "IMPROVE_SOLUTION":
                Graph graphe = storage.getGraphToImprove(TOP_TO_KEEP);
                result = improve_solution(graphe.points, graphe.hitPoints);
                try {
                    storage.saveSolution(graphe.id, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(true) return hitPoints;
                break;
            case "GATHER_SOLUTIONS": // besoin d'internet
                result = storage.getBestSolution(points, hitPoints);
                break;
            default:
                System.out.println("ERREUR : MODE INCONNU");
                System.exit(-1);
        }


        return result;
    }

    private ArrayList<Point> improve_solution(ArrayList<Point> points, ArrayList<Point> result) {
        long startTime = System.currentTimeMillis();
        Random random_generator = new Random();
        int [][] shortestPaths = GraphUtils.calculShortestPaths(points, edgeThreshold);
        ArrayList<Point> adapted_result = null;
        ArrayList<Point> best_result = result;

        while(true){
            if(random_generator.nextBoolean()){
                result = GraphUtils.localSearch(result, edgeThreshold);
            } else {
                result = bruteForce_window(result, 8);
            }
            adapted_result = GraphUtils.adapt_result(shortestPaths, points, result);
            System.out.println("Score : " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(GraphUtils.adapt_result(shortestPaths, points, best_result))) best_result = adapted_result;
            if(((System.currentTimeMillis() - startTime)/1000) > IMPROVE_TIMEOUT) break;
        }

        return best_result;
    }

    private ArrayList<Point> start_solution(ArrayList<Point> points, ArrayList<Point> hitPoints) {
        Random random_generator = new Random();
        int randomness = random_generator.nextInt(MAX_GREEDY_RANDOMNESS);
        int [][] shortestPaths = GraphUtils.calculShortestPaths(points, edgeThreshold);

        ArrayList<Point> result = greedy(shortestPaths, points, hitPoints, randomness);
        ArrayList<Point> adapted_result = new ArrayList<>();
        ArrayList<Point> best_result = hitPoints; // pas une bonne solution parce que ça pourrait empecher la sauvegarde de la recherche si les hitpoints sont en fait le résultat d'un calcul précédent (peut arriver si on est en offline)

        for (int i = 0; i < 333; i++){
            if(random_generator.nextBoolean()){
                result = GraphUtils.localSearch(result, edgeThreshold);
            } else {
                result = bruteForce_window(result, 6);
            }

            adapted_result = GraphUtils.adapt_result(shortestPaths, points, result);
            //System.out.println("Score : " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(GraphUtils.adapt_result(shortestPaths, points, best_result))) best_result = adapted_result;
        }

        // local search + brute fenetre 6
        return adapted_result;
    }


    protected static ArrayList<Point> greedy(int [][] shortestPaths, ArrayList<Point> points, ArrayList<Point> hitPoints, int randomness){
        Random random_generator = new Random();
        int start = random_generator.nextInt(hitPoints.size() - 1);

        ArrayList<Point> result = new ArrayList<>();
        ArrayList<Point> rest = new ArrayList<>();
        rest.addAll(hitPoints);

        result.add(rest.remove(start));
        while (!rest.isEmpty()) {
            Point last = result.get(result.size() - 1);
            Point next = rest.get(0);

            if(random_generator.nextInt(100) < randomness)

            for (Point p : rest) {
                if (GraphUtils.real_distance(shortestPaths, points, p, last) < GraphUtils.real_distance(shortestPaths, points, next, last)) {
                    next = p;
                }
            }

            result.add(next);
            rest.remove(next);
        }

        return result;
    }


    protected static ArrayList<Point> bruteForce_window(ArrayList<Point> current_list, int window) {
        if (window >= current_list.size()) return current_list;

        Random random_generator = new Random();
        int r_num = random_generator.nextInt(current_list.size() - 1);

        double current_score = Evaluator.score(current_list);
        double new_score = Integer.MAX_VALUE;

        ArrayList<Point> liste = current_list;

        long startTime = System.currentTimeMillis();

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
            if(((System.currentTimeMillis() - startTime)/1000) > 15) return current_list;

            new_score = Evaluator.score(liste);
        }

        return liste;
    }


}