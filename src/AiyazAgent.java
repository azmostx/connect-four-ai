public class AiyazAgent extends Agent {
    private final int rows, columns;
    private final char self, opponent;
    private int[] blankSpots;
    private static final int DEPTH = 7;

    public AiyazAgent(Connect4Game game, boolean iAmRed) {
        super(game, iAmRed);
        this.rows = game.getRowCount();
        this.columns = game.getColumnCount();
        this.self = iAmRed ? 'R' : 'Y';
        this.opponent = iAmRed ? 'Y' : 'R';
    }

    @Override
    public void move() {
        char[][] matrix = myGame.getBoardMatrix();
        blankSpots = new int[columns];
        for (int i = 0; i < columns; i++) {
            for (int j = rows - 1; j >= -1; j--) {
                if (j != -1 && matrix[j][i] != 'B') continue;
                blankSpots[i] = j;
                break;
            }
        }
        if (blankSpots[columns / 2] >= rows - 2) {
            if (iAmRed) myGame.getColumn(columns / 2).getSlot(blankSpots[columns / 2]).addRed();
            else myGame.getColumn(columns / 2).getSlot(blankSpots[columns / 2]).addYellow();
            return;
        }
        int index = 0;
        float max = Float.NEGATIVE_INFINITY;
        int minDepth = DEPTH;
        for (int i = 0; i < columns; i++) {
            if (blankSpots[i] == -1) {
                if (index == i) index++;
                continue;
            }
            add(matrix, i, self);
            Object[] temp = minimax(matrix, DEPTH, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, false);
            remove(matrix, i);
            if ((float) temp[0] > max || (float) temp[0] == max && (int) temp[1] < minDepth) {
                index = i;
                max = (float) temp[0];
                minDepth = (int) temp[1];
            }
        }
        if (iAmRed) myGame.getColumn(index).getSlot(blankSpots[index]).addRed();
        else myGame.getColumn(index).getSlot(blankSpots[index]).addYellow();
    }

    private Object[] minimax(char[][] matrix, int limit, float alpha, float beta, boolean maximizing) {
        float result = evaluate(matrix);
        int maxMinDepth = maximizing ? DEPTH : 0;
        if (limit == 0 || result == Float.POSITIVE_INFINITY || result == Float.NEGATIVE_INFINITY)
            return new Object[]{result, DEPTH - limit};
        if (maximizing) {
            result = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < columns; i++) {
                if (blankSpots[i] == -1) continue;
                add(matrix, i, self);
                Object[] temp = minimax(matrix, limit - 1, alpha, beta, false);
                remove(matrix, i);
                if ((float) temp[0] > result || (float) temp[0] == result && (int) temp[1] < maxMinDepth) {
                    result = (float) temp[0];
                    maxMinDepth = (int) temp[1];
                }
                alpha = Math.max(alpha, (float) temp[0]);
                if (beta <= alpha) break;
            }
        } else {
            result = Float.POSITIVE_INFINITY;
            for (int i = 0; i < columns; i++) {
                if (blankSpots[i] == -1) continue;
                add(matrix, i, opponent);
                Object[] temp = minimax(matrix, limit - 1, alpha, beta, true);
                remove(matrix, i);
                if ((float) temp[0] < result || (float) temp[0] == result && (int) temp[1] > maxMinDepth) {
                    result = (float) temp[0];
                    maxMinDepth = (int) temp[1];
                }
                beta = Math.min(beta, (float) temp[0]);
                if (beta <= alpha) break;
            }
        }
        return new Object[]{result, maxMinDepth};
    }

    private float evaluate(char[][] matrix) {
        int oneAwayForSelf = 0;
        int twoAwayForSelf = 0;
        int oneAwayForOther = 0;
        int twoAwayForOther = 0;

        // Horizontal
        for (int r = 0; r < rows; r++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int c = 0; c < columns; c++) {
                if (matrix[r][c] == 'B') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r][c - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == self) {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                            else twoAwayForOther++;
                        }
                        if (matrix[r][c - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled >= 4)
                            return current == self ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == self) {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                                else twoAwayForOther++;
                            }
                            if (matrix[r][c - (blanks + filled - 1)] == 'B') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        // Vertical
        for (int c = 0; c < columns; c++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int r = 0; r < rows; r++) {
                if (matrix[r][c] == 'B') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r - (blanks + filled - 1)][c] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == self) {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                            else twoAwayForOther++;
                        }
                        if (matrix[r - (blanks + filled - 1)][c] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == self ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == self) {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                                else twoAwayForOther++;
                            }
                            if (matrix[r - (blanks + filled - 1)][c] == 'B') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        // Top Left to Bottom Right
        for (int r = 0; r < rows - 1; r++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int c = 0; c < columns && r + c < rows; c++) {
                int rc = r + c;
                if (matrix[rc][c] == 'B') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[rc - (blanks + filled - 1)][c - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == self) {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                            else twoAwayForOther++;
                        }
                        if (matrix[rc - (blanks + filled - 1)][c - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[rc][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == self ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == self) {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                                else twoAwayForOther++;
                            }
                            if (matrix[rc - (blanks + filled - 1)][c - (blanks + filled - 1)] == 'B') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[rc][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int c = 1; c < columns; c++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int r = 0; r < rows && r + c < columns; r++) {
                int rc = r + c;
                if (matrix[r][rc] == 'B') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r - (blanks + filled - 1)][rc - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == self) {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                            else twoAwayForOther++;
                        }
                        if (matrix[r - (blanks + filled - 1)][rc - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][rc] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == self ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == self) {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                                else twoAwayForOther++;
                            }
                            if (matrix[r - (blanks + filled - 1)][rc - (blanks + filled - 1)] == 'B') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][rc];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        // Bottom Left to Top Right
        for (int r = rows - 1; r >= 0; r--) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int c = 0; c < columns && r - c >= 0; c++) {
                int rc = r - c;
                if (matrix[rc][c] == 'B') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[rc + (blanks + filled - 1)][c - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == self) {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                            else twoAwayForOther++;
                        }
                        if (matrix[rc + (blanks + filled - 1)][c - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[rc][c] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == self ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == self) {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                                else twoAwayForOther++;
                            }
                            if (matrix[rc + (blanks + filled - 1)][c - (blanks + filled - 1)] == 'B') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[rc][c];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        for (int c = 1; c < columns; c++) {
            int blanks = 0;
            int tailBlanks = 0;
            int filled = 0;
            char current = '\u0000';
            for (int r = rows - 1; r >= 0 && c + (rows - 1 - r) < columns; r--) {
                int rc = (rows - 1 - r) + c;
                if (matrix[r][rc] == 'B') {
                    blanks++;
                    tailBlanks++;
                    if (blanks >= 3) {
                        if (matrix[r + (blanks + filled - 1)][rc - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                    if (blanks + filled == 4) {
                        if (current == self) {
                            if (blanks == 1) {
                                oneAwayForSelf++;
                            } else twoAwayForSelf++;
                        } else {
                            if (blanks == 1) oneAwayForOther++;
                            else twoAwayForOther++;
                        }
                        if (matrix[r + (blanks + filled - 1)][rc - (blanks + filled - 1)] == 'B') blanks--;
                        else filled--;
                        if (tailBlanks > blanks) tailBlanks = blanks;
                    }
                } else {
                    if (matrix[r][rc] == current) {
                        filled++;
                        tailBlanks = 0;
                        if (filled == 4)
                            return current == self ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        if (blanks + filled == 4) {
                            if (current == self) {
                                if (blanks == 1) {
                                    oneAwayForSelf++;
                                } else twoAwayForSelf++;
                            } else {
                                if (blanks == 1) oneAwayForOther++;
                                else twoAwayForOther++;
                            }
                            if (matrix[r + (blanks + filled - 1)][rc - (blanks + filled - 1)] == 'B') blanks--;
                            else filled--;
                            if (tailBlanks > blanks) tailBlanks = blanks;
                        }
                    } else {
                        current = matrix[r][rc];
                        blanks = tailBlanks;
                        tailBlanks = 0;
                        filled = 1;
                    }
                }
            }
        }

        if (DEPTH % 2 == 0) return 27.0f * oneAwayForSelf + 8.0f * twoAwayForSelf - 54.0f * oneAwayForOther - 16.0f * twoAwayForOther;
        else return 54.0f * oneAwayForSelf + 16.0f * twoAwayForSelf - 27.0f * oneAwayForOther - 8.0f * twoAwayForOther;
    }

    private void add(char[][] matrix, int column, char color) {
        while (blankSpots[column] == -1) column++;
        matrix[blankSpots[column]][column] = color;
        blankSpots[column]--;
    }

    private void remove(char[][] matrix, int column) {
        blankSpots[column]++;
        matrix[blankSpots[column]][column] = 'B';
    }

    @Override
    public String getName() {
        return "Aiyaz";
    }
}