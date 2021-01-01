package tracks.singlePlayer;

import java.util.Random;

import qlearning.StateManager;
import qlearning.StateManager.ESTADOS;
import tools.Utils;
import tracks.ArcadeMachine;

public class Test {

    public static void main(String[] args) {

    	String QLearningTraining = "qlearning.TrainingAgent";
    	String QLearningTesting = "qlearning.TestingAgent";


		//Load available games
		String spGamesCollection =  "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		boolean visuals = true;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 80;
		
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];


		String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
	
		
		int levelIdx = 3; // level names from 0 to 4 (game_lvlN.txt).
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
		StateManager stateManager;
		
		boolean training = true; // Modo entrenamiento, crea una nueva tabla Q y juega M partidas aleatorias
		
		boolean verbose = true; // Mostrar informacion de la partida mientras se ejecuta
		if(training)	// Crea la tabla Q a random y juega partidas con acciones aleatorias
		{
			boolean testingAfterTraining = true; // Probar todos los niveles despues del entrenamiento
			boolean randomTablaQ = false; // Verdadero: crea la tabla Q con valores random, si no, a cero
			
			stateManager = new StateManager(randomTablaQ,false);
			int M = 500; // Numero de partidas a jugar
					
			for (int i = 0; i < M; i++) {
				levelIdx = new Random().nextInt(5); // level names from 0 to 4 (game_lvlN.txt).
				level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
				System.out.println("\t\t\t\t\t\t\t\t\t\tIteración " + i + " / "+ M);
				System.out.println("\t\t\t\t\t\t\t\t\t\tlevel: " + levelIdx);
				ArcadeMachine.runGames(game, new String[]{level1}, 1, QLearningTraining, null);
			}
		
			stateManager.saveQTable();
			
			if(testingAfterTraining) // Probar todos los niveles
			{
				double[] ticksPartidas = new double[5];
				
				stateManager = new StateManager("TablaQ.csv", verbose);
				for (int i = 0; i <= 4; i++) {
				
					levelIdx = i; // level names from 0 to 4 (game_lvlN.txt).
					level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
					ticksPartidas[i] = ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0)[2];
				}
				
				System.out.println("____________________________________________________");
				System.out.println("____________ ESTADISTICAS PARTIDAS _________________");
				double total = 0;
				for(int i = 0; i <= 4; i++) {
						System.out.println("TICKS JUEGO " + i + " =\t"+ ticksPartidas[i]);
						total += ticksPartidas[i];
				}
				
				System.out.println("MEDIA TICKS =\t" + total / 5.0);
				System.out.println("____________________________________________________");
				
			}
		}
		else // Modo Test, probar el nivel indicado
		{
			stateManager = new StateManager("TablaQ.csv", true);
			ArcadeMachine.runOneGame(game, level1, visuals, QLearningTesting, recordActionsFile, seed, 0);
		}
		
		System.out.println("____________ CONTADORES ESTADOS _____________________");
		stateManager.getContadoresEstados();
		

		StateManager.pintaQTableResumen();
		
		

		}
    }

