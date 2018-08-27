# Artificial Intelligence Algorithms

## Search algorithms which solves the adaptation of n-queens problem efficiently
This project focuses on different search techniques in AI to effectively solve an adaptation of n-queens problem. Three search techniques have been identified
* **Depth First search** - performs well on sparse inputs and medium sized search space
* **Breadth First search** - performs well on dense inputs and small sized search space
* **Simulated Annealing** - performs well on sparse inputs and large sized search space

The goal of the code is to analyze various search techniques performance on various sizes and formations of search spaces and how effectively the algorithm prunes the search space to coverge to a solution faster. This code was tested against varying sizes of search spaces.

## Game playing algorithm which plays the fruit rage game against a AI agent

The goal of this project is to play a fruit rage game against an AI agent and win. Fruit rage game is an adaptation of the popular android game **Candy Crush**. A **Min-Max algorithm** has been implemented which decides the depth of the search space the AI agent chooses to search before making the move against computer AI agent. The depth is a function of the current score and the amount of time left for the game to continue. The AI agent identifies the move to be made using a intelligent function which has been implemented.

For further details about the problem statement look into the PDF.

## AI Inference Engine

This algorithm implements an Inference engine which takes a huge set of rules called as the Knowledge base. Based on the rules which the AI algorithm has learnt the Inference engine can answer the queries which are given as input. The algorithm used is **Resolution Algorithm**.
