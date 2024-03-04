package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;

public class MonteCarloBot extends ExampleSneakyBot {
    private static final int NUM_SIMULATIONS = 1000;
    private Random rand = new Random();

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> moves = state.getField().getAvailableMoves();
        int maxWins = -1;
        IMove bestMove = null;

        for (IMove move : moves) {
            int wins = 0;
            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                GameSimulator simulator = createSimulator(state);
                simulator.updateGame(move);
                while (simulator.getGameOver() == GameOverState.Active) {
                    List<IMove> availableMoves = simulator.getCurrentState().getField().getAvailableMoves();
                    IMove randomMove = availableMoves.get(rand.nextInt(availableMoves.size()));
                    simulator.updateGame(randomMove);
                }
                if (simulator.getGameOver() == GameOverState.Win) {
                    wins++;
                }
            }
            if (wins > maxWins) {
                maxWins = wins;
                bestMove = move;
            }
        }

        return bestMove;
    }

    @Override
    public String getBotName() {
        return "MonteCarloBot";
    }

    private GameSimulator createSimulator(IGameState state) {
        GameSimulator simulator = new GameSimulator(new GameState());
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }
}