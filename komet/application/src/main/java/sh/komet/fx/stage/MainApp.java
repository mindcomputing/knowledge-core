/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.komet.fx.stage;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.javafx.application.PlatformImpl;
import de.codecentric.centerdevice.MenuToolkit;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import java.util.Arrays;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.preferences.ApplicationPreferences;
import sh.isaac.komet.statement.StatementView;
import sh.isaac.komet.statement.StatementViewController;
import sh.isaac.model.statement.ClinicalStatementImpl;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------
public class MainApp
        extends Application {
// TODO add TaskProgressView
// http://dlsc.com/2014/10/13/new-custom-control-taskprogressview/
// http://fxexperience.com/controlsfx/features/   

    public static final String SPLASH_IMAGE = "prism-splash.png";
    protected static final Logger LOG = LogManager.getLogger();
    private static Stage primaryStage;

    //~--- methods -------------------------------------------------------------
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    // Create drop label for identified components
    // Create walker panel
    // grow & shrink icons for tabs & tab panels...
    // for each tab group, add a + control to create new tabs...
    @Override
    public void start(Stage stage)
            throws Exception {
        MainApp.primaryStage = stage;
        //stage.initStyle(StageStyle.UTILITY);
        // TODO have SvgImageLoaderFactory autoinstall as part of a HK2 service.
        LOG.info("Startup memory info: "
                + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString());

        SvgImageLoaderFactory.install();
        LookupService.startupPreferenceProvider();

        Get.configurationService().setSingleUserMode(true);  //TODO eventually, this needs to be replaced with a proper user identifier
        Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
        Get.configurationService().getGlobalDatastoreConfiguration().setMemoryConfiguration(MemoryConfiguration.ALL_CHRONICLES_IN_MEMORY);

        //TODO this will likely go away, at some point...
//        DirectImporter.importDynamic = true;
        LookupService.startupIsaac();

        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
            System.out.println("Beta features enabled");
        }

        if (Get.metadataService()
                .wasMetadataImported()) {
            final StampCoordinate stampCoordinate = Get.coordinateFactory()
                    .createDevelopmentLatestStampCoordinate();
            final LogicCoordinate logicCoordinate = Get.coordinateFactory()
                    .createStandardElProfileLogicCoordinate();
            final EditCoordinate editCoordinate = Get.coordinateFactory()
                    .createClassifierSolorOverlayEditCoordinate();
            final ClassifierService logicService = Get.logicService()
                    .getClassifierService(
                            stampCoordinate,
                            logicCoordinate,
                            editCoordinate);
            final Task<ClassifierResults> classifyTask = logicService.classify();
            final ClassifierResults classifierResults = classifyTask.get();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
        Parent root = loader.load();
        KometStageController controller = loader.getController();

        root.setId(UUID.randomUUID()
                .toString());

        stage.setTitle("Viewer");

        // Get the toolkit
        MenuToolkit tk = MenuToolkit.toolkit();  //Note, this only works on Mac....
        MenuBar mb = new MenuBar();

        for (AppMenu ap : AppMenu.values()) {
            if (ap == AppMenu.NEW_WINDOW) {
                continue;
            }
            if (!FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                if (ap == AppMenu.EDIT) {
                    continue;
                }
                if (ap == AppMenu.FILE) {
                    continue;
                }
                if (ap == AppMenu.TOOLS) {
                    continue;
                }
            }
            mb.getMenus().add(ap.getMenu());

            for (MenuProvider mp : LookupService.get().getAllServices(MenuProvider.class)) {
                if (mp.getParentMenus().contains(ap)) {
                    for (MenuItem mi : mp.getMenuItems(ap, primaryStage.getOwner())) {
                        ap.getMenu().getItems().add(mi);
                    }
                }
            }
            ap.getMenu().getItems().sort((MenuItem o1, MenuItem o2) -> o1.getText().compareTo(o2.getText()));

            switch (ap) {
                case APP:
                    if (tk != null) {
                        MenuItem aboutItem = new MenuItem("About...");
                        aboutItem.setOnAction(this::handleAbout);
                        ap.getMenu().getItems().add(aboutItem);
                        ap.getMenu().getItems().add(new SeparatorMenuItem());
                    }
                    if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                        MenuItem prefsItem = new MenuItem("Preferences...");
                        //TODO TEMP to do  something. Need to make it do something better. 
                        prefsItem.setOnAction(this::handlePrefs);
                        ap.getMenu().getItems().add(prefsItem);
                        ap.getMenu().getItems().add(new SeparatorMenuItem());
                    }
                    if (tk == null) {
                        MenuItem quitItem = new MenuItem("Quit");
                        quitItem.setOnAction(this::close);
                        ap.getMenu().getItems().add(quitItem);
                    }
                    break;
                case WINDOW:
                    Menu newWindowMenu = AppMenu.NEW_WINDOW.getMenu();
                    if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
                        MenuItem newStatementWindowItem = new MenuItem("Statement window");
                        newStatementWindowItem.setOnAction(this::newStatement);
                        newWindowMenu.getItems().addAll(newStatementWindowItem);
                        for (MenuProvider mp : LookupService.get().getAllServices(MenuProvider.class)) {
                            if (mp.getParentMenus().contains(AppMenu.NEW_WINDOW)) {
                                newWindowMenu.getItems().addAll(Arrays.asList(mp.getMenuItems(AppMenu.NEW_WINDOW, primaryStage.getOwner())));
                            }
                        }
                    }
                    MenuItem newKometWindowItem = new MenuItem("Viewer window");
                    newKometWindowItem.setOnAction(this::newViewer);
                    newWindowMenu.getItems().addAll(newKometWindowItem);
                    AppMenu.WINDOW.getMenu().getItems().add(newWindowMenu);
                    AppMenu.NEW_WINDOW.getMenu().getItems().sort((o1, o2) -> {
                        return o1.getText().compareTo(o2.getText()); 
                    });
                    break;
                case HELP:
                    if (tk == null) {
                        MenuItem aboutItem = new MenuItem("About...");
                        aboutItem.setOnAction(this::handleAbout);
                        ap.getMenu().getItems().add(aboutItem);
                    }

                    break;
                default:
                    break;
            }
        }

        if (tk != null) {
            // this is used on Mac... I don't think its uses anywhere else... 
            // Dan notes, code is rather confusing, and I can't test... it was making both an appMenu and a defaultApplicationMenu
            //but not being consistent about things, no idea which was actually being used on mac.
            // TBD: services menu
            tk.setForceQuitOnCmdQ(false);
            tk.setApplicationMenu(AppMenu.APP.getMenu());

            AppMenu.APP.getMenu().getItems().addAll(tk.createHideMenuItem(AppMenu.APP.getMenu().getText()), tk.createHideOthersMenuItem(),
                    tk.createUnhideAllMenuItem(), new SeparatorMenuItem(), tk.createQuitMenuItem(AppMenu.APP.getMenu().getText()));

            AppMenu.WINDOW.getMenu().getItems().addAll(new SeparatorMenuItem(),
                    tk.createMinimizeMenuItem(),
                    tk.createZoomMenuItem(), tk.createCycleWindowsItem(),
                    new SeparatorMenuItem(), tk.createBringAllToFrontItem());

            tk.autoAddWindowMenuItems(AppMenu.WINDOW.getMenu());
            tk.setGlobalMenuBar(mb);
        } else {
            //And for everyone else....
            BorderPane wrappingPane = new BorderPane(root);
            wrappingPane.setTop(mb);
            root = wrappingPane;
            stage.setHeight(stage.getHeight() + 20);
        }

        com.sun.glass.ui.Application.GetApplication().setEventHandler(
                new com.sun.glass.ui.Application.EventHandler() {
            @Override
            public void handleQuitAction(com.sun.glass.ui.Application app, long time) {
                shutdown();
            }

            @Override
            public boolean handleThemeChanged(String themeName) {
                return PlatformImpl.setAccessibilityTheme(themeName);
            }
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.ico")));
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.png")));

        // GraphController.setSceneForControllers(scene);
        scene.getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        scene.getStylesheets()
                .add(Iconography.getStyleSheetStringUrl());
        FxGet.statusMessageService()
                .addScene(scene, controller::reportStatus);
        stage.show();
        stage.setOnCloseRequest(MenuProvider::handleCloseRequest);

        // SNAPSHOT
        // Chronology
        // Reflector
        //
        // Logic, Language, Dialect, Chronology,
        // LILAC Reflector (LOGIC,
        // COLLD Reflector: Chronology of Logic, Language, and Dialect : COLLAD
        // COLLDAE Chronology of Logic, Langugage, Dialect, and Extension
        // CHILLDE
        // Knowledge, Language, Dialect, Chronology
        // KOLDAC
    }

    private void close(ActionEvent event) {
        event.consume();
        shutdown();
    }

    private void newStatement(ActionEvent event) {
        Manifold statementManifold = Manifold.make(ManifoldGroup.CLINICAL_STATEMENT);
        StatementViewController statementController = StatementView.show(statementManifold,
                "Clinical statement " + MenuProvider.WINDOW_SEQUENCE.getAndIncrement(), MenuProvider::handleCloseRequest);
        statementController.setClinicalStatement(new ClinicalStatementImpl(statementManifold));
        statementController.getClinicalStatement().setManifold(statementManifold);
        MenuProvider.WINDOW_COUNT.incrementAndGet();
    }

    private void newViewer(ActionEvent event) {

        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
            Parent root = loader.load();
            KometStageController controller = loader.getController();

            root.setId(UUID.randomUUID()
                    .toString());

            if (MenuProvider.WINDOW_SEQUENCE.get() > 1) {
                stage.setTitle("Viewer " + MenuProvider.WINDOW_SEQUENCE.incrementAndGet());
            } else {
                stage.setTitle("Viewer");
            }

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.ico")));
            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icons/KOMET.png")));

            // GraphController.setSceneForControllers(scene);
            scene.getStylesheets()
                    .add(FxGet.fxConfiguration().getUserCSSURL().toString());
            scene.getStylesheets()
                    .add(Iconography.getStyleSheetStringUrl());
            FxGet.statusMessageService()
                    .addScene(scene, controller::reportStatus);
            stage.show();
            //Dan notes, this seems like a really bad idea on an auxiliary window.
            //KEC: Yes, logic updated to count windows, and only close when just one is left... 
            stage.setOnCloseRequest(MenuProvider::handleCloseRequest);
            MenuProvider.WINDOW_COUNT.incrementAndGet();

            //TODO Also, this window has no menus...
        } catch (IOException ex) {
            FxGet.dialogs().showErrorDialog("Error opening new KOMET window.", ex);
        }
    }

    private void handlePrefs(ActionEvent event) {
        FxGet.kometPreferences().showPreferences("ISAAC's KOMET Preferences", FxGet.applicationNode(ApplicationPreferences.class));
    }

    private void handleAbout(ActionEvent event) {
        event.consume();
        System.out.println("Handle about...");
        //create stage which has set stage style transparent
        final Stage stage = new Stage(StageStyle.TRANSPARENT);

        //create root node of scene, i.e. group
        Group rootGroup = new Group();

        //create scene with set width, height and color
        Scene scene = new Scene(rootGroup, 806, 675, Color.TRANSPARENT);

        //set scene to stage
        stage.setScene(scene);

        //center stage on screen
        stage.centerOnScreen();
        Image image = new Image(MainApp.class.getResourceAsStream("/images/about@2x.png"));
        ImageView aboutView = new ImageView(image);

        aboutView.setFitHeight(675);
        aboutView.setPreserveRatio(true);
        aboutView.setSmooth(true);
        aboutView.setCache(true);
        rootGroup.getChildren().add(aboutView);
        //show the stage
        stage.show();

        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == false) {
                stage.close();
            }
        });
    }

    protected void shutdown() {
        Get.applicationStates().remove(ApplicationStates.RUNNING);
        Get.applicationStates().add(ApplicationStates.STOPPING);
        Thread shutdownThread = new Thread(() -> {  //Can't use the thread poool for this, because shutdown 
            //system stops the thread pool, which messes up the shutdown sequence.
            LookupService.shutdownSystem();
            Platform.runLater(() -> {
                try {
                    Platform.exit();
                    System.exit(0);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
        }, "shutdown-thread");
        shutdownThread.setDaemon(true);
        shutdownThread.start();
    }

}
