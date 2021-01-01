package qlearning;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import qlearning.StateManager.ESTADOS;

import tools.ElapsedCpuTimer;


public class TrainingAgent extends AbstractPlayer {
	boolean verbose = StateManager.verbose;
	
	//PARAMETROS DEL APRENDIZAJE
	private double alpha = 0.1; // Factor Exploracion tamaño del paso
	private double gamma = 0.8; // Factor descuento recompensa futura
	
	// VARIABLES 
	ArrayList<Observation>[] inmov;
	Dimension dim;
	
	private int numFilas;
	private int numCol;
	private int blockSize;
	
	//private char[][] mapaBlank;
	private char[][] mapaObstaculos;
	
	// VARIABLES Q LEARNING
	private int vidaAnterior;
	
	static int numAccionesPosibles;
	
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
    	if(verbose) System.out.println("______________________\nCOMIENZA LA PARTIDA\n_______________________");
        randomGenerator = new Random();
        actions = so.getAvailableActions();
        inmov = so.getImmovablePositions();
        dim = so.getWorldDimension();

        blockSize = so.getBlockSize();
        
		numCol = so.getWorldDimension().width / so.getBlockSize();
		numFilas = so.getWorldDimension().height / so.getBlockSize();
		
		//mapaBlank = new char[numFilas][numCol];
		//mapaObstaculos = new char[numFilas][numCol];
		
		//for(int i=0; i<numFilas; i++)
		//	for(int j=0; j<numCol; j++)
		//		mapaBlank[i][j] = ' ';
		if(verbose) System.out.println("DIMENSION MUNDO: " + so.getWorldDimension().toString());
		if(verbose) System.out.println("NUM FILAS = " + numFilas);
		if(verbose) System.out.println("NUM COL = " + numCol);
		
		// Inicializamos el modulo Util
		Util.numCol = this.numCol;
		Util.numFilas = this.numFilas;

		
		vidaAnterior = so.getAvatarHealthPoints();
    	numAccionesPosibles = StateManager.ACCIONES.length;
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	// -----------------------------------------------------------------------
    	// 01 - PERCEPCIÓN DEL ENTORNO
    	// -----------------------------------------------------------------------
    	int vidaActual = stateObs.getAvatarHealthPoints();
    	
    	double[] pos = StateManager.getCeldaPreciso(stateObs.getAvatarPosition(),dim);
    	int[] posJugador = StateManager.getIndiceMapa(pos); // Indice del mapa
    	
    	if(verbose) System.out.println("VIDA ACTUAL = "+vidaActual);
    	if(verbose) System.out.println("POSICION = " + posJugador[0] + "-" + posJugador[1]);
    	
    	
    	this.mapaObstaculos = StateManager.getMapaObstaculos(stateObs); //Actualizamos el mapa percibido
    	mapaObstaculos[posJugador[0]][posJugador[1]] = 'O'; //Marcamos la posicion del jugador

    	if(verbose) Util.pintaMapaObstaculos(mapaObstaculos);
    	
    	// Percibimos el estado actual e incrementamos su contador
    	ESTADOS estadoActual = StateManager.getEstado(stateObs, vidaAnterior, this.mapaObstaculos);
    	estadoActual.incrementa();
    	if(verbose) System.out.println("Estado actual: " + estadoActual.toString());
    	
    	// -----------------------------------------------------------------------
    	// ALGORITMO Q LEARNING
    	// -----------------------------------------------------------------------
    	
    	if(verbose)pintaQTable(estadoActual);
    	
    	// Seleccionar una entre las posibles acciones desde el estado actual
    	ACTIONS action;
    	boolean randomPolicy = true; 
    	
    	// Criterio de selección: random
    	if(randomPolicy) {
	    	
	        int index = randomGenerator.nextInt(numAccionesPosibles);
	        action = StateManager.ACCIONES[index];
    	}
    	else // Criterio seleccion: maxQ
    	{
    		
        	action = getAccionMaxQ(estadoActual);
    	}
    	

    	if(verbose) System.out.println("--> DECIDE HACER: " + action.toString());
        
        // Calcular el siguiente estado habiendo elegido esa acción
    	ESTADOS estadoSiguiente = StateManager.getEstadoFuturo(stateObs, action);
    	if(verbose) System.out.println("ESTADO SIGUIENTE: " + estadoSiguiente.toString());
    	
    	
        // Using this possible action, consider to go to the next state
        double q = StateManager.Q.get(new ParEstadoAccion(estadoActual, action));
    	if(verbose) System.out.println("Consulto q actual Q<" + estadoActual.toString() +","+action.toString()+"> = " + q);

        double maxQ = maxQ(estadoSiguiente);
        //int r = StateManager.R.get(new ParEstadoAccion(estadoActual, action));
        int r = StateManager.R.get(new ParEstadoAccion(estadoSiguiente, action));
        
        double value = q + alpha * (r + gamma * maxQ - q);
        
        // Actualizamos la tabla Q
        StateManager.Q.put(new ParEstadoAccion(estadoActual, action), value);
 	
    		
    
    	
//    	try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
    	

		vidaAnterior = vidaActual;
		
		if(verbose) System.out.println("--> DECIDE HACER: " + action.toString());
		
	  	//if(stateObs.isGameOver()) this.saveQTable(); //Guardamos la tablaQ si termina el juego
	  	
		if(verbose)
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        return action;
    }
/*    
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
*/
	/**
	 * Lee el fichero que contiene la tabla Q (QTable.csv)
	 * Vuelca el fichero en un diccionario de pares Estado-Accion N x M
	 * siendo N = numEstados ; M = numAccionesPosibles
	 * Si no existe lo crea e inicializa
	 */
	private HashMap<ParEstadoAccion, Double> getQTable()
	{
		HashMap <ParEstadoAccion, Double> Q = new HashMap <ParEstadoAccion, Double>();
		
		//Q.put(new ParEstadoAccion(ESTADOS.ESQUIVO_OBSTACULO,ACTIONS.ACTION_DOWN), 0.0);
		
		// leer el fichero..........
		
		return Q;
	}
	
	
	private double maxQ(ESTADOS s) {
        ACTIONS[] actions = StateManager.ACCIONES;
        double maxValue = Double.MIN_VALUE;
        
        for (int i = 0; i < actions.length; i++) {
        	
        	//if(verbose) System.out.print("maxQ<"+ s.toString() + "," );
        	//if(verbose) System.out.print(actions[i]+"> = ");
            double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
            //if(verbose) System.out.println(value);
 
            if (value > maxValue)
                maxValue = value;
        }
        
        return maxValue;
    }
	
	public static ACTIONS getAccionMaxQ(ESTADOS s)
	{
		 ACTIONS[] actions = StateManager.ACCIONES; // Acciones posibles
         ACTIONS accionMaxQ = ACTIONS.ACTION_NIL;
         
         double maxValue = Double.MIN_VALUE; // 0.000...001
	        
	        for (int i = 0; i < actions.length; i++) {
	        	
	        	//if(verbose) System.out.print("Actual maxQ<"+ s.toString() + "," );
	        	//if(verbose) System.out.print(actions[i]+"> = ");
	            double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
	            //if(verbose) System.out.println(value);
	 
	            if (value > maxValue) {
	                maxValue = value;
	                accionMaxQ = actions[i];
	            }
	        }

	        if(maxValue < 1) // Inicialmente estan a 0, una random
	        {
	          int index = new Random().nextInt(numAccionesPosibles);
	          accionMaxQ = actions[index];
	        }
	        
	        return accionMaxQ;
	}
	
	private void pintaQTable(ESTADOS s)
	{
		ACTIONS[] actions = StateManager.ACCIONES;

        System.out.println("----------Q TABLE -----------------");
        
        for (int i = 0; i < actions.length; i++) {
        	 System.out.print("Actual Q<"+ s.toString() + "," );
        	 System.out.print(actions[i]+"> = ");
        	
        	double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
        	
            System.out.println(value);
        }
	        
        System.out.println("----------Q TABLE -----------------");
	}
} 
    
/*
 * Metodo que muestra el mapa del juego
 * 
 * @param stateObs
 * @param posCol
 * @param posFila
 */
/*	private void mostrarObstaculos(int posFila, int posCol) {
		// Mostrar numeros de columnas
		if(verbose) System.out.print("  ");
		for (int c = 0; c < numCol; c++) {
			if (c < 10)
				if(verbose) System.out.print(" " + c + " ");
			else
				if(verbose) System.out.print(c + " ");
		}
		if(verbose) System.out.println();
	
		// MOSTRAR GRID DE OBSTACULOS PARSEADOS
		for (int i = 0; i < numFilas; i++) {
			if (i < 10)
				if(verbose) System.out.print(i + " ");
			else
				if(verbose) System.out.print(i);
	
			for (int j = 0; j < numCol; j++) {
				if (posFila == i && posCol == j) {
					if(verbose) System.out.print(" O ");
				} else if (verObstaculos[i][j] == 1) {
					if(verbose) System.out.print(" X ");
				} else if (verObstaculos[i][j] == 2) {
					if(verbose) System.out.print(" P ");
				} else if (verObstaculos[i][j] == 3) {
					if(verbose) System.out.print(" A ");
				} else if (verObstaculos[i][j] == 4) {
					if(verbose) System.out.print(" - ");
				} else {
					if(verbose) System.out.print("   ");
				}
			}
			if(verbose) System.out.println();
		}
		if(verbose) System.out.println();
	}
*/
