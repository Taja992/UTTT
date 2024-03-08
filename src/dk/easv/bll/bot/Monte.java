package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;

public class Monte implements IBot {
    private static final String botName = "Monte Carlo Bot-Brandon";
    private Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        // Get all available moves
        List<IMove> availableMoves = state.getField().getAvailableMoves();

        // Check for winning moves
        for (IMove move : availableMoves) {
            // Clone the game state to simulate the move
            IGameState simulatedState = cloneGameState(state);
            // Simulate the move
            simulatedState.getField().getBoard()[move.getX()][move.getY()] = "0";
            // Check if the move is a winning move
            if (checkWinCondition(simulatedState, move.getX() / 3 * 3, move.getY() / 3 * 3, "0")) {
                return move; // Return the winning move
            }
        }

        // Check for opponent's winning moves
        for (IMove move : availableMoves) {
            // Clone the game state to simulate the opponent's move
            IGameState simulatedState = cloneGameState(state);
            // Simulate the opponent's move
            simulatedState.getField().getBoard()[move.getX()][move.getY()] = "1";
            // Update the move number
            simulatedState.setMoveNumber(simulatedState.getMoveNumber() + 1);
            // Check if the opponent's move is a winning move
            if (checkWinCondition(simulatedState, move.getX() / 3 * 3, move.getY() / 3 * 3, "1")) {
                return move; // Block the opponent's winning move
            }
        }

        // Initialize the best move and the best win rate
        IMove bestMove = availableMoves.getFirst();
        double bestWinRate = Double.NEGATIVE_INFINITY;
        // Calculate the time per move
        long timePerMove = 990 / availableMoves.size();

        // Iterate over all available moves
        for (int i = 0; i < availableMoves.size(); i++) {
            IMove move = availableMoves.get(i);
            // Skip the move if it opens a board
            if (opensBoard(state, move)) {
                continue;
            }
            // Initialize the number of wins and simulations
            int wins = 0;
            int simulations = 0;
            // Record the start time
            long startTime = System.currentTimeMillis();
            // Simulate the game until the time per move is up
            while (System.currentTimeMillis() - startTime < timePerMove) {
                // Clone the game state to simulate the game
                IGameState simulatedState = cloneGameState(state);
                // Simulate the move
                simulatedState.getField().getBoard()[move.getX()][move.getY()] = "0";
                // If the simulated game is a win, increment the number of wins
                if (simulateGame(simulatedState)) {
                    wins++;
                }
                // Increment the number of simulations
                simulations++;
            }

            // Print the number of simulations
            System.out.println("Move: " + move + ", Simulations: " + simulations);

            // Calculate the win rate
            double winRate = (double) wins / simulations;
            // If the win rate is better than the best win rate, update the best move and the best win rate
            if (winRate > bestWinRate) {
                bestWinRate = winRate;
                bestMove = move;
            }
        }

        // Return the best move
        return bestMove;
    }

    private boolean simulateGame(IGameState state) {
        // Keep playing until the game board is full
        while (!state.getField().isFull()) {
            // Get the list of available moves
            List<IMove> availableMoves = state.getField().getAvailableMoves();
            // If there are no more moves available, the game is a draw
            if (availableMoves.isEmpty()) {
                return false;
            }
            // Choose a random move from the available moves
            IMove randomMove = availableMoves.get(random.nextInt(availableMoves.size()));

            // Determine the current player based on the move number
            String currentPlayer = state.getMoveNumber() % 2 == 0 ? "0" : "1";
            // Make the move on the game board
            state.getField().getBoard()[randomMove.getX()][randomMove.getY()] = currentPlayer;
            // Increment the move number
            state.setMoveNumber(state.getMoveNumber() + 1);

            // Check if the current player has won the game
            int subBoardX = randomMove.getX() / 3;
            int subBoardY = randomMove.getY() / 3;
            if (checkWinCondition(state, subBoardX * 3, subBoardY * 3, currentPlayer)) {
                // If the current player has won, return true
                return true;
            }
        }

        // If the game board is full and no player has won, the game is a draw
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