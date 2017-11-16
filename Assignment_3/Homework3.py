import re

KB = []
ASK = []
visited={}

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

    # Read the sentences
	for i in range(noOfSentences):
		line = file.readline().replace('\n', '').replace(' ', '')
		line=standardize(line,i)
		predicates = line.split('|')
		KB.append(predicates)


def standardize(sentence,num):
    
	'''
	:param sentence: KB sentence to be standardized
	:param num: literal used for standardizing
	:return: standardized sentence to be added to KB
	'''

	predicates = sentence.split('|')
	argDictionary={}

    # Get the list of variables in the sentence
	for index,predicate in enumerate(predicates):
		arguments= predicate.split('(')[1].replace(')','').split(',')
		for arg in arguments:
			if arg not in argDictionary and len(arg)==1:
				argDictionary[arg]=arg+str(num)

    # Replace the new variable names in the sentences
	for key,value in argDictionary.items():
		sentence=sentence.replace('('+key+',','('+argDictionary[key]+',')\
						.replace(','+key+')',','+argDictionary[key]+')')\
						.replace('('+key+')','('+argDictionary[key]+')')\
						.replace(','+key+',',','+argDictionary[key]+',')

	return sentence


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

	return template + '\)', tempstring[1].replace(')', '').split(','), len(tempstring[1])


def resolve(sentence1, sentence2):
	'''

	:param sentence1: sentence 1
	:param sentence2: sentence 2
	:return: resolved sentence
	'''

	for predicate in sentence1:

		if '~' in predicate:
			if predicate[1:] in sentence2:
				sentence2.remove(predicate[1:])
				sentence1.remove(predicate)
		else:
			if '~' + predicate in sentence2:
				sentence2.remove('~' + predicate)
				sentence1.remove(predicate)

	return sentence1 + sentence2


def identifyVariable(x, y):
    
    '''
    This function is used to identify which is the variable and which is a literal
    '''

	if not x[0].islower() and not y[0].islower() and x != y:
		# when both the predicates have	literals in the location and are different
		return x, y, False, False
	elif not x[0].islower() and not y[0].islower() and x == y:
		# when both the predicates have literals in the location and are same
		return x, y, True, True

	if x[0].islower():
		return x, y, True, True
	elif y[0].islower():
		return y, x, True, True


def unifyHelper(map, sentence):
    
    '''
    :param map:         dictionary caontaining the mapping for variables and literals
	:param sentence:    sentence on which the unification needs to be performed
	:return:            returns a unified sentence
    '''
	for i in range(len(sentence)):
		for x, y in map.items():
			sentence[i] = sentence[i].replace(x, y)

	return sentence


def unification(sentence1, sentence2):
    
	'''
	:param sentence1: List of predicates sentence 1
	:param sentence2: List of predicates in sentence 2
	:return: Calculate possible unification and score of unification
	'''

	match = []
	unify = {}
	flag=False
	for str1 in sentence1:

		regex, variables1, num = getRegex(str1)
		functionmatch = re.compile(regex)

		matchedPredicate = list(filter(functionmatch.match, sentence2))

		if (len(matchedPredicate) != 0):
			flag=True
			variables2 = matchedPredicate[0].split('(')[1].replace(')', '').split(',')

			for x, y in zip(variables1, variables2):

				x, y, add, valid = identifyVariable(x, y)

				if (x not in unify) and valid and add:
					unify[x] = y
				elif not valid:
					flag=False

			match += matchedPredicate

    # unify contains the mapping of the variables and the corresponding values
    # this was calculated based on the values which were passed

	unifyHelper(unify, sentence2)
	unifyHelper(unify, sentence1)

	return flag,resolve(sentence2, sentence1)


def proveQuery(query):

	'''
	:param query: The query to be proved
	:return: returns if the query is True/False
	'''
    key=0
	for rule in KB:

		# pass the query by reference always
		# keep the query variable local never change it

		unified,temp=unification(query[:],rule[:])
        if unified:
		    key = hash(tuple(temp))

		if unified and (key not in visited):

			#print(rule,query)
			# the next state to be passed
			nextQuery=temp[:]

			# Adding the sentence to dictionary
			visited[key]=1

			#print(nextQuery)

			if len(nextQuery) == 0:
				return True
			else:
				if(proveQuery(nextQuery[:])):
					return True


def ASK_Queries():
	readSentences()

	for query in ASK:

		for index in range(len(query)):

			if '~' in query[index]:
				query[index]=query[index][1:]
			else:
				query[index]='~'+query[index]

		visited.clear()
		visited[hash(tuple(query))]=1

		fileObject = open('output.txt', 'a')

		if proveQuery(query):
			fileObject.write('TRUE\n')
		else:
			fileObject.write('FALSE\n')
		fileObject.close()


ASK_Queries()






