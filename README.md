# Connect-4
This is a connect 4 game made in Java using [MinMax with alpha-beta pruning algorithm](https://www.youtube.com/watch?v=l-hh51ncgDI). It features a user-friendly GUI created using Java Swing. To execute the program make sure to download and import the [org.json](https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar) library.

![Screenshot (140)](https://github.com/AxilleasGalanis/Connect-4/assets/130224323/7e5378dc-7123-4d7a-a8d6-f17b118032d2)

# Features
The horizontal menu shown in the figure has the following options.

● New Game: This menu is the starting point of a new game. By selecting it,
a list of difficulty levels appears and the player can select one of them. These levels are as follows:
1) Trivial: The AI ​​module looks at one move in depth.
2) Medium: The AI ​​module looks at three moves in depth.
3) Hard: The AI ​​module looks at one five moves in depth.

● 1st Player: A menu of two options appears with the options “AI” and
"You". The AI ​​option is defaulted at program startup and gives the first movement of each new game to the AI ​​module, while the You option to the application user. The choice
“AI” or “You” has no effect on the current game and is about the next one.

● History: The game canvas "disappears" from the window and a scrollable list appears
of previously completed games. For every game
completed, the following information is displayed: 
1) start date and time,
2) the level difficulty of the AI ​​module and
3) whether the AI ​​module or the end user won.

The games are listed in chronological descending order, where the most recent is
at the top of the list. By double left clicking on any game, it also appears
the canvas again. In this canvas the history of the selected game evolves.

● Help: The menu is disabled.
