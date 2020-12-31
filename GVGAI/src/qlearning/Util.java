package qlearning;

import java.awt.Dimension;
import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

public class Util {

	public static int numCol;
	public static int numFilas;
/*	
	public static int[] getCelda(Vector2d vector, Dimension dim) {
    	int x = (int) Math.floor(vector.x /  dim.getWidth() * numCol);
    	int y = (int) Math.floor(vector.y /  dim.getHeight() * numFilas);
    	
    	return new int[] {y,x};
	}
*/
	//mapaBlank = new char[numFilas][numCol];
	//private static char[][] mapaObstaculos = new char[numFilas][numCol];
	
	//for(int i=0; i<numFilas; i++)
	//	for(int j=0; j<numCol; j++)
	//		mapaBlank[i][j] = ' ';
	
	//13, 6 -> TANQUE AZUL
	//14, 7 -> COCHE VERDE
	//15, 9 -> BIDÓN DE GASOLINA
	//10,16 -> ÁRBOLES
	//1 -> JUGADOR
/*
	public static char[][] getMapaObstaculos(StateObservation obs)
	{
		char[][] mapaObstaculos = new char[numFilas][numCol];
		for(int i=0; i<numFilas; i++)
			for(int j=0; j<numCol; j++)
				mapaObstaculos[i][j] = ' ';
		
		
	    	for(ArrayList<Observation> lista : obs.getMovablePositions())
	    		for(Observation objeto : lista)
	    		{
	    			
	    			int[] pos = Util.getCelda(objeto.position, obs.getWorldDimension());
	    		
	    			
	    			System.out.println("Objeto en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());
	    			//System.out.println(this.mapaObstaculos[pos[0]][pos[1]]);
	    			
	    			switch(objeto.itype)
					{    					
						case 1:
							mapaObstaculos[pos[0]][pos[1]] = 'O';
							break;
						case 10:
						case 16:
							mapaObstaculos[pos[0]][pos[1]] = '|';
							break;
						case 6:
						case 7:
						case 13:
						case 14:
							mapaObstaculos[pos[0]][pos[1]] = 'X';
							break;
						case 9:
						case 15:
							mapaObstaculos[pos[0]][pos[1]] = 'G';
							break; 
						default:
							mapaObstaculos[pos[0]][pos[1]] = '.';
							break;
	    		}
			}
    	
    	return mapaObstaculos;
	}
*/	
	public static void pintaMapaObstaculos(char [][] mapaObstaculos)
	{
		
		System.out.println("-----------------------------------");
		for(int i=0; i<numFilas*2; i++) {
    		System.out.println();
    		for(int j=0; j<numCol*2; j++)
    			System.out.print(mapaObstaculos[i][j]);
    	}
    	System.out.println();
	}
}
