package algorithms;

import javafx.util.Pair;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Collections;

public class DefaultTeam {
    public static final int TOP_TO_KEEP = 10;
    // Pas de variable statique pour rendre possible l'utilisation sur plusieurs CPUs
    protected String MODE = "IMPROVE";
    protected Integer IMPROVE_TIMEOUT = 20000000;
    protected Integer NB_GRAPHS_TO_IMPROVE = 5;
    StorageUtils storage;
    int edgeThreshold = -1;

    public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
        if(storage == null) this.storage = new StorageUtils();
        if(this.edgeThreshold == -1) this.edgeThreshold = edgeThreshold;

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
                Pair<ArrayList<Point>, ArrayList<Point>> graph = storage.getOneGraphWithNoSolution();

                if(graph == null){
                    System.out.println("Aucun graphe sans solution n'a été trouvé, création d'une nouvelle solution pour un graphe existant");
                }

                start_solution(graph.getKey(), graph.getValue());

                break;
            case "IMPROVE_SOLUTION":
                // prendre le graphe ayant le moins de solutions s'il y en a un avec moins de TOP_TO_KEEP solution sinon prendre au hasard
                // prendre une des TOP_TO_KEEP meilleures solutions et travailler dessus
                break;
            case "GATHER_SOLUTIONS":
                // besoin d'internet -> get best score de graphe ayant hash = hash(points + hitpoints)
                break;
            default:
                System.out.println("ERREUR : MODE INCONNU");
                System.exit(-1);
        }


        return result;
    }

    private void start_solution(ArrayList<Point> key, ArrayList<Point> value) {
    }
}