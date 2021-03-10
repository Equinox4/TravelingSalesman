package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Point;

public class MultiCPUProcess extends Thread {
    private int [][] shortestPaths;
    private Graph graph;

    private double score;
    private int window;


    double getScore() {
        return score;
    }

    ArrayList<Point> getListe() {
        return graph.solution;
    }

    MultiCPUProcess (ThreadGroup tg, String name, Graph graph, int window, int [][] shortestPaths) {
        super(tg,name);
        this.graph = new Graph(graph.id, graph.points, graph.solution);
        this.window = window;
        this.score = -1;
        this.shortestPaths = shortestPaths;
    }

    public void run() {
        graph.newSolution(DefaultTeam.bruteForce_window(graph.solution, window));
        score = Evaluator.score(GraphUtils.adapt_result(shortestPaths, graph.points, graph.solution));
    }
}
