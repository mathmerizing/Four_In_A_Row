public class Minimax
        extends Bot
{
    private int depth;
    private int leaveCount = 0;

    public Minimax(int playerNum, Board board, int depth) throws
            Exception
    {
        super(playerNum, board);
        this.depth = depth;
    }

    @Override
    public void move(Board board)
    {
        try
        {
            leaveCount = 0;
            board.nextMove(this.playerNum, this.minimaxMove(board, this.depth, Integer.MIN_VALUE, Integer.MAX_VALUE,
                                                            true));
            System.err.println("      Number of leaves: " + leaveCount);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int minimaxMove(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) throws
            Exception
    {
        int maxEval = Integer.MIN_VALUE;
        int[] evaluatedMoves = new int[board.getPossibleMoves().length];
        System.err.println("      possible moves: " + evaluatedMoves.length);
        for (int i = 0; i < board.getPossibleMoves().length; i++)
        {
            Board boardCopy = board.copy();
            boardCopy.nextMove(1, board.getPossibleMoves()[i]);
            evaluatedMoves[i] = minimax(boardCopy, depth - 1, alpha, beta, !maximizingPlayer);
        }
        int pos = board.getPossibleMoves()[0];
        int biggestValue = Integer.MIN_VALUE;
        for (int i = 0; i < board.getPossibleMoves().length; i++)
        {
            if (evaluatedMoves[i] > biggestValue)
            {
                pos = board.getPossibleMoves()[i];
                biggestValue = evaluatedMoves[i];
            }
        }
        return pos;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) throws
            Exception
    {
        if (depth == 0 || board.getPossibleMoves().length == 0)
        {
            leaveCount++;
            return evaluate(board, maximizingPlayer);
        }

        if (maximizingPlayer)
        {
            int maxEval = Integer.MIN_VALUE;
            for (int move : board.getPossibleMoves())
            {
                Board boardCopy = board.copy();
                boardCopy.nextMove(1, move);
                int eval = minimax(boardCopy, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                {
                    break;
                }
            }
            return maxEval;
        }
        else
        {
            int minEval = Integer.MAX_VALUE;
            for (int move : board.getPossibleMoves())
            {
                Board boardCopy = board.copy();
                boardCopy.nextMove(-1, move);
                int eval = minimax(boardCopy, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                {
                    break;
                }
            }
            return minEval;
        }
    }

    private int[][] boardCoefficients = {{3, 4, 5, 7, 5, 4, 3}, // coefficients for board values
            {4, 6, 8, 10, 8, 6, 4},
            {5, 8, 11, 13, 11, 8, 5},
            {5, 8, 11, 13, 11, 8, 5},
            {4, 6, 8, 10, 8, 6, 4},
            {3, 4, 5, 7, 5, 4, 3}};

    private int[] xInRowCoefficients = {0, 10, 100}; // coefficients for x in a row

    private int evaluate(Board board, boolean maximizingPlayer)
    {
        if (board.isGameOver())
        {
            if (maximizingPlayer)
            {
                return Integer.MIN_VALUE;
            }
            else
            {
                return Integer.MAX_VALUE;
            }
        }
        else if (board.getPossibleMoves().length == 0)
        {
            return 0;
        }
        int total = 0;
        total += boardStrength(board);
        total += connectedStrength(board);
        return total;
    }

    private int boardStrength(Board board)
    {
        int total = 0;
        for (int row = 0; row < board.getRows(); row++)
        {
            for (int col = 0; col < board.getColumns(); col++)
            {
                total += board.getBoardState()[row][col] * boardCoefficients[row][col];
            }
        }
        return total;
    }

    private boolean isValidColorPosition(Board board, int row, int col, int color)
    {
        return row >= 0
                && col >= 0
                && row < board.getRows()
                && col < board.getColumns()
                && board.getBoardState()[row][col] == color;
    }

    private int getLength(Board board, IntegerPair position, IntegerPair directions, int currentColor, boolean[][]
            visited, int count)
    {
        int row = position.getFirst() + count * directions.getFirst();
        int col = position.getSecond() + count * directions.getSecond();
        while (isValidColorPosition(board, row, col, currentColor))
        {
            visited[row][col] = true;
            count += 1;
            row += directions.getFirst();
            col += directions.getSecond();
        }
        return count;
    }

    private final int EMPTY_COLOR = 0;

    private IntegerPair getLengthPair(Board board, IntegerPair last, IntegerPair direction, int playerColor,
                                      boolean[][] visited)
    {

        IntegerPair position = new IntegerPair(last.getFirst() + direction.getFirst(),
                                               last.getSecond() + direction.getSecond());

        int playerLength = getLength(board, position, direction, playerColor, visited, 0);

        int possibleLength = getLength(board, position, direction, EMPTY_COLOR, visited, playerLength);

        return new IntegerPair(playerLength, possibleLength);
    }

    private int getStrength(Board board, IntegerPair last, int color, boolean[][] visited, int[][] directions)
    {

        int strength = 0;

        for (int i = 0; i < 4; ++i)
        {
            IntegerPair direction = new IntegerPair(directions[0][i], directions[1][i]);
            IntegerPair a = getLengthPair(board, last, direction, color, visited);

            direction = new IntegerPair(-directions[0][i], -directions[1][i]);
            IntegerPair b = getLengthPair(board, last, direction, color, visited);

            if (a.getSecond() + b.getSecond() >= 3)
            {
                if (a.getFirst() + b.getFirst() >= 3) {
                    System.out.println(board);
                }
                strength += color * xInRowCoefficients[a.getFirst() + b.getFirst()];
            }
        }
        return strength;
    }

    private int connectedStrength(Board board)
    {
        int total = 0;
        int[][] s = {{1, 1, 0, -1}, {0, 1, 1, 1}};
        boolean[][] visited = new boolean[board.getRows()][board.getColumns()];
        for (int row = 0; row < board.getRows(); ++row)
        {
            for (int col = 0; col < board.getColumns(); ++col)
            {
                if (visited[row][col])
                {
                    continue;
                }
                int color = board.getBoardState()[row][col];
                if (color != EMPTY_COLOR)
                {
                    total += getStrength(board, new IntegerPair(row, col), color, visited, s);
                }
                visited[row][col] = true;
            }
        }
        return total;
    }


}
