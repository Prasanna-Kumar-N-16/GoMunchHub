import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.Random;
import javafx.util.Duration;

public class MinesweeperGame extends Application {
    // Constants for cell size
    private static final int CELL_SIZE = 32;

    // Constants for Beginner difficulty for number of rows ,
    // number of columns and total number of mines.
    private static final int BEGINNER_ROWS = 6;
    private static final int BEGINNER_COLS = 9;
    private static final int BEGINNER_MINES = 11;

    // Constants for Intermediate difficulty for number of rows ,
    // number of columns and total number of mines.
    private static final int INTERMEDIATE_ROWS = 12;
    private static final int INTERMEDIATE_COLS = 18;
    private static final int INTERMEDIATE_MINES = 36;

    // Constants for Advanced difficulty for number of rows ,
    // number of columns and total number of mines.
    private static final int ADVANCED_ROWS = 21;
    private static final int ADVANCED_COLS = 26;
    private static final int ADVANCED_MINES = 92;

    // Game parameters
    private int rows;
    private int cols;
    private int mines;
    private Button[][] buttons;
    private boolean[][] isMine;
    private boolean[][] isRevealed;

    // Timer variables
    private Timeline timer;
    private int elapsedTimeSeconds;
    private Label timerLabel;

    // Mines label to display remaining flags
    private Label minesLabel;

    // Difficulty
    private Difficulty updatedDifficulty;

    // Time Limits
    private static final int BEGINNER_TIME_LIMIT = 60;
    private static final int INTERMEDIATE_TIME_LIMIT = 180;
    private static final int ADVANCED_TIME_LIMIT = 660;

    // Flags counter
    private int flagsPlaced;

    // Stage reference
    private Stage primaryStage;

    // Entry point of the JavaFX application
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Store the reference to the primary stage

        primaryStage.setTitle("Minesweeper");

        // Display difficulty selection dialog
        DifficultySelectionDialog dialog = new DifficultySelectionDialog();
        dialog.showAndWait();

        if (dialog.getSelectedDifficulty() == null) {
            // Exit the application if the user cancels the dialog
            System.exit(0);
        }

        // Get the selected difficulty from the dialog and update the difficulty
        updatedDifficulty = dialog.getSelectedDifficulty();
        setGameParameters(dialog.getSelectedDifficulty());

        GridPane grid = createGameGrid();

        // Create and add a label for the game timer to the grid
        timerLabel = new Label("Time: 0 seconds");
        grid.add(timerLabel, cols, 0);

        // Create and add a label for the remaining flags to the grid
        minesLabel = new Label(" Flags left:  0 ");
        grid.add(minesLabel, cols, 1);

        // Create a scene with the game grid and set it to the primary stage
        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);

        // Show the primary stage
        primaryStage.show();

        // Initialize the game logic and state
        initializeGame();

        // Initialize and start the game timer
        initializeTimer();
    }

    /**
     * Initializes the timer for the game.
     */
    private void initializeTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), this::updateTimer));
        timer.setCycleCount(Timeline.INDEFINITE);
        elapsedTimeSeconds = 0;
        timer.play();
    }

    /**
     * Updates the game timer.
     *
     * @param event The ActionEvent triggering the timer update.
     */
    private void updateTimer(ActionEvent event) {
        elapsedTimeSeconds++;

        // Check if the time limit has been reached for the current difficulty
        int timeLimit = getTimeLimit();
        if (elapsedTimeSeconds >= timeLimit) {
            timer.stop();
            showAlert(false);
        }
        // Set Label Text
        timerLabel.setText("Time: " + elapsedTimeSeconds + " seconds");
    }

    /**
     * Retrieves the time limit based on the selected difficulty.
     *
     * @return The time limit in seconds.
     */
    private int getTimeLimit() {
        switch (updatedDifficulty) {
            case BEGINNER:
                return BEGINNER_TIME_LIMIT;
            case INTERMEDIATE:
                return INTERMEDIATE_TIME_LIMIT;
            case ADVANCED:
                return ADVANCED_TIME_LIMIT;
            default:
                return 0;
        }
    }

    /**
     * Creates the game grid and initializes buttons.
     *
     * @return The GridPane representing the game grid.
     */
    private GridPane createGameGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        // initialize variables
        buttons = new Button[rows][cols];
        isMine = new boolean[rows][cols];
        isRevealed = new boolean[rows][cols];

        // Loop through rows and columns to create buttons
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button button = new Button();
                button.setMinSize(CELL_SIZE, CELL_SIZE);

                // Create effectively final variables for lambda expression
                final int finalRow = row;
                final int finalCol = col;

                // Add the event handler for the left and right mouse button clicks
                button.setOnMouseClicked(event -> handleButtonClick(finalRow, finalCol, event.getButton()));

                grid.add(button, col, row);
                buttons[row][col] = button;
            }
        }

        return grid;
    }

    /**
     * Initializes the game by placing mines and updating buttons.
     */
    private void initializeGame() {
        placeMines();
        updateButtons();
        flagsPlaced = 0; // Initialize the number of flags placed
    }

    /**
     * Sets game parameters (rows, columns, mines) based on the selected difficulty.
     *
     * @param difficulty The selected difficulty level.
     */
    private void setGameParameters(Difficulty difficulty) {
        switch (difficulty) {
            case BEGINNER:
                rows = BEGINNER_ROWS;
                cols = BEGINNER_COLS;
                mines = BEGINNER_MINES;
                break;
            case INTERMEDIATE:
                rows = INTERMEDIATE_ROWS;
                cols = INTERMEDIATE_COLS;
                mines = INTERMEDIATE_MINES;
                break;
            case ADVANCED:
                rows = ADVANCED_ROWS;
                cols = ADVANCED_COLS;
                mines = ADVANCED_MINES;
                break;
        }
    }

    /**
     * Randomly places mines on the game grid.
     */
    private void placeMines() {
        Random random = new Random();

        for (int i = 0; i < mines; i++) {
            int row, col;
            do {
                // assign values randomly
                row = random.nextInt(rows);
                col = random.nextInt(cols);
            } while (isMine[row][col]);
            isMine[row][col] = true;
        }
    }

    /**
     * Handles button clicks based on the mouse button.
     *
     * @param row    The row of the clicked button.
     * @param col    The column of the clicked button.
     * @param button The MouseButton representing the type of click.
     */
    private void handleButtonClick(int row, int col, MouseButton button) {
        if (button == MouseButton.SECONDARY) {
            handleFlagClick(row, col);
        } else if (isMine[row][col]) {
            // if mine selected show alert
            showAlert(true);
        } else {
            revealCell(row, col);
            checkGameWin();
        }
    }

    /**
     * Handles right-click (flag) on a cell.
     *
     * @param row The row of the clicked button.
     * @param col The column of the clicked button.
     */
    private void handleFlagClick(int row, int col) {
        Button clickedButton = buttons[row][col];

        if (!isRevealed[row][col]) {
            if (clickedButton.getText().equals("F")) {
                // Remove the flag
                clickedButton.setText("");
                flagsPlaced--;
            } else {
                // Place a flag
                clickedButton.setText("F");
                flagsPlaced++;
            }

            // Update the flags left display
            updateFlagsLeftDisplay();
        }
    }

    /**
     * Reveals a cell and adjacent cells recursively.
     *
     * @param row The row of the clicked button.
     * @param col The column of the clicked button.
     */
    private void revealCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || isRevealed[row][col]) {
            return;
        }

        // default value is true
        isRevealed[row][col] = true;
        buttons[row][col].setDisable(true);

        int mineCount = countAdjacentMines(row, col);
        if (mineCount > 0) {
            buttons[row][col].setText(String.valueOf(mineCount));
        } else {
            // Recursive reveal for adjacent cells with no mines
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    revealCell(i, j);
                }
            }
        }
    }

    /**
     * Counts the number of mines in adjacent cells.
     *
     * @param row The row of the clicked button.
     * @param col The column of the clicked button.
     * @return The number of mines in adjacent cells.
     */
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                // Check if the neighboring cell is within the bounds of the grid and contains a
                // mine
                if (i >= 0 && i < rows && j >= 0 && j < cols && isMine[i][j]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Updates the state of revealed cells and the flags left display.
     */
    private void updateButtons() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Check if the current cell has been revealed
                if (isRevealed[row][col]) {
                    buttons[row][col].setDisable(true);
                    int mineCount = countAdjacentMines(row, col);
                    // If there are adjacent mines, display the mine count on the button
                    if (mineCount > 0) {
                        buttons[row][col].setText(String.valueOf(mineCount));
                    }
                }
            }
        }

        // Display the number of flags left
        updateFlagsLeftDisplay();
    }

    /**
     * Updates the display of flags left.
     */
    private void updateFlagsLeftDisplay() {
        int flagsLeft = mines - flagsPlaced;
        minesLabel.setText(" Flags left: " + flagsLeft);
    }

    /**
     * Shows a game-over alert.
     *
     * @param isGameOver Indicates whether the game is over due to a mine hit.
     */
    private void showAlert(boolean isGameOver) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Check if the game is over due to hitting a mine or time's up
        if (isGameOver) {
            alert.setTitle("Game Over");
            alert.setHeaderText("Game Over");
            alert.setContentText("You hit a mine! Game over.");
        } else {
            // Set the alert title and content for time's up
            alert.setTitle("Time's Up");
            alert.setHeaderText("Game Over");
            alert.setContentText("Your time is up! Game over.");
        }

        // Stop the game timer
        timer.stop();

        Toolkit.getDefaultToolkit().beep();

        // Cover the mines with an "X" flag
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!isMine[row][col] && buttons[row][col].getText().equals("F")) {
                    // Mark cells with a red "X" where the player marked a flag but there is no mine
                    buttons[row][col].setText("X");
                    buttons[row][col].setStyle("-fx-text-fill: red"); // Set text color to red
                } else if (isMine[row][col] && !buttons[row][col].getText().equals("F")) {
                    buttons[row][col].setText("X");
                }
            }
        }

        // Create a sequential transition for the explosion
        SequentialTransition explosionTransition = new SequentialTransition();

        // Play explosion animation for mine buttons one after another
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Check if the cell contains a mine and the button is not marked as a flag
                if (isMine[row][col] && !buttons[row][col].getText().equals("F")) {
                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), buttons[row][col]);
                    scaleIn.setToX(1.5);
                    scaleIn.setToY(1.5);

                    // Create a scale-out transition for the mine explosion
                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), buttons[row][col]);
                    scaleOut.setToX(1.0);
                    scaleOut.setToY(1.0);

                    // Add the mine explosion transition to the overall explosion transition
                    SequentialTransition mineExplosion = new SequentialTransition(scaleIn, scaleOut);
                    explosionTransition.getChildren().add(mineExplosion);
                }
            }
        }

        // Introduce a pause before showing the game-over alert
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5)); // Adjust the duration as needed
        pause.setOnFinished(event -> {
            explosionTransition.setOnFinished(e -> {
                // Run the following code on the JavaFX Application Thread
                Platform.runLater(() -> {
                    // Show the game-over alert
                    alert.showAndWait();
                    // Create a confirmation alert for restarting or exiting the game
                    Alert restartAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    restartAlert.setTitle("Game Over");
                    restartAlert.setHeaderText("Game Over");
                    restartAlert.setContentText("Do you want to restart the game?");

                    // Define restart and exit buttons for the alert
                    ButtonType restartButton = new ButtonType("Restart");
                    ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
                    restartAlert.getButtonTypes().setAll(restartButton, exitButton);

                    // Show the restart alert and wait for user input
                    Optional<ButtonType> result = restartAlert.showAndWait();

                    // Process user input from the restart alert
                    if (result.isPresent() && result.get() == restartButton) {
                        // Restart the game if the user chooses to restart
                        restartGame();
                    } else {
                        // Gracefully exit the JavaFX application if the user chooses to exit
                        Platform.exit();
                    }
                });
            });
            explosionTransition.play(); // Start the explosion animation
        });

        pause.play();
    }

    /**
     * Restarts the game.
     */
    private void restartGame() {
        // Reset game-related variables
        elapsedTimeSeconds = 0;
        flagsPlaced = 0;

        // Reset mine, revealed, and button arrays
        isMine = new boolean[rows][cols];
        isRevealed = new boolean[rows][cols];
        buttons = new Button[rows][cols];

        // Close the previous window
        primaryStage.close();

        // Create a new stage and start the game
        Stage newStage = new Stage();
        start(newStage);
    }

    /**
     * Checks if the player has won the game.
     */
    private void checkGameWin() {
        int unrevealedCells = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                 // Check if the current cell is unrevealed and does not contain a mine
                if (!isRevealed[row][col] && !isMine[row][col]) {
                    unrevealedCells++;
                }
            }
        }

        // Check if there are no unrevealed cells left (the board is cleared)
        if (unrevealedCells == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Congratulations");
            alert.setHeaderText("Congratulations");
            alert.setContentText("You've cleared the board! You win!");
            // Show the winning alert and wait for user acknowledgment
            alert.showAndWait();
            System.exit(0);
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        launch(args);
    }
}
