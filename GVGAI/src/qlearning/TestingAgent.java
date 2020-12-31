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


public class TestingAgent extends AbstractPlayer {
	boolean verbose = StateManager.verbose;
	
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
    public TestingAgent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
    	if(verbose) System.out.println("______________________\nCOMIENZA LA PARTIDA\n_______________________");
        randomGenerator = new Random();
        actions = so.getAvailableActions();
        inmov = so.getImmovablePositions();
        dim = so.getWorldDimension();

        blockSize = so.getBlockSize();
        
		numCol = so.getWorldDimension().width / so.getBlockSize();
		numFilas = so.getWorldDimension().height / so.getBlockSize();
		
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
    	// ALGORITMO Q LEARNING EXPLOTACION DE LA TABLA Q
    	// -----------------------------------------------------------------------
    	
    	if(verbose) StateManager.pintaQTable(estadoActual);
    	
    	    	
    	// Criterio seleccion: maxQ
    	ACTIONS action = getAccionMaxQ(estadoActual);
    	
    	if(verbose) System.out.println("--> DECIDE HACER: " + action.toString());
        
   
		vidaAnterior = vidaActual;
		
	  	
		if(verbose)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        return action;
    }

	
	private ACTIONS getAccionMaxQ(ESTADOS s)
	{
		 ACTIONS[] actions = StateManager.ACCIONES; // Acciones posibles
         ACTIONS accionMaxQ = ACTIONS.ACTION_NIL;
         
         double maxValue = Double.MIN_VALUE;
	        
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
	        
	        if(maxValue < 1.0) // Inicialmente estan a 0, una random
	        {
	          int index = randomGenerator.nextInt(numAccionesPosibles);
	          accionMaxQ = actions[index];
	        }
	        
	        return accionMaxQ;
	}
	

} 
    