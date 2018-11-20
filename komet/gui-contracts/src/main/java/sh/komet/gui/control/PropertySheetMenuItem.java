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

//~--- JDK imports ------------------------------------------------------------
import sh.komet.gui.control.property.PropertyEditorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.apache.logging.log4j.LogManager;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;

import sh.komet.gui.interfaces.EditInFlight;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.model.observable.CommitAwareConceptSpecificationProperty;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class PropertySheetMenuItem
        implements EditInFlight {
    protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

   PropertySheet propertySheet = new PropertySheet();
   List<PropertySpec> propertiesToEdit = new ArrayList<>();
   private final ArrayList<ChangeListener<CommitStates>> completionListeners = new ArrayList<>();
   Map<ConceptSpecification, ReadOnlyProperty<?>> propertyMap;
   ObservableVersion observableVersion;
   boolean makeAnalogOnExecute;
   Manifold manifold;

   //~--- constructors --------------------------------------------------------
   public PropertySheetMenuItem(Manifold manifold,
           ObservableCategorizedVersion categorizedVersion,
           boolean makeAnalogOnExecute) {
      this.manifold = manifold;
      this.observableVersion = categorizedVersion;
      this.makeAnalogOnExecute = makeAnalogOnExecute;
      this.propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
      this.propertySheet.setMode(PropertySheet.Mode.NAME);
      this.propertySheet.setSearchBoxVisible(false);
      this.propertySheet.setModeSwitcherVisible(false);
   }

   //~--- methods -------------------------------------------------------------
   @Override
   public void addCompletionListener(ChangeListener<CommitStates> listener) {
      completionListeners.add(listener);
   }

   /**
    * Drools, or some other service, populates which properties to edit. A later call to prepareToExecute will then set
    * the constraints on a property editor using rules.
    *
    * @param nameOnPropertySheet
    * @param propertySpecification
    * @param propertyEditorType
    */
   public void addPropertyToEdit(String nameOnPropertySheet,
           ConceptSpecification propertySpecification,
           PropertyEditorType propertyEditorType) {
      this.propertiesToEdit.add(new PropertySpec(nameOnPropertySheet, propertySpecification, propertyEditorType));
   }

   @Override
   public void cancel() {
      completionListeners.forEach((listener) -> {
         listener.changed(observableVersion.commitStateProperty(), CommitStates.UNCOMMITTED, CommitStates.CANCELED);
      });
      completionListeners.clear();
   }

   public void commit() {
       try {
           CommitTask commitTask = Get.commitService()
                   .commit(FxGet.editCoordinate(), "temporary comment", getVersionInFlight());
           Optional<CommitRecord> optionalCommitRecord = commitTask.get();
           completionListeners.forEach((listener) -> {
               listener.changed(observableVersion.commitStateProperty(), CommitStates.UNCOMMITTED, CommitStates.COMMITTED);
           });
           completionListeners.clear();
       } catch (InterruptedException | ExecutionException ex) {
           LOG.error(ex.getLocalizedMessage(), ex);
           FxGet.dialogs().showErrorDialog(ex.getLocalizedMessage(), ex);
       }
   }

   public void prepareToExecute() {
      if (makeAnalogOnExecute) {
         this.observableVersion = this.observableVersion.makeAnalog(FxGet.editCoordinate());

         if (this.observableVersion.getChronology()
                 .getVersionType() == VersionType.CONCEPT) {
            Get.commitService()
                    .addUncommitted((ConceptChronology) this.observableVersion.getChronology());
         } else {
            Get.commitService()
                    .addUncommitted((SemanticChronology) this.observableVersion.getChronology());
         }
      }

      Platform.runLater(() -> {
        FxGet.rulesDrivenKometService()
              .populatePropertySheetEditors(this);
      });
      this.manifold.addEditInFlight(this);
   }

   private Item addItem(Item item) {
      propertySheet.getItems()
              .add(item);
      return item;
   }
   

   //~--- get methods ---------------------------------------------------------
   private PropertySheetItemConceptWrapper getConceptProperty(ConceptSpecification propertyConceptSpecification,
           String nameForProperty) {
      ReadOnlyProperty<?> property = getPropertyMap().get(propertyConceptSpecification);
      if (property == null) {
          int assemblageNid = observableVersion.getAssemblageNid();
          OptionalInt propertyIndex = Get.assemblageService().getPropertyIndexForSemanticField(
                  propertyConceptSpecification.getNid(), 
                  assemblageNid, manifold);
          if (propertyIndex.isPresent()) {
              property = observableVersion.getProperties().get(propertyIndex.getAsInt());
          }
      }
      ObjectProperty<ConceptSpecification> conceptProperty = null;
      if (property instanceof ObjectProperty) {
          conceptProperty = (ObjectProperty<ConceptSpecification>) property;
      } else if (property instanceof CommitAwareIntegerProperty) {
          CommitAwareIntegerProperty intProperty = (CommitAwareIntegerProperty) property;
          conceptProperty = new CommitAwareConceptSpecificationProperty(intProperty);
      }
      if (conceptProperty == null) {
         throw new IllegalStateException("No property for: " + propertyConceptSpecification);
      }
      PropertySheetItemConceptWrapper item = new PropertySheetItemConceptWrapper(
              manifold,
              nameForProperty,
              conceptProperty);
      item.setSpecification(propertyConceptSpecification);
      return item;
   }

   private PropertySheetStatusWrapper getStatusProperty(ConceptSpecification propertyConceptSpecification,
           String nameForProperty) {
      return new PropertySheetStatusWrapper(nameForProperty,
              (ObjectProperty<Status>) getPropertyMap().get(propertyConceptSpecification));
   }

   private PropertySheetTextWrapper getTextProperty(ConceptSpecification propertyConceptSpecification,
           String nameForProperty) {
      ReadOnlyProperty<?> property = getPropertyMap().get(propertyConceptSpecification);
      if (property == null) {
          int assemblageNid = observableVersion.getAssemblageNid();
          OptionalInt propertyIndex = Get.assemblageService().getPropertyIndexForSemanticField(
                  propertyConceptSpecification.getNid(), 
                  assemblageNid, manifold);
          if (propertyIndex.isPresent()) {
              property = observableVersion.getProperties().get(propertyIndex.getAsInt());
          }
          getPropertyMap().put(propertyConceptSpecification, property);
      }
      StringProperty stringProperty = (StringProperty) property;

      PropertySheetTextWrapper wrapper = new PropertySheetTextWrapper(nameForProperty,
              stringProperty);
       wrapper.setSpecification(propertyConceptSpecification);
      return wrapper;
   }
   
   private PropertySheetItemIntegerWrapper getIntegerProperty(ConceptSpecification propertyConceptSpecification,
           String nameForProperty) {
      ReadOnlyProperty<?> property = getPropertyMap().get(propertyConceptSpecification);
      if (property == null) {
          int assemblageNid = observableVersion.getAssemblageNid();
          OptionalInt propertyIndex = Get.assemblageService().getPropertyIndexForSemanticField(
                  propertyConceptSpecification.getNid(), 
                  assemblageNid, manifold);
          if (propertyIndex.isPresent()) {
              property = observableVersion.getProperties().get(propertyIndex.getAsInt());
          }
          getPropertyMap().put(propertyConceptSpecification, property);
      }
       IntegerProperty integerProperty = (IntegerProperty) property;

      PropertySheetItemIntegerWrapper wrapper = new PropertySheetItemIntegerWrapper(nameForProperty,
              integerProperty);
       wrapper.setSpecification(propertyConceptSpecification);
      return wrapper;
   }

   public Map<ConceptSpecification, ReadOnlyProperty<?>> getPropertyMap() {
      if (propertyMap == null) {
         propertyMap = observableVersion.getPropertyMap();
      }

      return propertyMap;
   }

   public PropertySheet getPropertySheet() {
      return propertySheet;
   }

   public List<Item> getPropertySheetItems() {
      List<Item> items = new ArrayList<>();

      propertiesToEdit.forEach(
              (propertySpec) -> {
                 switch (propertySpec.propertyEditorType) {
                    case CONCEPT:
                       items.add(
                               addItem(
                                       getConceptProperty(
                                               propertySpec.propertyConceptSpecification,
                                               propertySpec.nameOnPropertySheet)));
                       break;
                    case STATUS:
                       items.add(
                               addItem(getStatusProperty(
                                       propertySpec.propertyConceptSpecification,
                                       propertySpec.nameOnPropertySheet)));
                       break;

                    case TEXT:
                       items.add(
                               addItem(getTextProperty(
                                       propertySpec.propertyConceptSpecification,
                                       propertySpec.nameOnPropertySheet)));
                       break;
                    case INTEGER:
                       items.add(
                               addItem(getIntegerProperty(
                                       propertySpec.propertyConceptSpecification,
                                       propertySpec.nameOnPropertySheet)));
                       break;
                    default:
                       throw new RuntimeException("an Can't handle: " + propertySpec);
                 }
              });
      return items;
   }

   @Override
   public ObservableVersion getVersionInFlight() {
      return this.observableVersion;
   }

   public void setVersionInFlight(ObservableVersion version) {
      this.observableVersion = version;
   }
   //~--- inner classes -------------------------------------------------------

   private static class PropertySpec {

      final String nameOnPropertySheet;
      final ConceptSpecification propertyConceptSpecification;
      final PropertyEditorType propertyEditorType;

      //~--- constructors -----------------------------------------------------
      public PropertySpec(String propertySheetName,
              ConceptSpecification propertyConceptSpecification,
              PropertyEditorType propertyEditorType) {
         this.nameOnPropertySheet = propertySheetName;
         this.propertyConceptSpecification = propertyConceptSpecification;
         this.propertyEditorType = propertyEditorType;
      }

      //~--- methods ----------------------------------------------------------
      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (obj == null) {
            return false;
         }

         if (getClass() != obj.getClass()) {
            return false;
         }

         final PropertySpec other = (PropertySpec) obj;

         if (!Objects.equals(this.nameOnPropertySheet, other.nameOnPropertySheet)) {
            return false;
         }

         return this.propertyEditorType == other.propertyEditorType;
      }

      @Override
      public int hashCode() {
         int hash = 5;

         hash = 79 * hash + Objects.hashCode(this.nameOnPropertySheet);
         hash = 79 * hash + Objects.hashCode(this.propertyEditorType);
         return hash;
      }

      @Override
      public String toString() {
         return "PropertySpec{" + "propertySheetName=" + nameOnPropertySheet + ", propertyConceptSpecification="
                 + propertyConceptSpecification + ", propertyEditorType=" + propertyEditorType + '}';
      }
   }
}
