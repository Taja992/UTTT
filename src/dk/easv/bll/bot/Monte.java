package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;

public class Monte implements IBot {
    private static final String botName = "Monte Carlo Bot-Brandon";
    private static final int NUM_SIMULATIONS = 1000;
    private Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        IMove bestMove = null;
        double bestWinRate = Double.NEGATIVE_INFINITY;

        for (IMove move : availableMoves) {
            int wins = 0;
            for (int i = 0; i < NUM_SIMULATIONS; i++) {
                IGameState simulatedState = cloneGameState(state);
                simulatedState.getField().getBoard()[move.getX()][move.getY()] = "0";
                if (simulateGame(simulatedState)) {
                    wins++;
                }
            }

            double winRate = (double) wins / NUM_SIMULATIONS;
            if (winRate > bestWinRate) {
                bestWinRate = winRate;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private boolean simulateGame(IGameState state) {
        while (!state.getField().isFull()) {
            List<IMove> availableMoves = state.getField().getAvailableMoves();
            if (availableMoves.isEmpty()) {
                // No more moves available, game is a draw
                return false;
            }
            IMove randomMove = availableMoves.get(random.nextInt(availableMoves.size()));

            state.getField().getBoard()[randomMove.getX()][randomMove.getY()] = state.getMoveNumber() % 2 == 0 ? "0" : "1";
            state.setMoveNumber(state.getMoveNumber() + 1);

            // Check for win condition in the sub-board
            int subBoardX = randomMove.getX() / 3;
            int subBoardY = randomMove.getY() / 3;
            if (checkWinCondition(state, subBoardX * 3, subBoardY * 3)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkWinCondition(IGameState state, int startX, int startY) {
        String[][] board = state.getField().getBoard();
        String player = state.getMoveNumber() % 2 == 0 ? "0" : "1";

        // Check rows
        for (int y = 0; y < 3; y++) {
            if (board[startX][startY + y].equals(player) &&
                    board[startX + 1][startY + y].equals(player) &&
                    board[startX + 2][startY + y].equals(player)) {
                return true;
            }
        }

        // Check columns
        for (int x = 0; x < 3; x++) {
            if (board[startX + x][startY].equals(player) &&
                    board[startX + x][startY + 1].equals(player) &&
                    board[startX + x][startY + 2].equals(player)) {
                return true;
            }
        }

        // Check diagonals
        if (board[startX][startY].equals(player) &&
                board[startX + 1][startY + 1].equals(player) &&
                board[startX + 2][startY + 2].equals(player)) {
            return true;
        }

        if (board[startX + 2][startY].equals(player) &&
                board[startX + 1][startY + 1].equals(player) &&
                board[startX][startY + 2].equals(player)) {
            return true;
        }

        return false;
    }

    @Override
    public String getBotName() {
        return botName;
    }

    private IGameState cloneGameState(IGameState state) {
        return new GameState(state);
    }
}