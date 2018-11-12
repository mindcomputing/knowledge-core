package sh.komet.gui.search.control;

import java.util.HashMap;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.manifold.Manifold;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampCoordinateImpl;
import sh.komet.gui.search.flwor.LetItemPanel;
import sh.komet.gui.search.flwor.LetItemsController;

/**
 *
 * @author aks8m
 */

public class LetPropertySheet {
    private static final Logger LOG = LogManager.getLogger();

    private final BorderPane propertySheetBorderPane = new BorderPane();
    private final ObservableList<PropertySheet.Item> items;
    private final Manifold manifoldForDisplay;
    private final MenuButton addLetClauseButton = new MenuButton("Add let clause...");
    private final ToolBar letToolbar = new ToolBar(addLetClauseButton);
    { 
        propertySheetBorderPane.setTop(letToolbar);
    }
    private LetItemsController letItemsController;
    private final HashMap<LetItemKey, LetItemPanel> letItemPanelMap = new HashMap();
    private final ObservableMap<LetItemKey, Object> letItemObjectMap = FXCollections.observableHashMap();
    
    private final ObservableList<LetItemKey> stampCoordinateKeys = FXCollections.observableArrayList();
    private final ObservableList<LetItemKey> languageCoordinateKeys = FXCollections.observableArrayList();
    
    public LetPropertySheet(Manifold manifold){
        this.manifoldForDisplay = manifold;
        items = FXCollections.observableArrayList();
        MenuItem addStampCoordinate = new MenuItem("Add stamp coordinate");
        addStampCoordinate.setOnAction(this::addStampCoordinate);
        addLetClauseButton.getItems().add(addStampCoordinate);

        MenuItem addLanguageCoordinate = new MenuItem("Add language coordinate");
        addLanguageCoordinate.setOnAction(this::addLanguageCoordinate);
        addLetClauseButton.getItems().add(addLanguageCoordinate);

        AnchorPane.setBottomAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setTopAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setLeftAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setRightAnchor(this.propertySheetBorderPane, 0.0);
        
        letItemObjectMap.addListener(this::letItemsChanged);
    }

    public ObservableList<LetItemKey> getStampCoordinateKeys() {
        return stampCoordinateKeys;
    }

    public ObservableList<LetItemKey> getLanguageCoordinateKeys() {
        return languageCoordinateKeys;
    }
    
    private void letItemsChanged(MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) {
        LetItemKey key = change.getKey();
        if (change.wasRemoved()) {
            stampCoordinateKeys.remove(key);
            languageCoordinateKeys.remove(key);
        }
        if (change.wasAdded()) {
          if (change.getValueAdded() instanceof StampCoordinate) {
              if (!stampCoordinateKeys.contains(key)) {
                  stampCoordinateKeys.add(key);
              }
          }
          if (change.getValueAdded() instanceof LanguageCoordinate) {
              if (!languageCoordinateKeys.contains(key)) {
                  languageCoordinateKeys.add(key);
              }
          }
        }
    }
    
    public void addLanguageCoordinate(LetItemKey newLetItem, LanguageCoordinate newLanguageCoordinate) {
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        ObservableLanguageCoordinate languageCoordinate;
        if (newLanguageCoordinate instanceof ObservableLanguageCoordinate) {
            languageCoordinate = (ObservableLanguageCoordinate) newLanguageCoordinate;
        } else {
            languageCoordinate = new ObservableLanguageCoordinateImpl(newLanguageCoordinate);
        }
        letItemObjectMap.put(newLetItem, languageCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), languageCoordinate);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());
        
        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }
    public void reset() {
        this.letItemPanelMap.clear();
        this.letItemObjectMap.clear();
        this.stampCoordinateKeys.clear();
        this.languageCoordinateKeys.clear();
        this.letItemsController.reset();
    }
    public void addItem(LetItemKey newLetItem, Object newObject) {
        if (newObject instanceof StampCoordinate) {
            addStampCoordinate(newLetItem, (StampCoordinate) newObject);
        } else if (newObject instanceof LanguageCoordinate) {
            addLanguageCoordinate(newLetItem, (LanguageCoordinate) newObject);
        } else {
            letItemObjectMap.put(newLetItem, newObject);
            LOG.warn("Can't create panel for " + newLetItem + ": " + newObject);
        }
    }
    
    private void addLanguageCoordinate(ActionEvent action) {
        int sequence = 1;
        String keyName = null;
        boolean unique = false;
        TRY_NEXT: while (!unique) {
            if (sequence > 1) {
                keyName = "[US, UK] English " + sequence++;
            } else {
                keyName = "[US, UK] English";
                sequence++;
            }
            for (LetItemKey key: letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }
        LetItemKey newLetItem = new LetItemKey(keyName);
        addLanguageCoordinate(newLetItem, this.manifoldForDisplay.getLanguageCoordinate().deepClone());
    }

    private void addStampCoordinate(ActionEvent action) {
        int sequence = 1;
        String keyName = "STAMP " + sequence;
        boolean unique = false;
        TRY_NEXT: while (!unique) {
            keyName = "STAMP " + sequence++;
            for (LetItemKey key: letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }
        
        LetItemKey newLetItem = new LetItemKey(keyName);
        ObservableStampCoordinate stampCoordinate = this.manifoldForDisplay.getStampCoordinate().deepClone();
        addStampCoordinate(newLetItem, stampCoordinate);
    }

    public void addStampCoordinate(LetItemKey newLetItem, StampCoordinate newStampCoordinate) {
        ObservableStampCoordinate stampCoordinate;
        if (newStampCoordinate instanceof ObservableStampCoordinate) {
            stampCoordinate = (ObservableStampCoordinate) newStampCoordinate;
        } else {
            stampCoordinate = new ObservableStampCoordinateImpl(newStampCoordinate);
        }
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        letItemObjectMap.put(newLetItem, stampCoordinate);

        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), stampCoordinate);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());
        
        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public ObservableMap<LetItemKey, Object> getLetItemObjectMap() {
        return letItemObjectMap;
    }

    public Node getNode() {
        return this.propertySheetBorderPane;
    }

    public void setLetItemsController(LetItemsController letItemsController) {
        this.propertySheetBorderPane.setCenter(letItemsController.getNode());
        this.letItemsController = letItemsController;
        this.letItemsController.getLetListViewletListView().getSelectionModel().getSelectedIndices().addListener(this::handleSelectionChange);
    }
    
    private void handleSelectionChange(ListChangeListener.Change<? extends Integer> c) {
        if (c.getList().isEmpty()) {
            letItemsController.getLetItemBorderPane().setCenter(null);
        } else {
            LetItemKey selectedLetItem = this.letItemsController.getLetListViewletListView().getItems().get(c.getList().get(0));
            Node letNode = letItemPanelMap.get(selectedLetItem).getNode();
            if (letNode != letItemsController.getLetItemBorderPane().getCenter()) {
                letItemsController.getLetItemBorderPane().setCenter(letNode);
            }
            
        }
    }
}
