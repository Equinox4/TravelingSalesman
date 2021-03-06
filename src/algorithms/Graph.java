package algorithms;

import java.awt.*;
import java.util.ArrayList;

public class Graph {
    public int id;
    public ArrayList<Point> points;
    public ArrayList<Point> solution;
    public ArrayList<Point> hitPoints;

    public Graph(int id, ArrayList<Point> points, ArrayList<Point> solution){
        this.id = id;
        this.points = points;
        this.solution = solution;
        this.hitPoints = solution; // pour que ça soit clair partout dans le code
    }
}
