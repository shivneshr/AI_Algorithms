import re
import time
import sys


# Main KB to use to prove the statements
KB = []

# Set of queries to ASK
ASK = []

# Visited sentence to avoid infinite loop
visited = {}

# Stores the start tieme of the query
start=0

# Stores the universal number used for standardization
num=0


def readSentences():
	'''
	:return: read the given input.txt to store the Query and Sentences in KB
	'''

	global ASK
	global KB

	# File from which we are reading the KB
	file = open("input.txt", "r")

	sortKB=[]

	# No. of queries asked for the KB
	noOfQuery = int(file.readline())

	# Read the queries
	for i in range(noOfQuery):
		line = file.readline().replace('\n', '').replace(' ', '')
		predicates = line.split('|')
		ASK.append(predicates)

	# No. of sentences present in the KB
	noOfSentences = int(file.readline())

	# Read the sentences
	for i in range(noOfSentences):
		line = file.readline().replace('\n', '').replace(' ', '')
		predicates,cnt = standardize(line.split('|'))
		sortKB.append([cnt,predicates])

	sorted_KB = sorted(sortKB, key=lambda x: x[0])

	for pair in sorted_KB:
		KB.append(pair[1])


def standardize(predicates):
	'''
	:param sentence: KB sentence to be standardized
	:param num: literal used for standardizing
	:return: standardized sentence to be added to KB
	'''

	global num
	argDictionary = {}

	if len(predicates)==0:
		return [],0

	# Get the list of variables in the sentence
	for index, predicate in enumerate(predicates):
		arguments = predicate.split('(')[1].replace(')', '').split(',')
		for arg in arguments:
			if arg not in argDictionary and isVar(arg):
				argDictionary[arg] = arg[0] + str(num)
				num+=1

	sentence='|'.join(predicates)

	# Replace the new variable names in the sentences
	for key, value in argDictionary.items():
		sentence = sentence.replace('(' + key + ',', '(' + argDictionary[key] + ',') \
			.replace(',' + key + ')', ',' + argDictionary[key] + ')') \
			.replace('(' + key + ')', '(' + argDictionary[key] + ')') \
			.replace(',' + key + ',', ',' + argDictionary[key] + ',')

	return sentence.split('|'),len(argDictionary)


def hashStandard(predicates):

	if len(predicates)==0:
		return []

	argDictionary = {}
	cnt=0

	# Get the list of variables in the sentence
	for index, predicate in enumerate(predicates):
		arguments = predicate.split('(')[1].replace(')', '').split(',')
		for arg in arguments:
			if arg not in argDictionary and isVar(arg):
				argDictionary[arg] = 'v' + str(cnt)
				cnt+=1

	sentence = '|'.join(predicates)

	# Replace the new variable names in the sentences
	for key, value in argDictionary.items():
		sentence = sentence.replace('(' + key + ',', '(' + argDictionary[key] + ',') \
			.replace(',' + key + ')', ',' + argDictionary[key] + ')') \
			.replace('(' + key + ')', '(' + argDictionary[key] + ')') \
			.replace(',' + key + ',', ',' + argDictionary[key] + ',')

	return sentence.split('|')


# -------------------- Helper Methods -------------------------

def isVar(var):
	return var[0].islower()

def negatePredicate(predicate):
    if predicate[0] == '~':
		return predicate[1:]
	else:
		return '~' + predicate

def isLiteral(var):
	return not var[0].islower()

def isTautology(sentence):

	for predicate in sentence:
		if negatePredicate(predicate) in sentence:
			return True
	return False

def convertToTemplate(predicate):
    
	ls=predicate.split('(')
	name=ls[0]
	args=ls[1].replace(')','').split(',')
	construct=''
	for arg in args:
		if isVar(arg):
			construct+='v,'
		else:
			construct+=arg+','
	return name+'('+construct[:len(construct)-1]+')'

def removeduplicates(output):

	hashdup={}
	result=[]

	for predicate in output:

		temp = convertToTemplate(predicate)

		if temp not in hashdup:
			result.append(predicate)
			hashdup[temp]=1

	return result

# -------------------- Helper Methods End ----------------------


# -------------------- Resolution ------------------------------

def resolve(sentence1, sentence2, predicate):
	'''

	:param sentence1: sentence 1
	:param sentence2: sentence 2
	:return: resolved sentence
	'''

	sentence1.remove(predicate)
	sentence2.remove(negatePredicate(predicate))

	return sentence1 + sentence2

# -------------------- Resolution End ---------------------------


# -------------------- Unify Helper -----------------------------

def unifyHelper(map, sentences):
	'''
	:param map:         dictionary caontaining the mapping for variables and literals
	:param sentence:    sentence on which the unification needs to be performed
	:return:            returns a unified sentence
	'''
	for i, sentence in enumerate(sentences):
		for x, y in map.items():
			sentences[i] = sentences[i].replace('(' + x + ',', '(' + y + ',') \
				.replace(',' + x + ')', ',' + y + ')') \
				.replace('(' + x + ')', '(' + y + ')') \
				.replace(',' + x + ',', ',' + y + ',')

	return sentences

def getRegex(predicate):
	'''
	:param predicate: the predicate for which the regex expression needs to be generated
	:return: return regex of the negation of the predicate

    eg: input = ~Mother(x,y)
        output = Mother\(\s*\w*\s*,\s*\w*\s*\)

        The output is to be searched in the Rule
	'''

	tempstring = predicate.split('(')
	# getting the predicate name
	predicatename = tempstring[0]
	template = ''

	# Iterating over the varibales in the predicate
	for i in range(len(tempstring[1].split(',')) - 1):
		template += '(\s*\w*\s*)' + ','

	template += '(\s*\w*\s*)'

	# Negating the regex because we need to search for the negation
	if ('~' in predicatename):
		template = predicatename[1:] + '\(' + template
	else:
		template = '~' + predicatename + '\(' + template

	return template + '\)', tempstring[1].replace(')', '').split(',')

def unify_variable(var1,var2,theta):

	if var1 in theta:
		return unify_internal(theta[var1], var2, theta)
	elif var2 in theta:
		return unify_internal(var1, theta[var2], theta)
	else:
		theta[var1] = var2
	return theta

def unify_internal(x,y,theta):

	if theta is None:
		return None
	elif type(x) == str and type(y)==str and x == y:
		return theta
	elif type(x) == str and type(y) == str and isVar(x):
		return unify_variable(x,y,theta)
	elif type(x) == str and type(y) == str and isVar(y):
		return unify_variable(y,x,theta)
	elif type(x)==list and type(y)==list and len(x) > 0 and len(y) > 0:
		return unify_internal(x[1:],y[1:],unify_internal(x[0],y[0],theta))
	elif type(x) == list and type(y) == list and len(x) == 0 and len(y) == 0:
		return theta
	else:
		return None

def goDeep(key,theta):

	if key in theta:
		if isVar(theta[key]):
			return goDeep(theta[key],theta)
		else:
			return theta[key]
	else:
		return key

def generateNewDict(theta,modified):

	for key in theta:
		modified[key]=goDeep(key,theta)

	return modified

# -------------------- Unify Helper End -------------------------

# -------------------- Unification Logic ------------------------

def resolution(sentence1, sentence2):
	'''
	:param sentence1: List of predicates sentence 1
	:param sentence2: List of predicates in sentence 2
	:return: Calculate possible unifications and return set of resolved sentences
	'''

	resolvedSentences = []

	# str1 is the current sentence
	for predicate1 in sentence1:

		# matching the predicates
		regex, arguments1 = getRegex(predicate1)
		functionMatch = re.compile(regex)

		# have to improve this regex matching - use of hash maps
		# extracting the arguments of the sentence 1
		matchedPredicate = list(filter(functionMatch.match, sentence2))

		for predicate2 in matchedPredicate:

			unify={}
			# we have to store this variable separately for each sentence - hash map
			# Extracting the arguments of the sentence 2
			arguments2 = predicate2.split('(')[1].replace(')', '').split(',')

			unify_intermediate = unify_internal(arguments1, arguments2, {})

			if unify_intermediate:
				unify = generateNewDict(unify_intermediate, {})

			if unify_intermediate is not None:
				result1 = unifyHelper(unify, sentence1[:])
				result2 = unifyHelper(unify, sentence2[:])
				predicate = unifyHelper(unify, [predicate1])

				result=resolve(result1, result2, predicate[0])

				if not isTautology(result):
					result, argnum = standardize(result)
					resolvedSentences.append(result)

	return resolvedSentences

# -------------------- Unification Logic End --------------------


def proveQuery(query):
	'''
	:param query: The query to be proved
	:return: returns if the query is True/False
	'''

	# If it exceeds 15 secs we assume the query is not supported by KB
	end = time.time()
	if (end - start) > 15:
		return

	for rule in KB:
		# pass the query by reference always
		# keep the query variable local never change it

		resolvedSentences = resolution(rule[:],query[:])

		for output in resolvedSentences:

			temp = removeduplicates(output[:])
			hashtemp=temp[:]
			hashtemp.sort()
			hashtemp=hashStandard(hashtemp)

			key = hash(tuple(hashtemp))

			if key not in visited:
				visited[key] = 1

				nextQuery = temp

				if len(nextQuery) == 0:
					return True
				else:
					res=proveQuery(nextQuery[:])
					if (res):
						return True

def ASK_Queries():
    	
	'''
	This functions runs the set of given queries to get the inference for each
	of them.
	'''

	global start

	sys.setrecursionlimit(10000)

	# Read the file input
	readSentences()

	fileObject = open('output.txt', 'w')
	for query in ASK:
		for index in range(len(query)):
			query[index] = negatePredicate(query[index])

		# Adding the negated query to the KB before starting
		KB.append(query)

		# This is the hashMap to make sure we are not visiting the same state
		visited.clear()
		visited[hash(tuple(query))] = 1
		start = time.time()

		# Calling the prove Query to get the inference 
		if proveQuery(query):
			fileObject.write('TRUE\n')
		else:
			fileObject.write('FALSE\n')

		# Removing the initially added query from KB
		KB.remove(query)

	fileObject.close()

# Ask the quries to the Knowledge Base
ASK_Queries()