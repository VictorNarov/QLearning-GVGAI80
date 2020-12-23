package qlearning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import qlearning.Util;



public class StateManager {
	static boolean verbose = true;
	Random randomGenerator;
	
	// Contenedor de constantes para identificar los estados
	public static enum ESTADOS{
		OBTENGO_GASOLINA,
		ESQUIVO_OBSTACULO,
		MUERTE_SEGURA,
		GASOLINA_ARRIBA,
		GASOLINA_ABAJO,
		GASOLINA_IZQDA,
		GASOLINA_DCHA,
		HUECO_ARRIBA,
		HUECO_ABAJO,
		HUECO_IZQDA,
		HUECO_DCHA,
		OBSTACULO_ARRIBA,
		NIL
	}
	
	public static HashMap<ParEstadoAccion, Integer> R; // TABLA R
	public static HashMap<ParEstadoAccion, Double> Q; // TABLA Q
		
	//VARIABLES
	private static char mapaObstaculos[][];
	private StateObservation obs;
	private static int posActual[];

	
	private int numEstados = ESTADOS.values().length;
	private int numAcciones = ACTIONS.values().length;
	
	public StateManager()
	{
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		inicializaTablaR();
		inicializaTablaQ(true);
		

	}
	
	private void inicializaTablaR()
	{
		R = new HashMap<ParEstadoAccion, Integer>(numEstados*numAcciones);
		
		// Inicializamos todas las recompensas a cero
		// excepto a obtener gasolina y esquivar obstaculos, que ser�n premiadas
		for (ESTADOS estado: ESTADOS.values()) 
			for(ACTIONS accion : ACTIONS.values())
			{
				int valorR = 0;
				
				if(estado.equals(ESTADOS.OBTENGO_GASOLINA))
					valorR = 100;
				
				else if(estado.equals(ESTADOS.ESQUIVO_OBSTACULO))
					valorR = 75;
				
				R.put(new ParEstadoAccion(estado,accion), valorR);
			}
		
	}
	
	private void inicializaTablaQ(boolean random)
	{
		Q = new HashMap<ParEstadoAccion, Double>(numEstados*numAcciones);
		
		if(random)
			// Inicializamos todos los valores Q a random
			for (ESTADOS estado: ESTADOS.values()) 
				for(ACTIONS accion : ACTIONS.values())			
					Q.put(new ParEstadoAccion(estado,accion), randomGenerator.nextDouble() * 100);
			else
			// Inicializamos todos los valores Q a cero
				for (ESTADOS estado: ESTADOS.values()) 
					for(ACTIONS accion : ACTIONS.values())
				
						Q.put(new ParEstadoAccion(estado,accion), 0.0);
				
		if(new ParEstadoAccion(ESTADOS.ESQUIVO_OBSTACULO, ACTIONS.ACTION_DOWN).equals(new ParEstadoAccion(ESTADOS.ESQUIVO_OBSTACULO, ACTIONS.ACTION_DOWN)))
			if (verbose) System.out.println("tremendo hashhhhhhhhhhhh");
	}
	
	public static ESTADOS getEstado(StateObservation obs, int vidaAnterior)
	{
		int vidaActual = obs.getAvatarHealthPoints();
		posActual = Util.getCelda(obs.getAvatarPosition(), obs.getWorldDimension());
		
		if (verbose) System.out.println("POS ACTUAL = " + posActual[0]+"-"+posActual[1]);
		
		if(posActual[0] != -1 && posActual[1] != -1) {
		
		
			mapaObstaculos = Util.getMapaObstaculos(obs);
			
			if(vidaActual > vidaAnterior)
				return ESTADOS.OBTENGO_GASOLINA;// "+GASOLINA"
			
			int[] posGasolina = getPosGasolina();
			if (verbose) System.out.println("POS GASOLINA: " + posGasolina[0] + "-" + posGasolina[1]);
			
			if(posGasolina[0] != -1 && posGasolina[1] != -1) // SI HAY GASOLINA
				return getEstadoGasolina(posGasolina); // Obtiene el estado en funcion de la posicion de la gasolina
			
			int[] numObstaculosFila = getObstaculosFila();
			int numObstaculosIzqda = numObstaculosFila[0];
			int numObstaculosDcha = numObstaculosFila[1];
			
			if (verbose) System.out.println("N obstaculos izqda = " + numObstaculosIzqda);
			if (verbose) System.out.println("N obstaculos dcha = " + numObstaculosDcha);
			
			if(numObstaculosDcha >= 1 || numObstaculosIzqda >= 1)
				return ESTADOS.ESQUIVO_OBSTACULO; // ESQUIVO OBSTACULOS
			
			if(mapaObstaculos[posActual[0]-1][posActual[1]] == ' ')
				return ESTADOS.OBSTACULO_ARRIBA;
			
			if(estoyRodeadoObstaculos())
				return ESTADOS.MUERTE_SEGURA; // "MUERTE SEGURA"
			
		}
		
		return ESTADOS.NIL;
		
	}
	

	
	/*
	 * Devuelve un array de dos enteros indicando el numero de obstaculos a la izqda y a la derecha del agente
	 */
	private static int[] getObstaculosFila()
	{
		int numCol = Util.numCol;
		int numObstaculosDcha = 0;
		int numObstaculosIzqda = 0;
		
				
		//Desde la casilla a la derecha hasta el arbol de la derecha
		for (int i = posActual[1]+1; i < numCol -1; i++) {
			// Si en la fila actual, columna i tenemos un obstaculo
			//if (verbose) System.out.println("mirando casilla derecha: " + posActual[0]+"-"+i);
			if(mapaObstaculos[posActual[0]][i] == 'X')
				numObstaculosDcha++; //Incrementamos el contador
		}
		
		//Desde la casilla a la izqda hasta el arbol de la izqda
		for (int i = posActual[1]-1; i > 1; i--) {
			// Si en la fila actual, columna i tenemos un obstaculo
			if(mapaObstaculos[posActual[0]][i] == 'X')
				numObstaculosIzqda++; //Incrementamos el contador
		}
		
		
		return new int[] {numObstaculosIzqda,numObstaculosDcha};
		
	}
	
	private static boolean estoyRodeadoObstaculos()
	{
		// X X X ||   X 
		//   O   || X O X
				
		return(mapaObstaculos[posActual[0]-1][posActual[1]]=='X' 
				&& ( (mapaObstaculos[posActual[0]-1][posActual[1]+1]=='X'
					&& mapaObstaculos[posActual[0]-1][posActual[1]-1]=='X')
				|| (mapaObstaculos[posActual[0]][posActual[1]-1]=='X'
						&& mapaObstaculos[posActual[0]][posActual[1]-1]=='X') )
				);
	}
	
	private static int[] getPosGasolina()
	{
		int posGasolina[] = new int[] {-1,-1};
		boolean encontrado = false;
		
		// Recorremos el mapa para buscar la pos de la gasolina
		while(!encontrado) {
			for(int i=0; i< Util.numFilas; i++)
				for (int j = 1; j < Util.numCol-1; j++)  //Quitando los arboles del borde
					if(mapaObstaculos[i][j] == 'G') {
						posGasolina = new int[]{i,j};
						encontrado = true;
					}
			break;
		}
		
		return posGasolina;
	}
	
	private static ESTADOS getEstadoGasolina(int [] posGasolina)
	{
		
		//Misma columna y gasolina por encima
		if(posGasolina[1] == posActual[1] && posGasolina[0] >= posActual[0]) 
			return ESTADOS.GASOLINA_ARRIBA; 
		
		//Gasolina a la derecha
		else if(posGasolina[1] > posActual[1])
			return ESTADOS.GASOLINA_DCHA;
		
		//Gasolina a la izqda
		else if(posGasolina[1] < posActual[1])
			return ESTADOS.GASOLINA_IZQDA;
		
		//Gasolina abajo
		else if(posGasolina[0] > posActual[0])
			return ESTADOS.GASOLINA_ABAJO;	
		
		return ESTADOS.NIL;
		
	}
	
	 
}

