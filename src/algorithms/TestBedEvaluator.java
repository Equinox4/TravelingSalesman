package algorithms;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;

import algorithms.DefaultTeam;

public class TestBedEvaluator {
  private static String testFolder="aaga2016/angularTSPDG/tests";
  private static int edgeThreshold=55;
  private static boolean proxyPPTI = false;
  public static void main(String[] args) {
    for (int i=0;i<args.length;i++){
      if (args[i].charAt(0)=='-'){
        if (args[i+1].charAt(0)=='-'){
          System.err.println("Option "+args[i]+" expects an argument but received none");
          return;
        }
        switch (args[i]){
          case "-test":
            try {
              testFolder = args[i+1];
            } catch (Exception e){
              System.err.println("Invalid argument for option "+args[i]+": TestBed folder expected");
              return;
            }
            break;
          case "-edgeThreshold":
            try {
              edgeThreshold=Integer.parseInt(args[i+1]);
            } catch (Exception e){
              System.err.println("Invalid argument for option "+args[i]+": Integer type expected");
              return;
            }
            break;
          case "-proxyPPTI":
            proxyPPTI=true;
            break;
          default:
            System.err.println("Unknown option "+args[i]);
            return;
        }
        i++;
      }
    }
    evalFiles(proxyPPTI);
  }
  private static double result;
  private static int fails;

  protected static double getResult() {
    return result;
  }
  protected static int getFails() {
    return fails;
  }
  protected static void evalFiles(boolean proxyPPTI) {
    result = 0;
    fails=0;
    for (int index=0;index<200;index++){

      String line;
      String[] coordinates;
      ArrayList<Point> points=new ArrayList<Point>();
      try {
        if (proxyPPTI) {
          System.setProperty("http.proxyHost", "proxy.ufr-info-p6.jussieu.fr");
          System.setProperty("http.proxyPort", "3128");
          System.setProperty("https.proxyHost", "proxy.ufr-info-p6.jussieu.fr");
          System.setProperty("https.proxyPort", "3128");
        }
        InputStream url = new URL("https://www-apr.lip6.fr/~buixuan/files/"+testFolder+"/input"+index+".points").openStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(url));
        try {
          while ((line=input.readLine())!=null) {
            coordinates=line.split("\\s+");
            points.add(new Point(Integer.parseInt(coordinates[0]),
                  Integer.parseInt(coordinates[1])));
          }
          ArrayList<Point> hitPoints = new ArrayList<Point>();
          for (int i=points.size()/20;i<2*points.size()/20;i++) hitPoints.add((Point)points.get(i).clone());

          System.out.println("Input "+index+" successfully read. Computing...");
          ArrayList<Point> pts = new DefaultTeam().calculAngularTSP(points,edgeThreshold,hitPoints);
          System.out.println("   >>> Computation completed. Evaluating... ");
          if (!Evaluator.isValid(points,pts,hitPoints,edgeThreshold)) fails++;
          else result+=Evaluator.score(pts);
          System.out.println("   >>> Evaluation completed. Fails: "+fails);
          System.out.println("   >>> Average score (excluding fails): "+result/(index+1-fails));
        } catch (IOException e) {
          System.err.println("Exception: interrupted I/O.");
        } finally {
          try {
            input.close();
          } catch (IOException e) {
            System.err.println("I/O exception: unable to close files.");
          }
        }
      //} catch (FileNotFoundException e) {
      } catch (Exception e) {
        fails++;
        System.err.println("Input file not found.");
      }
    }
    
    System.out.println("--------------------------------------");
    System.out.println("");
    System.out.println("Total fails: " + fails);
    System.out.println("Average score: " + result/(200-fails));
    
    return;
  }
}
