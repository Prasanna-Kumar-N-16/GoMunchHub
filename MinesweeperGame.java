import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Random;

public class MinesweeperGame extends Application {
    private static final int CELL_SIZE = 30;
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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Minesweeper");

        // Display difficulty selection dialog
        DifficultySelectionDialog dialog = new DifficultySelectionDialog();
        dialog.showAndWait();

        if (dialog.getSelectedDifficulty() == null) {
            System.exit(0); // Exit the application if the user cancels the dialog
        }

        setGameParameters(dialog.getSelectedDifficulty());

        GridPane grid = createGameGrid();
        initializeGame();

        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.show();
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
    
                button.setOnAction(event -> handleButtonClick(finalRow, finalCol));
                grid.add(button, col, row);
                buttons[row][col] = button;
            }
        }
    
        return grid;
    }

    private void initializeGame() {
        placeMines();
        updateButtons();
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

    private void handleButtonClick(int row, int col) {
        if (isMine[row][col]) {
            showGameOverAlert();
        } else {
            revealCell(row, col);
            checkGameWin();
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
    }

    private void showGameOverAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over");
        alert.setContentText("You hit a mine! Game over.");
        alert.showAndWait();
        System.exit(0);
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
