#include <iostream>
#include <utility>
#include <cmath>
#include <vector>
#include <stack>
#include <map>

using namespace std;

map<int, pair<int,int> > objectLocation;
map<int, pair<int,int> > queenPosition;
map<int, map<int, pair<int,int> > > rowranges;
map<int,map<int,pair<int,int> > > queenDiag1;
map<int,map<int,pair<int,int> > > queenDiag2;
map<int,map<int,pair<int,int> > > obstacleDiag1;
map<int,map<int, pair<int,int> > > obstacleDiag2;
map<int,vector<int> > obstaclej;
map<int,vector<int> > obstacles;
map<int,vector<int> > queenColumn;
map<int,vector<int> > queenRow;


int hashm(int x,int y,int n)
{
    return x + y * n;
}

void printMatrix(int n)
{
    for(int row=0;row<n;row++) {
        for (int col = 0; col < n; col++) {
            if (queenPosition.find(hashm(row,col,n))!=queenPosition.end())
                cout<<1<<" ";
            else if(objectLocation.find(hashm(row,col,n))!=objectLocation.end())
                cout<<2<<" ";
            else
                cout<<0<<" ";
        }
        cout<<endl;
    }
};

void print(map<int, pair<int,int> > x)
{
    std::map<int,pair<int,int> > ::iterator it;

    for(it=x.begin();it!=x.end();it++)
    {
        cout<<it->first<<" "<<it->second.first<<" "<<it->second.second<<endl;
    }

}

void print(map<int,vector<int> > x)
{
    std::map<int,vector<int> > ::iterator it;

    for(it=x.begin();it!=x.end();it++)
    {
        cout<<it->first<<"    ";
        for(int j=0;j<it->second.size();j++)
            cout<<it->second[j]<<" ";

        cout<<endl;
    }

    cout<<endl;

}

void print(map<int, map<int, pair<int,int> > > x)
{
    std::map<int, map<int, pair<int,int> > > ::iterator it;
    std::map<int, pair<int,int> > ::iterator it1;

    for(it=x.begin();it!=x.end();it++)
    {
        for(it1=it->second.begin();it1!=it->second.end();it1++)
        {
            cout<<"map1 "<<it->first<<"   "<<"map2 "<<it1->first<<"  "<<it1->second.first<<" "<<it1->second.second;
        }

        cout<<endl;
    }

}

bool calculateConflict(int row, int col, int n){

    // Checking if there is conflict in j location
    //print(queenColumn);
    if(queenColumn.find(col)!=queenColumn.end() && queenColumn[col].size()!=0)
    {
        int max=0,maxq;
        if(obstaclej.find(col)!=obstaclej.end() && obstaclej[col].size()!=0)
        {
            vector<int> ob=obstaclej[col];
            for(int i=0;i<n;i++)
            {
                if(ob[i]<row && ob[i]>max)
                    max=ob[i];
            }
            maxq=*max_element(queenColumn[col].begin(),queenColumn[col].end());
            if(row>max && max>maxq)
            {
            }
            else
                return true;
        }
        else
            return true;
    }



    if(queenDiag1.find(row-col)!=queenDiag1.end() && queenDiag1[row-col].size()!=0)
    {
        if(obstacleDiag1.find(row-col)!=obstacleDiag1.end() && obstacleDiag1[row-col].size()!=0)
        {
            std::map<int, pair<int,int> > ::iterator it1;
            int dis=100000000;
            int flag=1;

            for(it1=queenDiag1[row-col].begin();it1!=queenDiag1[row-col].end();it1++)
            {
                if(abs(row - it1->second.first) == abs(col - it1->second.second))
                {
                    int tmp=abs(row - it1->second.first);
                    if(tmp<dis)
                        dis=tmp;
                }
            }

            for(it1=obstacleDiag1[row-col].begin();it1!=obstacleDiag1[row-col].end();it1++)
            {
                if(abs(row - it1->second.first) == abs(col - it1->second.second))
                {
                    int tmp=abs(row - it1->second.first);
                    if(tmp<dis && col>it1->second.second)
                    {
                        flag=1;
                        break;
                    }
                    else
                        flag=0;
                }
            }
            if(flag==0)
                return true;
        }
        else
            return true;

    }

    if(queenDiag2.find(row+col)!=queenDiag2.end() && queenDiag2[row+col].size()!=0)
    {

        if(obstacleDiag2.find(row-col)!=obstacleDiag2.end() && obstacleDiag2[row-col].size()!=0)
        {
            std::map<int, pair<int,int> > ::iterator it1;
            int dis=100000000;
            int flag=1;

            for(it1=queenDiag2[row-col].begin();it1!=queenDiag2[row-col].end();it1++)
            {
                if(abs(row - it1->second.first) == abs(col - it1->second.second))
                {
                    int tmp=abs(row - it1->second.first);
                    if(tmp<dis)
                        dis=tmp;
                }
            }

            for(it1=obstacleDiag2[row-col].begin();it1!=obstacleDiag2[row-col].end();it1++)
            {
                if(abs(row - it1->second.first) == abs(col - it1->second.second))
                {
                    int tmp=abs(row - it1->second.first);
                    if(tmp<dis && col>it1->second.second)
                    {
                        flag=1;
                        break;
                    }
                    else
                        flag=0;
                }
            }
            if(flag==0)
                return true;
        }
        else
            return true;
    }

    return false;

}

void addQueenHash(int row,int col,int n){

    queenPosition[hashm(row,col,n)]=make_pair(row,col);

    queenDiag2[row+col][hashm(row,col,n)]=make_pair(row,col);
    queenDiag1[row-col][hashm(row,col,n)]=make_pair(row,col);
    queenColumn[col].push_back(row);
    queenRow[row].push_back(col);

}

void removeQueenHash(int row,int col, int n){

    queenPosition.erase(hashm(row,col,n));
    queenDiag1[row-col].erase(hashm(row,col,n));
    queenDiag2[row+col].erase(hashm(row,col,n));
    queenColumn[col].pop_back();
    queenRow[row].pop_back();

}


bool MatrixDFS(int n, int sal, map<int, map<int, pair<int,int> > > ranges){

    //print(ranges);
    std::map<int, map<int, pair<int,int> > > ::iterator it;
    std::map<int, pair<int,int> > ::iterator itErase;
    std::map<int, pair<int,int> > ::iterator it1;
    map<int, map<int, pair<int,int> > > removeList;
    map<int, map<int, pair<int,int> > > tmpranges;
    tmpranges.insert(ranges.begin(),ranges.end());
    if(sal>0)
    {
        for(it=ranges.begin();it!=ranges.end();it++)
        {
            it1=it->second.begin();
            while(it1!=it->second.end())
            {
                for (int l = it1->second.first; l < it1->second.second; l++) {
                    if (calculateConflict(it->first, l, n) == false) {
                        addQueenHash(it->first, l, n);
                        //pair<int, int> tmp = it1->second;
                        tmpranges[it->first].erase(hashm(it1->second.first,it1->second.second,n));
                        //it->second.erase(it1);

                        //printMatrix(n);

                        if (MatrixDFS(n, sal - 1, tmpranges) == false) {
                            removeQueenHash(it->first, l, n);
                            //it->second[hashm(it->first, l, n)] = tmp;
                            //printMatrix(n);
                        } else
                            return true;
                    }
                }
                removeList[it->first][hashm(it1->second.first, it1->second.second, n)] = make_pair(it1->second.first,it1->second.second);
                //print(tmpranges);
                tmpranges[it->first].erase(hashm(it1->second.first, it1->second.second, n));
                //cout<<it1->first<<endl;
                ++it1;
                //print(tmpranges);
            }
        }

        for(it=removeList.begin();it!=removeList.end();it++)
        {
            for(it1=it->second.begin();it1!=it->second.end();it1++)
            {
                ranges[it->first][it1->first]=it1->second;
            }
        }
        return false;
    }else
    {
        return true;
    }
}

int main() {
    int n,sal,tree,row,col;

    cin>>n>>sal>>tree;

    while( tree-- >0 ) {

        cin>>row>>col;

        objectLocation[hashm(row,col,n)] = make_pair(row,col);

        if(obstacleDiag1.find(row+col)==obstacleDiag1.end())
        {
            obstacleDiag1[row+col][hashm(row,col,n)]=make_pair(row,col);
        }

        if(obstacleDiag2.find(row+col)==obstacleDiag2.end())
        {
            obstacleDiag2[row-col][hashm(row,col,n)]=make_pair(row,col);
        }

        if(obstaclej.find(col)==obstaclej.end())
        {
            obstaclej[col].push_back(row);
        }

        obstacles[row].push_back(col);
    }


    std::map<int,vector<int> > ::iterator it;
	
    for(it=obstacles.begin();it!=obstacles.end();it++)
    {
        if(it->second[0]!=0)
        {
            it->second.insert(it->second.begin(),0);
        }
        if(it->second.back()!=n)
        {
            it->second.push_back(n);
        }

    }

    for(int index=0;index<n;index++)
    {
        if(obstacles.find(index)==obstacles.end())
        {
            rowranges[index][hashm(0,n,n)]=make_pair(0,n);
        }
        else
        {
            vector<int> tmp=obstacles[index];

            for(int k=0;k<tmp.size()-1;k++)
            {
                if(tmp[k]+1!=tmp[k+1])
                {
                    rowranges[index][hashm(tmp[k]+1,tmp[k+1],n)]=make_pair(tmp[k]+1,tmp[k+1]);
                }
            }
        }
    }


    MatrixDFS(n, sal,rowranges);
    printMatrix(n);
//cout<<"Here objectLocation"<<endl;
//
    //print(objectLocation);
//
    //cout<<"Here queenPosition"<<endl;
//
    //print(queenPosition);
//
    //cout<<"Here obstacleDiag1"<<endl;
//
    //print(obstacleDiag1);
//
    //cout<<"Here obstacleDiag2"<<endl;
//
    //print(obstacleDiag2);
//
    //cout<<"Here obstaclej"<<endl;
//
    //print(obstaclej);
//
    //cout<<"Here obstacles"<<endl;
//
    //print(obstacles);
//
    //cout<<"Here RowRanges"<<endl;
//
    //print(rowranges);

    return 0;
}
