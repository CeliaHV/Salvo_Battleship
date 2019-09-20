# Salvo_Battleship
First Online Game Multiplayer made it with HTML, CSS, Javascript, Vue.js, Java & Spring.

In this project, I've been contacted by a board game company looking to use their brand recognition to market online games with a retro touch. In particular, they want you to create a multi-player online version of a Salvo-like game engine. Salvo was a pencil and paper game that was the basis for the popular Battleship game. The basic idea involves guessing where other players have hidden objects. This can be varied to create many different kinds of games with different user interfaces.

I've created a front-end web application that game players interact with, and a back-end game server to manage the games, scoring, and player profiles. I've used the jQuery JavaScript library for the front-end client, and the Spring Boot framework for the Java-based RESTful web server.

In Part One, I've implemented the core architecture:
- A small Java back-end server to store Salvo game data, and send that data to client apps via a RESTful API.
- A front-end browser-based game interface that graphically shows players the state of the game, including ships they've placed, damage sustained, and scores.

In Part Two, I've implemented game play:
- Players can create new games and join games that others have created.
- When a game has both players, players can place their ships on their grids.
- When ships have been placed, players can begin trading salvos (shots) and seeing the results (hits, sinks, and misses).
- When all of a player's ships have been sunk, the game ends and the winner is added to the leaderboard.
