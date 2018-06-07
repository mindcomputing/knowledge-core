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



package sh.komet.gui.manifold;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import javafx.scene.Node;
import javafx.scene.control.Label;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinateProxy;
import sh.isaac.api.coordinate.LogicCoordinateProxy;
import sh.isaac.api.coordinate.ManifoldCoordinateProxy;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampCoordinateProxy;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.EditInFlight;

//~--- classes ----------------------------------------------------------------

/**
 * Manifold: Uniting various features, in this case an object that contains a set of coordinates and selections that
 * enable coordinated activities across a set of processes or graphical panels. In geometry, a coordinate system is a
 * system which uses one or more numbers, or coordinates, to uniquely determine the position of a point or other
 * geometric element on a manifold such as Euclidean space. ISAAC uses a number of coordinates, language coordinates,
 * stamp coordinates, edit coordinates to specify how a function should behave, in addition to current state information
 * such as a current position (selection). We use Manifold here to prevent confusion with other types of context,
 * context menus, etc.
 *
 * TODO: Move to general API when better refined.
 * TODO: Handle clearing the concept snapshot if any dependent fields are invalidated.  
 *
 * @author kec
 */
public class Manifold
         implements StampCoordinateProxy, LanguageCoordinateProxy, LogicCoordinateProxy, ManifoldCoordinateProxy,
                    ChangeListener<ConceptSpecification> {
   private static final WeakHashMap<Manifold, Object>              MANIFOLD_CHANGE_LISTENERS = new WeakHashMap<>();

   private static final HashMap<String, Supplier<Node>>            ICONOGRAPHIC_SUPPLIER     = new HashMap<>();
   private static final HashMap<String, ArrayDeque<HistoryRecord>> GROUP_HISTORY_MAP         = new HashMap<>();
   private static final ObservableSet<EditInFlight>                EDITS_IN_PROCESS = FXCollections.observableSet();

   public enum ManifoldGroup {UNLINKED("unlinked"), SEARCH("search"), TAXONOMY("taxonomy"), FLWOR("flwor"), CLINICAL_STATEMENT("statement");
      private String groupName;
      private ManifoldGroup(String name) {
         this.groupName = name;
      }
      
      public String getGroupName() {
         return groupName;
      }
      
   }
   
   //~--- static initializers -------------------------------------------------

   static {
      ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.UNLINKED.getGroupName(), () -> new Label());
      ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.SEARCH.getGroupName(), () -> Iconography.SIMPLE_SEARCH.getIconographic());
      ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.TAXONOMY.getGroupName(), () -> Iconography.TAXONOMY_ICON.getIconographic());
      ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.FLWOR.getGroupName(), () -> Iconography.FLWOR_SEARCH.getIconographic());
   }

   //~--- fields --------------------------------------------------------------

   private final SimpleObjectProperty<ConceptSnapshotService> conceptSnapshotProperty = new SimpleObjectProperty<>();
   final ArrayDeque<HistoryRecord>                            manifoldHistory         = new ArrayDeque<>();
   final SimpleStringProperty                                 groupNameProperty;
   final SimpleObjectProperty<UUID>                           manifoldUuidProperty;
   final ObservableManifoldCoordinate                         observableManifoldCoordinate;
   final ObservableEditCoordinate                             observableEditCoordinate;
   final SimpleObjectProperty<ConceptSpecification>           focusedConceptSpecificationProperty;

   //~--- constructors --------------------------------------------------------

   private Manifold(String name,
                    UUID manifoldUuid,
                    ObservableManifoldCoordinate observableManifoldCoordinate,
                    ObservableEditCoordinate editCoordinate) {
      this(name, manifoldUuid, observableManifoldCoordinate, editCoordinate, null);
   }

   private Manifold(String group,
                    UUID manifoldUuid,
                    ObservableManifoldCoordinate observableManifoldCoordinate,
                    ObservableEditCoordinate editCoordinate,
                    ConceptSpecification focusedObject) {
      this.groupNameProperty                = new SimpleStringProperty(group);
      this.manifoldUuidProperty             = new SimpleObjectProperty<>(manifoldUuid);
      this.observableManifoldCoordinate     = observableManifoldCoordinate;
      this.observableEditCoordinate         = editCoordinate;
      this.focusedConceptSpecificationProperty = new SimpleObjectProperty<>(focusedObject);
      this.focusedConceptSpecificationProperty.addListener(new WeakChangeListener<>(this));

      // MANIFOLD_CHANGE_LISTENERS is a map with weak reference keys, so the following line is not a leak...
      MANIFOLD_CHANGE_LISTENERS.put(this, null);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void changed(ObservableValue<? extends ConceptSpecification> observable,
                       ConceptSpecification oldValue,
                       ConceptSpecification newValue) {
      if (newValue != null) {
         MANIFOLD_CHANGE_LISTENERS.forEach(
             (manifold, u) -> {
                HistoryRecord historyRecord = new HistoryRecord(
                                                  newValue.getNid(),
                                                        manifold.getFullySpecifiedDescriptionText(newValue));
                ArrayDeque<HistoryRecord> groupHistory = GROUP_HISTORY_MAP.computeIfAbsent(
                                                            ManifoldGroup.UNLINKED.getGroupName(),
                                                                   k -> new ArrayDeque<>());

                addHistory(historyRecord, groupHistory);

                if ((manifold != this) &&
                    !manifold.getGroupName().equals(ManifoldGroup.UNLINKED.getGroupName()) &&
                    manifold.getGroupName().equals(this.getGroupName())) {
                   manifold.focusedConceptProperty()
                           .set(newValue);
                   addHistory(historyRecord, manifold.manifoldHistory);
                   groupHistory = GROUP_HISTORY_MAP.computeIfAbsent(manifold.getGroupName(), k -> new ArrayDeque<>());
                   addHistory(historyRecord, groupHistory);
                }
             });
      }
   }

   @Override
   public Manifold deepClone() {
      return new Manifold(
          groupNameProperty.get(),
          UUID.randomUUID(),
          observableManifoldCoordinate.deepClone(),
          observableEditCoordinate.deepClone(),
          focusedConceptSpecificationProperty.get());
   }

   public SimpleObjectProperty<ConceptSpecification> focusedConceptProperty() {
      return focusedConceptSpecificationProperty;
   }

   public SimpleStringProperty groupNameProperty() {
      return groupNameProperty;
   }
   
   /**
    * Get a manifold for local use within a control group that is not linked to the selection of other concept
    * presentations.
    *
    * @param group
    * @return a new manifold on each call.
    */
   public static final Manifold make(ManifoldGroup group) {
      return make(group.getGroupName());
   }

   /**
    * Get a manifold for local use within a control group that is not linked to the selection of other concept
    * presentations.
    *
    * @param groupName
    * @return a new manifold on each call.
    */
   public static final Manifold make(String groupName) {
      return newManifold(
          groupName,
          UUID.randomUUID(),
          Get.configurationService()
             .getUserConfiguration(Optional.empty()).getManifoldCoordinate(),
          Get.configurationService()
             .getUserConfiguration(Optional.empty()).getEditCoordinate());
   }

   
   public static Manifold newManifold(String name,
                                      UUID manifoldUuid,
                                      ObservableManifoldCoordinate observableManifoldCoordinate,
                                      ObservableEditCoordinate editCoordinate) {
      return new Manifold(name, manifoldUuid, observableManifoldCoordinate, editCoordinate, null);
   }

   public static Manifold newManifold(String name,
                                      UUID manifoldUuid,
                                      ObservableManifoldCoordinate observableManifoldCoordinate,
                                      ObservableEditCoordinate editCoordinate,
                                      ConceptChronology focusedObject) {
      return new Manifold(name, manifoldUuid, observableManifoldCoordinate, editCoordinate, focusedObject);
   }

   public LatestVersion<String> getDescriptionText(int conceptNid) {
      LatestVersion<DescriptionVersion> latestVersion = getDescription(conceptNid, this);
      if (latestVersion.isPresent()) {
         LatestVersion<String> latestText = new LatestVersion<>(latestVersion.get().getText());
         for (DescriptionVersion contradition: latestVersion.contradictions()) {
            latestText.addLatest(contradition.getText());
         }
         return latestText;
      }
      return new LatestVersion<>();
   }

   @Override
   public String toString() {
      return "Manifold{" + "groupNameProperty=" + groupNameProperty + ", manifoldUuidProperty=" +
             manifoldUuidProperty + ", observableManifoldCoordinate=" + observableManifoldCoordinate +
             ", editCoordinate=" + observableEditCoordinate + ", focusedConceptProperty=" +
             focusedConceptSpecificationProperty + '}';
   }

   private static void addHistory(HistoryRecord history, ArrayDeque<HistoryRecord> historyDequeue) {
      if (historyDequeue.isEmpty() ||!historyDequeue.peekFirst().equals(history)) {
         historyDequeue.push(history);

         while (historyDequeue.size() > 50) {
            historyDequeue.removeLast();
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public Optional<ConceptSpecification> getConceptForGroup(String groupName) {
      Optional<Manifold> optionalManifold = MANIFOLD_CHANGE_LISTENERS.keySet()
                                                                     .stream()
                                                                     .filter(
                                                                           manifold -> manifold.getGroupName()
                                                                                 .equals(groupName))
                                                                     .findAny();

      if (optionalManifold.isPresent()) {
         return optionalManifold.get()
                                .getFocusedConcept();
      }

      return Optional.empty();
   }

   public ConceptSnapshotService getConceptSnapshotService() {
      if (conceptSnapshotProperty.getValue() == null) {
         conceptSnapshotProperty.set(Get.conceptService()
                .getSnapshot(observableManifoldCoordinate));
      }
      return conceptSnapshotProperty.get();
   }

   public ObservableEditCoordinate getEditCoordinate() {
      return observableEditCoordinate;
   }

   public Optional<ConceptSpecification> getFocusedConcept() {
      return Optional.ofNullable(this.focusedConceptSpecificationProperty.get());
   }

   //~--- set methods ---------------------------------------------------------

   public void setFocusedConceptChronology(ConceptChronology newFocusedObject) {
      if (newFocusedObject != null) {
         HistoryRecord history = new HistoryRecord(
                                     newFocusedObject.getNid(),
                                     getFullySpecifiedDescriptionText(newFocusedObject));

         addHistory(history, this.manifoldHistory);
      }

      this.focusedConceptSpecificationProperty.set(newFocusedObject);
   }

   //~--- get methods ---------------------------------------------------------

   public static Collection<HistoryRecord> getGroupHistory(String groupName) {
      return GROUP_HISTORY_MAP.computeIfAbsent(groupName, k -> new ArrayDeque<>());
   }

   public String getGroupName() {
      return groupNameProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setGroupName(String name) {
      this.groupNameProperty.setValue(name);
   }

   //~--- get methods ---------------------------------------------------------

   public static Set<String> getGroupNames() {
      return ICONOGRAPHIC_SUPPLIER.keySet();
   }

   public Collection<HistoryRecord> getHistoryRecords() {
      return manifoldHistory;
   }

   public Node getIconographic() {
      return ICONOGRAPHIC_SUPPLIER.get(getGroupName())
                                  .get();
   }

   public static Node getIconographic(String groupName) {
      return ICONOGRAPHIC_SUPPLIER.get(groupName)
                                  .get();
   }

   @Override
   public ObservableLanguageCoordinate getLanguageCoordinate() {
      return this.observableManifoldCoordinate.getLanguageCoordinate();
   }

   @Override
   public ObservableLogicCoordinate getLogicCoordinate() {
      return this.observableManifoldCoordinate.getLogicCoordinate();
   }

   @Override
   public ObservableManifoldCoordinate getManifoldCoordinate() {
      return observableManifoldCoordinate;
   }

   public UUID getManifoldUuid() {
      return manifoldUuidProperty.get();
   }

   public void setManifoldUuid(UUID manifoldUuid) {
      this.manifoldUuidProperty.set(manifoldUuid);
   }

   public SimpleObjectProperty<UUID> getManifoldUuidProperty() {
      return manifoldUuidProperty;
   }

   @Override
   public ObservableStampCoordinate getStampCoordinate() {
      return this.observableManifoldCoordinate.getStampCoordinate();
   }
   
   public void addEditInFlight(EditInFlight editInFlight) {
      EDITS_IN_PROCESS.add(editInFlight);
      editInFlight.addCompletionListener((observable, oldValue, newValue) -> {
         EDITS_IN_PROCESS.remove(editInFlight);
      });
   }

    @Override
    public Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return this.observableManifoldCoordinate.getNextProrityLanguageCoordinate();
    }

    @Override
    public LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
        return this.observableManifoldCoordinate.getDefinitionDescription(descriptionList, stampCoordinate);
    }

    @Override
    public StampCoordinate getImmutableAllStateAnalog() {
        return getStampCoordinate().getImmutableAllStateAnalog();
    }

    @Override
    public int[] getModulePreferenceList() {
        return this.observableManifoldCoordinate.getModulePreferenceList();
    }
    
    
}

