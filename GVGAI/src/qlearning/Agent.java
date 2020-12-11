package qlearning;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class TrainingAgent extends AbstractPlayer {
	// VARIABLES 
	ArrayList<Observation>[] inmov;
	Dimension dim;
	
	private int numFilas;
	private int numCol;
	private int blockSize;
	
	private char[][] mapaBlank;
	private char[][] mapaObstaculos;
	
	// VARIABLES Q LEARNING
	ArrayList<String> 
	
	
    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;
    /**
     * List of available actions for the agent
     */
    protected ArrayList<Types.ACTIONS> actions;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     * En el constructor mirar y guardar las cosas estaticas
     */
    public TrainingAgent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        randomGenerator = new Random();
        actions = so.getAvailableActions();
        inmov = so.getImmovablePositions();
        dim = so.getWorldDimension();

        blockSize = so.getBlockSize();
        
		numCol = so.getWorldDimension().width / so.getBlockSize();
		numFilas = so.getWorldDimension().height / so.getBlockSize();
		
		mapaBlank = new char[numFilas][numCol];
		mapaObstaculos = new char[numFilas][numCol];
		
		for(int i=0; i<numFilas; i++)
			for(int j=0; j<numCol; j++)
				mapaBlank[i][j] = ' ';
		
		System.out.println("NUM FILAS = " + numFilas);
		System.out.println("NUM COL = " + numCol);
		
		// Inicializamos el modulo Util
		Util.numCol = numCol;
		Util.numFilas = numFilas;
		
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	System.out.println("COMBUSTIBLE > " + stateObs.getAvatarHealthPoints());
    	mapaObstaculos = new char[numFilas][numCol];
    
    	int[] posJugador = getCelda(stateObs.getAvatarPosition(),dim);
    	mapaObstaculos[posJugador[0]][posJugador[1]] = 'O';
    	
    	//ArrayList<Observation>[][] mapa = stateObs.getObservationGrid();
    	
    	System.out.println("-----------------------------------");
    	//System.out.println(mapa.length);
    	
    	//13, 6 -> TANQUE AZUL
    	//14, 7 -> COCHE VERDE
    	//15, 9 -> BIDÓN DE GASOLINA
    	//10,16 -> ÁRBOLES
    	//1 -> JUGADOR
    	for(ArrayList<Observation> lista : stateObs.getMovablePositions())
    		for(Observation ob : lista)
    		{
    			int[] pos = getCelda(ob, dim);
    			//System.out.println(pos[0] + "," + pos[1]+" -> "+ob.itype);
    			
    			switch(ob.itype)
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
						mapaObstaculos[pos[0]][pos[1]] = ' ';
    			
    		}
		}
    		
    	
    	for(int i=0; i<numFilas; i++) {
    		System.out.println();
    		for(int j=0; j<numCol; j++)
    			System.out.print(mapaObstaculos[i][j]);
    	}
    	System.out.println();
    	
//    	try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
    	
      	
    	
        
        return ACTIONS.ACTION_RIGHT;
    }
    
    private int[] getCelda(Vector2d vector, Dimension dim2) {
    	int x = (int) Math.floor(vector.x /  dim.getWidth() * numCol);
    	int y = (int) Math.floor(vector.y /  dim.getHeight() * numFilas);
    	
    	return new int[] {y,x};
	}


	private int[] getCelda(Observation ob, Dimension dim)
    {
    	int x = (int) Math.floor(ob.position.x /  dim.getWidth() * numCol);
    	int y = (int) Math.floor(ob.position.y /  dim.getHeight() * numFilas);
    	
    	return new int[] {y,x};
    }
    
} 
    
//	/**
//	 * Metodo que muestra el mapa del juego
//	 * 
//	 * @param stateObs
//	 * @param posCol
//	 * @param posFila
//	 */
//	private void mostrarObstaculos(int posFila, int posCol) {
//		// Mostrar numeros de columnas
//		System.out.print("  ");
//		for (int c = 0; c < numCol; c++) {
//			if (c < 10)
//				System.out.print(" " + c + " ");
//			else
//				System.out.print(c + " ");
//		}
//		System.out.println();
//
//		// MOSTRAR GRID DE OBSTACULOS PARSEADOS
//		for (int i = 0; i < numFilas; i++) {
//			if (i < 10)
//				System.out.print(i + " ");
//			else
//				System.out.print(i);
//
//			for (int j = 0; j < numCol; j++) {
//				if (posFila == i && posCol == j) {
//					System.out.print(" O ");
//				} else if (verObstaculos[i][j] == 1) {
//					System.out.print(" X ");
//				} else if (verObstaculos[i][j] == 2) {
//					System.out.print(" P ");
//				} else if (verObstaculos[i][j] == 3) {
//					System.out.print(" A ");
//				} else if (verObstaculos[i][j] == 4) {
//					System.out.print(" - ");
//				} else {
//					System.out.print("   ");
//				}
//			}
//			System.out.println();
//		}
//		System.out.println();
//	}
//    

