import sys
import random
import datetime
import time

class State:

	def __init__(self, cnt=None, n=None, sal=None,rowranges_t=None, queenPosition_t=None):

		if queenPosition_t is not None:
			self.queenPosition_t = {key: value[:] for key, value in queenPosition_t.items()}
		else:
			self.queenPosition_t=dict()

		if rowranges_t is not None:
			self.rowranges_t = {key: value[:] for key, value in rowranges_t.items()}
		else:
			self.rowranges_t=dict()

		self.n = n
		self.cover = set()
		self.cnt = cnt
		self.sal = sal

	def addQueenHash_t(self,i,j):

		self.queenPosition_t[hashFunction(i, j)] = [i, j]


# BFS Queue

BFSQueue = []

# All these are varying

queenPosition=dict()
queenRow=dict()
queenColumn=dict()
queenDiag1=dict()
queenDiag2=dict()

rowranges = dict()

# These are non-varying set initially

obstaclesLocationDict=dict()
obstacles=dict()
obstaclesj=dict()
obstaclesDiag1=dict()
obstaclesDiag2=dict()

# BFS Variables
attackConfig=dict()

# SA Variables

oldcount=0
newcount=0

queenConflict=[]
oldqueenconflicts=[]
newQueenPosition=[]
queenRange=dict()
oldqueenranges=dict()

temperature=100.00
start_time=datetime.datetime.now()
cuttofftime=280
starttime=time.time()
end_time=start_time+datetime.timedelta(minutes=4.8)

terminate=False


# Print BFS Object

def print_BFSMatrix(n,ob,file):

	for i in range(ob.n):
		for j in range(ob.n):
			if hashFunction(i,j) in ob.queenPosition_t:
				file.write("1")
			elif hashFunction(i,j) in obstaclesLocationDict:
				file.write("2")
			else:
				file.write("0")
		file.write("\n")
	file.close()

# Printing the matrix in the end

def print_matrix(n,file):

	for i in range(n):
		for j in range(n):
			if hashFunction(i,j) in queenPosition:
				file.write("1")
			elif hashFunction(i,j) in obstaclesLocationDict:
				file.write("2")
			else:
				file.write("0")
		file.write("\n")
	file.close()

def print_matrix_c(n):
	for i in range(n):
		for j in range(n):
			if hashFunction(i,j) in queenPosition:
				print(1," ",end="")
			elif hashFunction(i,j) in obstaclesLocationDict:
				print(2," ",end="")
			else:
				print(0," ",end="")
		print()
	print("",end="\n\n")

# returning a hash function

def hashFunction(x,y):
	return hash((x,y))

# adding appropriate hashes when queen is added placed
def addQueenHash(i,j):

	queenPosition[hashFunction(i, j)] = [i, j]

	if i+j not in queenDiag2:
		queenDiag2[i+j] = []
	queenDiag2[i+j].append([i, j])

	if i-j not in queenDiag1:
		queenDiag1[i-j] = []
	queenDiag1[i-j].append([i, j])

	if j not in queenColumn:
		queenColumn[j] = []
	queenColumn[j].append(i)

	if i not in queenRow:
		queenRow[i] = []
	queenRow[i].append(j)

# adding queenHash for SA
def addQueenHashSA(i,j,ranges):

	queenPosition[hashFunction(i, j)] = [i, j]
	queenRange[hashFunction(i,j)] = ranges
	rowranges[i].remove(ranges)

	if i+j not in queenDiag2:
		queenDiag2[i+j] = []
	queenDiag2[i+j].append([i, j])

	if i-j not in queenDiag1:
		queenDiag1[i-j] = []
	queenDiag1[i-j].append([i, j])

	if j not in queenColumn:
		queenColumn[j] = []
	queenColumn[j].append(i)

	if i not in queenRow:
		queenRow[i] = []
	queenRow[i].append(j)

# removes appropriate hashes when queen is added un-placed
def removeQueenHash(i, j):

	queenPosition.pop(hashFunction(i, j))
	del queenDiag1[i-j][-1]
	del queenDiag2[i+j][-1]
	del queenColumn[j][-1]
	del queenRow[i][-1]

# removing queenHash for SA
def removeQueenHashSA(i, j):

	queenPosition.pop(hashFunction(i, j))
	rowranges[i].append(queenRange[hashFunction(i,j)])
	queenRange.pop(hashFunction(i,j))
	queenDiag1[i-j].remove([i,j])
	queenDiag2[i+j].remove([i,j])
	queenColumn[j].remove(i)
	queenRow[i].remove(j)

# calculate conflict DFS

def calculateConflict(i1,j1,n):

	# if no obstacles are there - obstacle checks reduce

	if len(obstaclesLocationDict) == 0:
		if j1 in queenColumn and queenColumn[j1]:
			return True
		elif i1 - j1 in queenDiag1 and queenDiag1[i1 - j1]:
			return True
		elif i1 + j1 in queenDiag2 and queenDiag2[i1 + j1]:
			return True
		else:
			return False

	else:

		# check j row only in upper direction  - Column checks

		if j1 in queenColumn and queenColumn[j1]:

			if j1 in obstaclesj and obstaclesj[j1]:
				lsto = [x for x in obstaclesj[j1] if x < i1]
				lstq = queenColumn[j1]
				if lsto:
					# Should we put max of lsto here or just retrieve the latest j?
					if i1 >= lsto[-1] >= lstq[-1]:
						pass
					else:
						return True
				else:
					return True
			else:
				return True

		# check diagonal \ only in up direction

		if i1 - j1 in queenDiag1 and queenDiag1[i1 - j1]:

			if i1 - j1 in obstaclesDiag1:

				# filtering all the obstacles after the current position being checked
				obstacleDiag = [x for x in obstaclesDiag1[i1 - j1] if x[0] < i1]

				# If queen is present but no obstacle then Conflict else check is obstacle in in-between
				if obstacleDiag:

					# Find the location of the closest queen
					dis =0
					flag = 1

					queenDiagLst=queenDiag1[i1 - j1]

					if (abs(i1 - queenDiagLst[-1][0]) == abs(j1 - queenDiagLst[-1][1])):
						dis = abs(i1 - queenDiagLst[-1][0])

					if (abs(i1 - obstacleDiag[-1][0]) == abs(j1 - obstacleDiag[-1][1])):
						tmp = abs(i1 - obstacleDiag[-1][0])

						if (tmp < dis and (i1 > obstacleDiag[-1][0])):
							flag = 1
						else:
							flag = 0

					if (flag == 0):
						return True
				else:
					return True
			else:
				return True


		# check diagonal / only in up direction

		if i1 + j1 in queenDiag2 and queenDiag2[i1 + j1]:

			if i1 + j1 in obstaclesDiag2:

				obstacleDiag = [x for x in obstaclesDiag2[i1 + j1] if x[0] < i1]

				if obstacleDiag:

					# Find the location of the closest queen
					dis = 0
					flag = 1

					queenDiagLst = queenDiag2[i1 + j1]


					if (abs(i1 - queenDiagLst[-1][0]) == abs(j1 - queenDiagLst[-1][1])):
						dis = abs(i1 - queenDiagLst[-1][0])

					if (abs(i1 - obstacleDiag[-1][0]) == abs(j1 - obstacleDiag[-1][1])):
						tmp = abs(i1 - obstacleDiag[-1][0])

						if (tmp < dis and (i1 > obstacleDiag[-1][0])):
							flag = 1
						else:
							flag = 0

					if (flag == 0):
						return True
				else:
					return True
			else:
				return True

		return False

# calculate Conflict_BFS

def calculateConflict_BFS(i1,j1,n,ob):

	if(hashFunction_t(i1,j1,n) in ob.cover):
		return True
	else:
		return False

# calculate Conflict SA

def calculateConflictSA(i1,j1,n):
	# if no obstacles are there

	if len(obstaclesLocationDict) == 0:
		if j1 in queenColumn and len(queenColumn[j1]) > 0:
			return True
		elif i1 - j1 in queenDiag1 and len(queenDiag1[i1 - j1]) > 0:
			return True
		elif i1 + j1 in queenDiag2 and len(queenDiag2[i1 + j1]) > 0:
			return True
		else:
			return False

	else:
		# check j row only in upper direction

		if j1 in queenColumn and len(queenColumn[j1]) > 0:

			if j1 in obstaclesj and len(obstaclesj[j1]) > 0:

				queenBeforeLst=[x for x in queenColumn[j1] if x <i1]
				queenAfterLst=[x for x in queenColumn[j1] if x > i1]

				obstacleBeforeLst=[x for x in obstaclesj[j1] if x < i1]
				obstacleAfterLst=[x for x in obstaclesj[j1] if x > i1]


				queenBefore=-1; queenAfter=100000000; obstacleBefore=-1; obstacleAfter=100000000;

				if len(queenBeforeLst)!=0:
					queenBefore = max(queenBeforeLst)
				if len(queenAfterLst)!=0:
					queenAfter = min(queenAfterLst)

				if len(obstacleAfterLst)!=0:
					obstacleAfter = min(obstacleAfterLst)
				if len(obstacleBeforeLst)!=0:
					obstacleBefore= max(obstacleBeforeLst)

				if(queenBefore>obstacleBefore):
					return True
				elif(queenAfter<obstacleAfter):
					return True

			else:
				return True

		# check diagonal \ only in up direction

		if i1 - j1 in queenDiag1 and len(queenDiag1[i1 - j1]) > 0:

			if i1 - j1 in obstaclesDiag1 and len(obstaclesDiag1[i1 - j1]) > 0:

				queenBeforeLst = [x for x in queenDiag1[i1 - j1] if x[0] < i1]
				queenAfterLst = [x for x in queenDiag1[i1 - j1] if x[0] > i1]

				obstacleBeforeLst = [x for x in obstaclesDiag1[i1 - j1] if x[0] < i1]
				obstacleAfterLst = [x for x in obstaclesDiag1[i1 - j1] if x[0] > i1]

				queenBefore = -1;
				queenAfter = 100000000;
				obstacleBefore = -1;
				obstacleAfter = 100000000;

				if len(queenBeforeLst)!=0:
					queenBefore = max([sublist[0] for sublist in queenBeforeLst])
				if len(queenAfterLst)!=0:
					queenAfter = min([sublist[0] for sublist in queenAfterLst])

				if len(obstacleAfterLst)!=0:
					obstacleAfter = min([sublist[0] for sublist in obstacleAfterLst])
				if len(obstacleBeforeLst)!=0:
					obstacleBefore= max([sublist[0] for sublist in obstacleBeforeLst])

				# find the location of the closest queen



				if (queenBefore > obstacleBefore):
					return True
				elif (queenAfter < obstacleAfter):
					return True
			else:
				return True


		# check diagonal / only in up direction

		if i1 + j1 in queenDiag2 and len(queenDiag2[i1 + j1]) > 0:

			if i1 + j1 in obstaclesDiag2 and len(obstaclesDiag2[i1 + j1]) > 0:

				queenBeforeLst = [x for x in queenDiag2[i1 + j1] if x[0] < i1]
				queenAfterLst = [x for x in queenDiag2[i1 + j1] if x[0] > i1]

				obstacleBeforeLst = [x for x in obstaclesDiag2[i1 + j1] if x[0] < i1]
				obstacleAfterLst = [x for x in obstaclesDiag2[i1 + j1] if x[0] > i1]

				queenBefore = -1;
				queenAfter = 100000000;
				obstacleBefore = -1;
				obstacleAfter = 100000000;

				if len(queenBeforeLst) != 0:
					queenBefore = max([sublist[0] for sublist in queenBeforeLst])
				if len(queenAfterLst) != 0:
					queenAfter = min([sublist[0] for sublist in queenAfterLst])

				if len(obstacleAfterLst) != 0:
					obstacleAfter = min([sublist[0] for sublist in obstacleAfterLst])
				if len(obstacleBeforeLst) != 0:
					obstacleBefore = max([sublist[0] for sublist in obstacleBeforeLst])

				# find the location of the closest queen

				if (queenBefore > obstacleBefore):
					return True
				elif (queenAfter < obstacleAfter):
					return True
			else:
				return True

		return False

# Simulated Annealing

def addQueenHashTmp(i,j):

	if i+j not in queenDiag2:
		queenDiag2[i+j] = []
	queenDiag2[i+j].append([i, j])

	if i-j not in queenDiag1:
		queenDiag1[i-j] = []
	queenDiag1[i-j].append([i, j])

	if j not in queenColumn:
		queenColumn[j] = []
	queenColumn[j].append(i)

	if i not in queenRow:
		queenRow[i] = []
	queenRow[i].append(j)

def removeQueenHashTmp(i, j):

	queenDiag1[i-j].remove([i,j])
	queenDiag2[i+j].remove([i,j])
	queenColumn[j].remove(i)
	queenRow[i].remove(j)

# Counts the number of conflict in the current state

def countConflict(n):

	count=0
	queenConflict.clear()

	for key,values in queenPosition.items():
		row = values[0]
		col=values[1]
		removeQueenHashTmp(row,col)
		if(calculateConflictSA(row,col,n)):
			count+=1
			queenConflict.append([row,col])
		addQueenHashTmp(row, col)
	random.shuffle(queenConflict)
	return count


def calculateNeighbour():
	global oldqueenranges
	global oldqueenconflicts

	# backing up the information about existing state
	oldqueenranges = {key: value[:] for key, value in queenRange.items()}
	oldqueenconflicts = queenConflict[:]

	sal=0

	# removing the queens which are conflicting from the current state
	for queenPos in queenConflict:
		removeQueenHashSA(queenPos[0],queenPos[1])
		sal+=1

	placeQueenRandom(sal)


def revert():

	global queenConflict
	global oldqueenconflicts

	for value in newQueenPosition:
		removeQueenHashSA(value[0],value[1])

	queenConflict = oldqueenconflicts[:]
	queenRange={key: value[:] for key, value in oldqueenranges.items()}

	for value in queenConflict:
		addQueenHashSA(value[0],value[1],queenRange[hashFunction(value[0],value[1])])


def shouldProceed(sal,n):

	global newcount
	global oldcount
	global start_time
	global starttime
	global end_time
	global terminate
	global cuttofftime

	cur_time = datetime.datetime.now()
	nr = (end_time - cur_time).total_seconds() * 1000
	if time.time()-starttime > cuttofftime-20:
		#printOutput("SA", "FAIL", n)
		#print(nr)
		#print((cur_time-start_time).total_seconds())
		terminate=True

		return False

	if newcount<oldcount:
		return True
	else:
		dr = (end_time - start_time).total_seconds() * 1000
		val = ((nr / dr) * 100)/2
		if(random.choice(range(100))<val):
			return True
		else:
			return False

def anneal(n,sal):

	global oldcount
	global newcount
	global terminate

	while True:
		if oldcount>0:
			calculateNeighbour()
			newcount=countConflict(n)
			if(shouldProceed(sal,n)):

				oldcount=newcount
			else:
				if(terminate):
					return False
				revert()
		else:
			return True



# Place Queens randomly in the matrix
def placeQueenRandom(sal=0):

	newQueenPosition.clear()
	# Moving the queens to create a new state
	keylist={k:v for k, v in rowranges.items() if v !=[]}
	while sal>0:
		row=0
		while True:
			row=random.choice(list(keylist.keys()))
			if(len(rowranges[row])!=0):
				break
		ranges=random.choice(rowranges[row])
		col=random.choice(range(ranges[0],ranges[1]))
		addQueenHashSA(row,col,ranges)
		newQueenPosition.append([row,col])
		sal-=1
	return



# MatrixDFS

def MatrixDFS(n,sal,ranges,cnt):

	global starttime
	global cuttofftime

	removeList=dict()
	if time.time() - starttime < cuttofftime:
		if(sal>0):
			for row,values in ranges.items():
				if cnt>=sal:
					for col in values[:]:
						for x in range(col[0],col[-1]):
							#queenPosition[hashFunction(row, x)] = [row, x]
							#print_matrix(n)
							#queenPosition.pop(hashFunction(row, x))
							if(calculateConflict(row,x,n)==False):
								if(cnt>=sal):
									addQueenHash(row, x)
									#print_matrix(n)
									ranges[row].remove(col)
									cnt-=1
									if(MatrixDFS(n,sal-1,ranges,cnt)==False):
										removeQueenHash(row,x)
										ranges[row].insert(0,col)
										cnt+=1
										#print_matrix(n)
									else:
										return True
						if(row not in removeList):
							removeList[row]=[]
						removeList[row].append(col)
						ranges[row].remove(col)
						cnt-=1
				else:
					break;
			for key, vals in removeList.items():
				cnt+=len(vals)
				ranges[key]=vals+ranges[key]
			return False
		else:
			return True
	else:
		return False


# MatrixBFS

def prepareInitialState(cnt, n, sal):
	global BFSQueue

	ranges = {key: value[:] for key, value in rowranges.items()}

	for row, values in ranges.items():
		if cnt >= sal:
			for col in values[:]:
				for x in range(col[0], col[-1]):
					ranges[row].remove(col)
					ob = State(cnt - 1, n, sal - 1, ranges)
					ranges[row].insert(0, col)
					ob.addQueenHash_t(row, x)
					ob.cover = attackConfig[hashFunction_t(row, x, n)]
					BFSQueue.append(ob)
				ranges[row].remove(col)
				cnt -= 1
				if cnt < sal:
					break


def BFSSolve(cnt, n, sal):

	global BFSQueue
	global starttime
	global cuttofftime
	ctr=0

	prepareInitialState(cnt, n, sal)

	while BFSQueue:
		state = BFSQueue.pop(0)
		if time.time() - starttime < cuttofftime:
			if state.sal > 0:
				for row, values in state.rowranges_t.items():
					if state.cnt >= state.sal:
						for col in values[:]:
							for x in range(col[0], col[-1]):
								if (calculateConflict_BFS(row, x, n, state) == False):
									state.rowranges_t[row].remove(col)
									ob = State(state.cnt - 1, n, state.sal - 1, state.rowranges_t, state.queenPosition_t)
									state.rowranges_t[row].insert(0, col)
									ob.addQueenHash_t(row, x)
									ob.cover = state.cover.union(attackConfig[hashFunction_t(row, x, n)])
									if (state.sal - 1 == 0):
										return ob
									BFSQueue.append(ob)
							state.rowranges_t[row].remove(col)
							state.cnt -= 1
							if state.cnt < state.sal:
								break
					else:
						break
			else:
				return state
		else:
			return None

	return None


# creates the entire space into a rangelist

def createRangeList(n):

	rangecount=0

	for lst in obstacles:
		if obstacles[lst][0] != 0:
			obstacles[lst].insert(0, -1)
		if obstacles[lst][-1] != n:
			obstacles[lst].append(n)

	# Creating the ranges for it
	for lst in range(n):
		if (lst not in rowranges):
			rowranges[lst] = []
		if (lst in obstacles):
			for x in range(len(obstacles[lst]) - 1):
				if (obstacles[lst][x] + 1 != obstacles[lst][x + 1]):
					rowranges[lst].append([obstacles[lst][x] + 1, obstacles[lst][x + 1]])
					rangecount += 1
		else:
			rowranges[lst].append([0, n])
			rangecount += 1

	return rangecount

def hashFunction_t(x, y,n):

	return ((x*n)+y)

def hashObjects(row,col):

	# Location of obstacles hashed
	obstaclesLocationDict[hashFunction(row, col)] = [row, col]
	# hashed based on diagonals
	if (row + col not in obstaclesDiag2):
		obstaclesDiag2[row + col] = []
	obstaclesDiag2[row + col].append([row, col])
	if (row - col not in obstaclesDiag1):
		obstaclesDiag1[row - col] = []
	obstaclesDiag1[row - col].append([row, col])
	# hashed based on column
	if (col not in obstaclesj):
		obstaclesj[col] = []
	obstaclesj[col].append(row)
	# hashed based on row
	if (row not in obstacles):
		obstacles[row] = []
	obstacles[row].append(col)

def printOutput(type,message,n,ob=None):

	with open("output.txt","w") as file:
		file.write(message+"\n")

		if(message=="OK" and (type=="DFS" or type=="SA")):
			print_matrix(n,file)
		elif(message=="OK" and type=="BFS"):
			print_BFSMatrix(n,ob,file)

def calculateAttackConfig(n):

	for row in range(n):
		for col in range(n):
			hash=hashFunction_t(row,col,n)
			attackConfig[hash]=set()

			# for column

			for row_i in range(row+1,n):
				hash_i=hashFunction_t(row_i,col,n)
				if hashFunction(row_i,col) not in obstaclesLocationDict:
					attackConfig[hash].add(hash_i)
				else:
					break

			#print(attackConfig)

			# for \ diagonal
			row_i =row+1
			col_i =col+1
			while(row_i<n and col_i<n):
				hash_i=hashFunction_t(row_i,col_i,n)
				if hashFunction(row_i,col_i) not in obstaclesLocationDict:
					attackConfig[hash].add(hash_i)
				else:
					break
				row_i+=1
				col_i+=1

			#print(attackConfig)

			# for / diagonal
			row_i = row + 1
			col_i = col - 1

			while(row_i<n and col_i>=0):
				hash_i = hashFunction_t(row_i, col_i,n)
				if hashFunction(row_i,col_i) not in obstaclesLocationDict:
					attackConfig[hash].add(hash_i)
				else:
					break
				row_i += 1
				col_i -= 1

			#print(attackConfig)


def Main():

	sys.setrecursionlimit(10000)
	# Read from the file

	with open('input.txt',"r") as file:

		type=file.readline().rstrip('\n')
		n=int(file.readline())
		sal=int(file.readline())

		for x in range(n):
			inp=list(file.readline())
			for y in range(n):
				if int(inp[y])==2:
					# reading obstacles
					row = x
					col = y

					hashObjects(row, col)

		rangecount = createRangeList(n)


		if(rangecount<sal):
			printOutput(type,"FAIL", n)
		else:
			if type=="DFS":

				if(MatrixDFS(n, sal,rowranges,rangecount)==False):
					printOutput(type,"FAIL",n)
				else:
					printOutput(type,"OK",n)
			elif type=="BFS":
				if sal>0:
					calculateAttackConfig(n)
					resultObj = BFSSolve(rangecount, n, sal)

					if resultObj is not None:
						printOutput(type,"OK", n, resultObj)
					else:
						printOutput(type,"FAIL",n)
				else:
					printOutput("DFS", "OK", n)
			elif type=="SA":

				global oldcount
				placeQueenRandom(sal)
				oldcount = countConflict(n)
				if(anneal(n, sal)):
					printOutput(type, "OK", n)
				else:
					printOutput(type, "FAIL", n)

Main()