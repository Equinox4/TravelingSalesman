package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class TeamRandom {
  public ArrayList<Point> calculAngularTSP(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
    int[][] paths=new int[points.size()][points.size()];
    for (int i=0;i<paths.length;i++) for (int j=0;j<paths.length;j++) paths[i][j]=i;

    double[][] dist=new double[points.size()][points.size()];

    for (int i=0;i<paths.length;i++) {
      for (int j=0;j<paths.length;j++) {
        if (i==j) {dist[i][i]=0; continue;}
        if (points.get(i).distance(points.get(j))<=edgeThreshold) dist[i][j]=points.get(i).distance(points.get(j)); else dist[i][j]=Double.POSITIVE_INFINITY;
        paths[i][j]=j;
      }
    }

    for (int k=0;k<paths.length;k++) {
      for (int i=0;i<paths.length;i++) {
        for (int j=0;j<paths.length;j++) {
          if (dist[i][j]>dist[i][k] + dist[k][j]){
            dist[i][j]=dist[i][k] + dist[k][j];
            paths[i][j]=paths[i][k];

          }
        }
      }
    }

    ArrayList<Point> result = new ArrayList<Point>();
    int s,t;
    Point p,q;
    for (int index=0;index<hitPoints.size();index++){
      s=points.indexOf(hitPoints.get(index));
      t=points.indexOf(hitPoints.get((index+1)%hitPoints.size()));
      while (paths[s][t]!=t && paths[s][t]!=s){
        if (points.get(s).distance(points.get(paths[s][t]))>edgeThreshold){
          System.err.println("FATAL ERROR. VERY PANICKED.");
        }

        p=points.get(s);
        q=points.get(paths[s][t]);

        result.add(p);

        s=paths[s][t];
      }
      if (points.get(s).distance(points.get(paths[s][t]))>edgeThreshold || paths[s][t]!=t){
        System.err.println("FATAL ERROR. VERY PANICKED.");
      }

      p=points.get(s);
      q=points.get(t);

      result.add(p);
      result.add(q);
    }
    return result;
  }

}
