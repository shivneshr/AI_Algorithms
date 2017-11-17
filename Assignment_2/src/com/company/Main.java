package com.company;

import java.io.*;
import java.util.*;


class Position<S, T> {
    public final S row;
    public final T col;

    public Position(S x, T y) {
        this.row = x;
        this.col = y;
    }
}

public class Main {

    //Common global variables
    public static int[][] matrix;

    // Calculate and storing the best score, row, col values
    public static int[][] bestState=null;
    public static Double bestScore=-Double.MAX_VALUE,timeLeft;
    public static int movescore;
    public static int maxRow=-1,maxCol=-1,n,noOfFruits;
    public static Queue<Position<Integer,Integer>> queue=new LinkedList<>();


    //File variables and reading
    public static String inputFilename="input.txt";

    // Variable metrics
    public static int call=0,prune=0;

    public static void readMatrixFile(String FileName) {

        try {

            // Creating the file reader object
            File fileobj=new File("./"+FileName);
            FileReader fileReader = new FileReader(fileobj);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // Getting the size of the board
            n=Integer.parseInt(bufferedReader.readLine());

            //Getting the number of fruits
            noOfFruits=Integer.parseInt(bufferedReader.readLine());

            //Getting the time allocated
            timeLeft = Double.parseDouble(bufferedReader.readLine());

            // Declaring the matrix with the size
            matrix=new int[n][n];

            // Reading the matrix
            for(int row=0;row<n;row++) {

                String line=bufferedReader.readLine();
                String[] matrixLine=line.split("");

                for(int col=0;col<n;col++)
                {
                    if(matrixLine[col].equals("*"))
                        matrix[row][col]=-1;
                    else
                        matrix[row][col]=Integer.parseInt(matrixLine[col]);
                }
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + FileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + FileName + "'");
        }

    }

    public static void writeMatrixFile(int[][] matrix,String location) {

        try {
            PrintWriter bw = new PrintWriter("output.txt","UTF-8");

            bw.write(location);
            bw.println();

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matrix[i][j] == -1)
                        bw.print("*");
                    else
                        bw.print(matrix[i][j]);
                }
                bw.println();
            }
            bw.close();

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static int[][] deepCopyIntMatrix(int[][] input) {

        /*
        * This function is used in order to pass the copy of matrix during DFS operation of MINMAX algorithm
        */

        if (input == null)
            return null;

        int[][] result = new int[input.length][];
        for (int r = 0; r < input.length; r++) {
            result[r] = input[r].clone();
        }
        return result;
    }

    public static int hashFunc(int row,int col){
        /*
        * Hashing the entries in the 2-D matrix
        */
        return (row*n)+col;
    }

    public static void hashLocation(int row,int col, Map<Integer,Integer> visitedHash){
        /*
        * Hash helper code for find_islands code
        */
        visitedHash.put(hashFunc(row,col),1);
    }

    public static void addIslandCount(int row,int col,int count,Map<Integer,Integer> countHash){

        countHash.put(hashFunc(row,col),count);
    }

    public static void hashChainedCells(int key,int row, int col,Map<Integer,List<Position<Integer,Integer>>> chainedCells) {

        /*
        * Optimization technique to improve the access to the locations grouped based on Islands in a matrix
        * */

        if(!chainedCells.containsKey(key)){
            chainedCells.put(key,new LinkedList<Position<Integer,Integer>>());
        }

        if(row!=-1 && col!=-1)
        {
            chainedCells.get(key).add(new Position<Integer, Integer>(row,col));
        }
    }

    public static void removeIsland(int[][] matrix, int key,List<Position<Integer,Integer>> islandPositions){

        /*
        *  One of the most crucial steps to this program
        *  This function identifies all the locations part of the given IsLand using ChainedCell
        *  Removes the locations from the board
        *  Applies the gravity over the modified board and replaces the empty locations with '-1' => '*'
        * */

        Map<Integer,Integer> maxrow=new HashMap<>();

        for(Position<Integer,Integer> pair: islandPositions){

            matrix[pair.row][pair.col]=-1;

            if(!maxrow.containsKey(pair.col)){
                maxrow.put(pair.col,pair.row);
            }
            else
            {
                if(maxrow.get(pair.col)<pair.row)
                    maxrow.put(pair.col,pair.row);
            }
        }

        for(int x:maxrow.keySet())
        {
            int row=maxrow.get(x);
            int inPlacePtr=row;

            while(row>=0)
            {
                if(matrix[row][x]!=-1){
                    matrix[inPlacePtr][x]=matrix[row][x];
                    inPlacePtr--;
                }
                row--;
            }
            while(inPlacePtr>=0)
            {
                matrix[inPlacePtr][x]=-1;
                inPlacePtr--;
            }
        }
    }


    public static int findIslands(int[][] matrix,int val,Map<Integer,List<Position<Integer,Integer>>> chainedCells){

        /*
        * Run after every new board is generated to find the number of Islands available in it
        * The size and location of the IsLand is crucial in improving the MIN-MAX (game playing) algorithm
        * */


        // Count of number of elements in the island
        int count=0;
        Position<Integer,Integer> maincell=queue.peek();
        int hashvalue=hashFunc(maincell.row,maincell.col);

        matrix[maincell.row][maincell.col]=-1;

        // Using BFS to find the set of islands for a particular value
        while(queue.size()!=0){

            Position<Integer,Integer> cell=queue.peek();
            int row=cell.row;
            int col=cell.col;

            hashChainedCells(hashvalue,row,col,chainedCells);
            queue.remove();

            count++;

            // Checking Bottom
            if(row+1<n && matrix[row+1][col]==val)
            {
                if(matrix[row+1][col]!=-1)
                {
                    //hashLocation(row+1,col,visitedHash);
                    matrix[row+1][col]=-1;
                    queue.add(new Position<Integer,Integer>(row+1,col));
                }
            }

            // Checking Right
            if(col+1<n && matrix[row][col+1]==val)
            {
                if(matrix[row][col+1]!=-1)
                {
                    //hashLocation(row,col+1,visitedHash);
                    matrix[row][col+1]=-1;
                    queue.add(new Position<Integer,Integer>(row,col+1));
                }
            }

            // Checking Left
            if(col-1>=0 && matrix[row][col-1]==val)
            {
                if(matrix[row][col-1]!=-1)
                {
                    //hashLocation(row,col-1,visitedHash);
                    matrix[row][col-1]=-1;
                    queue.add(new Position<Integer,Integer>(row,col-1));
                }
            }

            // Checking Up
            if(row-1>=0 && matrix[row-1][col]==val)
            {
                if(matrix[row-1][col]!=-1)
                {
                    //hashLocation(row-1,col,visitedHash);
                    matrix[row-1][col]=-1;
                    queue.add(new Position<Integer,Integer>(row-1,col));
                }
            }
        }
        return count;
    }

    public static void findPartitions(int[][] matrix,Map<Integer,List<Position<Integer,Integer>>> chainedCells,Map<Integer,Integer> countHash){

        // This is the coordinating function for the findIslands proc

        Map<Integer,Integer> visitedHash=new HashMap<>();
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++)
            {
                if(matrix[i][j]!=-1)
                {
                    queue.clear();
                    queue.add(new Position<Integer,Integer>(i,j));
                    hashLocation(i,j,visitedHash);
                    int count=findIslands(matrix,matrix[i][j],chainedCells);
                    addIslandCount(i,j,count,countHash);
                }
            }
        }

    }

    private static List<Map.Entry<Integer, Integer>> sortHashMap(Map<Integer, Integer> unsortMap, final boolean sortOrder) {

        List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,Map.Entry<Integer, Integer> o2)
            {
                if (sortOrder)
                {
                    return o2.getValue().compareTo(o1.getValue());
                }
                else
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
            }
        });

        return list;
    }

    // This is only used when depth==1
    public static double findMaxMap(Map<Integer,Integer> countHash,Map<Integer,List<Position<Integer,Integer>>> chainedCells,int[][] newMatrix) {
        /*
        * When the depth is equal to one we are just having the maximizing step in the Min-Max algorithm
        * We just have to find the Island with the maximum step and remove it from the 2D - Matrix
        * */

        int maxkey=0,value=0;

        for(Map.Entry<Integer,Integer> entry: countHash.entrySet())
        {
            if(value<entry.getValue())
            {
                value=entry.getValue();
                maxkey=entry.getKey();
            }
        }

        removeIsland(newMatrix,maxkey,chainedCells.get(maxkey));
        bestState=newMatrix;
        maxRow=maxkey/n;
        maxCol=maxkey%n;
        movescore=countHash.get(maxkey);
        return value;
    }

    public static double minmax(int[][] matrix,int depth,boolean isMax,int cutOffHeight,Double alpha,Double beta,Double score) {
        /*
        *  This the main MIN MAX function which performs recursion
        *  We can control the depth of the function based on whether we are trying to maximize our score or minimizing the opponents score
        * */

        Map<Integer,Integer> countHash=new HashMap<>();
        Map<Integer,List<Position<Integer,Integer>>> chainedCells=new HashMap<>();
        findPartitions(deepCopyIntMatrix(matrix),chainedCells, countHash);

        List<Map.Entry<Integer,Integer>> sortedMap;

        // When we are going only one level in the board
        if(depth==cutOffHeight && cutOffHeight==1)
        {
            int[][] newmatrix=deepCopyIntMatrix(matrix);
            if(isMax)
                return Math.pow(findMaxMap(countHash,chainedCells,newmatrix),2);
            else
                return -1*Math.pow(findMaxMap(countHash,chainedCells,newmatrix),2);
        }

        // Exit condition when the boards is empty
        if(chainedCells.size()==0)
            return score;

        if(isMax) {

            // Maximizing Agent

            sortedMap = sortHashMap(countHash,isMax);

            Double v = -Double.MAX_VALUE;

            for (Map.Entry<Integer,Integer> key : sortedMap) {

                if(depth==cutOffHeight)
                {
                    double maxvalue=Collections.max(countHash.values());
                    return score+Math.pow(maxvalue,2);
                }

                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key.getKey(), chainedCells.get(key.getKey()));

                // Receiving the Minimized score from the MIN agent to be Maximized
                v = Math.max(v, minmax(newmatrix, depth + 1, false, cutOffHeight, alpha, beta, score + Math.pow(countHash.get(key.getKey()), 2)));


                if (bestScore < v && depth==1) {
                    bestScore = v;
                    maxRow=key.getKey()/n;
                    maxCol=key.getKey()%n;
                    bestState=deepCopyIntMatrix(newmatrix);
                    movescore=countHash.get(key.getKey());
                }

                // Pruning step
                alpha = Math.max(alpha, v);
                if (beta <= alpha) {
                    prune++;
                    break;
                }
            }

            return v;
        }
        else
        {
            // Minimizing Agent

            sortedMap = sortHashMap(countHash,isMax);
            Double v=Double.MAX_VALUE;

            for (Map.Entry<Integer,Integer> key : sortedMap) {

                if(depth==cutOffHeight)
                {
                    double maxvalue=Collections.max(countHash.values());
                    return score-Math.pow(maxvalue,2);
                }

                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key.getKey(), chainedCells.get(key.getKey()));

                // Receiving MAX score value which needs to be minimized
                v=Math.min(v,minmax( newmatrix,depth+1, true, cutOffHeight,alpha,beta,score - Math.pow(countHash.get(key.getKey()),2)));

                // Update beta with minimum score possible by MIN agents move
                beta = Math.min(beta, v);

                // Pruning step
                if (beta <= alpha) {
                    prune++;
                    break;
                }
            }

            return v;

        }

    }

    public static int calculateCutOffHeight(){
        /*
        *  This is the function which decides what depth the Min-Max agent should be run for every board
        *  Total time for each player is only 5 mins
        *  Hence running Min-Max for depth more than 6 would lead to timeout and disqualification
        *  The function decided this depth based on
        *       Time into the game
        *       Time left for the player
        *       No. of Islands left out
        *       Total size of the board
        *  This is totally configurable based on the situation
        *
        *  Currently it has been modified to simulate a bell curve for the moves which seemed to be the most
        *  effective with another agent
        *
        * */
        Map<Integer,Integer> countHash=new HashMap<>();
        Map<Integer,List<Position<Integer,Integer>>> chainedCells=new HashMap<>();
        findPartitions(deepCopyIntMatrix(matrix),chainedCells, countHash);

        int noOfIslands=countHash.size();
        int cutOffHeight=3;

        // Bell curve
        if(noOfIslands>(2*n))
            cutOffHeight=3;
        else if(noOfIslands<(2*n) && noOfIslands>(n/2) && timeLeft>50)
            cutOffHeight=5;
        else
            cutOffHeight=3;

        // Max depth safety
        if(noOfIslands>150)
            cutOffHeight=2;
        else if(noOfIslands<50 && noOfIslands>20) {
            if(cutOffHeight>5)
                cutOffHeight = 5;
        }
        else if(noOfIslands<=20 && noOfIslands>10)
            cutOffHeight=7;
        else if(noOfIslands<=10 && noOfIslands>5)
            cutOffHeight=9;
        else if(noOfIslands<=5)
            cutOffHeight=1;

        if(timeLeft<=50 && timeLeft>20)
            cutOffHeight=3;
        if(timeLeft<=20 && timeLeft>10)
            cutOffHeight=2;
        if(timeLeft<=10)
            cutOffHeight=1;

        return cutOffHeight;
    }

    public static void main(String[] args) {

        // Read the matrix input from file
        // They will also be providing the time left
        readMatrixFile(inputFilename);

        // Calculate the cut-off height for the Min-Max
        int cutOffHeight=calculateCutOffHeight();
        minmax(matrix,1,true,cutOffHeight,-Double.MAX_VALUE,Double.MAX_VALUE,0.0);

        // This is just the representation to point out a location in the matrix where the move was made
        // Column - represented using Alphabets [A-Z]
        // Rows - numbers
        char columnAlphabet=(char)(65+maxCol);
        String location=columnAlphabet+""+(maxRow+1);

        // Write the matrix after the move to the file
        writeMatrixFile(bestState,location);
    }
}
