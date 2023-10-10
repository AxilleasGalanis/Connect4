import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import org.json.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Connect4 {
    private final JFrame frame;
    private final JPanel panel;
    private final JButton newGameButton;
    private final JButton firstPlayerButton;
    private final JButton historyButton;
    private final JButton helpButton;
    private final JPopupMenu newGameMenu;
    private final JPopupMenu firstPlayerMenu;
    private final JMenuItem trivialOption;
    private final JMenuItem mediumOption;
    private final JMenuItem hardOption;
    private final JRadioButton aiRadioButton;
    private final JRadioButton youRadioButton;
    private final Connect4Board board;
    
    public Connect4() {
        frame = new JFrame("Connect 4");
        panel = new JPanel();
        newGameButton = new JButton("New Game");
        firstPlayerButton = new JButton("1st Player");
        historyButton = new JButton("History");
        helpButton = new JButton("Help");
        newGameMenu = new JPopupMenu();
        firstPlayerMenu = new JPopupMenu();
        trivialOption = new JMenuItem("Trivial");
        mediumOption = new JMenuItem("Medium");
        hardOption = new JMenuItem("Hard");
        aiRadioButton = new JRadioButton("AI", true);
        youRadioButton = new JRadioButton("You");
        board = new Connect4Board();
        board.fisrtPlayer = 1;
        board.currentPlayer = board.fisrtPlayer;
        
        trivialOption.addActionListener(e -> {
            setDifficulty(1);
        });
        
        mediumOption.addActionListener(e -> {
            setDifficulty(3);
        });
        
        hardOption.addActionListener(e -> {
            setDifficulty(5);
        });
        
        aiRadioButton.addActionListener(e -> {
            board.fisrtPlayer = 1;
            board.currentPlayer = board.fisrtPlayer;
            youRadioButton.setSelected(false);
        });
        
        youRadioButton.addActionListener(e -> {
            board.fisrtPlayer = 2;
            board.currentPlayer = board.fisrtPlayer;
            aiRadioButton.setSelected(false);
        });
        
        newGameMenu.add(trivialOption);
        newGameMenu.add(mediumOption);
        newGameMenu.add(hardOption);
        
        newGameButton.addActionListener(e -> {
            newGameMenu.show(newGameButton, 0, newGameButton.getHeight());
        });
        
        firstPlayerMenu.add(aiRadioButton);
        firstPlayerMenu.add(youRadioButton);
        
        firstPlayerButton.addActionListener(e -> {
            firstPlayerMenu.show(firstPlayerButton, 0, firstPlayerButton.getHeight());
        });
        
        historyButton.addActionListener(e -> {
            // create a new list model to hold the file names
            DefaultListModel<String> listModel = new DefaultListModel<>();

            // get the path of the directory where the saved games are stored
            String homeDirectory = System.getProperty("user.home");
            File directory = new File(homeDirectory, "connect4");

            // iterate over the files in the directory and add their names to the list model
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName().replace('_', ':');
                        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                        listModel.addElement(fileName);
                    }
                }
            }

            // create a new JList with the list model
            JList<String> list = new JList<>(listModel);
            //list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane listScrollPane = new JScrollPane(list);

            // create a new panel and add the JList to it
            JPanel historyPanel = new JPanel(new BorderLayout());
            historyPanel.add(listScrollPane, BorderLayout.CENTER);
            board.setVisible(false);
            // create a new dialog to display the panel
            JDialog historyDialog = new JDialog(frame, "Game History", true);
            historyDialog.getContentPane().add(historyPanel);
            historyDialog.pack();
            historyDialog.setLocationRelativeTo(frame);

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int index = list.locationToIndex(e.getPoint());
                        String selected = listModel.get(index).replace(':', '_');
                        // add code here to save the format of the JSON file into a string
                        if (selected != null) {
                            // close the dialog box
                            historyDialog.dispose();

                            // make the board visible
                            board.setVisible(true);

                            // get the path of the directory where the saved games are stored
                            String homeDirectory = System.getProperty("user.home");
                            File directory = new File(homeDirectory, "connect4");

                            // load the selected game from the JSON file and update the game state
                           try (BufferedReader br = new BufferedReader(new FileReader(new File(directory, selected + ".json")))) {
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    sb.append(line);
                                }
                                String jsonString = sb.toString();
                                board.replay(jsonString);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });

            historyDialog.setVisible(true);

            // add a window listener to the dialog to handle when it is closed
            historyDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent e) {
                    board.setVisible(true);
                }
            });
        });


        panel.add(newGameButton);
        panel.add(firstPlayerButton);
        panel.add(historyButton);
        panel.add(helpButton);
        
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(board, BorderLayout.CENTER);
        newGameButton.setFocusable(false);
        firstPlayerButton.setFocusable(false);
        historyButton.setFocusable(false);
        helpButton.setFocusable(false);
        newGameMenu.setFocusable(false);
        frame.setFocusable(false);
        panel.setFocusable(false);
        board.setFocusable(true);
        board.requestFocus();
        
        // Set the outline color to black
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private void setDifficulty(int maxDepth) {
        board.gameEnded = false;
        board.MAX_DEPTH = maxDepth;
        board.newGame();
    }
    
    public static void main(String[] args) {
        Connect4 game = new Connect4();
    }
}

class Connect4Board extends JPanel {
    private final int ROWS = 6;
    private final int COLS = 7;
    private final int CELL_SIZE = 40;
    private final int SPACING = 8;
    private final Color CIRCLE_COLOR = Color.WHITE;
    private static final Color YELLOW_COLOR = Color.YELLOW;
    private static final Color RED_COLOR = Color.RED;
    private final List<List<Color>> board;
    public int MAX_DEPTH = -1;
    private final int AI_PLAYER = 1;
    private final int HUMAN_PLAYER = 2;
    public int currentPlayer;
    public int fisrtPlayer;
    public boolean gameEnded = false;
    private List<Move> moves = new ArrayList<>();

    public Connect4Board() {
        board = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            List<Color> row = new ArrayList<>();
            for (int j = 0; j < COLS; j++) {
                row.add(CIRCLE_COLOR);
            }
            board.add(row);
        }
        setPreferredSize(new Dimension(COLS * (CELL_SIZE + SPACING), ROWS * (CELL_SIZE + SPACING) + SPACING));
    }
    
    private void saveGame() {
        JSONArray jsonArray = new JSONArray();
        for (Move move : moves) {
            JSONObject moveJson = new JSONObject();
            moveJson.put("player", move.getPlayer());
            moveJson.put("column", move.getColumn());
            jsonArray.put(moveJson);
        }
        String json = jsonArray.toString();
        try {
            // Get the user's home directory
            String homeDirectory = System.getProperty("user.home");

            // Create a directory named "connect4" inside the home directory
            File connect4Dir = new File(homeDirectory, "connect4");
            if (!connect4Dir.exists()) {
                connect4Dir.mkdir();
            }
            
            String timestamp = new SimpleDateFormat("yyyy.MM.dd - HH_mm").format(new Date());
            // Create a file with a timestamp as the name inside the connect4 directory
            String name="";
            if (MAX_DEPTH == 1) {
                if (currentPlayer == AI_PLAYER)
                    name = timestamp + "  L_ Trivial   W_ AI" + ".json";
                else
                    name = timestamp + "  L_ Trivial   W_ P" + ".json";
            }
            else if (MAX_DEPTH == 3) {
                if (currentPlayer == AI_PLAYER)
                    name = timestamp + "  L_ Medium   W_ AI" + ".json";
                else
                    name = timestamp + "  L_ Medium   W_ P" + ".json";
            }
            else if (MAX_DEPTH == 5) {
                if (currentPlayer == AI_PLAYER)
                    name = timestamp + "  L_ Hard   W_ AI" + ".json";
                else
                    name = timestamp + "  L_ Hard   W_ AI" + ".json";
            }
            File file = new File(connect4Dir, name);

            // Write the JSON string to the file
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void replay(String moves) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board.get(i).set(j, CIRCLE_COLOR);
                paintImmediately(0, 0, getWidth(), getHeight());
                
            }
        }
        
        JSONArray jsonArray = new JSONArray(moves);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            int column = obj.getInt("column");
            int player = obj.getInt("player");
            int row = ROWS - 1;
            for (; row >= 0; row--) {
                if (board.get(row).get(column) == CIRCLE_COLOR) {
                    break;
                }
            }
            for (int j = 0; j <= row; j++){
                if (player == 1)
                    board.get(j).set(column, YELLOW_COLOR);
                else
                    board.get(j).set(column, RED_COLOR);
                paintImmediately(0, 0, getWidth(), getHeight());
                if (j == row){
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                board.get(j).set(column, CIRCLE_COLOR);
                paintImmediately(0, 0, getWidth(), getHeight());
            }
            System.out.println();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        gameEnded = true;
    }

    public void newGame() {
        moves = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board.get(i).set(j, CIRCLE_COLOR);
                paintImmediately(0, 0, getWidth(), getHeight());
                
            }
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (getMouseListeners().length > 0 && getMouseListeners()[0] != null)
            removeMouseListener(getMouseListeners()[0]);
        if (getKeyListeners().length > 0 && getKeyListeners()[0] != null)
            removeKeyListener(getKeyListeners()[0]); 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameEnded && e.getClickCount() == 2) {
                    int col = getColumnAt(e.getPoint());
                    if (col >= 0) {
                        addRedChecker(col);
                    }
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameEnded) {
                    int keyCode = e.getKeyCode();
                    if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_6) {
                        int col = keyCode - KeyEvent.VK_0; // Convert key code to column index
                        addRedChecker(col);
                    }
                }
            }
        });
        if ((currentPlayer == AI_PLAYER && !gameEnded) || (fisrtPlayer == AI_PLAYER && !gameEnded)) {
            makeAIMove();
        }
    }
    
    private int getColumnAt(Point p) {
        int x = p.x;
        int col = (x - SPACING) / (CELL_SIZE + SPACING);
        if (col >= 0 && col < COLS) {
            return col;
        }
        return -1;
    }

    private void addRedChecker(int col) {
        int row = ROWS - 1;
        for (; row >= 0; row--) {
            if (board.get(row).get(col) == CIRCLE_COLOR) {
                break;
            }
        }
        for (int i = 0; i <= row; i++){
            board.get(i).set(col, RED_COLOR);
            paintImmediately(0, 0, getWidth(), getHeight());
            if (i == row){
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            board.get(i).set(col, CIRCLE_COLOR);
            paintImmediately(0, 0, getWidth(), getHeight());
        }
        moves.add(new Move(HUMAN_PLAYER, col));
        if (isGameOver()) {
            gameEnded = true;
            saveGame();
            JOptionPane.showMessageDialog(this, "You won!");
            return;
        }
        currentPlayer = AI_PLAYER;
        makeAIMove();
    }
    
    private void addYellowChecker(int col) {
        int row = ROWS - 1;
        for (; row >= 0; row--) {
            if (board.get(row).get(col) == CIRCLE_COLOR) {
                break;
            }
        }
        for (int i = 0; i <= row; i++){
            board.get(i).set(col, YELLOW_COLOR);
            paintImmediately(0, 0, getWidth(), getHeight());
            if (i == row){
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            board.get(i).set(col, CIRCLE_COLOR);
            paintImmediately(0, 0, getWidth(), getHeight());
        }
        moves.add(new Move(AI_PLAYER, col));
        if (isGameOver()) {
            gameEnded = true;
            saveGame();
            JOptionPane.showMessageDialog(this, "You lost!");
        }
    }
    
    private void makeAIMove() {
        if (!gameEnded) {
            int col = findBestMove();
            addYellowChecker(col);
            currentPlayer = HUMAN_PLAYER;
        }
    }
    
    private int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = COLS / 2;

        for (int col = 0; col < COLS; col++) {
            if (isValidMove(col)) {
                makeMove(col, AI_PLAYER);
                int score = minMax(MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                undoMove(col);
                
                if (score > bestScore || (score == bestScore && Math.abs(col - (COLS / 2)) < Math.abs(bestMove - (COLS / 2)))) {
                    bestScore = score;
                    bestMove = col;
                }
            }
        }

        return bestMove;
    }
    
    private int minMax(int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        int score = evaluate();
        
        if (depth == 0 || isGameOver()) {
            return score;
        }
        
        if (isMaximizingPlayer) {
            int bestScore = Integer.MIN_VALUE;
            
            for (int col = 0; col < COLS; col++) {
                if (isValidMove(col)) {
                    makeMove(col, AI_PLAYER);
                    int currScore = minMax(depth - 1, alpha, beta, false);
                    undoMove(col);
                    
                    bestScore = Math.max(bestScore, currScore);
                    alpha = Math.max(alpha, bestScore);
                    
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            
            for (int col = 0; col < COLS; col++) {
                if (isValidMove(col)) {
                    makeMove(col, HUMAN_PLAYER);
                    int currScore = minMax(depth - 1, alpha, beta, true);
                    undoMove(col);
                    
                    bestScore = Math.min(bestScore, currScore);
                    beta = Math.min(beta, bestScore);
                    
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            
            return bestScore;
        }
    }
    
    private boolean isValidMove(int col) {
        return board.get(0).get(col) == CIRCLE_COLOR;
    }
    
    private void makeMove(int col, int player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board.get(row).get(col) == CIRCLE_COLOR) {
                if  (player == 1)
                    board.get(row).set(col, YELLOW_COLOR);
                else
                    board.get(row).set(col, RED_COLOR);
                break;
            }
        }
    }

    
    private void undoMove(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (board.get(row).get(col) != CIRCLE_COLOR) {
                board.get(row).set(col, CIRCLE_COLOR);
                break;
            }
        }
    }
    
    private boolean isGameOver() {
        // Check for a horizontal win
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                Color player = board.get(row).get(col);
                if (player != CIRCLE_COLOR && player == board.get(row).get(col + 1) && player == board.get(row).get(col + 2) && player == board.get(row).get(col + 3)) {
                    return true;
                }
                Color AI = board.get(row).get(col);
                if (AI != CIRCLE_COLOR && AI == board.get(row).get(col + 1) && AI == board.get(row).get(col +2 ) && AI == board.get(row).get(col + 3)) {
                    return true;
                }
            }
        }

        // Check for a vertical win
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row <= ROWS - 4; row++) {
                Color player = board.get(row).get(col);
                if (player != CIRCLE_COLOR && player == board.get(row + 1).get(col) && player == board.get(row + 2).get(col) && player == board.get(row + 3).get(col)) {
                    return true;
                }
                Color AI = board.get(row).get(col);
                if (AI != CIRCLE_COLOR && AI == board.get(row + 1).get(col) && AI == board.get(row + 2).get(col) && AI == board.get(row + 3).get(col)) {
                    return true;
                }
            }
        }

        // Check for a diagonal win (left to right)
        for (int row = 0; row <= ROWS - 4; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                Color player = board.get(row).get(col);
                if (player != CIRCLE_COLOR && player == board.get(row + 1).get(col + 1) && player == board.get(row + 2).get(col + 2) && player == board.get(row + 3).get(col + 3)) {
                    return true;
                }
                Color AI = board.get(row).get(col);
                if (AI != CIRCLE_COLOR && AI == board.get(row + 1).get(col + 1) && AI == board.get(row + 2).get(col + 2) && AI == board.get(row + 3).get(col + 3)) {
                    return true;
                }
            }
        }

        // Check for a diagonal win (right to left)
        for (int row = 0; row <= ROWS - 4; row++) {
            for (int col = COLS - 1; col >= 3; col--) {
                Color player = board.get(row).get(col);
                if (player != CIRCLE_COLOR && player == board.get(row + 1).get(col - 1) && player == board.get(row + 2).get(col - 2) && player == board.get(row + 3).get(col - 3)) {
                    return true;
                }
                Color AI = board.get(row).get(col);
                if (AI != CIRCLE_COLOR && AI == board.get(row + 1).get(col - 1) && AI == board.get(row + 2).get(col - 2) && AI == board.get(row + 3).get(col - 3)) {
                    return true;
                }
            }
        }

        // Check for a tie game
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(col)) {
                // If there is at least one valid move, the game is not over
                return false;
            }
        }

        // All columns are full and no player has won, so the game is a tie
        return true;
    }
    
    private int evaluateWindow(List<Color> window) {
        int aiCount = 0;
        int humanCount = 0;

        for (int i = 0; i < 4; i++) {
            if (window.get(i) == YELLOW_COLOR) {
                aiCount++;
            } else if (window.get(i) == RED_COLOR) {
                humanCount++;
            }
        }

        if (aiCount == 4) {
            return 10000;
        } else if (humanCount == 4) {
            return -10000;
        } else if (aiCount == 3 && humanCount == 0) {
            return 16;
        } else if (humanCount == 3 && aiCount == 0) {
            return -16;
        } else if (aiCount == 2 && humanCount == 0) {
            return 4;
        } else if (humanCount == 2 && aiCount == 0) {
            return -4;
        } else if (aiCount == 1 && humanCount == 0) {
            return 1;
        } else if (humanCount == 1 && aiCount == 0) {
            return -1;
        } else {
            return 0;
        }
    }

    
    private int evaluate() {
        int score = 0;

        // Check rows for possible win or loss
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                List<Color> window = Arrays.asList(board.get(row).get(col), board.get(row).get(col + 1), board.get(row).get(col + 2), board.get(row).get(col + 3));
                score += evaluateWindow(window);
            }
        }

        // Check columns for possible win or loss
        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = 0; col < COLS; col++) {
                List<Color> window = Arrays.asList(board.get(row).get(col), board.get(row + 1).get(col), board.get(row + 2).get(col), board.get(row + 3).get(col));
                score += evaluateWindow(window);
            }
        }

        // Check diagonals for possible win or loss (left to right)
        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                List<Color> window = Arrays.asList(board.get(row).get(col), board.get(row + 1).get(col + 1), board.get(row + 2).get(col + 2), board.get(row + 3).get(col + 3));
                score += evaluateWindow(window);
            }
        }

        // Check diagonals for possible win or loss (right to left)
        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = 3; col < COLS; col++) {
                List<Color> window = Arrays.asList(board.get(row).get(col), board.get(row + 1).get(col - 1), board.get(row + 2).get(col - 2), board.get(row + 3).get(col - 3));
                score += evaluateWindow(window);
            }
        }

        return score;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw circles for the grid
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * (CELL_SIZE + SPACING) + CELL_SIZE / 2 + SPACING;
                int y = row * (CELL_SIZE + SPACING) + CELL_SIZE / 2 + SPACING;
                g.setColor(board.get(row).get(col));
                g.fillOval(x - CELL_SIZE / 2, y - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.BLACK);
                g.drawOval(x - CELL_SIZE / 2, y - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}

class Move {
    public int player;
    public int col;

    public Move(int player, int col) {
        this.player = player;
        this.col = col;
    }
    
    public int getColumn() {
       return this.col;
    }
    
     public int getPlayer() {
       return this.player;
    }
}
