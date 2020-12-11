package qlearning;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import qlearning.Util;
public class StateManager {
	
	//VARIABLES
	private ArrayList<String> estados;
	private char mapaObstaculos[][];
	private StateObservation obs;
	private int posActual[];
	private int numFilas = Util.numFilas;
	private int numCol = Util.numCol;

	
	public StateManager()
	{
		
		// CREA LOS ESTADOS
		estados = new ArrayList<String>();
		
		//ESTADOS CON RECOMPENSA
		estados.add("+GASOLINA");			// 0 
		estados.add("ESQUIVO OBSTACULOS");	// 1
				
		//ESTADOS CON CASTIGO
		estados.add("MUERTE SEGURA");		// 2
		
		//RESTO DE ESTADOS
		estados.add("GASOLINA ARRIBA");
		estados.add("GASOLINA IZQDA");   	
		estados.add("GASOLINA DCHA");
		estados.add("GASOLINA ABAJO");
		
		
		estados.add("HUECO IZQDA");
		estados.add("HUECO DCHA");
		estados.add("HUECO ARRIBA");
		
	
	}
	
	private String getEstado(StateObservation obs, int vidaAnterior)
	{
		int vidaActual = obs.getAvatarHealthPoints();
		posActual = Util.getCelda(obs.getAvatarPosition(), obs.getWorldDimension());
		
		mapaObstaculos = getMapaObstaculos();
		
		if(vidaActual > vidaAnterior)
			return "+GASOLINA";// "+GASOLINA"
		
		String posGasolina = getGasolina();
		if(!posGasolina.equals("NO HAY")) // SI HAY GASOLINA
			return posGasolina;
		
		int[] numObstaculosFila = getObstaculosFila();
		int numObstaculosIzqda = numObstaculosFila[0];
		int numObstaculosDcha = numObstaculosFila[1];
		
		if(numObstaculosDcha >= 1 || numObstaculosIzqda >= 1)
			return "ESQUIVO OBSTACULOS"; // ESQUIVO OBSTACULOS
		
		
		if(estoyRodeadoObstaculos())
			return "MUERTE SEGURA"; // "MUERTE SEGURA"
		
		

		
		return null;
		
	}
	
	//13, 6 -> TANQUE AZUL
	//14, 7 -> COCHE VERDE
	//15, 9 -> BIDÓN DE GASOLINA
	//10,16 -> ÁRBOLES
	//1 -> JUGADOR
	private char[][] getMapaObstaculos()
	{
		mapaObstaculos = new char[numFilas][numCol];
		

    	for(ArrayList<Observation> lista : obs.getMovablePositions())
    		for(Observation objeto : lista)
    		{
    			//System.out.println(pos[0] + "," + pos[1]+" -> "+ob.itype);
    			int[] pos = Util.getCelda(objeto.position, obs.getWorldDimension());
    			
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
						mapaObstaculos[pos[0]][pos[1]] = ' ';		
    		}
		}
    	
    	return mapaObstaculos;
	}
	
	/*
	 * Devuelve un array de dos enteros indicando el numero de obstaculos a la izqda y a la derecha del agente
	 */
	private int[] getObstaculosFila()
	{
		int numCol = Util.numCol;
		int numObstaculosDcha = 0;
		int numObstaculosIzqda = 0;
		
				
		//Desde la casilla a la derecha hasta el arbol de la derecha
		for (int i = posActual[1]+1; i < numCol-1; i++) {
			// Si en la fila actual, columna i tenemos un obstaculo
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
	
	private boolean estoyRodeadoObstaculos()
	{
		// X X X ||   X 
		//   O   || X O X
				
		return(mapaObstaculos[posActual[0]+1][posActual[1]]=='X' 
				&& ( (mapaObstaculos[posActual[0]+1][posActual[1]+1]=='X'
					&& mapaObstaculos[posActual[0]+1][posActual[1]-1]=='X')
				|| (mapaObstaculos[posActual[0]][posActual[1]+1]=='X'
						&& mapaObstaculos[posActual[0]][posActual[1]-1]=='X') )
				);
	}
	
	private String getGasolina()
	{
		int posGasolina[] = new int[] {-1,-1};
		boolean encontrado = false;
		
		// Recorremos el mapa para buscar la pos de la gasolina
		while(!encontrado) {
			for(int i=0; i<numFilas; i++)
				for (int j = 1; j < numCol-1; j++)  //Quitando los arboles del borde
					if(mapaObstaculos[i][j] == 'G') {
						posGasolina = new int[]{i,j};
						encontrado = true;
					}
			break;
		}
	
		if(posGasolina[0] == -1 && posGasolina[1] == -1)
			return "NO HAY";  // La gasolina no ha aparecido
		
		//Misma columna y gasolina por encima
		else if(posGasolina[1] == posActual[1] && posGasolina[0] >= posActual[0]) 
			return "GASOLINA ARRIBA"; 
		
		//Gasolina a la derecha
		else if(posGasolina[1] > posActual[1])
			return "GASOLINA DCHA";
		
		//Gasolina a la izqda
		else if(posGasolina[1] < posActual[1])
			return "GASOLINA IZQDA";
		
		//Gasolina abajo
		else if(posGasolina[0] > posActual[0])
			return "GASOLINA ABAJO";	
		
		return null;
		
	}
}
