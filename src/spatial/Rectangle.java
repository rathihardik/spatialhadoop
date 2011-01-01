package spatial;

/**
 * A class that holds coordinates of a rectangle.
 * @author aseldawy
 *
 */
public class Rectangle {
	public int id;
	public float x1;
	public float x2;
	public float y1;
	public float y2;

	public Rectangle(int id, float x1, float y1, float x2, float y2) {
		this.set(id, x1, y1, x2, y2);
	}

	public void set(int id, float x1, float y1, float x2, float y2) {
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
	
	public static void main(String[] args) {
		Rectangle r1 = new Rectangle(1, 0,0, 100, 100);
		Rectangle r2 = new Rectangle(1, 0,0, 50, 50);
		Rectangle r3 = new Rectangle(1, -100,-100, -50, -50);
		Rectangle r4 = new Rectangle(1, -100,0, -50, 50);
		if (!r1.intersects(r2))
			System.err.println("error 1");
		if (r1.intersects(r3))
			System.err.println("error 2");
		if (r1.intersects(r4))
			System.err.println("error 3");
		System.out.println("Rectangle success");
	}
}
