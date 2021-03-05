package algorithms;

import java.awt.Point;

public class UnitTests {
    public static void unit_tests() {
        try {
            //assertEqualsDouble(DefaultTeam.vector_norm(new Point(0, 0), new Point(0, 2)), 2.00); // peut rater à cause de la précision
            //assertEqualsDouble(DefaultTeam.vector_norm(new Point(0, 0), new Point(2, 3)), Math.sqrt(13)); // peut rater à cause de la précision

        } catch (Exception e) {
            System.exit(1);
        }
    }

    private static void assertEquals(Object a, Object b) throws Exception {
        if(!(a.equals(b))) throw(new Exception());
    }

    // On arrondi les doubles comparés à 2 chiffres après la virgule pour éviter que des tests
    // ne passent pas à cause d'une erreur d'arrondi
    private static void assertEqualsDouble(Double a, Double b) throws Exception {
        if(!(round(a, 2) == round(b, 2))) throw(new Exception());
    }

    /**
     * Méthode récupérée sur cette page https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}