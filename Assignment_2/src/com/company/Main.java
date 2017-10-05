package com.company;

import javafx.util.Pair;

import java.io.*;
import java.util.*;


public class Main {

    public static int[][] matrix=new int[][]{
            {3,1,0,2,3,2,2,3,1,0},
            {0,1,2,1,2,3,2,0,1,3},
            {3,0,2,1,1,1,1,1,1,3},
            {0,2,2,1,0,3,1,1,3,2},
            {0,2,3,0,0,1,1,0,1,2},
            {0,3,2,3,3,2,1,0,1,0},
            {2,0,0,3,0,2,2,0,1,2},
            {2,2,0,2,2,0,0,0,2,1},
            {0,1,3,0,0,0,0,0,2,0},
            {2,2,0,0,0,2,2,2,3,1}
    };

    public static Queue<Pair<Integer,Integer>> queue=new LinkedList<>();


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

    public static void readFile()
    {
        // The name of the file to open.
        String fileName = "input";

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.

            File fileobj=new File("./"+fileName);
            FileReader fileReader =
                    new FileReader(fileobj);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {

            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }

    }

    public static void removeIsland(int[][] matrix, int key,Map<Integer,List<Pair<Integer,Integer>>> chainedCells){

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

        for(int i=0;i<10;i++)
        {
            for(int j=0;j<10;j++)
            {
                if(matrix[i][j]==-1)
                    System.out.print("* ");
                else
                    System.out.print(matrix[i][j]+" ");
            }
            System.out.println();
        }
    }

    public static int findIslands(int val, int n, Map<Integer,Integer> visitedHash,Map<Integer,List<Pair<Integer,Integer>>> chainedCells){

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

            // Checking bottom
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
            if(col-1>0 && matrix[row][col-1]==val)
            {
                if(!visitedHash.containsKey(hashFunc(row,col-1)))
                {
                    hashLocation(row,col-1,visitedHash);
                    queue.add(new Pair<>(row,col-1));
                }

            }

            // Checking Up
            if(row-1>0 && matrix[row-1][col]==val)
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

    public static void findPartitions(int n,Map<Integer,List<Pair<Integer,Integer>>> chainedCells,Map<Integer,Integer> countHash){

        Map<Integer,Integer> visitedHash=new HashMap<>();
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++)
            {
                if(!visitedHash.containsKey(hashFunc(i,j)))
                {
                    queue.clear();
                    queue.add(new Pair<>(i,j));
                    hashLocation(i,j,visitedHash);
                    int count=findIslands(matrix[i][j],n,visitedHash,chainedCells);
                    addIslandCount(i,j,count,countHash);
                }
            }
        }

    }

    public static int minmax(int n,int depth,int[][] matrix,boolean isMax,int h, int score)
    {
        if(depth==h)
            return score;


        Map<Integer,Integer> countHash=new HashMap<>();
        Map<Integer,List<Pair<Integer,Integer>>> chainedCells=new HashMap<>();
        int max=0;

        if(isMax)
        {
            findPartitions(n,chainedCells,countHash);

        }
        else
        {

        }
        return 0;
    }

    public static void main(String[] args) {

        int n=10;

        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++)
            {
                System.out.print(matrix[i][j]+" ");
            }
            System.out.println();
        }

        findPartitions(n);
        for(Map.Entry<Integer,Integer> entry: countHash.entrySet()){

            System.out.println(entry.getKey()/10+" "+entry.getKey()%10+" "+entry.getValue());
            removeIsland(deepCopyIntMatrix(matrix),entry.getKey());
        }
    }
}
