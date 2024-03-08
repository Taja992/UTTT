package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;

public class Monte2 implements IBot {
    private static final String botName = "Monte Carlo Bot-Fused";
    private Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();

// Check for winning moves
        for (IMove move : availableMoves) {
            IGameState simulatedState = cloneGameState(state);
            simulatedState.getField().getBoard()[move.getX()][move.getY()] = "0";
            if (checkWinCondition(simulatedState, move.getX() / 3 * 3, move.getY() / 3 * 3, "0")) {
                return move; // Return the winning move
            }
        }

// Check for opponent's winning moves
        for (IMove move : availableMoves) {
            IGameState simulatedState = cloneGameState(state);
            simulatedState.getField().getBoard()[move.getX()][move.getY()] = "1"; // Simulate the opponent's move
            simulatedState.setMoveNumber(simulatedState.getMoveNumber() + 1); // Update the move number
            if (checkWinCondition(simulatedState, move.getX() / 3 * 3, move.getY() / 3 * 3, "1")) {
                return move; // Block the opponent's winning move
            }
        }

        IMove bestMove = availableMoves.getFirst();
        double bestWinRate = Double.NEGATIVE_INFINITY;
        long timePerMove = 990 / availableMoves.size();

        for (int i = 0; i < availableMoves.size(); i++) {
            IMove move = availableMoves.get(i);
            if (opensBoard(state, move)) {
                continue; // Skip this move
            }
            int wins = 0;
            int simulations = 0;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timePerMove) {
                IGameState simulatedState = cloneGameState(state);
                simulatedState.getField().getBoard()[move.getX()][move.getY()] = "0";
                if (simulateGame(simulatedState)) {
                    wins++;
                }
                simulations++;
            }

            System.out.println("Move: " + move + ", Simulations: " + simulations); // Print the number of simulations

            double winRate = (double) wins / simulations;
            if (winRate > bestWinRate) {
                bestWinRate = winRate;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private boolean isAdvantageousForOpponent(IGameState state, IMove move) {
        IGameState simulatedState = cloneGameState(state);
        simulatedState.getField().getBoard()[move.getX()][move.getY()] = "1"; // Simulate the opponent's move
        simulatedState.setMoveNumber(simulatedState.getMoveNumber() + 1); // Update the move number
        return opensBoard(simulatedState, move) || checkWinCondition(simulatedState, move.getX() / 3 * 3, move.getY() / 3 * 3, "1");
    }

    private boolean simulateGame(IGameState state) {
        while (!state.getField().isFull()) {
            List<IMove> availableMoves = state.getField().getAvailableMoves();
            if (availableMoves.isEmpty()) {
                // No more moves available, game is a draw
                return false;
            }
    
            IMove bestMove = null;
            String currentPlayer = state.getMoveNumber() % 2 == 0 ? "0" : "1";
    
            if (currentPlayer.equals("0")) { // Bot's turn
                for (IMove move : availableMoves) {
                    if (!isAdvantageousForOpponent(state, move)) {
                        bestMove = move;
                        break;
                    }
                }
    
                if (bestMove == null) {
                    // All moves are advantageous for the opponent, make a random move
                    bestMove = availableMoves.get(random.nextInt(availableMoves.size()));
                }
            } else { // Opponent's turn
                // Simulate the opponent's move. This is a simple strategy where the opponent makes a random move.
                // You can replace this with a different strategy if you have information about the opponent's likely strategy.
                bestMove = availableMoves.get(random.nextInt(availableMoves.size()));
            }
    
            state.getField().getBoard()[bestMove.getX()][bestMove.getY()] = currentPlayer;
            state.setMoveNumber(state.getMoveNumber() + 1);
    
            // Check for win condition in the sub-board
            int subBoardX = bestMove.getX() / 3;
            int subBoardY = bestMove.getY() / 3;
            if (checkWinCondition(state, subBoardX * 3, subBoardY * 3, currentPlayer)) {
                return currentPlayer.equals("0");
            }
        }
    
        return false;
    }

    private boolean checkWinCondition(IGameState state, int startX, int startY, String player) {
        String[][] board = state.getField().getBoard();

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

    private boolean opensBoard(IGameState state, IMove move) {
        int subBoardX = move.getX() % 3;
        int subBoardY = move.getY() % 3;
        String[][] board = state.getField().getBoard();

        for (int x = subBoardX * 3; x < (subBoardX + 1) * 3; x++) {
            for (int y = subBoardY * 3; y < (subBoardY + 1) * 3; y++) {
                if (board[x][y].equals(IField.EMPTY_FIELD)) {
                    return false;
                }
            }
        }

        return true;
    }
    @Override
    public String getBotName() {
        return botName;
    }

    private IGameState cloneGameState(IGameState state) {
        return new GameState(state);
    }
}