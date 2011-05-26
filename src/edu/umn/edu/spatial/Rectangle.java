package edu.umn.edu.spatial;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.apache.hadoop.io.WritableComparable;

/**
 * A class that holds coordinates of a rectangle.
 * @author aseldawy
 *
 */
public class Rectangle implements WritableComparable<Rectangle>, Serializable, Cloneable {
  /**
   * Auto generated
   */
  private static final long serialVersionUID = 7801822896513739736L;

  public int id;
  public int x1;
  public int x2;
  public int y1;
  public int y2;
  public int type;

  public Rectangle() {
    this.set(0, 0, 0, 0, 0);
  }
  public int midX(){
    return (x1+x2)/2;
  }
  public int midY(){
    return (y1+y2)/2;
  }

  public Rectangle(int id, int x1, int y1, int x2, int y2) {
    int xl = Math.min(x1, x2);
    int xu = Math.max(x1, x2);
    int yl = Math.min(y1, y2);
    int yu = Math.max(y1, y2);

    this.set(id, xl, yl, xu, yu);
  }

  public void set(int id, int x1, int y1, int x2, int y2) {
    this.id = id;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  public boolean intersects(Rectangle r2) {
    return !(this.x2 < r2.x1 || r2.x2 < this.x1) &&
    !(this.y2 < r2.y1 || r2.y2 < this.y1);
  }
  public int getXlower() {return x1;}
  public int getXupper() {return x2;}
  public int getYlower() {return y1;}
  public int getYupper() {return y2;}
  public int getId() {return id;}

  public void write(DataOutput out) throws IOException {
    out.writeInt(id);
    out.writeInt(x1);
    out.writeInt(y1);
    out.writeInt(x2);
    out.writeInt(y2);
    out.writeInt(this.type);
  }

  public void readFields(DataInput in) throws IOException {
    this.id = in.readInt();
    this.x1 = in.readInt();
    this.y1 = in.readInt();
    this.x2 = in.readInt();
    this.y2 = in.readInt();
    this.type = in.readInt();
  }

  /**
   * Comparison is done by lexicographic ordering of attributes
   * < x1, y2, x2, y2>
   */
  public int compareTo(Rectangle rect2) {
    // Sort by id
    int difference = this.x1 - rect2.x1;
    if (difference == 0) {
      difference = this.y1 - rect2.y1;
      if (difference == 0) {
        difference = this.x2 - rect2.x2;
        if (difference == 0) {
          difference = this.y2 - rect2.y2;
        }
      }
    }
    return difference;
  }

  public boolean equals(Object obj) {
    Rectangle r2 = (Rectangle) obj;
    boolean result = this.x1 == r2.x1 && this.x2 == r2.x2 && this.y1 == r2.y1 && this.y2 == r2.y2;
    return result;
  }

  @Override
  public int hashCode() {
    return x1+y1+x2+y2;
  }

  public String toString() {
    return "Rectangle #"+id+" ("+x1+","+y1+","+x2+","+y2+")";
  }
  public double distanceTo(Rectangle s) {
    int dx = s.x1 - this.x1;
    int dy = s.y1 - this.y1;
    return dx*dx+dy*dy;
  }

  public boolean contains(Point queryPoint) {
    return queryPoint.x >= this.x1 && queryPoint.x <= this.x2 &&
    queryPoint.y >= this.y1 && queryPoint.y <= this.y2;
  }

  public double maxDistance(Point point) {
    int dx = Math.max(point.x - this.x1, this.x2 - point.x);
    int dy = Math.max(point.y - this.y1, this.y2 - point.y);

    return Math.sqrt(dx*dx+dy*dy);
  }

  public double minDistance(Point point) {
    int dx = Math.min(point.x - this.x1, this.x2 - point.x);
    int dy = Math.min(point.y - this.y1, this.y2 - point.y);

    return Math.min(dx, dy);
  }

  @Override
  public Object clone() {
    Rectangle rect2 = new Rectangle(this.id, this.x1, this.y1, this.x2, this.y2);
    rect2.type = this.type;
    return rect2;
  }

  public static void main(String[] args) {
    Random random = new Random();
    List<Rectangle> R = new ArrayList<Rectangle>();
    List<Rectangle> S = new ArrayList<Rectangle>();

    for (int i = 0; i < 128000; i++) {
      int x1 = random.nextInt(102400);
      int y1 = random.nextInt(102400);
      int w = random.nextInt(10);
      int h = random.nextInt(10);
      R.add(new Rectangle(0, x1, y1, x1+w, y1+h));
    }

    for (int i = 0; i < 128000; i++) {
      int x1 = random.nextInt(102400);
      int y1 = random.nextInt(102400);
      int w = random.nextInt(10);
      int h = random.nextInt(10);
      S.add(new Rectangle(0, x1, y1, x1+w, y1+h));
    }

    long t1 = System.currentTimeMillis();

    Collections.sort(R);
    Collections.sort(S);
    
    ArrayList<Rectangle> result1 = new ArrayList<Rectangle>();
    ArrayList<Rectangle> result2 = new ArrayList<Rectangle>();
    
    int i = 0,j = 0;
    while (i < R.size() && j < S.size()) {
      Rectangle r, s;
      if (R.get(i).compareTo(S.get(j)) < 0) {
        r = R.get(i);
        int jj = j;

        while ((jj < S.size())
            && ((s = S.get(jj)).getXlower() <= r.getXupper())) {
          if (r.intersects(s)) {
            //output.collect(r, s);
            result1.add(r);
            result2.add(s);
          }
          jj++;
        }
        i++;
      } else {
        s = S.get(j);
        int ii = i;

        while ((ii < R.size())
            && ((r = R.get(ii)).getXlower() <= s.getXupper())) {
          if (r.intersects(s)) {
            //output.collect(r, s);
            result1.add(r);
            result2.add(s);
          }
          ii++;
        }
        j++;
      }
    }

    long t2 = System.currentTimeMillis();
    
    System.out.println("Millis "+(t2-t1));
  }
}
