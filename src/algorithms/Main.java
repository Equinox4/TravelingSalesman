package algorithms;

import java.util.ArrayList;

public class Main {

    private static final Integer MAIN_TIMEOUT = 20_000_000; // en millisecondes

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        DefaultTeam dt = new DefaultTeam();
        int edgeThreshold = 55; // c'est ce que j'ai vu en faisant un print

        while (System.currentTimeMillis() - startTime <= MAIN_TIMEOUT) {
            //dt.calculAngularTSP(new ArrayList<>(), edgeThreshold, new ArrayList<>());
            dt.experimental_improve_solution();
        }
    }
}
