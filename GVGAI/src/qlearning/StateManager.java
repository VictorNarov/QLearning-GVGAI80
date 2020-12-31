package qlearning;

import java.awt.Dimension;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import core.game.StateObservation;
import core.game.Observation;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class StateManager {
	public static boolean verbose = true;
	Random randomGenerator;
	
	/* Contenedor de constantes para identificar los estados */
	public static enum ESTADOS {
		OBTENGO_GASOLINA(0),
		ESQUIVO_OBSTACULO(0),
		MUERTE_SEGURA(0),
		GASOLINA_ARRIBA(0),
		GASOLINA_ABAJO(0),
		GASOLINA_IZQDA(0),
		GASOLINA_DCHA(0),
		HUECO_ARRIBA(0),
		HUECO_ABAJO(0),
		HUECO_IZQDA(0),
		HUECO_DCHA(0),
		OBSTACULO_ARRIBA(0),
		OBSTACULOS_IZQDA(0),
		OBSTACULOS_DCHA(0),
		BORDE_DCHA(0),
		BORDE_IZQDA(0),
		NIL(0);

		private int contador; //Cuenta cada vez que se percibe ese estado
		
		ESTADOS(int c) { this.contador = c; }
		
		ESTADOS(){	this.contador = 0; }
		
		public void incrementa() { this.contador++; }
		
		public int getContador(){ return this.contador;}
		
		// Devuelve el enum ESTADOS al que se corresponde la cadena pasada por parametro
		public static ESTADOS buscaEstado(String nombreEstado)
		{
			for(ESTADOS s : ESTADOS.values()) {
				if(s.toString().equals(nombreEstado))
					return s;
					
			}
			
			return null;
			
		}
	}
	
	// Acciones posibles
	public static final ACTIONS[] ACCIONES = {ACTIONS.ACTION_UP,ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_NIL};
	
	
	public static HashMap<ParEstadoAccion, Integer> R; // TABLA R
	public static HashMap<ParEstadoAccion, Double> Q; // TABLA Q
		
	/* Variables */
	//private static char mapaObstaculos[][];
	private static int posActual[];
	private int numEstados = ESTADOS.values().length;
	private int numAcciones = ACCIONES.length;
	
	public StateManager() {
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		inicializaTablaR();
		inicializaTablaQ(true);
	}
	
	public StateManager(String ficheroTablaQ)
	{
		if(verbose) System.out.println("Inicializando tablas Q y R.....");
		
		randomGenerator = new Random();
		inicializaTablaR();
		inicializaTablaQ(true);
		cargaTablaQ(ficheroTablaQ);
		
	}
	
	private void inicializaTablaR()
	{
		R = new HashMap<ParEstadoAccion, Integer>(numEstados*numAcciones);
		
		// Inicializamos todas las recompensas a cero
		// excepto la de obtener gasolina y esquivar obstaculos, que serán premiadas
		
		for (ESTADOS estado: ESTADOS.values()) 
			for(ACTIONS accion : ACCIONES)
			{
				int valorR = 0;
				
				if(estado.equals(ESTADOS.OBTENGO_GASOLINA))
					valorR = 100;
				
				else if(estado.equals(ESTADOS.ESQUIVO_OBSTACULO))
					valorR = 75;
				
				else if(estado.equals(ESTADOS.MUERTE_SEGURA))
					valorR = -100;
				/* 
				 En nuestro caso, recompensamos más que vaya a coger gasolina,
				 puesto que no sirve de nada que esquive si se queda sin gasolina.
				 */
				
				R.put(new ParEstadoAccion(estado,accion), valorR);
			}
		// Castigamos el suicidio
		R.put(new ParEstadoAccion(ESTADOS.BORDE_IZQDA,ACTIONS.ACTION_LEFT), -100);
		R.put(new ParEstadoAccion(ESTADOS.BORDE_DCHA,ACTIONS.ACTION_RIGHT), -100);
		R.put(new ParEstadoAccion(ESTADOS.OBSTACULOS_IZQDA,ACTIONS.ACTION_LEFT), -1000);
		R.put(new ParEstadoAccion(ESTADOS.OBSTACULOS_DCHA,ACTIONS.ACTION_RIGHT), -1000);
		R.put(new ParEstadoAccion(ESTADOS.OBSTACULO_ARRIBA,ACTIONS.ACTION_UP), -1000);
		
		R.put(new ParEstadoAccion(ESTADOS.HUECO_ABAJO,ACTIONS.ACTION_DOWN), 75);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_ARRIBA,ACTIONS.ACTION_UP), 75);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_IZQDA,ACTIONS.ACTION_LEFT), 75);
		R.put(new ParEstadoAccion(ESTADOS.HUECO_DCHA,ACTIONS.ACTION_RIGHT), 75);
		
		R.put(new ParEstadoAccion(ESTADOS.GASOLINA_ABAJO,ACTIONS.ACTION_DOWN), 75);
		//R.put(new ParEstadoAccion(ESTADOS.GASOLINA_ARRIBA,ACTIONS.ACTION_UP), -750);
		R.put(new ParEstadoAccion(ESTADOS.GASOLINA_IZQDA,ACTIONS.ACTION_LEFT), 75);
		R.put(new ParEstadoAccion(ESTADOS.GASOLINA_DCHA,ACTIONS.ACTION_RIGHT), 75);
	}
	
	/*
	 * Inializamos la TablaQ
	 */
	private void inicializaTablaQ(boolean random)
	{
		Q = new HashMap<ParEstadoAccion, Double>(numEstados*numAcciones);
		
		if(random) {
			/* Inicializamos todos los valores Q a random */
			for (ESTADOS estado: ESTADOS.values()) 
				for(ACTIONS accion : ACCIONES)			
					Q.put(new ParEstadoAccion(estado,accion), randomGenerator.nextDouble() * 100);
		}
		else {
			/* Inicializamos todos los valores Q a cero */
			for (ESTADOS estado: ESTADOS.values()) 
				for(ACTIONS accion : ACCIONES)
					Q.put(new ParEstadoAccion(estado,accion), 0.0);
		}
						
	}
	
	public static ESTADOS getEstadoFuturo(StateObservation obs, ACTIONS action)
	{
		int vidaActual = obs.getAvatarHealthPoints();
		obs.advance(action);
		return getEstado(obs, vidaActual, getMapaObstaculos(obs));
	}
	
	public static ESTADOS getEstado(StateObservation obs, int vidaAnterior, char[][] mapaObstaculos)
	{
		int vidaActual = obs.getAvatarHealthPoints();
		double [] pos = getCeldaPreciso(obs.getAvatarPosition(), obs.getWorldDimension()); 
		posActual = getIndiceMapa(pos);
		
		if (verbose) System.out.println("POS ACTUAL = " + pos[0]+"-"+pos[1]);
		if(verbose) System.out.println("POSICION REAL: " + obs.getAvatarPosition().toString());
		
		if(posActual[1] == 2)
			return ESTADOS.BORDE_IZQDA;
		
		if(posActual[1] == Util.numCol*2-2)
			return ESTADOS.BORDE_DCHA;
		
		if(estoyRodeadoObstaculos(mapaObstaculos) || obs.isGameOver() || posActual[0]==-1 && posActual[1]==-1)
			return ESTADOS.MUERTE_SEGURA; // "MUERTE SEGURA"
		
		if(posActual[0] != -1 && posActual[1] != -1) {
			//mapaObstaculos = Util.getMapaObstaculos(obs);
			
			if(vidaActual > vidaAnterior)
				return ESTADOS.OBTENGO_GASOLINA;// "+GASOLINA"
			
			
			int[] numObstaculosFila = getObstaculosFila(mapaObstaculos);
			int numObstaculosIzqda = numObstaculosFila[0];
			int numObstaculosDcha = numObstaculosFila[1];
			
			if (verbose) System.out.println("N obstaculos izqda = " + numObstaculosIzqda);
			if (verbose) System.out.println("N obstaculos dcha = " + numObstaculosDcha);
			
			int[] posGasolina = getPosGasolina(mapaObstaculos);
			
			if (verbose) System.out.println("POS GASOLINA: " + posGasolina[0] + "-" + posGasolina[1]);
			
/*						
			// Percibimos los huecos
			if(posActual[0]-2 > 0) {

				if(mapaObstaculos[posActual[0]-1][posActual[1]] == ' ' && mapaObstaculos[posActual[0]-2][posActual[1]] == ' ')
					return ESTADOS.HUECO_ARRIBA;
			}
			
			if(posActual[0]+1 < Util.numFilas) {
				if(mapaObstaculos[posActual[0]+1][posActual[1]] == ' ')
					return ESTADOS.HUECO_ABAJO;
			}
			
			if(posActual[1]-1 > 0) {
				
				if(mapaObstaculos[posActual[0]][posActual[1]-1] == ' ')
					return ESTADOS.HUECO_IZQDA;
				
			}
			
			if(posActual[0]+1 < Util.numCol) {
			
				if(mapaObstaculos[posActual[0]][posActual[1]+1] == ' ')
					return ESTADOS.HUECO_DCHA;
			}
			
*/

			
			if(posGasolina[0] != -1 && posGasolina[1] != -1) { // SI HAY GASOLINA
				ESTADOS estadoGasolina = getEstadoGasolina(posGasolina); // Obtiene el estado en funcion de la posicion de la gasolina
				if(!estadoGasolina.equals(ESTADOS.GASOLINA_ARRIBA))
					return estadoGasolina;
			}
			
			if(numObstaculosDcha >= 1 && numObstaculosIzqda >= 1 || mapaObstaculos[posActual[0]-1][posActual[1]] == ' ' && mapaObstaculos[posActual[0]-2][posActual[1]] == ' ')
				return ESTADOS.ESQUIVO_OBSTACULO; // ESQUIVO OBSTACULOS
			
			if(posActual[0]-1 > Util.numFilas)	
				if(mapaObstaculos[posActual[0]-1][posActual[1]] == 'X' || mapaObstaculos[posActual[0]-2][posActual[1]] == 'X' || mapaObstaculos[posActual[0]-3][posActual[1]] == 'X' || mapaObstaculos[posActual[0]-4][posActual[1]] == 'X')
					return ESTADOS.OBSTACULO_ARRIBA;
			
			if(posActual[1]+1 < Util.numCol)
				if(mapaObstaculos[posActual[0]-1][posActual[1]+1] == 'X' || mapaObstaculos[posActual[0]][posActual[1]+1] == 'X')
					return ESTADOS.OBSTACULOS_DCHA;
			
			if(posActual[1]-1 > 0)
				if(mapaObstaculos[posActual[0]-1][posActual[1]-1] == 'X' || mapaObstaculos[posActual[0]][posActual[1]-1] == 'X')
					return ESTADOS.OBSTACULOS_IZQDA;
					
		}
		
		return ESTADOS.NIL;
	}
	
	/*
	 * Devuelve un array de dos enteros indicando el numero de obstaculos a la izqda y a la derecha del agente
	 */
	private static int[] getObstaculosFila(char[][] mapaObstaculos)
	{
		int numCol = Util.numCol;
		int numObstaculosDcha = 0;
		int numObstaculosIzqda = 0;
				
		//Desde la casilla a la derecha hasta el arbol de la derecha
		for (int i = posActual[1]+1; i < numCol*2 -1; i++) {
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
	
	
	private static boolean estoyRodeadoObstaculos(char[][] mapaObstaculos)
	{
		// X X X ||   X 
		//   O   || X O X
		
		boolean cond1=false, cond2=false, cond3=false, cond4=false, cond5=false;
		
		try {
			cond1 = mapaObstaculos[posActual[0]-1][posActual[1]]=='X';
			cond2 = mapaObstaculos[posActual[0]-1][posActual[1]+1]=='X';
			cond3 = mapaObstaculos[posActual[0]-1][posActual[1]-1]=='X';
			cond4 = mapaObstaculos[posActual[0]][posActual[1]+1]=='X';
			cond5 = mapaObstaculos[posActual[0]][posActual[1]-1]=='X';
		} catch(Exception ex) {
			//
		}
		
		return(cond1 && ( (cond2 && cond3) || (cond4 && cond5) ));
		
		/*return(mapaObstaculos[posActual[0]-1][posActual[1]]=='X' 
				&& ( (mapaObstaculos[posActual[0]-1][posActual[1]+1]=='X'
					&& mapaObstaculos[posActual[0]-1][posActual[1]-1]=='X')
				|| (mapaObstaculos[posActual[0]][posActual[1]-1]=='X'
						&& mapaObstaculos[posActual[0]][posActual[1]-1]=='X') )
				);*/
		
	}
	
	private static int[] getPosGasolina(char[][] mapaObstaculos)
	{
		int posGasolina[] = new int[] {-1,-1};
		boolean encontrado = false;
		
		// Recorremos el mapa para buscar la pos de la gasolina
		//int i=0, j=1;
//		while( i < Util.numFilas && !encontrado ) {
//			while( j < Util.numCol-1 && !encontrado ) {
//				if( mapaObstaculos[i][j] == 'G' ) {
//					posGasolina = new int[] {i,j};
//					encontrado = true;
//				} else
//					j++;
//			}
//			i++;
//		}
		
		

			for(int i=0; i< Util.numFilas; i++) {
				for (int j = 1; j < Util.numCol-1; j++)  //Quitando los arboles del borde
					if(mapaObstaculos[i][j] == 'G') {
						posGasolina = new int[]{i,j};
						encontrado = true;
						break;
					}
				if(encontrado) break;
			}

		
		return posGasolina;
	}
	
	private static ESTADOS getEstadoGasolina(int [] posGasolina)
	{
		
		/* Misma columna y gasolina por encima */
		if(posGasolina[1] == posActual[1] && posGasolina[0] <= posActual[0]) 
			return ESTADOS.GASOLINA_ARRIBA; 
		
		/* Gasolina a la derecha */
		else if(posGasolina[1] > posActual[1])
			return ESTADOS.GASOLINA_DCHA;
		
		/* Gasolina a la izqda */
		else if(posGasolina[1] < posActual[1])
			return ESTADOS.GASOLINA_IZQDA;
		
		/* Gasolina abajo */
		else if(posGasolina[0] > posActual[0])
			return ESTADOS.GASOLINA_ABAJO;	
		
		return ESTADOS.NIL;
	}
	
	public void getContadoresEstados()
	{
		for (ESTADOS s : ESTADOS.values()) {
			
			System.out.println(s.toString() + " : " + s.getContador());
		}
	}
	
	/**
	 * Si no le indicamos el nombre del fichero, usa uno por defecto.
	 */
	public void saveQTable() {
		saveQTable("TablaQ.csv");
	}
	
	/**
	 * Escribe la tabla Q del atributo de la clase en 
	 * el fichero QTable.csv, para poder ser leída en 
	 * una siguiente etapa de aprendizaje.
	 */
	public void saveQTable(String fileName) 
	{
		/* Creación del fichero de salida */
	    try (PrintWriter csvFile = new PrintWriter(new File(fileName))) {
			
			if( verbose ) System.out.println(" GENERANDO EL FICHERO DE LA TABLAQ... ");
			
			StringBuilder buffer = new StringBuilder();
			buffer.append("ESTADOS");
			buffer.append(";");
			
			for( ACTIONS accion : StateManager.ACCIONES ) {
				buffer.append( accion.toString() );
				buffer.append(";");
			}
			
			buffer.append("\n");
			
			for ( ESTADOS estado: ESTADOS.values() ) {
				buffer.append(estado.toString());
				buffer.append(";");

				for( ACTIONS accion : StateManager.ACCIONES ) {
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
	
	private void cargaTablaQ(String filename) {
		
		/* Creación del fichero de salida */
	    try (Scanner fichero = new Scanner(new File(filename));){
	    	
			if( verbose ) System.out.println(" CARGANDO EL FICHERO DE LA TABLAQ: "+filename);
			
		
	    	
			String linea = fichero.nextLine();
			String [] cabecera = linea.split(";");
			
			ACTIONS[] actions = new ACTIONS[cabecera.length];
						
			for(int i = 1; i<cabecera.length; i++)
			{
				for(ACTIONS a : ACCIONES)
				{
					if(verbose) System.out.println("NOMBRE ACCION: " + a.toString());
					if(a.toString().equals(cabecera[i])) {
						actions[i] = a;
						if(verbose) System.out.println(actions[i] + " = " + a.toString());
						break;
					}
				}
			}
			
			while(fichero.hasNextLine())
			{
				linea = fichero.nextLine();
				
				String [] campos = linea.split(";");
	
				
				//Según el estado
				ESTADOS estado = ESTADOS.buscaEstado(campos[0]);
				
				
				//Por cada celda, le metemos el valor Q reemplazando coma por punto
				for(int i=1; i<campos.length; i++)
					Q.put(new ParEstadoAccion(estado,actions[i]), Double.parseDouble(campos[i].replace(',', '.').replace('"', Character.MIN_VALUE)));
					
			}
			
			fichero.close();
	
	    } catch( Exception ex ) {
	    	System.out.println(ex.getMessage());
		}
	}
// _____________________________________________________________________
//                    METODOS PERCEPCION MAPA
// _____________________________________________________________________
	
	public static char[][] getMapaObstaculos(StateObservation obs)
	{
		// El desplazamiento de un jugador es en 0.5 casillas
		char[][] mapaObstaculos = new char[Util.numFilas*2][Util.numCol*2];
		
		for(int i=0; i<Util.numFilas*2; i++)
			for(int j=0; j<Util.numCol*2; j++)
				mapaObstaculos[i][j] = ' ';
		
		
	    	for(ArrayList<Observation> lista : obs.getMovablePositions())
	    		for(Observation objeto : lista)
	    		{
	    			
	    			double[] pos = getCeldaPreciso(objeto.position, obs.getWorldDimension()); // Posicion en casilla real 0.5
	    			int [] indicePos = getIndiceMapa(pos); // Indice del mapa
	    		
	    			
	    			System.out.println("Objeto en " + pos[0] + "-" + pos[1] + " = "+ objeto.itype + " REAL: " + objeto.position.toString());
	    			//System.out.println(this.mapaObstaculos[pos[0]][pos[1]]);
	    			
	    			switch(objeto.itype)
					{    					
						case 1:
							mapaObstaculos[indicePos[0]][indicePos[1]] = 'O';
							break;
						case 10:
						case 16:
							mapaObstaculos[indicePos[0]][indicePos[1]] = '|';
							break;
						case 6:
						case 7:
						case 13:
						case 14:
							mapaObstaculos[indicePos[0]][indicePos[1]] = 'X';
							break;
						case 9:
						case 15:
							mapaObstaculos[indicePos[0]][indicePos[1]] = 'G';
							break; 
						default:
							mapaObstaculos[indicePos[0]][indicePos[1]] = '.';
							break;
	    		}
			}
    	
    	return mapaObstaculos;
	}
	
	/*
	 * Obtiene la posicion en filas,col con precisión .5
	 */
	public static double[] getCeldaPreciso(Vector2d vector, Dimension dim) {
		
    	double x = vector.x /  dim.getWidth() * Util.numCol;
    	double y = vector.y /  dim.getHeight() * Util.numFilas;
    	
    	return new double[] {y,x};
	}
	
	/*
	 * Devuelve el indice del mapa de obstaculos que corresponde el parametro de posicion
	 */
	public static int[] getIndiceMapa(double [] pos)
	{
		return new int[]{(int)(pos[0]*2), (int)(pos[1]*2)};
	}
}