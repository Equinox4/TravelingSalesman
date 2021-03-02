package algorithms;

import java.awt.Point;
import java.util.ArrayList;

public class Evaluator {
  public static boolean isValid(ArrayList<Point>points, ArrayList<Point>inpts, ArrayList<Point> hp, int edgeThreshold){
    ArrayList<Point> pts = (ArrayList<Point>)inpts.clone();
    for (int i=1;i<pts.size()+1;i++){
      if (pts.get(i-1).equals(pts.get(i%pts.size()))){
        pts.remove(i%pts.size());
        i--;
      }
    }
    if (!points.containsAll(pts) || !pts.containsAll(hp)) return false;
    for (int i=0;i<pts.size();i++) if (pts.get((i+1)%pts.size()).distance(pts.get(i))>edgeThreshold) return false; return true;
  }
  public static double score(ArrayList<Point> inpts){
    ArrayList<Point> pts = (ArrayList<Point>)inpts.clone();
    for (int i=1;i<pts.size()+1;i++){
      if (pts.get(i-1).equals(pts.get(i%pts.size()))){
        pts.remove(i%pts.size());
        i--;
      }
    }
    if (pts.size()<=1) return 0;
    double result = angle(pts.get(pts.size()-1),pts.get(0),pts.get(1))+pts.get(pts.size()-1).distance(pts.get(0));
    for (int i=0;i<pts.size()-1;i++) {
      result+=pts.get(i).distance(pts.get(i+1))+angle(pts.get(i),pts.get(i+1),pts.get((i+2)%pts.size()));
    }
    return result;
  }
  public static double angle(Point p, Point q, Point r){
    if (p.distance(q)*q.distance(r)==0) return 0;
    double dot = ((q.getX()-p.getX()) * (r.getX()-q.getX()) + (q.getY()-p.getY()) * (r.getY()-q.getY()));
    double unitDot = dot/ (p.distance(q)*q.distance(r));
    if (unitDot<-1) unitDot=-1;
    if (unitDot>1) unitDot=1;
    double result = (100/Math.PI)*Math.acos(unitDot);
    return result;
  }
}
