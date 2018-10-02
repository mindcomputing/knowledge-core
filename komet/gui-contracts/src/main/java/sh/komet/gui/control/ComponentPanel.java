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
package sh.komet.gui.control;

//~--- non-JDK imports --------------------------------------------------------
import java.util.Optional;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;
import static sh.komet.gui.util.FxUtils.setupHeaderPanel;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public final class ComponentPanel
        extends BadgedVersionPanel {

    private int extraGridRows = 1;
    private final CategorizedVersions<ObservableCategorizedVersion> categorizedVersions;
    private final AnchorPane extensionHeaderPanel = setupHeaderPanel("Extensions:");
    private final AnchorPane versionHeaderPanel = setupHeaderPanel("Change history:", "Revert");

    //~--- constructors --------------------------------------------------------
    public ComponentPanel(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
            OpenIntIntHashMap stampOrderHashMap) {
        super(manifold, categorizedVersion, stampOrderHashMap);

        this.categorizedVersions = categorizedVersion.getCategorizedVersions();

        // gridpane.gridLinesVisibleProperty().set(true);
        this.getStyleClass()
                .add(StyleClasses.COMPONENT_PANEL.toString());

        ObservableVersion observableVersion = getCategorizedVersion().getObservableVersion();

        isContradiction.set(this.categorizedVersions.getLatestVersion()
                .isContradicted());

        if (!this.categorizedVersions.getUncommittedVersions().isEmpty()) {
            if (this.categorizedVersions.getUncommittedVersions().size() > 1) {
                System.err.println("Error: can't handle more than one uncommitted version in this editor...");
            }
            ObservableCategorizedVersion uncommittedVersion = this.categorizedVersions.getUncommittedVersions().get(0);
            Optional<PropertySheetMenuItem> propertySheetMenuItem = uncommittedVersion.getUserObject(PROPERTY_SHEET_ATTACHMENT);
            if (propertySheetMenuItem.isPresent()) {
                this.addEditingPropertySheet(propertySheetMenuItem.get());
            } else {
//                if (uncommittedVersion.getAuthorNid() == ) {
//                    
//                }
                System.err.println("Error: No property sheet editor for this uncommitted version...\n       " + uncommittedVersion);
            }
        }

        if (this.categorizedVersions.getLatestVersion()
                .isContradicted()) {
            this.categorizedVersions.getLatestVersion()
                    .contradictions()
                    .forEach(
                            (contradiction) -> {
                                if (contradiction.getStampSequence() != -1) {
                                    versionPanels.add(new VersionPanel(manifold, contradiction, stampOrderHashMap));
                                }
                            });
        }

        this.categorizedVersions.getHistoricVersions()
                .forEach(
                        (historicVersion) -> {
                            if (historicVersion.getStampSequence() != -1) {
                                versionPanels.add(new VersionPanel(manifold, historicVersion, stampOrderHashMap));
                            }
                        });
        observableVersion.getChronology()
                .getObservableSemanticList()
                .forEach(
                        (osc) -> {
                            switch (osc.getVersionType()) {
                                case DESCRIPTION:
                                case LOGIC_GRAPH:
                                case RF2_RELATIONSHIP:
                                    break;  // Ignore, description and logic graph where already added as an independent panel

                                default:
                                    addChronology(osc, stampOrderHashMap);
                            }
                        });
        expandControl.setVisible(!versionPanels.isEmpty() || !extensionPanels.isEmpty());
    }

    public static boolean isSemanticTypeSupported(Chronology chronology) {
        return isSemanticTypeSupported(chronology.getVersionType());
    }
    public static boolean isSemanticTypeSupported(VersionType semanticType) {
        switch (semanticType) {
            case STRING:
            case COMPONENT_NID:
            case LOGIC_GRAPH:
            case LONG:
            case MEMBER:
            case CONCEPT:
            case DESCRIPTION:
            case Nid1_Int2:
                return true;

            case RF2_RELATIONSHIP:
                return false;

            default:
                //may consider supporting more types in the future. 
                return false;
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void addExtras() {
        switch (expandControl.getExpandAction()) {
            case HIDE_CHILDREN:
                if (!versionPanels.isEmpty()) {
                    gridpane.getChildren()
                            .remove(versionHeaderPanel);
                }
                versionPanels.forEach(
                        (panel) -> {
                            gridpane.getChildren()
                                    .remove(panel);
                        });
                if (!extensionPanels.isEmpty()) {
                    gridpane.getChildren()
                            .remove(extensionHeaderPanel);
                }
                extensionPanels.forEach(
                        (panel) -> {
                            gridpane.getChildren()
                                    .remove(panel);
                        });
                break;

            case SHOW_CHILDREN:
                if (!versionPanels.isEmpty()) {
                    addPanel(versionHeaderPanel);
                }
                versionPanels.forEach(this::addPanel);
                if (!extensionPanels.isEmpty()) {
                    addPanel(extensionHeaderPanel);
                }
                extensionPanels.forEach(this::addPanel);
                break;

            default:
                throw new UnsupportedOperationException("am Can't handle: " + expandControl.getExpandAction());
        }
    }

    private void addChronology(ObservableChronology observableChronology, OpenIntIntHashMap stampOrderHashMap) {
        if (isSemanticTypeSupported(observableChronology)) {
            CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
                    = observableChronology.getCategorizedVersions(
                            getManifold());

            if (oscCategorizedVersions.getLatestVersion()
                    .isPresent()) {
                ComponentPanel newPanel = new ComponentPanel(getManifold(),
                        oscCategorizedVersions.getLatestVersion().get(), stampOrderHashMap);

                extensionPanels.add(newPanel);
            } else if (!oscCategorizedVersions.getUncommittedVersions().isEmpty()) {
                ComponentPanel newPanel = new ComponentPanel(getManifold(),
                        oscCategorizedVersions.getUncommittedVersions().get(0), stampOrderHashMap);
                extensionPanels.add(newPanel);
            }
        }
    }

    private void addPanel(Node panel) {
        extraGridRows++;
        gridpane.getChildren()
                .remove(panel);
        GridPane.setConstraints(
                panel,
                0,
                getRows() + extraGridRows,
                getColumns(),
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.ALWAYS,
                Priority.NEVER,
                new Insets(2));
        gridpane.getChildren()
                .add(panel);
    }

    @Override
    protected boolean isLatestPanel() {
        return true;
    }
}
