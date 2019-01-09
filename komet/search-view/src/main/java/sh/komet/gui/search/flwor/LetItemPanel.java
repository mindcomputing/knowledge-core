/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.search.flwor;

import sh.isaac.api.query.LetItemKey;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import org.controlsfx.control.PropertySheet;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.observable.ObservableConceptProxy;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.api.query.ManifoldCoordinateForQuery;
import sh.komet.gui.control.PropertySheetItemDateTimeWrapper;
import sh.komet.gui.control.PropertySheetItemObjectListWrapper;
import sh.komet.gui.control.PropertySheetStampPrecedenceWrapper;
import sh.komet.gui.control.PropertySheetStatusSetWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptSetWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class LetItemPanel {
    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.LET_ITEM_KEY____SOLOR.toExternalString());
    
    PropertySheet sheet = new PropertySheet();
    {
        sheet.setMode(PropertySheet.Mode.NAME);
        sheet.setSearchBoxVisible(false);
        sheet.setModeSwitcherVisible(false);
    }

    private final ListView<LetItemKey> letListView;
    
    private final Manifold manifold;
    
    private final LetItemKey letItemKey;
    
    private final Observable letItem;
    
    private final LetPropertySheet letPropertySheet;

    public LetItemPanel(Manifold manifold, LetItemKey letItemKey, 
            ListView<LetItemKey> letListViewletListView, Observable letItem, 
            LetPropertySheet letPropertySheet) {
        this.manifold = manifold;
        this.sheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        this.sheet.getItems().add(new PropertySheetTextWrapper(manifold, nameProperty));
        this.letListView = letListViewletListView;
        this.letItem = letItem;
        this.letItemKey = letItemKey;
        this.letPropertySheet = letPropertySheet;
        this.nameProperty.setValue(this.letItemKey.getItemName());
        this.nameProperty.addListener((observable, oldValue, newValue) -> {
            this.letItemKey.setItemName(newValue);
            this.letListView.getItems().set(this.letListView.getItems().indexOf(letItemKey), letItemKey);
            this.letListView.getSelectionModel().select(letItemKey);
        });
        if (letItem instanceof ObservableStampCoordinate) {
            setupStampCoordinate((ObservableStampCoordinate) letItem);
        }
        if (letItem instanceof ObservableLanguageCoordinate) {
            setupLanguageCoordinate((ObservableLanguageCoordinate) letItem);
        }
        if (letItem instanceof ObservableLogicCoordinate) {
            setupLogicCoordinate((ObservableLogicCoordinate) letItem);
        }
        if (letItem instanceof ManifoldCoordinateForQuery) {
            setupManifoldCoordinate((ManifoldCoordinateForQuery) letItem);
        }
        if (letItem instanceof ObservableConceptProxy) {
            setupConceptProxy((ObservableConceptProxy) letItem);
        }
        if (letItem instanceof SimpleStringProperty) {
            setupString((SimpleStringProperty) letItem);
        }
    }
    private void setupString(SimpleStringProperty stringItem) {
        this.sheet.getItems().add(new PropertySheetTextWrapper(manifold, stringItem));
    }
    
    
    private void setupConceptProxy(ObservableConceptProxy conceptProxyItem) {
        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, conceptProxyItem));
    }
    
    public Node getNode() {
        return sheet;
    }

    private void setupManifoldCoordinate(ManifoldCoordinateForQuery setupManifoldCoordinate) {

        ObservableList<PremiseType> premiseTypes = 
                FXCollections.observableArrayList(PremiseType.INFERRED, PremiseType.STATED);
        this.sheet.getItems().add(new PropertySheetItemObjectListWrapper("Premise type", setupManifoldCoordinate.premiseTypeProperty(), 
                premiseTypes));
        this.sheet.getItems().add(new PropertySheetItemObjectListWrapper("Stamp coordinate", setupManifoldCoordinate.stampCoordinateKey(), letPropertySheet.getStampCoordinateKeys()));
        this.sheet.getItems().add(new PropertySheetItemObjectListWrapper("Language coordinate", setupManifoldCoordinate.languageCoordinateKeyProperty(), letPropertySheet.getLanguageCoordinateKeys()));
        this.sheet.getItems().add(new PropertySheetItemObjectListWrapper("Logic coordinate", setupManifoldCoordinate.logicCoordinateKeyProperty(), letPropertySheet.getLogicCoordinateKeys()));
    }
    

    private void setupLanguageCoordinate(ObservableLanguageCoordinate languageCoordinateItem) {

        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, languageCoordinateItem.languageConceptProperty(), 
                TermAux.ENGLISH_LANGUAGE.getNid(),TermAux.SPANISH_LANGUAGE.getNid()));
        this.sheet.getItems().add(new PropertySheetConceptListWrapper(manifold, languageCoordinateItem.dialectAssemblagePreferenceListProperty()));
        this.sheet.getItems().add(new PropertySheetConceptListWrapper(manifold, languageCoordinateItem.descriptionTypePreferenceListProperty()));
    }
    
    private void setupStampCoordinate(ObservableStampCoordinate stampCoordinateItem) {
        this.sheet.getItems().add(new PropertySheetStatusSetWrapper(manifold, stampCoordinateItem.allowedStatesProperty()));
        ObservableStampPosition stampPosition = stampCoordinateItem.stampPositionProperty().get();
        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, "Path", 
                stampPosition.stampPathConceptSpecificationProperty(), TermAux.DEVELOPMENT_PATH, TermAux.MASTER_PATH));
        this.sheet.getItems().add(new PropertySheetItemDateTimeWrapper("Time", stampPosition.timeProperty()));
        this.sheet.getItems().add(new PropertySheetStampPrecedenceWrapper("Precedence", stampCoordinateItem.stampPrecedenceProperty()));
        this.sheet.getItems().add(new PropertySheetConceptSetWrapper(manifold, stampCoordinateItem.moduleSpecificationsProperty()));
        this.sheet.getItems().add(new PropertySheetConceptListWrapper(manifold, stampCoordinateItem.modulePreferenceListForVersionsProperty()));
    }
    
    private void setupLogicCoordinate(ObservableLogicCoordinate logicCoordinateItem) {

        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, "Logic profile", logicCoordinateItem.descriptionLogicProfileProperty(), 
                new ConceptSpecification[] { TermAux.EL_PLUS_PLUS_LOGIC_PROFILE }));
        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, "Classifier", logicCoordinateItem.classifierProperty(), 
                new ConceptSpecification[] { TermAux.SNOROCKET_CLASSIFIER }));
        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, "Concepts to classify", logicCoordinateItem.conceptAssemblageProperty(), 
                new ConceptSpecification[] { TermAux.SOLOR_CONCEPT_ASSEMBLAGE }));
        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, "Stated assemblage", logicCoordinateItem.statedAssemblageProperty(), 
                new ConceptSpecification[] { TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE }));
        this.sheet.getItems().add(new PropertySheetItemConceptWrapper(manifold, "Inferred assemblage", logicCoordinateItem.inferredAssemblageProperty(), 
                new ConceptSpecification[] { TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE }));
    }
}
