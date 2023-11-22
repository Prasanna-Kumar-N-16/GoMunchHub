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
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.awt.Toolkit;

import java.util.Random;
import javafx.util.Duration;

public class MinesweeperGame extends Application {
    private static final int CELL_SIZE = 32;
    private static final int BEGINNER_ROWS = 6;
    private static final int BEGINNER_COLS = 9;
    private static final int BEGINNER_MINES = 11;
    private static final int INTERMEDIATE_ROWS = 12;
    private static final int INTERMEDIATE_COLS = 18;
    private static final int INTERMEDIATE_MINES = 36;
    private static final int ADVANCED_ROWS = 21;
    private static final int ADVANCED_COLS = 26;
    private static final int ADVANCED_MINES = 92;

    private int rows;
    private int cols;
    private int mines;
    private Button[][] buttons;
    private boolean[][] isMine;
    private boolean[][] isRevealed;

    private Timeline timer;
    private int elapsedTimeSeconds;
    private Label timerLabel;

    private Label minesLabel;

    private Difficulty updatedDifficulty;

    private int flagsPlaced;

    // Set the time limits in seconds for each difficulty level
    private static final int BEGINNER_TIME_LIMIT = 60;
    private static final int INTERMEDIATE_TIME_LIMIT = 180;
    private static final int ADVANCED_TIME_LIMIT = 660;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Minesweeper");

        // Display difficulty selection dialog
        DifficultySelectionDialog dialog = new DifficultySelectionDialog();
        dialog.showAndWait();

        if (dialog.getSelectedDifficulty() == null) {
            System.exit(0); // Exit the application if the user cancels the dialog
        }

        updatedDifficulty = dialog.getSelectedDifficulty();
        setGameParameters(dialog.getSelectedDifficulty());

        GridPane grid = createGameGrid();

        timerLabel = new Label("Time: 0 seconds");
        grid.add(timerLabel, cols, 0);

        minesLabel = new Label(" Flags left:  0 ");
        grid.add(minesLabel, cols, 1);

        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeGame();
        initializeTimer();
    }

    private void initializeTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), this::updateTimer));
        timer.setCycleCount(Timeline.INDEFINITE);
        elapsedTimeSeconds = 0;
        timer.play();
    }

    private void updateTimer(ActionEvent event) {
        elapsedTimeSeconds++;

        // Check if the time limit has been reached for the current difficulty
        int timeLimit = getTimeLimit();
        if (elapsedTimeSeconds >= timeLimit) {
            timer.stop();
            showAlert(false);
        }

        timerLabel.setText("Time: " + elapsedTimeSeconds + " seconds");
    }

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

    private GridPane createGameGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);

        buttons = new Button[rows][cols];
        isMine = new boolean[rows][cols];
        isRevealed = new boolean[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button button = new Button();
                button.setMinSize(CELL_SIZE, CELL_SIZE);

                // Create effectively final variables
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

    private void initializeGame() {
        placeMines();
        updateButtons();
        flagsPlaced = 0; // Initialize the number of flags placed
    }

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

    private void placeMines() {
        Random random = new Random();

        for (int i = 0; i < mines; i++) {
            int row, col;
            do {
                row = random.nextInt(rows);
                col = random.nextInt(cols);
            } while (isMine[row][col]);
            isMine[row][col] = true;
        }
    }

    private void handleButtonClick(int row, int col, MouseButton button) {
        if (button == MouseButton.SECONDARY) {
            handleFlagClick(row, col);
        } else if (isMine[row][col]) {
            showAlert(true);
        } else {
            revealCell(row, col);
            checkGameWin();
        }
    }

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

    private void revealCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || isRevealed[row][col]) {
            return;
        }

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

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < cols && isMine[i][j]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void updateButtons() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (isRevealed[row][col]) {
                    buttons[row][col].setDisable(true);
                    int mineCount = countAdjacentMines(row, col);
                    if (mineCount > 0) {
                        buttons[row][col].setText(String.valueOf(mineCount));
                    }
                }
            }
        }

        // Display the number of flags left
        updateFlagsLeftDisplay();
    }

    private void updateFlagsLeftDisplay() {
        int flagsLeft = mines - flagsPlaced;
        minesLabel.setText(" Flags left: " + flagsLeft);
    }

    private void showAlert(boolean isGameOver) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        if (isGameOver) {
            alert.setTitle("Game Over");
            alert.setHeaderText("Game Over");
            alert.setContentText("You hit a mine! Game over.");
        } else {
            alert.setTitle("Time's Up");
            alert.setHeaderText("Game Over");
            alert.setContentText("Your time is up! Game over.");
        }

        Toolkit.getDefaultToolkit().beep();
 

        // Cover the mines with an "X" flag
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if(!isMine[row][col] && buttons[row][col].getText().equals("F")){
                     // Mark cells with a red "X" where the player marked a flag but there is no mine
                    buttons[row][col].setText("X");
                    buttons[row][col].setStyle("-fx-text-fill: red"); // Set text color to red
                }else if (isMine[row][col] && !buttons[row][col].getText().equals("F")) {
                    buttons[row][col].setText("X");
                } 
            }
        }

        // Create a sequential transition for the explosion
        SequentialTransition explosionTransition = new SequentialTransition();

        // Play explosion animation for mine buttons one after another
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (isMine[row][col] && !buttons[row][col].getText().equals("F")) {
                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), buttons[row][col]);
                    scaleIn.setToX(1.5);
                    scaleIn.setToY(1.5);

                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), buttons[row][col]);
                    scaleOut.setToX(1.0);
                    scaleOut.setToY(1.0);

                    SequentialTransition mineExplosion = new SequentialTransition(scaleIn, scaleOut);
                    explosionTransition.getChildren().add(mineExplosion);
                }
            }
        }

        // Introduce a pause before showing the game-over alert
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5)); // Adjust the duration as needed
        pause.setOnFinished(event -> {
            explosionTransition.setOnFinished(e -> {
                Platform.runLater(() -> {
                    alert.showAndWait();
                    System.exit(0);
                });
            });
            explosionTransition.play(); // Start the explosion animation
        });

        pause.play();
    }

    private void checkGameWin() {
        int unrevealedCells = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!isRevealed[row][col] && !isMine[row][col]) {
                    unrevealedCells++;
                }
            }
        }

        if (unrevealedCells == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Congratulations");
            alert.setHeaderText("Congratulations");
            alert.setContentText("You've cleared the board! You win!");
            alert.showAndWait();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
