package qlearning;

import java.awt.Dimension;

import tools.Vector2d;

public class Util {

	public static int numCol;
	public static int numFilas;

	
	public static int[] getCelda(Vector2d vector, Dimension dim) {
    	int x = (int) Math.floor(vector.x /  dim.getWidth() * numCol);
    	int y = (int) Math.floor(vector.y /  dim.getHeight() * numFilas);
    	
    	return new int[] {y,x};
	}
	

}
