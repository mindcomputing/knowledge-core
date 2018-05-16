package sh.isaac.komet.preferencesfx.view;

import sh.isaac.komet.preferencesfx.model.Category;
import sh.isaac.komet.preferencesfx.model.PreferencesFxModel;
import sh.isaac.komet.preferencesfx.util.SearchHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.fx.ui.controls.tree.FilterableTreeItem;

/**
 * Contains presenter logic of the {@link NavigationView}.
 *
 * @author François Martin
 * @author Marco Sanfratello
 */
public class NavigationPresenter implements Presenter {
  private static final Logger LOGGER =
      LogManager.getLogger(NavigationPresenter.class.getName());

  private PreferencesFxModel model;
  private SearchHandler searchHandler;
  private NavigationView navigationView;

  private HashMap<Category, FilterableTreeItem<Category>> categoryTreeItemMap = new HashMap<>();

  /**
   * Constructs a new presenter for the {@link NavigationView}.
   *
   * @param model          the model of PreferencesFX
   * @param navigationView corresponding view to this presenter
   */
  public NavigationPresenter(PreferencesFxModel model, NavigationView navigationView) {
    this.model = model;
    searchHandler = model.getSearchHandler();
    this.navigationView = navigationView;
    init();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeViewParts() {
    initializeTreeItems();
    setSelectedCategory(model.getDisplayedCategory());
    searchHandler.init(
        model,
        navigationView.searchFld.textProperty(),
        navigationView.rootItem.predicateProperty()
    );
    setupCellValueFactory();
  }

  private void initializeTreeItems() {
    addRecursive(navigationView.rootItem, model.getCategories());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setupEventHandlers() {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setupValueChangedListeners() {
    // Update displayed category upon selection
    navigationView.treeView.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldTreeItem, newTreeItem) -> {
          if (newTreeItem != null) {
            model.setDisplayedCategory(newTreeItem.getValue());
          }
        }
    );

    // Update selected category upon search changes
    searchHandler.categoryMatchProperty().addListener(
        (observable, oldCategory, newCategory) -> {
          LOGGER.trace(
              String.format("Category match changed! oldCategory: %s newCategory: %s selecting:"
                  + " %s", oldCategory, newCategory, categoryTreeItemMap.get(newCategory)));
          navigationView.treeView.getSelectionModel().select(
              categoryTreeItemMap.get(newCategory)
          );
        }
    );

    // Listens when a breadcrumb was clicked or the last selected category is loaded
    model.displayedCategoryProperty().addListener((observable, oldValue, newValue) ->
        setSelectedCategory(newValue)
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setupBindings() {

  }

  private void addRecursive(FilterableTreeItem treeItem, List<Category> categories) {
    for (Category category : categories) {
      FilterableTreeItem<Category> item = new FilterableTreeItem<>(category);
      // If there are subcategries, add them recursively.
      if (!Objects.equals(category.getChildren(), null)) {
        addRecursive(item, category.getChildren());
      }
      treeItem.getInternalChildren().add(item);
      categoryTreeItemMap.put(category, item);
    }
  }

  /**
   * Retrieves the currently selected category in the TreeSearchView.
   */
  public Category getSelectedCategory() {
    TreeItem<Category> selectedTreeItem =
        navigationView.treeView.getSelectionModel().getSelectedItem();
    if (selectedTreeItem != null) {
      return navigationView.treeView.getSelectionModel().getSelectedItem().getValue();
    }
    return null;
  }

  /**
   * Selects the given category in the NavigationView.
   *
   * @param category to be selected
   */
  public void setSelectedCategory(Category category) {
    navigationView.setSelectedItem(categoryTreeItemMap.get(category));
  }

  /**
   * Makes the TreeItems' text update when the description of a Category changes (due to i18n).
   */
  public void setupCellValueFactory() {
    navigationView.treeView.setCellFactory(param -> new TreeCell<Category>() {
      @Override
      protected void updateItem(Category category, boolean empty) {
        super.updateItem(category, empty);
        textProperty().unbind();
        if (empty || category == null) {
          setText(null);
          setGraphic(null);
        } else {
          textProperty().bind(category.descriptionProperty());
        }
      }
    });
  }

}
