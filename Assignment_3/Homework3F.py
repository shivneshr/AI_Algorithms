import re
import time
import sys


# Main KB to use to prove the statements
KB = []

# Set of queries to ASK
ASK = []

# Visited sentence to avoid infinite loop
visited = {}

# Hash all the predicate location to retrieve it fast
hashPredLoc={}

start=0

def readSentences():
	'''
	:return: read the given input.txt to store the Query and Sentences in KB
	'''

	global ASK
	global KB

	# File from which we are reading the KB
	file = open("input.txt", "r")

	# No. of queries asked for the KB
	noOfQuery = int(file.readline())

	# Read the queries
	for i in range(noOfQuery):
		line = file.readline().replace('\n', '').replace(' ', '')
		predicates = line.split('|')
		ASK.append(predicates)

	# No. of sentences present in the KB
	noOfSentences = int(file.readline())

	# Read the sentences, standardize them and store them in KB
	for i in range(noOfSentences):
		line = file.readline().replace('\n', '').replace(' ', '')
		line = standardize(line, i)
		predicates = line.split('|')
		KB.append(predicates)

#------------------------------- Sentence Standardization -------------------------------
def standardize(sentence, num):
	'''
	:param sentence: KB sentence to be standardized
	:param num: literal used for standardizing
	:return: standardized sentence to be added to KB
	'''

	predicates = sentence.split('|')
	argDictionary = {}

	# Get the list of variables in the sentence
	for index, predicate in enumerate(predicates):
		arguments = predicate.split('(')[1].replace(')', '').split(',')
		for arg in arguments:
			if arg not in argDictionary and isVar(arg):
				argDictionary[arg] = arg + str(num)

	# Replace the new variable names in the sentences
	for key, value in argDictionary.items():
		sentence = sentence.replace('(' + key + ',', '(' + argDictionary[key] + ',') \
			.replace(',' + key + ')', ',' + argDictionary[key] + ')') \
			.replace('(' + key + ')', '(' + argDictionary[key] + ')') \
			.replace(',' + key + ',', ',' + argDictionary[key] + ',')

	return sentence

#--------------------- Sentence Standardization end ------------

# -------------------- Helper Methods --------------------------

def negatePredicate(predicate):
	if predicate[0] == '~':
		return predicate[1:]
	else:
		return '~' + predicate


def isVar(var):
	return var[0].islower()


def isLiteral(var):
	return not var[0].islower()



def isTautology(sentence):

	for predicate in sentence:
		if negatePredicate(predicate) in sentence:
			return True
	return False


# -------------------- Helper Methods End ----------------------


# -------------------- Resolution Helper ------------------------------

def resolve(sentence1, sentence2, predicate):
	'''

	:param sentence1: sentence 1
	:param sentence2: sentence 2
	:return: resolved sentence
	'''

	sentence1.remove(predicate)
	sentence2.remove(negatePredicate(predicate))

	return sentence1 + sentence2


# -------------------- Resolution Helper End ---------------------------


# -------------------- Unify Helper -----------------------------

def unifyHelper(map, sentences):
	'''
	:param map:         dictionary caontaining the mapping for variables and literals
	:param sentence:    sentence on which the unification needs to be performed
	:return:            returns a unified sentence
	'''
	for i, sentence in enumerate(sentences):
		for x, y in map.items():
			sentences[i] = sentences[i].replace(x, y)

	return sentences


def identifyVariable(x, y):
	'''

	This function is used to identify which is the variable and which is a literal
	return : variable, literal/variable, add(T/F), valid(T/F)
	'''

	# when both the predicates have	constants in the location and are different -> invalid unification
	if isLiteral(x) and isLiteral(y) and x != y:
		return x, y, False, False
	# when both the predicates have constants in the location and are same -> no need to replace
	elif isLiteral(x) and isLiteral(y) and x == y:
		return x, y, False, True

	# if one of them is a variable and other a constant then we unify -> variable = constant
	# if both of them are variables then we unify variable = variable

	if isVar(x):
		return x, y, True, True
	elif isVar(y):
		return y, x, True, True


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


# -------------------- Unify Helper End -------------------------

# -------------------- Resolution Logic ------------------------

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

			unify = {}
			flag = False
			intersection = False

			# we have to store this variable separately for each sentence - hash map
			# Extracting the arguments of the sentence 2
			arguments2 = predicate2.split('(')[1].replace(')', '').split(',')

			for x, y in zip(arguments1, arguments2):

				flag = True
				x, y, add, valid = identifyVariable(x, y)

				if not valid:
					flag = False
					break

				if add:
					if x not in unify:
						unify[x] = y
					elif unify[x] == y:
						continue
					else:
						flag = False
						break

			if flag:
				result1 = unifyHelper(unify, sentence1[:])
				result2 = unifyHelper(unify, sentence2[:])
				predicate = unifyHelper(unify, [predicate1])

				result=resolve(result1, result2, predicate[0])
				#if not isTautology(result):
				resolvedSentences.append(result)

	return resolvedSentences

# -------------------- Resolution Logic End --------------------

def proveQuery(query):
	'''
	:param query: The query to be proved
	:return: returns if the query is True/False
	'''

	end = time.time()
	if (end - start) > 10:
		return

	for rule in KB:
		# pass the query by reference always
		# keep the query variable local never change it

		resolvedSentences = resolution(rule[:],query[:])

		for output in resolvedSentences:

			temp = list(set(output[:]))
			temp.sort()
			key = hash(tuple(temp))

			if key not in visited:
				visited[key] = 1
				
				# the next state to be passed
				nextQuery = list(set(output[:]))

				if len(nextQuery) == 0:
					return True
				else:
					res=proveQuery(nextQuery[:])
					if (res):
						return True

def ASK_Queries():

	global start

	sys.setrecursionlimit(100000)

	# read the file input
	readSentences()

	fileObject = open('output.txt', 'w')
	for query in ASK:

		for index in range(len(query)):
			query[index] = negatePredicate(query[index])

		KB.append(query)

		visited.clear()
		visited[hash(tuple(query))] = 1

		start = time.time()

		if proveQuery(query):
			fileObject.write('TRUE\n')
		else:
			fileObject.write('FALSE\n')

		KB.remove(query)

	fileObject.close()


ASK_Queries()