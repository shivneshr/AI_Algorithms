package com.company;

import javafx.util.Pair;

import java.io.*;
import java.util.*;


public class Main {

    //Common global variables
    public static int[][] matrix;
    public static int maxRow=-1,maxCol=-1,maxScore=-1,n;
    public static Queue<Pair<Integer,Integer>> queue=new LinkedList<>();

    //File variables and reading
    public static String Filename="input";
    public static void readMatrixFile() {
        // The name of the file to open.
        String fileName = Filename;
        String line = null;

        try {

            File fileobj=new File("./"+fileName);
            FileReader fileReader = new FileReader(fileobj);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int row=0;

            n=Integer.parseInt(bufferedReader.readLine());
            matrix=new int[n][n];
            while((line = bufferedReader.readLine()) != null) {

                String[] matrixLine=line.split("");

                for(int col=0;col<matrixLine.length;col++)
                {
                    if(matrixLine[col].equals("*"))
                        matrix[row][col]=-1;
                    else
                        matrix[row][col]=Integer.parseInt(matrixLine[col]);
                }
                row++;
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }

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
        return (row*10)+col;
    }

    public static void hashLocation(int row,int col, Map<Integer,Integer> visitedHash){

        visitedHash.put(hashFunc(row,col),1);
    }

    public static void addIslandCount(int row,int col,int count,Map<Integer,Integer> countHash){

        countHash.put(hashFunc(row,col),count);
    }

    public static void hashChainedCells(int key,int row, int col,Map<Integer,List<Pair<Integer,Integer>>> chainedCells) {
        if(!chainedCells.containsKey(key)){
            chainedCells.put(key,new ArrayList<Pair<Integer, Integer>>());
        }

        if(row!=-1 && col!=-1)
        {
            chainedCells.get(key).add(new Pair<>(row,col));
        }
    }

    public static void removeIsland(int[][] matrix, int key,Map<Integer,List<Pair<Integer,Integer>>> chainedCells,boolean print){

        List<Pair<Integer,Integer>> islandPositions=chainedCells.get(key);

        Map<Integer,Integer> maxrow=new HashMap<>();

        for(Pair<Integer,Integer> pair: islandPositions){

            matrix[pair.getKey()][pair.getValue()]=-1;

            if(!maxrow.containsKey(pair.getValue())){
                maxrow.put(pair.getValue(),pair.getKey());
            }
            else
            {
                if(maxrow.get(pair.getValue())<pair.getKey())
                    maxrow.put(pair.getValue(),pair.getKey());
            }
        }

        for(int x:maxrow.keySet())
        {
            int row=maxrow.get(x);
            List<Integer> validNumber=new ArrayList<>();
            while(row>=0)
            {
                if(matrix[row][x]!=-1){
                    validNumber.add(matrix[row][x]);
                }
                row--;
            }
            row=maxrow.get(x);
            int ctr=0;

            while(row>=0){
                if(validNumber.size()>ctr)
                {
                    matrix[row][x]=validNumber.get(ctr);
                    ctr++;
                }
                else
                {
                    matrix[row][x]=-1;
                }
                row--;
            }
        }

        if(print==true) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matrix[i][j] == -1)
                        System.out.print("* ");
                    else
                        System.out.print(matrix[i][j] + " ");
                }
                System.out.println();
            }

            System.out.println();
        }
    }

    public static int findIslands(int[][] matrix,int val,Map<Integer,Integer> visitedHash,Map<Integer,List<Pair<Integer,Integer>>> chainedCells){

        // Count of number of elements in the island
        int count=0;
        Pair<Integer,Integer> maincell=queue.peek();
        int hashvalue=hashFunc(maincell.getKey(),maincell.getValue());

        // Using BFS to find the set of islands for a particular value
        while(queue.size()!=0){

            Pair<Integer,Integer> cell=queue.peek();
            int row=cell.getKey();
            int col=cell.getValue();
            hashChainedCells(hashvalue,row,col,chainedCells);
            queue.remove();

            count++;

            // Checking Bottom
            if(row+1<n && matrix[row+1][col]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row+1,col)))
                {
                    hashLocation(row+1,col,visitedHash);
                    queue.add(new Pair<>(row+1,col));
                }

            }

            // Checking Right
            if(col+1<n && matrix[row][col+1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col+1)))
                {
                    hashLocation(row,col+1,visitedHash);
                    queue.add(new Pair<>(row,col+1));
                }
            }

            // Checking Left
            if(col-1>=0 && matrix[row][col-1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col-1)))
                {
                    hashLocation(row,col-1,visitedHash);
                    queue.add(new Pair<>(row,col-1));
                }

            }

            // Checking Up
            if(row-1>=0 && matrix[row-1][col]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row-1,col)))
                {
                    hashLocation(row-1,col,visitedHash);
                    queue.add(new Pair<>(row-1,col));
                }

            }
        }

        return count;
    }

    public static void findPartitions(int[][] matrix,Map<Integer,List<Pair<Integer,Integer>>> chainedCells,Map<Integer,Integer> countHash){

        Map<Integer,Integer> visitedHash=new HashMap<>();
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++)
            {
                if(!visitedHash.containsKey(hashFunc(i,j)) && matrix[i][j]!=-1)
                {
                    queue.clear();
                    queue.add(new Pair<>(i,j));
                    hashLocation(i,j,visitedHash);
                    int count=findIslands(matrix,matrix[i][j],visitedHash,chainedCells);
                    addIslandCount(i,j,count,countHash);
                }
            }
        }

    }


    public static int minmax(int[][] matrix,int depth,boolean isMax,int cutOffHeight,int alpha,int beta)
    {

        Map<Integer,Integer> countHash=new HashMap<>();
        Map<Integer,List<Pair<Integer,Integer>>> chainedCells=new HashMap<>();
        findPartitions(matrix,chainedCells, countHash);

        int row,col;

        if(isMax)
        {
            //System.out.println("MAX");
            int temp = 0;

            for (int key : chainedCells.keySet()) {
                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key, chainedCells,false);

                if(depth+1!=cutOffHeight)
                {
                    int val=minmax(newmatrix,depth + 1, false, cutOffHeight,alpha,beta);
                    temp = Math.max(temp,val+countHash.get(key));
                    alpha = Math.max(alpha, temp);
                    if (beta <= alpha)
                        break;
                    //System.out.println(key/10+" "+key%10+" "+temp);



                    if(maxRow!=-1)
                    {
                        if(maxScore<temp)
                        {
                            maxRow=key / 10;
                            maxCol=key % 10;
                            maxScore=temp;
                            System.out.println("MAX "+maxRow+" "+maxCol+" "+maxScore+" "+depth);
                            removeIsland(deepCopyIntMatrix(matrix),key,chainedCells,true);
                        }
                    }
                    else {
                        if(maxScore<temp) {
                            maxRow = key / 10;
                            maxCol = key % 10;
                            maxScore = temp;
                            System.out.println("MAX "+maxRow+" "+maxCol+" "+maxScore+" "+depth);
                            removeIsland(deepCopyIntMatrix(matrix), key, chainedCells, true);
                        }
                    }

                }
                else{
                    temp=Math.max(countHash.get(key),temp);
                }
            }
            return temp;
        }
        else
        {
            //System.out.println("MIN");
            int temp = Integer.MAX_VALUE;

            for (int key : chainedCells.keySet()) {
                int[][] newmatrix = deepCopyIntMatrix(matrix);
                removeIsland(newmatrix, key, chainedCells,false);

                if(depth+1<cutOffHeight) {
                    int val=minmax( newmatrix,depth+1, true, cutOffHeight,alpha,beta);
                    temp = Math.min(temp,val);
                    beta = Math.min(beta, temp);

                    if (beta <= alpha)
                        break;
                    //System.out.println(key/10+" "+key%10+" "+temp);
                }else {
                    temp=0;
                }
            }
            if(temp==Integer.MAX_VALUE)
                temp=0;
            return temp;
        }
    }

    public static void main(String[] args) {

        readMatrixFile();


        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++)
            {
                System.out.print(matrix[i][j]+" ");
            }
            System.out.println();
        }
        System.out.println();


        minmax(matrix,0,true,10,Integer.MIN_VALUE,Integer.MAX_VALUE);

        System.out.println("Max Row: "+maxRow+" MaxCol: "+maxCol+" Max Score: "+maxScore);
    }
}
