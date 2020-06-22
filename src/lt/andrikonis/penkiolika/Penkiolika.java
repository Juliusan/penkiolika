/*
 * Technical task for Danske bankas
 */
package lt.andrikonis.penkiolika;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * The representation of Fifteen game (puzzle).
 *
 * @author julius
 */
public class Penkiolika {

    /**
     * Default number of times to shuffle the game.
     */
    public static final int DEFAULT_SHUFFLE_TIMES = 101;

    /**
     * How empty cell is represented in the game state: {@value #EMPTY_CELL}.
     */
    public static final int EMPTY_CELL = 0;

    /**
     * The board, which is considered final.
     * <pre>
     * {@code
     *    1  2  3  4
     *    5  6  7  8
     *    9 10 11 12
     *   13 14 15 <empty cell>
     * }
     * </pre>
     *
     */
    public static final int[] FINAL_BOARD = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,EMPTY_CELL};

    // The state of the game. The order of the elements is:
    //  board[ 0] board[ 1] board[ 2] board[ 3]
    //  board[ 4] board[ 5] board[ 6] board[ 7]
    //  board[ 8] board[ 9] board[10] board[11]
    //  board[12] board[13] board[14] board[15]
    private int[] board;

    /**
     * Creates a new game, which is already completed.
     */
    public Penkiolika() {
        board = FINAL_BOARD.clone();
    }

    /**
     * Creates a new game whith brovided game state.
     *
     * @param board the state of the game to be created.
     */
    public Penkiolika(int[] board) {
        if (board.length == 16) {
            this.board = board.clone();
        } else {
            throw new IllegalArgumentException("Board size should equal to 16");
        }
    }

    /**
     * Returns the current state of the game.
     *
     * @return the state of the game.
     */
    public int[] getBoard() {
        return board.clone();
    }

    /**
     * Shuffles the game board default number of times. For details see
     * {@link #shuffle(int)}.
     */
    public void shuffle() {
        this.shuffle(DEFAULT_SHUFFLE_TIMES);
    }

    /**
     * Shuffles the game board. Moves the empty space specified number of times.
     * It is guaranteed that the empty space will be moved exactly specified
     * number of times. It is not guaranteed that the game state won't be repeated
     * during the shuffle. Therefore if the argument is even there is a tiny
     * possibility that the game state after the shuffle remains the same as before
     * it.
     * <p>
     * The shuffle is needed, because a randomly generated puzzle (game) might
     * not be solvable. A puzzle created from a final state and shuffled is
     * allways solvable (just as in the hardware version of the puzzle).
     *
     * @param times number of times to move the empty cell.
     */
    public synchronized void shuffle(int times) {
        Random random = new Random();
         while (times > 0) {
            int direction = random.nextInt(4);
            boolean moved = false;
            if (direction == 0) {
                moved = this.moveTop();
            } else if (direction == 1) {
                moved = this.moveBottom();
            } else if (direction == 2) {
                moved = this.moveLeft();
            } else if (direction == 3) {
                moved = this.moveRight();
            }
            if (moved) {    // no need to decrement, if the empty space hasn't been moved
                times--;
            }
        }
    }

    /**
     * Checks if the game is in the final state.
     *
     * @return true if and only if the game is in the final state.
     */
    public boolean isFinal() {
        return Arrays.equals(board, FINAL_BOARD);
    }

    /**
     * Moves the empty cell to the top, if it is a legal move. Does nothing
     * otherwise.
     *
     * @return true, if the move was performed, false if it is not a legal move.
     */
    public boolean moveTop() {
        return this.moveEmptyCell(i -> i >= 4, i -> i - 4);
    }

    /**
     * Moves the empty cell to the bottom, if it is a legal move. Does nothing
     * otherwise.
     *
     * @return true, if the move was performed, false if it is not a legal move.
     */
    public boolean moveBottom() {
        return this.moveEmptyCell(i -> i <= 11, i -> i + 4);
    }

    /**
     * Moves the empty cell to the left, if it is a legal move. Does nothing
     * otherwise.
     *
     * @return true, if the move was performed, false if it is not a legal move.
     */
    public boolean moveLeft() {
        return this.moveEmptyCell(i -> i % 4 != 0, i -> i - 1);
    }

    /**
     * Moves the empty cell to the right, if it is a legal move. Does nothing
     * otherwise.
     *
     * @return true, if the move was performed, false if it is not a legal move.
     */
    public boolean moveRight() {
        return this.moveEmptyCell(i -> i % 4 != 3, i -> i + 1);
    }

    // Convenience method to move an empty cell.
    // pCanBeMoved - predicate, that checks if the move current empty cell position
    //               is legal.
    // oGetNextCellIndex - operator, which returns a new empty cell position, when
    //                     provided with the current empty cell position.
    // Returns true, if the move was performed, false if it is not a legal move.
    private synchronized boolean moveEmptyCell(IntPredicate pCanBeMoved, IntUnaryOperator oGetNextCellIndex) {
        int emptyIndex = this.getEmptyCellIndex();
        boolean canBeMoved = pCanBeMoved.test(emptyIndex);
        if (canBeMoved) {
            this.swapCells(emptyIndex, oGetNextCellIndex.applyAsInt(emptyIndex));
        }
        return canBeMoved;
    }

    // Convenience method to find out the current position of an empty cell.
    private synchronized int getEmptyCellIndex() {
        for (int i=0; i < board.length; i++) {
            if (board[i] == EMPTY_CELL) {
                return i;
            }
        }
        return -1;
    }

    // Convenience method to swap too cells of the game (used to make an actual
    // move of an empty cell).
    // cell1, cell2 - indexes of the cells to be swapped.
    private synchronized void swapCells(int cell1, int cell2) {
        int tmpCellValue = board[cell1];
        board[cell1] = board[cell2];
        board[cell2] = tmpCellValue;
    }

    /**
     * Checks if this game is equal to the provided one.
     * @param o another object (possibly Fifteen game) to compare this game to.
     * @return true if and only if the provided object is Fifteen game and it
     * has the stame state as this game.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Penkiolika) {
            Penkiolika p = (Penkiolika)o;
            return Arrays.equals(this.board, p.board);
        } else {
            return false;
        }
    }

    /**
     * Calculates the hash code of this game.
     * @return the hash code of this game.
     */
    @Override
    public int hashCode() {
        return 11 + Arrays.hashCode(this.board);
    }
}
