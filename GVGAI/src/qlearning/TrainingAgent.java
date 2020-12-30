package qlearning;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import qlearning.StateManager.ESTADOS;

import tools.ElapsedCpuTimer;
import tools.Vector2d;


public class TrainingAgent extends AbstractPlayer {
	boolean verbose = true;
	
	//PARAMETROS DEL APRENDIZAJE
	private double alpha = 0.2; // Factor Explotacion
	private double gamma = 0.8; // Factor Exlporacion
	
	// VARIABLES 
	ArrayList<Observation>[] inmov;
	Dimension dim;
	
	private int numFilas;
	private int numCol;
	private int blockSize;
	
	private char[][] mapaBlank;
	private char[][] mapaObstaculos;
	
	// VARIABLES Q LEARNING
	private int vidaAnterior;
	
	int numAccionesPosibles;
	
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
		
		if(verbose) System.out.println("NUM FILAS = " + numFilas);
		if(verbose) System.out.println("NUM COL = " + numCol);
		
		// Inicializamos el modulo Util
		Util.numCol = this.numCol;
		Util.numFilas = this.numFilas;

		
		vidaAnterior = so.getAvatarHealthPoints();
    	numAccionesPosibles = so.getAvailableActions().size();
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
    	int[] posJugador = getCelda(stateObs.getAvatarPosition(),dim);
    	if(verbose) System.out.println("VIDA ACTUAL = "+vidaActual);
    	if(verbose) System.out.println("POSICION = " + posJugador[0] + "-" + posJugador[1]);
    	
    	this.mapaObstaculos = Util.getMapaObstaculos(stateObs); //Actualizamos el mapa percibido
    	mapaObstaculos[posJugador[0]][posJugador[1]] = 'O'; //Marcamos la posicion del jugador

    	Util.pintaMapaObstaculos(mapaObstaculos);
    	
    	// Percibimos el estado actual e incrementamos su contador
    	ESTADOS estadoActual = StateManager.getEstado(stateObs, vidaAnterior);
    	estadoActual.incrementa();
    	if(verbose) System.out.println("Estado actual: " + estadoActual.toString());
    	
    	// -----------------------------------------------------------------------
    	// ALGORITMO Q LEARNING
    	// -----------------------------------------------------------------------
    	
    	pintaQTable(estadoActual);
    	
    	// Seleccionar una entre las posibles acciones desde el estado actual
    	// Criterio de selección: random
//        int index = randomGenerator.nextInt(numAccionesPosibles);
//        ACTIONS action = stateObs.getAvailableActions().get(index);
    	
    	// Criterio seleccion: maxQ
    	ACTIONS action = getAccionMaxQ(estadoActual);
        
        // Calcular el siguiente estado habiendo elegido esa acción
    	
        StateObservation stateObsCopy = stateObs;
        stateObsCopy.advance(action);
    	ESTADOS estadoSiguiente = StateManager.getEstado(stateObsCopy, vidaActual);
    	if(verbose) System.out.println("ESTADO SIGUIENTE: " + estadoSiguiente.toString());
    	
    	
        // Using this possible action, consider to go to the next state
        double q = StateManager.Q.get(new ParEstadoAccion(estadoActual, action));
    	if(verbose) System.out.println("Consulto q actual Q<" + estadoActual.toString() +","+action.toString()+"> = " + q);

        double maxQ = maxQ(estadoSiguiente);
        int r = StateManager.R.get(new ParEstadoAccion(estadoActual, action));

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
		
        return action;
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
	/**
	 * Si no le indicamos el nombre del fichero, usa uno por defecto.
	 */
	private void saveQTable() {
		saveQTable("TablaQ.csv");
	}
	
	/**
	 * Escribe la tabla Q del atributo de la clase en 
	 * el fichero QTable.csv, para poder ser leída en 
	 * una siguiente etapa de aprendizaje.
	 */
	private void saveQTable(String fileName) 
	{
		/* Creación del fichero de salida */
	    try (PrintWriter csvFile = new PrintWriter(new File(fileName))) {
			
			if( verbose ) System.out.println(" GENERANDO EL FICHERO DE LA TABLAQ... ");
			
			StringBuilder buffer = new StringBuilder();
			buffer.append("ESTADOS");
			buffer.append(";");
			
			for( ACTIONS accion : ACTIONS.values() ) {
				buffer.append( accion.toString() );
				buffer.append(";");
			}
			
			buffer.append("\n");
			
			for ( ESTADOS estado: ESTADOS.values() ) {
				buffer.append(estado.toString());
				buffer.append(";");

				for( ACTIONS accion : ACTIONS.values() ) {
					double value = StateManager.Q.get(new ParEstadoAccion(estado, accion));
					
					buffer.append( '"' + Double.toString(value).replace('.', ',') + '"');
					buffer.append(";");
				}
				
				buffer.append("\n");
			}
			
			csvFile.write(buffer.toString());
			
			if ( verbose ) System.out.println( " FICHERO GENERADO CORRECTAMENTE! " );
			
			csvFile.close();
			
	    } catch( Exception ex ) {
	    	System.out.println(ex.getMessage());
		}
	}
	
	
	private double maxQ(ESTADOS s) {
        ACTIONS[] actions = ACTIONS.values();
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
	
	private ACTIONS getAccionMaxQ(ESTADOS s)
	{
		 ACTIONS[] actions = ACTIONS.values();
         ACTIONS accionMaxQ = ACTIONS.ACTION_NIL;
         
         double maxValue = Double.MIN_VALUE;
	        
	        for (int i = 0; i < actions.length; i++) {
	        	
	        	if(verbose) System.out.print("Actual maxQ<"+ s.toString() + "," );
	        	if(verbose) System.out.print(actions[i]+"> = ");
	            double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
	            if(verbose) System.out.println(value);
	 
	            if (value > maxValue) {
	                maxValue = value;
	                accionMaxQ = actions[i];
	            }
	        }
	        
	        if(maxValue < 1.0) // Inicialmente estan a 0, una random
	        {
	          int index = randomGenerator.nextInt(numAccionesPosibles);
	          accionMaxQ = ACTIONS.values()[index];
	        }
	        
	        return accionMaxQ;
	}
	
	private void pintaQTable(ESTADOS s)
	{
		ACTIONS[] actions = ACTIONS.values();

        if(verbose) System.out.println("----------Q TABLE -----------------");
        
        for (int i = 0; i < actions.length; i++) {
        	if(verbose) System.out.print("Actual Q<"+ s.toString() + "," );
        	if(verbose) System.out.print(actions[i]+"> = ");
        	
        	double value = StateManager.Q.get(new ParEstadoAccion(s, actions[i]));
        	
            if(verbose) System.out.println(value);
        }
	        
        this.saveQTable();
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
