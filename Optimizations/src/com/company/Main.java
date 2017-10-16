package com.company;



import java.io.*;
import java.util.*;


// Sort the dictionary based on the Island size
class ValueComparator implements Comparator<Integer> {
    HashMap<Integer,List<Position<Integer,Integer>>> map = new HashMap<>();

    public ValueComparator(Map<Integer,List<Position<Integer,Integer>>> map) {
        this.map.putAll(map);
    }

    public int compare(Integer keyA, Integer keyB) {
        if(map.get(keyA).size() >= map.get(keyB).size()){
            return -1;
        }else{
            return 1;
        }
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
    public static int maxRow=-1,maxCol=-1,n,noOfFruits;
    public static Queue<Position> queue=new LinkedList<>();


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

    public static void printMatrix(int[][] matrix){

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == -1)
                    System.out.print("* ");
                else if(matrix[i][j] == -2)
                    System.out.print("# ");
                else if(matrix[i][j] == -3)
                    System.out.print("& ");
                else
                    System.out.print(matrix[i][j] + " ");
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
                    queue.add(new Position(row+1,col));
                }
            }

            // Checking Right
            if(col+1<n && matrix[row][col+1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col+1)))
                {
                    hashLocation(row,col+1,visitedHash);
                    queue.add(new Position(row,col+1));
                }
            }

            // Checking Left
            if(col-1>=0 && matrix[row][col-1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col-1)))
                {
                    hashLocation(row,col-1,visitedHash);
                    queue.add(new Position(row,col-1));
                }
            }

            // Checking Up
            if(row-1>=0 && matrix[row-1][col]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row-1,col)))
                {
                    hashLocation(row-1,col,visitedHash);
                    queue.add(new Position(row-1,col));
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
    public static TreeMap<Integer,List<Position<Integer,Integer>>> sortMapByValue(Map<Integer,List<Position<Integer,Integer>>> map){
        Comparator<Integer> comparator = new ValueComparator(map);
        //TreeMap is a map sorted by its values
        TreeMap<Integer,List<Position<Integer,Integer>>> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }

    public static Double minmax(int[][] matrix,int depth,boolean isMax,int cutOffHeight,Double alpha,Double beta,Double score)
    {
        call++;
        Map<Integer,Integer> countHash=new HashMap<>();
        Map<Integer,List<Position<Integer,Integer>>> chainedCells=new HashMap<>();
        findPartitions(matrix,chainedCells, countHash);

        Map<Integer,List<Position<Integer,Integer>>> sortedMap = sortMapByValue(chainedCells);



        //System.out.println("Depth "+depth +" Score: "+ score);
        //printMatrix(matrix);

        if(depth>=cutOffHeight)
            return score;

        if(chainedCells.size()==0)
            return score;

        if(isMax) {

            //System.out.println("MAX");
            Double v = -Double.MAX_VALUE;

            for (int key : sortedMap.keySet()) {

                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key, chainedCells.get(key));

                // Receiving the Minimized score from the MIN agent
                v = Math.max(v, minmax(newmatrix, depth + 1, false, cutOffHeight, alpha, beta, score + Math.pow(countHash.get(key), 2)));


                if (bestScore < v && depth==1) {
                    bestScore = v;
                    maxRow=key/n;
                    maxCol=key%n;
                    bestState=deepCopyIntMatrix(newmatrix);
                    //System.out.println("Score: "+Math.pow(countHash.get(key),2));
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
            Double v=Double.MAX_VALUE;
            //System.out.println("MIN");
            for (int key : chainedCells.keySet()) {

                    int[][] newmatrix = deepCopyIntMatrix(matrix);
                    removeIsland(newmatrix, key, chainedCells.get(key));

                    // Receiving MAX score value which needs to be minimized
                    v=Math.min(v,minmax( newmatrix,depth+1, true, cutOffHeight,alpha,beta,score - Math.pow(countHash.get(key),2)));


                    //if(bestScore<val)
                        //bestScore=val;
                    //System.out.println("MIN Depth= "+depth+" Score= "+score);
                    //printMatrix(newmatrix);

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

        minmax(matrix,1,true,4,-Double.MAX_VALUE,Double.MAX_VALUE,0.0);

        System.out.println("Row "+maxRow+" Col "+maxCol);
        char columnAlphabet=(char)(65+maxCol);
        String location=columnAlphabet+""+(maxRow+1);
        writeMatrixFile(bestState,location);
        System.out.println(location);
        printMatrix(bestState);
        System.out.println(call+" "+prune);
    }
}


//int[][] temp=new int[][]{{1,2,3},{1,2,3},{1,2,3}};
//int[][] temp1=new int[][]{{2,1,3},{1,2,3},{1,2,3}};
//int[] i={1,2,3};
//int[] i1={1,2,3};

//System.out.println(Arrays.equals(i,i1));
//System.out.println(Arrays.hashCode(i));
//System.out.println(Arrays.hashCode(i1));

//System.out.println(Arrays.equals(temp,temp1));
//System.out.println(Arrays.deepHashCode(temp));
//System.out.println(Arrays.deepHashCode(temp1));
