package sh.isaac.komet.preferencesfx.history.view;

import sh.isaac.komet.preferencesfx.history.Change;
import sh.isaac.komet.preferencesfx.history.History;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 * Shows a dialog with a table of {@link Change} made in the {@link History}.
 *
 * @author François Martin
 * @author Marco Sanfratello
 * @apiNote Used for debugging purposes only.
 */
public class HistoryDialog extends DialogPane {

  private History history;
  private HistoryTable historyTable;
  private VBox historyBox;
  private Button undoAllBtn;
  private Button redoAllBtn;

  private Dialog dialog = new Dialog();

  /**
   * Initializes a new dialog for debugging the history.
   *
   * @param history the history which should be shown in the table
   */
  public HistoryDialog(History history) {
    this.history = history;
    historyTable = new HistoryTable(history.getChanges());
    setupParts();
    layoutForm();
    setupClose();
    setupBindings();
    setupEventHandler();
    dialog.initModality(Modality.NONE);
    dialog.show();
  }

  private void setupParts() {
    historyBox = new VBox();
    undoAllBtn = new Button("Undo All");
    redoAllBtn = new Button("Redo All");
    historyBox.getChildren().addAll(historyTable, undoAllBtn, redoAllBtn);
  }

  private void layoutForm() {
    dialog.setTitle("PreferencesFX Undo / Redo History Debug View");
    dialog.setResizable(true);

    dialog.setDialogPane(this);
    setContent(historyBox);
  }

  private void setupBindings() {
    historyTable.addSelectionBinding(history.currentChangeProperty());
  }

  private void setupEventHandler() {
    undoAllBtn.setOnAction(event -> history.undoAll());
    redoAllBtn.setOnAction(event -> history.redoAll());
  }

  /**
   * Instantiates a close button and makes it invisible.
   * Dialog requires at least one button to close the dialog window.
   *
   * @see <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Dialog.html">
   * javafx.scene.control.Dialog</a>
   * Chapter: Dialog Closing Rules
   */
  private void setupClose() {
    this.getButtonTypes().add(ButtonType.CLOSE);
    Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
    closeButton.managedProperty().bind(closeButton.visibleProperty());
    closeButton.setVisible(false);
  }

}
