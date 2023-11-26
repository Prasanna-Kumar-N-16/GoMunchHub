import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A dialog for selecting the difficulty level of the Minesweeper game.
 */
public class DifficultySelectionDialog {
    private Difficulty selectedDifficulty;

    /**
     * Constructor to initialize the selected difficulty as null.
     */
    public DifficultySelectionDialog() {
        selectedDifficulty = null;
    }

    /**
     * Shows the dialog and waits for user interaction.
     */
    public void showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Select Difficulty");

        VBox vBox = new VBox(25);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(25));

        Label label = new Label("Select Difficulty:");

        ToggleGroup toggleGroup = new ToggleGroup();

        RadioButton beginnerRadioButton = new RadioButton("Beginner");
        beginnerRadioButton.setToggleGroup(toggleGroup);
        // Set the selected difficulty when the Beginner radio button is clicked
        beginnerRadioButton.setOnAction(event -> selectedDifficulty = Difficulty.BEGINNER);

        RadioButton intermediateRadioButton = new RadioButton("Intermediate");
        intermediateRadioButton.setToggleGroup(toggleGroup);
        // Set the selected difficulty when the Intermediate radio button is clicked
        intermediateRadioButton.setOnAction(event -> selectedDifficulty = Difficulty.INTERMEDIATE);

        RadioButton advancedRadioButton = new RadioButton("Advanced");
        advancedRadioButton.setToggleGroup(toggleGroup);
        // Set the selected difficulty when the Advanced radio button is clicked
        advancedRadioButton.setOnAction(event -> selectedDifficulty = Difficulty.ADVANCED);

        Button startButton = new Button("Start");
        // Close the dialog when the Start button is clicked
        startButton.setOnAction(event -> dialogStage.close());

        // Add UI components to the VBox
        vBox.getChildren().addAll(label, beginnerRadioButton, intermediateRadioButton, advancedRadioButton, startButton);

        Scene scene = new Scene(vBox);

        dialogStage.setWidth(400);
        dialogStage.setHeight(400);

        dialogStage.setScene(scene);
        // Show the dialog and wait for it to be closed
        dialogStage.showAndWait();
    }

    /**
     * Gets the selected difficulty.
     *
     * @return The selected difficulty.
     */
    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
