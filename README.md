# Gang of Four Information Set Monte Carlo Tree Search

This project implements the Information Set Monte Carlo Tree Search model (see https://core.ac.uk/download/pdf/30267707.pdf) for playing the card game Gang of Four.

## Information Set Monte Carlo Tree Search

ISMCTS extends MCTS to games without perfect information. For more specifics see the paper I used to create this project which is linked above. I implemented both naive Monte Carlo, which simply simulates random games for each possible outcome, and full ISMCTS. While ISMCTS seems to perform well, from a theoretical perspective I don't find it to be a very interesting or powerful algorithm as it does not converge to an optimal policy (as base MCTS does for perfect information games).

## The Client

The project includes a python client that runs the engine and is capable of interfacing with my web version of Gang of Four (https://github.com/mtp61/gof-online). This enables any combination of humans and bots to play together online with ease. Once you have an instance of gof-online running on your local machine, simply run client.py with optional command line arguments specifying the game name and username for the bot. 

## Performance

I think this agent plays at a slightly sub-human level. I have played quite a bit of Gang of Four and this bot seems to play very similarly to a good human player, though anecdotally it does seem to end up with more loose singles at the end than a skilled human would. Gang of Four is a game of extreme variance, so conclusively showing how good the program is would take many hundreds of games, which I have not had the time to do. Despite this, I am very happy with the performance of the agent as slightly sub-human performance is already more than I expected when starting the project. It's important to note that since ISMCTS can be stopped after any number of iterations, so if the computer was given enough time it would likely be super-human. I played the bot with a maximum iteration count of 10000, which usually resulted in moves being generated in 5-10 seconds. 
