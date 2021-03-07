package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class DefaultTeam {
    private enum Mode {
        INIT, // a n'utiliser qu'une fois, après avoir totalement nettoyé la BDD et avant de passer aux autres modes
        CREATE_SOLUTION,
        IMPROVE_SOLUTION,
        GATHER_SOLUTIONS
    };
    private static final Mode DEFAULT_MODE = Mode.IMPROVE_SOLUTION;

    public static final int TOP_TO_KEEP = 10;
    private static final int MAX_GREEDY_RANDOMNESS = 65; // en %
    // Pas de variable statique pour rendre possible l'utilisation sur plusieurs CPUs
    protected Integer NB_GRAPHS_TO_IMPROVE = 5;
    private static final Integer IMPROVE_TIMEOUT = 300000; // en millisecondes
    private int edgeThreshold = -1;
    private StorageUtils storage = new StorageUtils();
    private Random random_generator = new Random();

    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        if(this.edgeThreshold == -1) this.edgeThreshold = edgeThreshold;

        System.out.println("edgeThreshold : " + edgeThreshold);

        ArrayList<Point> result = new ArrayList<>();

        switch (DEFAULT_MODE) {
            case INIT :
                try {
                    storage.saveGraph(points, hitPoints);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                break;

            case CREATE_SOLUTION:
                // reception graphe
                Graph graph = storage.getOneGraphWithNoSolution();
                //Graph graph = storage.getGraphFromId(356);
                ArrayList<Point> graph_hitPoints;

                if (graph != null){
                    graph_hitPoints = graph.hitPoints;
                } else {
                    System.out.println("Aucun graphe sans solution n'a ete trouvé, creation d'une nouvelle solution pour un graphe en ayant deja");
                    graph = storage.getOneRandomGraphWithSolution();

                    if (graph == null || graph.points.isEmpty() || graph.hitPoints.isEmpty()) {
                        System.out.println("Aucun (graphe + hitpoints) disponibles, veuillez vous connecter à internet");
                        System.exit(-1);
                    }

                    // on reccupere les hitPoints du graphe original pour pouvoir calculer la validité de la soluce
                    graph_hitPoints = storage.getGraphFromId(graph.id).hitPoints;
                }

                // traitement graphe
                int [][] shortestPaths = GraphUtils.calculShortestPaths(graph.points, edgeThreshold);
                // pas une bonne solution parce que ça pourrait empecher la sauvegarde de la recherche si les hitpoints sont en fait le résultat d'un calcul précédent (peut arriver si on est en offline)
                ArrayList<Point> best_result = GraphUtils.adapt_result(shortestPaths, graph.points, graph.hitPoints);

                for (int i = 0; i < 80; i++) {
                    result = start_solution(graph.points, graph.hitPoints);

                    System.out.printf("Score [it:%d][id:%d] : %d (best:%d)%n", i, graph.id, (int) Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, result)), (int) Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, best_result)));

                    // j'ai ajouté une verif de validité à cause du graphe 356 qui cree des solution bizarres
                    if (result.size() == 50
                            && Evaluator.isValid(graph.points, GraphUtils.adapt_result(shortestPaths, graph.points, result), graph_hitPoints, edgeThreshold)
                            && Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, result)) < Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, best_result))) {
                        best_result = result;
                    }
                }

                System.out.println("MEILLEUR SCORE : " + Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, best_result)));

                // sauvegarde résultat
                try {
                    storage.saveSolution(graph.id, best_result, (int) Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, best_result)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return hitPoints;

            case IMPROVE_SOLUTION:
                Graph graphe = storage.getGraphToImprove(TOP_TO_KEEP);
                result = graphe.solution;
                Graph tmp_graphe = storage.getGraphFromId(graphe.id);

                int [][] shortest_paths = GraphUtils.calculShortestPaths(
                        graphe.points,
                        edgeThreshold
                );
                if (
                    !(
                        Evaluator.isValid(
                                graphe.points,
                                result,
                                tmp_graphe.hitPoints,
                                edgeThreshold
                        )
                    ) && !(
                        Evaluator.isValid(
                                tmp_graphe.points,
                                GraphUtils.adapt_result(
                                        shortest_paths,
                                        graphe.points,
                                        result),
                                tmp_graphe.hitPoints,
                                edgeThreshold)
                    )
                ){
                    System.out.println("Solution corrompue [graphId:"+graphe.id+"], suppression et travail sur une autre solution ...");
                    storage.deleteSolution(graphe.id, result);
                    //calculAngularTSP(graphe.points, edgeThreshold, tmp_graphe.hitPoints);
                } else {
                    result = improve_solution(graphe);
                    try {
                        if(result != null){
                            System.out.println("Sauvegarde de l'amelioration [graphId:"+graphe.id+"] ...");
                            storage.saveSolution(graphe.id, result, (int) Evaluator.score(GraphUtils.adapt_result(shortest_paths, graphe.points, result)));
                        } else {
                            System.out.println("l'amelioration est corrompue, elle ne sera pas enregistree");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return hitPoints;

            case GATHER_SOLUTIONS: // besoin d'internet
                result = storage.getBestSolution(points, hitPoints);
                if(result.size() == 50){
                    result = GraphUtils.adapt_result(GraphUtils.calculShortestPaths(points, edgeThreshold), points, result);
                } else if(result.size() < 50) {
                    int graph_id = storage.getIdFromGraph(points, hitPoints);
                    System.out.println("Solution corrompue [graphId:"+graph_id+"] (contient moins de 50 pts), recuperation d'une nouvelle solution ...");
                    storage.deleteSolution(graph_id, result);
                    result = calculAngularTSP(points, edgeThreshold, hitPoints);
                }

                if(!Evaluator.isValid(points, result, hitPoints, edgeThreshold)){
                    int graph_id = storage.getIdFromGraph(points, hitPoints);
                    System.out.println("Solution corrompue [graphId:"+graph_id+"], suppression et recuperation d'une nouvelle solution ...");
                    storage.deleteSolution(graph_id, result);
                    result = calculAngularTSP(points, edgeThreshold, hitPoints);
                }
                break;
            default:
                System.out.println("ERREUR : MODE INCONNU");
                System.exit(-1);
        }

        return result;
    }

    private ArrayList<Point> improve_solution(Graph graph) {
        long startTime = System.currentTimeMillis();
        ArrayList<Point> points = graph.points;
        ArrayList<Point> result = graph.solution;
        int [][] shortestPaths = GraphUtils.calculShortestPaths(points, edgeThreshold);
        if(result.size() > 50){
            System.out.println("Reccupération d'une solution erronée, tentative de correction ...");
            Graph tmp = storage.getGraphFromId(graph.id);
            if(Evaluator.isValid(points, result, tmp.hitPoints, edgeThreshold)){
                ArrayList<Point> tmp_list = new ArrayList<>();
                for(Point pt : result){
                    if(tmp.hitPoints.contains(pt)) tmp_list.add(pt);
                }
                if(Evaluator.isValid(points, GraphUtils.adapt_result(shortestPaths, points, result), tmp.hitPoints, edgeThreshold)){
                    result = tmp_list;
                    System.out.println("Solution corrigée avec succes !");
                } else {
                    System.out.println("Echec de correction, creation d'une nouvelle solution ...");
                    storage.deleteSolution(graph.id, graph.solution);
                    result = start_solution(tmp.points, tmp.hitPoints);
                }
            } else {
                System.out.println("Solution corrompue, creation d'une nouvelle solution ...");
                storage.deleteSolution(graph.id, graph.solution);
                result = start_solution(tmp.points, tmp.hitPoints);
            }

        } else if(result.size() < 50) {
            System.out.println("Solution corrompue (contient moins de 50 pts), creation d'une nouvelle solution ...");
            storage.deleteSolution(graph.id, graph.solution);
            Graph tmp = storage.getGraphFromId(graph.id);
            result = start_solution(tmp.points, tmp.hitPoints);
        }
        //int [][] shortestPaths = GraphUtils.calculShortestPaths(points, edgeThreshold);
        ArrayList<Point> adapted_result = null;
        ArrayList<Point> best_result = result;

        while (System.currentTimeMillis() - startTime <= IMPROVE_TIMEOUT) {
            if (random_generator.nextBoolean()) {
                result = GraphUtils.localSearch(best_result, edgeThreshold);
            }
            else {
                result = bruteForce_window(best_result, 8);
            }

            adapted_result = GraphUtils.adapt_result(shortestPaths, points, result);
            System.out.println("Score : " + Evaluator.score(adapted_result));
            if (result.size() == 50 && Evaluator.score(adapted_result) < Evaluator.score(GraphUtils.adapt_result(shortestPaths, points, best_result))) best_result = result;
        }

        if(best_result.size() == 50)
            return best_result;
        else
            return null;
    }

    private ArrayList<Point> start_solution(ArrayList<Point> points, ArrayList<Point> hitPoints) {
        int randomness = random_generator.nextInt(MAX_GREEDY_RANDOMNESS);
        int [][] shortestPaths = GraphUtils.calculShortestPaths(points, edgeThreshold);

        ArrayList<Point> result = greedy(shortestPaths, points, hitPoints, randomness);
        ArrayList<Point> adapted_result = new ArrayList<>();
        ArrayList<Point> best_result = hitPoints; // pas une bonne solution parce que ça pourrait empecher la sauvegarde de la recherche si les hitpoints sont en fait le résultat d'un calcul précédent (peut arriver si on est en offline)

        for (int i = 0; i < 600; i++){
            if(random_generator.nextBoolean()){
                result = GraphUtils.localSearch(result, edgeThreshold);
            } else {
                result = bruteForce_window(result, 6);
            }

            adapted_result = GraphUtils.adapt_result(shortestPaths, points, result);
            //System.out.println("Score : " + Evaluator.score(adapted_result));
            if(Evaluator.score(adapted_result) < Evaluator.score(GraphUtils.adapt_result(shortestPaths, points, best_result))) best_result = result;
        }

        // local search + brute fenetre 6
        return best_result;
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
