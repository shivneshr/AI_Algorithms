package com.company;



import org.omg.CORBA.INTERNAL;

import java.io.*;
import java.util.*;


// Sort the dictionary based on the Island size
//class ValueComparator implements Comparator<Integer> {
//    HashMap<Integer,List<Position<Integer,Integer>>> map = new HashMap<>();
//
//    public ValueComparator(Map<Integer,List<Position<Integer,Integer>>> map) {
//        this.map.putAll(map);
//    }
//
//    public int compare(Integer keyA, Integer keyB) {
//        if(map.get(keyA).size() >= map.get(keyB).size()){
//            return -1;
//        }else{
//            return 1;
//        }
//    }
//}
//
//class ValueComparatorASC implements Comparator<Integer> {
//    HashMap<Integer,List<Position<Integer,Integer>>> map = new HashMap<>();
//
//    public ValueComparatorASC(Map<Integer,List<Position<Integer,Integer>>> map) {
//        this.map.putAll(map);
//    }
//
//    public int compare(Integer keyA, Integer keyB) {
//        if(map.get(keyA).size() <= map.get(keyB).size()){
//            return -1;
//        }else{
//            return 1;
//        }
//    }
//}


class ValueComparator implements Comparator<Integer> {
    HashMap<Integer,Integer> map = new HashMap<>();

    public ValueComparator(Map<Integer,Integer> map) {
        this.map.putAll(map);
    }

    public int compare(Integer keyA, Integer keyB) {

        return map.get(keyA).compareTo(map.get(keyB));
    }
}

class ValueComparatorASC implements Comparator<Integer> {
    HashMap<Integer,Integer> map = new HashMap<>();

    public ValueComparatorASC(Map<Integer,Integer> map) {
        this.map.putAll(map);
    }

    public int compare(Integer keyA, Integer keyB) {
        return map.get(keyB).compareTo(map.get(keyA));
    }
}

class Position<S, T> {
    public final S row;
    public final T col;

    public Position(S x, T y) {
        this.row = x;
        this.col = y;
    }
}
/*class Position{
    public int row;
    public int col;

    Position(int x,int y){
        row=x;col=y;
    }
}*/
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
    public static String inputFilename="input";

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

                for(int col=0;col<matrixLine.length;col++)
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
            PrintWriter bw = new PrintWriter("output","UTF-8");

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

    public static void writeToScore(int score){

         try {
             PrintWriter bw = new PrintWriter("score","UTF-8");

             bw.write(""+score+"");
             bw.println();
             bw.close();

         }catch (Exception ex)
         {
             ex.printStackTrace();
         }

    }

    public static void printMatrix(int[][] matrix){

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == -1)
                    System.out.print("*");
                else if(matrix[i][j] == -2)
                    System.out.print("#");
                else if(matrix[i][j] == -3)
                    System.out.print("&");
                else
                    System.out.print(matrix[i][j]);
            }
            System.out.println();
        }

        System.out.println();
    }


    public static int[][] deepCopyIntMatrix(int[][] input) {
        if (input == null)
            return null;
        int[][] result = new int[input.length][];
        for (int r = 0; r < input.length; r++) {
            result[r] = input[r].clone();
        }
        return result;
    }

    public static int hashFunc(int row,int col)
    {
        return (row*n)+col;
    }

    public static void hashLocation(int row,int col, Map<Integer,Integer> visitedHash){

        visitedHash.put(hashFunc(row,col),1);
    }

    public static void addIslandCount(int row,int col,int count,Map<Integer,Integer> countHash){

        countHash.put(hashFunc(row,col),count);
    }

    public static void hashChainedCells(int key,int row, int col,Map<Integer,List<Position<Integer,Integer>>> chainedCells) {
        if(!chainedCells.containsKey(key)){
            chainedCells.put(key,new ArrayList<Position<Integer,Integer>>());
        }

        if(row!=-1 && col!=-1)
        {
            chainedCells.get(key).add(new Position<Integer, Integer>(row,col));
        }
    }

    public static void removeIsland(int[][] matrix, int key,List<Position<Integer,Integer>> islandPositions){

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

    public static int findIslands(int[][] matrix,int val,Map<Integer,Integer> visitedHash,Map<Integer,List<Position<Integer,Integer>>> chainedCells){

        // Count of number of elements in the island
        int count=0;
        Position<Integer,Integer> maincell=queue.peek();
        int hashvalue=hashFunc(maincell.row,maincell.col);


        int[][] tempMat=deepCopyIntMatrix(matrix);

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
                if(!visitedHash.containsKey(hashFunc(row+1,col)))
                {
                    hashLocation(row+1,col,visitedHash);
                    queue.add(new Position<Integer,Integer>(row+1,col));
                }
            }

            // Checking Right
            if(col+1<n && matrix[row][col+1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col+1)))
                {
                    hashLocation(row,col+1,visitedHash);
                    queue.add(new Position<Integer,Integer>(row,col+1));
                }
            }

            // Checking Left
            if(col-1>=0 && matrix[row][col-1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col-1)))
                {
                    hashLocation(row,col-1,visitedHash);
                    queue.add(new Position<Integer,Integer>(row,col-1));
                }
            }

            // Checking Up
            if(row-1>=0 && matrix[row-1][col]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row-1,col)))
                {
                    hashLocation(row-1,col,visitedHash);
                    queue.add(new Position<Integer,Integer>(row-1,col));
                }
            }
        }
        return count;
    }

    public static void findPartitions(int[][] matrix,Map<Integer,List<Position<Integer,Integer>>> chainedCells,Map<Integer,Integer> countHash){

        Map<Integer,Integer> visitedHash=new HashMap<>();
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++)
            {
                if(!visitedHash.containsKey(hashFunc(i,j)) && matrix[i][j]!=-1)
                {
                    queue.clear();
                    queue.add(new Position<Integer,Integer>(i,j));
                    hashLocation(i,j,visitedHash);
                    int count=findIslands(matrix,matrix[i][j],visitedHash,chainedCells);
                    addIslandCount(i,j,count,countHash);
                }
            }
        }

    }

    // Sorting maps by value in descending value
    //public static TreeMap<Integer,List<Position<Integer,Integer>>> sortMapByValue(Map<Integer,List<Position<Integer,Integer>>> map){
    //    Comparator<Integer> comparator = new ValueComparator(map);
    //    //TreeMap is a map sorted by its values
    //    TreeMap<Integer,List<Position<Integer,Integer>>> result = new TreeMap<>(comparator);
    //    result.putAll(map);
    //    return result;
    //}
    //
    //// Sorting maps by value in descending value
    //public static TreeMap<Integer,List<Position<Integer,Integer>>> sortMapByValueASC(Map<Integer,List<Position<Integer,Integer>>> map){
    //    Comparator<Integer> comparator = new ValueComparatorASC(map);
    //    //TreeMap is a map sorted by its values
    //    TreeMap<Integer,List<Position<Integer,Integer>>> result = new TreeMap<>(comparator);
    //    result.putAll(map);
    //    return result;
    //}

    private static Map<Integer, Integer> sortByComparator(Map<Integer, Integer> unsortMap, final boolean order)
        {
            List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>()
            {
                public int compare(Map.Entry<Integer, Integer> o1,
                                   Map.Entry<Integer, Integer> o2)
                {
                    if (order)
                    {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                    else
                    {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                }
            });
            Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
            for (Map.Entry<Integer, Integer> entry : list)
            {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            return sortedMap;
        }



   public static Map<Integer,Integer> sortMapByValue(Map<Integer,Integer> map){
       Comparator<Integer> comparator = new ValueComparator(map);
       //TreeMap is a map sorted by its values
       Map<Integer,Integer> result = new TreeMap<>(comparator);
       result.putAll(map);
       return result;
   }

   // Sorting maps by value in descending value
   public static Map<Integer,Integer> sortMapByValueASC(Map<Integer,Integer> map){
       Comparator<Integer> comparator = new ValueComparatorASC(map);
       //TreeMap is a map sorted by its values
       Map<Integer,Integer> result = new TreeMap<>(comparator);
       result.putAll(map);
       return result;
   }


    public static double findMaxMap(Map<Integer,Integer> countHash,Map<Integer,List<Position<Integer,Integer>>> chainedCells,int[][] newMatrix)
    {
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

    public static double minmax(int[][] matrix,int depth,boolean isMax,int cutOffHeight,Double alpha,Double beta,Double score)
    {
        call++;
        Map<Integer,Integer> countHash=new HashMap<>();
        Map<Integer,List<Position<Integer,Integer>>> chainedCells=new HashMap<>();
        findPartitions(matrix,chainedCells, countHash);
        if(depth==1)
            System.out.println(countHash.size());
        Map<Integer,Integer> sortedMap;
        //System.out.println("Depth "+depth +" Score: "+ score);
        //printMatrix(matrix);

        if(depth==cutOffHeight && cutOffHeight==1)
        {
            int[][] newmatrix=deepCopyIntMatrix(matrix);
            if(isMax)
                return Math.pow(findMaxMap(countHash,chainedCells,newmatrix),2);
            else
                return -1*Math.pow(findMaxMap(countHash,chainedCells,newmatrix),2);
        }


        if(chainedCells.size()==0)
            return score;

        if(isMax) {

            sortedMap = sortByComparator(countHash,isMax);

             //for(Map.Entry<Integer,Integer> entry: sortedMap.entrySet())
             //{
             //    System.out.println(entry.getKey()/n+" "+entry.getKey()%n+" "+entry.getValue());
             //}

            //System.out.println("MAX");
            Double v = -Double.MAX_VALUE;

            for (int key : sortedMap.keySet()) {

                if(depth==cutOffHeight)
                {
                    double maxvalue=Collections.max(countHash.values());
                    //call+=countHash.size();
                    return score+Math.pow(maxvalue,2);
                }

                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key, chainedCells.get(key));

                // Receiving the Minimized score from the MIN agent
                v = Math.max(v, minmax(newmatrix, depth + 1, false, cutOffHeight, alpha, beta, score + Math.pow(countHash.get(key), 2)));


                if (bestScore < v && depth==1) {
                    bestScore = v;
                    maxRow=key/n;
                    maxCol=key%n;
                    System.out.println("Best Score "+bestScore);
                    bestState=deepCopyIntMatrix(newmatrix);
                    movescore=countHash.get(key);
                    System.out.println("Score: "+Math.pow(countHash.get(key),2));
                    //printMatrix(newmatrix);
                }



                //System.out.println("MAX Depth= "+depth+" Score= "+val);
                //printMatrix(newmatrix);

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
            sortedMap = sortByComparator(countHash,isMax);
            Double v=Double.MAX_VALUE;
            //System.out.println("MIN");
            
            for (int key : sortedMap.keySet()) {

                if(depth==cutOffHeight)                                 
                {
                    double maxvalue=Collections.max(countHash.values());
                    //call+=countHash.size();
                    return score-Math.pow(maxvalue,2);
                }

                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key, chainedCells.get(key));

                // Receiving MAX score value which needs to be minimized
                v=Math.min(v,minmax( newmatrix,depth+1, true, cutOffHeight,alpha,beta,score - Math.pow(countHash.get(key),2)));

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

    public static void main(String[] args) {

        readMatrixFile(inputFilename);

        //printMatrix(matrix);
        long startTime = System.currentTimeMillis();
        minmax(matrix,1,true,3,-Double.MAX_VALUE,Double.MAX_VALUE,0.0);

        char columnAlphabet=(char)(65+maxCol);

        System.out.println("Row "+maxRow+" Col "+maxCol);

        String location=columnAlphabet+""+(maxRow+1);
        writeMatrixFile(bestState,location);
        writeToScore(movescore);
        System.out.println(location);
        printMatrix(bestState);
        System.out.println(call+" "+prune);
        long endTime = System.currentTimeMillis();
        double time=endTime-startTime;
        System.out.println("That took " + time/1000 + " seconds");
    }
}
