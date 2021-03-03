package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Point;

public class MultiCPUProcess extends Thread {
    private ArrayList<Point> liste;

    private double score;
    private int window;


    double getScore() {
        return score;
    }

    ArrayList<Point> getListe() {
        return liste;
    }

    MultiCPUProcess (ThreadGroup tg, String name, ArrayList<Point> liste, int window) {
        super(tg,name);
        this.liste = liste;
        this.window = window;
        this.score = -1;
    }

    public void run() {
        liste = DefaultTeam.bruteForce_window(liste, window);
        score = Evaluator.score(DefaultTeam.adapt_result(liste));
    }



    /*
    
    -> 60x brute mono fenetre 9
    -> decroisement
    -> 40x brute multi fenetre 10
    -> 200x brute multi fenetre 7
    -> 10000x brute mono fenetre 5

    */
}
