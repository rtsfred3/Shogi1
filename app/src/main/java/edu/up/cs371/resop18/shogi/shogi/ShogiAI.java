package edu.up.cs371.resop18.shogi.shogi;

import java.util.Random;

public class ShogiAI {
    private ShogiPiece[][] bestChild = new ShogiPiece[11][9];

    private int numMoves = 50;
    private int count = 0;
    private int depth = 0;

    /**
     * ShogiAI Constructor which takes in the Game State and Max Depth desired in the minimax evaluation
     *
     * @param gState the current Game State
     * @param MAX_DEPTH max depth of recursion
     */
    public ShogiAI(ShogiGameState gState, int MAX_DEPTH){
        double start = (double)System.currentTimeMillis();
        eval(gState.getCurrentBoard(), -1000.0, 1000.0, true, depth, MAX_DEPTH);
        double end = (double)System.currentTimeMillis();
        timeMinutes(start, end, MAX_DEPTH);
    }

    /**
     * Calculates and displays how long an action took.
     *
     * @param start
     * @param end
     * @param depth
     */
    private void timeMinutes(double start, double end, int depth){
        double seconds = Math.floor(100.0*(end-start)/1000)/100.0;
        int minutes = 0;
        while(seconds >= 60){
            seconds -= 60;
            minutes++;
        }
        if((end-start)/1000 < 60){
            System.out.println("It took "+seconds+" seconds for depth "+depth+".");
        }else {
            System.out.println("It took "+minutes+" minutes "+seconds+" seconds for depth "+depth+".");
        }
    }

    /**
     * Calculates and displays how long an action took.
     *
     * @param start
     * @param end
     */
    public void printTime(double start, double end){ System.out.println("It took " + (end-start)/1000 + " seconds."); }

    public void printTime(String task, double start, double end){
        System.out.println(task+" took "+(end-start)/1000+" seconds.");
    }

    /**
     * Takes a parameter of a board and prints out the board
     *
     * @param board
     */
    public void printBoard(ShogiPiece[][] board){
        count += 1;
        for(int i = 1; i < board.length-1; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j] == null){
                    System.out.print("null ");
                }else{
                    System.out.print(board[i][j].getPiece()+" ");
                }
            }
            System.out.println();
        }
        System.out.println("Count = " + count);
        System.out.println();
    }

    /**
     * Returns best child board
     *
     * @return returns the best child
     */
    public ShogiPiece[][] getBestChild(){ return bestChild; }

    /**
     * Gives the max of two values
     *
     * @param a
     * @param b
     * @return returns the max value between a and b
     */
    private double max(double a, double b){ return (a > b) ? a : b; }

    /**
     * Give the min of two values
     *
     * @param a
     * @param b
     * @return returns the min value between a and b
     */
    private double min(double a, double b){ return (a < b) ? a : b; }

    /**
     * Takes in a state of the board and a move.
     * Then, it will return a new state of the board based on the move.
     *
     * @param board
     * @param move
     * @return returns new board based on move
     */
    public ShogiPiece[][] newGameState(ShogiPiece[][] board, int[] move){
        ShogiPiece[][] newBoard = new ShogiPiece[11][9];

        //Deep copy the board into newBoard
        for(int i = 0; i < newBoard.length; i++){
            for(int j = 0; j < newBoard[i].length; j++){
                if(board[i][j] != null){
                    newBoard[i][j] = new ShogiPiece(board[i][j].getRow(), board[i][j].getCol(), board[i][j].getPiece());
                    newBoard[i][j].setPlayer(board[i][j].getPlayer());
                    newBoard[i][j].promotePiece(board[i][j].getPromoted());
                    newBoard[i][j].setInCheck(board[i][j].getInCheck());
                    newBoard[i][j].setSelected(board[i][j].getSelected());
                }
            }
        }

        //Setting the new row and col
        int row = move[0];
        int col = move[1];

        //Setting the old row and col
        int oldRow = move[2];
        int oldCol = move[3];

        //Performing the move action
        if(board[oldRow][oldCol] != null){
            if(board[row][col] == null || board[row][col].getPlayer() != board[oldRow][oldCol].getPlayer()){
                newBoard[row][col] = new ShogiPiece(row, col, board[oldRow][oldCol].getPiece());
                newBoard[row][col].setPlayer(board[oldRow][oldCol].getPlayer());
                newBoard[row][col].promotePiece(board[oldRow][oldCol].getPromoted());
                newBoard[row][col].setInCheck(board[oldRow][oldCol].getInCheck());
                newBoard[row][col].setSelected(board[oldRow][oldCol].getSelected());
                newBoard[oldRow][oldCol] = null;
            }
        }

        return newBoard;
    }

    /**
     * Uses the board to create all possible legal moves for the current state
     *
     * @param board
     * @return returns all the legal moves for the current board
     */
    public int[][][] actList(ShogiPiece[][] board, boolean max){
        int maxPlayer = max ? 1 : 0;
        LegalMoves m = new LegalMoves(maxPlayer); //Sets the LegalMoves to look for the AI's moves

        ShogiPiece piece;

        int[][][] list = new int[numMoves][20][4]; //Create array for moves
        int a = 0;
        for(int row = 1; row < 10; row++){
            if(a == numMoves){ break; }
            for(int col = 0; col < board[row].length; col++){
                if(a == numMoves){ break; }
                if(board[row][col] != null){
                    piece = board[row][col];
                    if(max == !piece.getPlayer()){
                        //Gets all moves for current piece
                        int[][] possibleMoves = m.moves(board, piece.getPiece(), piece.getRow(), piece.getCol());

                        //Adds all moves for piece to list of legal moves
                        for (int i = 0; i < 20; i++) {
                            if (possibleMoves[i] == null){ continue; }
                            list[a][i][0] = possibleMoves[i][0]; //Add new row to move
                            list[a][i][1] = possibleMoves[i][1]; //Add new col to move

                            list[a][i][2] = piece.getRow(); //Add current row to move
                            list[a][i][3] = piece.getCol(); //Add current col to move
                        }
                        a++;
                    }
                }
            }
        }
        return list;
    }

    /**
     * Using the board and actList, childList will create all possible next states of the board
     * and return them as a 3D array
     *
     * @param board
     * @param actList
     * @return returns all next possible states of the board based on actList
     */
    private ShogiPiece[][][] childList(ShogiPiece[][] board, int[][][] actList){
        ShogiPiece[][][] list = new ShogiPiece[actList.length][11][9];
        int listLocation = 0;

        //This section is what takes the longest
        double start = (double)System.currentTimeMillis();

        //Goes through the length of actList to get moves
        for(int a = 0; a < actList.length; a++){
            if(listLocation == actList.length){ break; }
            for(int i = 0; i < actList[a].length; i++) {
                if(actList[a][i] == null){ break; } //If there are no moves it will stop stop
                if(listLocation == actList.length){ break; }
                for(int j = 0; j < actList[a][i].length; j++){
                    if(listLocation == actList.length){ break; }
                    if(actList[a][i][0] < 1 || actList[a][i][2] < 1){ break; } //If there are no moves it will stop

                    //Creates to state of the board based on the current board and the move at actList[i][j]
                    ShogiPiece[][] localBoard = newGameState(board, actList[a][i]);

                    //Deep copies the localBoard into 'list', which is a list of all next states of the board
                    if(listLocation < actList.length){
                        for(int k = 0; k < localBoard.length; k++){
                            for(int l = 0; l < localBoard[k].length; l++){
                                if(localBoard[k][l] != null){
                                    list[listLocation][k][l] = new ShogiPiece(localBoard[k][l].getRow(), localBoard[k][l].getCol(), localBoard[k][l].getPiece());
                                    list[listLocation][k][l].setPlayer(localBoard[k][l].getPlayer());
                                    list[listLocation][k][l].promotePiece(localBoard[k][l].getPromoted());
                                    list[listLocation][k][l].setSelected(localBoard[k][l].getSelected());
                                    list[listLocation][k][l].setInCheck(localBoard[k][l].getInCheck());
                                }else{
                                    list[listLocation][k][l] = null;
                                }
                            }
                        }

                        /*System.out.println("\nReference Board");
                        printBoard(localBoard);
                        System.out.println("Board from List");
                        printBoard(list[listLocation]);*/
                    }

                    listLocation++;

                    localBoard = new ShogiPiece[11][9];
                }
            }
        }
        double end = (double)System.currentTimeMillis();
        //printTime("Child List", start, end);
        //System.out.println("Length of childList: " + list.length);

        return list;
    }

    /**
     * This evaluated the current state of the board and branches out using recursion
     * to determine which moves is the best to take
     *
     * @param board is the current board
     * @param alpha mini for pruning
     * @param beta max for pruning
     * @param MAX AI or Player
     * @param depth always going to be zero
     * @param MAX_DEPTH depth you want
     * @return
     */
    public double eval(ShogiPiece[][] board, double alpha, double beta, boolean MAX, int depth, int MAX_DEPTH){
        double val; //Temporary holder for evaluation of child states
        int[][][] actList = actList(board, MAX); //Creates actList
        shuffleArray(actList);
        ShogiPiece[][][] childList = childList(board, actList); //Creates childList

        if(depth > MAX_DEPTH){
            return 0.5 + Math.random()*Math.random(); //Temporary heuristics until AI works
        }

        double bestVal = MAX ? -100 : +100; //temp max and min value for bestVal
        bestChild = null;

        //Iterates through child next possible states of the board for best child
        for (ShogiPiece[][] aChildList : childList) {
            //Recursion call for next board states
            val = eval(aChildList, alpha, beta, !MAX, depth + 1, MAX_DEPTH);

            /*
             * Checks who's move it is and compares the evaluation of the child to the current bestVal
             * which ever child state is the best becomes assigned to bestChild
             */
            if(MAX && val > bestVal){
                bestVal = max(val, bestVal);
                alpha = max(alpha, bestVal);
                bestChild = aChildList;
                if(beta <= alpha){ break; } //Alpha-beta pruning
            }else if(!MAX && -val < bestVal){
                bestVal = min(-val, bestVal);
                beta = min(beta, bestVal);
                bestChild = aChildList;
                if(beta <= alpha){ break; } //Alpha-beta pruning
            }

            //System.out.println("Best Val: "+bestVal);
        }

        return bestVal; //returns bestVal
    }

    private static void shuffleArray(int[][][] array){
        int index;
        int[][] temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--){
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

}