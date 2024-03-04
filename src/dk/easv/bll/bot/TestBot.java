package dk.easv.bll.bot;

import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;

public class TestBot implements IBot {
    private static final String botName = "Test Bot";

    @Override
    public IMove doMove(IGameState state) {
        return minimax(state, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, true).move;
    }

    private ScoredMove minimax(IGameState state, int depth, int alpha, int beta, boolean maximizingPlayer) {
        //get a list of all available moves from the game state via the interface
        List<IMove> availableMoves = state.getField().getAvailableMoves();


        if (depth == 0 || availableMoves.isEmpty()) {
            if (availableMoves.isEmpty()) {
                throw new IllegalStateException("No available moves left");
            } else {
                // Select a random move
                IMove randomMove = availableMoves.get(new Random().nextInt(availableMoves.size()));
                return new ScoredMove(randomMove, evaluate(state));
            }
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            IMove bestMove = null;

            for (IMove move : availableMoves) {
                IGameState newState = cloneGameState(state);
                newState.getField().getBoard()[move.getX()][move.getY()] = "0";

                int eval = minimax(newState, depth - 1, alpha, beta, false).score;
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = move;
                }

                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }

            return new ScoredMove(bestMove, maxEval);
        } else {
            int minEval = Integer.MAX_VALUE;
            IMove bestMove = null;

            for (IMove move : availableMoves) {
                IGameState newState = cloneGameState(state);
                newState.getField().getBoard()[move.getX()][move.getY()] = "1";

                int eval = minimax(newState, depth - 1, alpha, beta, true).score;
                if (eval < minEval) {
                    minEval = eval;
                    bestMove = move;
                }

                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }

            return new ScoredMove(bestMove, minEval);
        }
    }

    private int evaluate(IGameState state) {
        String[][] board = state.getField().getBoard();
        int score = 0;

        // Check rows for winner
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
                if (board[i][0].equals("0")) {
                    score = 1;
                } else if (board[i][0].equals("1")) {
                    score = -1;
                }
            }
        }

        // Check columns for winner
        for (int i = 0; i < 3; i++) {
            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) {
                if (board[0][i].equals("0")) {
                    score = 1;
                } else if (board[0][i].equals("1")) {
                    score = -1;
                }
            }
        }

        // Check diagonals for winner
        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) ||
                board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            if (board[1][1].equals("0")) {
                score = 1;
            } else if (board[1][1].equals("1")) {
                score = -1;
            }
        }

        return score;
    }

    @Override
    public String getBotName() {
        return botName;
    }

    private static class ScoredMove {
        public IMove move;
        public int score;

        public ScoredMove(IMove move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    private IGameState cloneGameState(IGameState state) {
        return new GameState(state);
    }
}