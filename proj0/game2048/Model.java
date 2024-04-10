package game2048;

import java.util.Formatter;
import java.util.Observable;


/**
 * The state of a game of 2048.
 *
 * @author YAO
 */
public class Model extends Observable {
    /**
     * Current contents of the board.
     */
    private Board board;
    /**
     * Current score.
     */
    private int score;
    /**
     * Maximum score so far.  Updated when game ends.
     */
    private int maxScore;
    /**
     * True iff game is ended.
     */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /**
     * Largest piece value.
     */
    public static final int MAX_PIECE = 2048;

    /**
     * A new 2048 game on a board of size SIZE with no pieces
     * and score 0.
     */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /**
     * A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes.
     */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /**
     * Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     * 0 <= COL < size(). Returns null if there is no tile there.
     * Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /**
     * Return the number of squares on one side of the board.
     * Used for testing. Should be deprecated and removed.
     */
    public int size() {
        return board.size();
    }

    /**
     * Return true iff the game is over (there are no moves, or
     * there is a tile with value 2048 on the board).
     */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /**
     * Return the current score.
     */
    public int score() {
        return score;
    }

    /**
     * Return the current maximum game score (updated at end of game).
     */
    public int maxScore() {
        return maxScore;
    }

    /**
     * Clear the board to empty and reset the score.
     */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /**
     * Add TILE to the board. There must be no Tile currently at the
     * same position.
     */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /**
     * Tilt the board toward SIDE. Return true if this changes the board.
     * <p>
     * 1. If two Tile objects are adjacent in the direction of motion and have
     * the same value, they are merged into one Tile of twice the original
     * value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     * tilt. So each move, every tile will only ever be part of at most one
     * merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     * value, then the leading two tiles in the direction of motion merge,
     * and the trailing tile does not.
     */
    public boolean tilt(Side side) {
        board.setViewingPerspective(side); //先设置好方向
        boolean changed;
        boolean temp_changed;
        changed = false;
        temp_changed = false;
        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.
        for (int i = 0; i < 4; i++) {
            temp_changed = processColumn(side, i);
            if (temp_changed) {
                changed = temp_changed;
            }
        }
        checkGameOver();
        if (changed) {
            setChanged();
        }
        board.setViewingPerspective(Side.NORTH); //return之前设置好正确方向(向NORTH)
        return changed;
    }

    /**
     * Process a column of the board
     *
     * @param column
     * @return moved
     */
    public boolean processColumn(Side s, int column) {
        /**
         * 记录是否发生merge合并，若合并则无法进行第二次合并
         */
        board.setViewingPerspective(s); //先设置好方向
        boolean moved = false;
        boolean changed = false;
        int empty_idx = 3; // 记录空白格索引
        int noMerge_idx = 3; // 记录无合并块索引
        int noMerge_value = 0; // 记录无合并块的值

        Tile t0 = board.tile(column, 3);
        if (t0 != null) {
            noMerge_value = t0.value();
            empty_idx = empty_idx - 1;
        }
        for (int i = 2; i >= 0; i--) {
            Tile t = board.tile(column, i);
            if (t != null) {
                if (t.value() == noMerge_value) {   //当有相同的值时进行合并
                    changed = board.move(column, noMerge_idx, t);
                    noMerge_idx = noMerge_idx - 1;
                } else {    //否则就移动到空白位置
                    changed = board.move(column, empty_idx, t);
                }
                if (changed) {
                    score += t.value() * 2;
                } else {
                    noMerge_value = t.value();
                    noMerge_idx = empty_idx;
                    empty_idx = empty_idx - 1;
                }
                moved = true;
            } else if (i > empty_idx) {
                empty_idx = i;
            }
        }
        board.setViewingPerspective(Side.NORTH);
        return moved;
    }

    /**
     * Checks if the game is over and sets the gameOver variable
     * appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /**
     * Determine whether game is over.
     */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /**
     * Returns true if at least one space on the Board is empty.
     * Empty spaces are stored as null.
     */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                if (b.tile(i, j) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                Tile _tile = b.tile(i, j);
                if (_tile != null && _tile.value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        boolean existsRow = false;
        boolean existsCol = false;
        existsRow = checkRow(b);
        existsCol = checkCol(b);
        return (existsRow || existsCol);
    }

    public static boolean checkRow(Board b) {
        Tile flag_tile = b.tile(0, 0);
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                Tile _tile = b.tile(i, j);
                if (_tile == null) { //存在空块
                    return true;
                } else if (_tile.value() == flag_tile.value() && _tile != flag_tile && (_tile.col() == flag_tile.col() || _tile.row() == flag_tile.row())) { //存在邻近块的值相等
                    return true;
                } else {
                    flag_tile = _tile;
                }
            }
        }
        return false;
    }

    public static boolean checkCol(Board b) {
        Tile flag_tile = b.tile(0, 0);
        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                Tile _tile = b.tile(j, i);
                if (_tile == null) { //存在空块
                    return true;
                } else if (_tile.value() == flag_tile.value() && _tile != flag_tile && (_tile.col() == flag_tile.col() || _tile.row() == flag_tile.row())) { //存在邻近块的值相等
                    return true;
                } else {
                    flag_tile = _tile;
                }
            }
        }
        return false;
    }


    @Override
    /** Returns the model as a string, used for debugging. */ public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */ public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */ public int hashCode() {
        return toString().hashCode();
    }
}
