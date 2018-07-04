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



package sh.isaac.model.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

import javafx.collections.ArrayChangeListener;
import javafx.collections.ObservableIntegerArray;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LanguageCoordinateImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "languageCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class LanguageCoordinateImpl
         implements LanguageCoordinate {
   /** The language concept nid. */
   int languageConceptNid;

   /** The dialect assemblage preference list. */
   int[] dialectAssemblagePreferenceList;

   /** The description type preference list. */
   int[] descriptionTypePreferenceList;
   
   int[] modulePreferenceList;
   
   LanguageCoordinateImpl nextProrityLanguageCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new language coordinate impl.
    */
   private LanguageCoordinateImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new language coordinate impl.
    *
    * @param languageConceptId the language concept id
    * @param dialectAssemblagePreferenceList the dialect assemblage preference list
    * @param descriptionTypePreferenceList the description type preference list
    * @param modulePreferenceList the module preference list.  See {@link LanguageCoordinate#getModulePreferenceList()}
    */
   public LanguageCoordinateImpl(int languageConceptId,
                                 int[] dialectAssemblagePreferenceList,
                                 int[] descriptionTypePreferenceList,
                                 int[] modulePreferenceList) {
      this.languageConceptNid         = languageConceptId;
      this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList;
      this.descriptionTypePreferenceList = descriptionTypePreferenceList;
      this.modulePreferenceList = modulePreferenceList;
   }
   
   /**
    * Instantiates a new language coordinate impl, with an unspecified set of modulePreferences.
    *
    * @param languageConceptId the language concept id
    * @param dialectAssemblagePreferenceList the dialect assemblage preference list
    * @param descriptionTypePreferenceList the description type preference list
    */
   public LanguageCoordinateImpl(int languageConceptId,
                                 int[] dialectAssemblagePreferenceList,
                                 int[] descriptionTypePreferenceList) {
      this(languageConceptId, dialectAssemblagePreferenceList, descriptionTypePreferenceList, new int[] {});
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final LanguageCoordinateImpl other = (LanguageCoordinateImpl) obj;

      if (this.languageConceptNid != other.languageConceptNid) {
         return false;
      }

      if (!Arrays.equals(this.dialectAssemblagePreferenceList, other.dialectAssemblagePreferenceList)) {
         return false;
      }

      return Arrays.equals(this.descriptionTypePreferenceList, other.descriptionTypePreferenceList);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 79 * hash + this.languageConceptNid;
      hash = 79 * hash + Arrays.hashCode(this.dialectAssemblagePreferenceList);
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "Language Coordinate{" + Get.conceptDescriptionText(this.languageConceptNid) +
             ", dialect preference: " + Get.conceptDescriptionTextList(this.dialectAssemblagePreferenceList) +
             ", type preference: " + Get.conceptDescriptionTextList(this.descriptionTypePreferenceList) + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description.
    *
    * @param descriptionList the description list
    * @param stampCoordinate the stamp coordinate
    * @return the description
    */
   @Override
   public LatestVersion<DescriptionVersion> getDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate, descriptionList, this);
   }

   /**
    * Gets the description type preference list.
    *
    * @return the description type preference list
    */
   @Override
   public int[] getDescriptionTypePreferenceList() {
      return this.descriptionTypePreferenceList;
   }

   public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      this.descriptionTypePreferenceList = descriptionTypePreferenceList;
   }


   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the dialect assemblage preference list.
    *
    * @return the dialect assemblage preference list
    */
   @Override
   public int[] getDialectAssemblagePreferenceList() {
      return this.dialectAssemblagePreferenceList;
   }

   public void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the fully specified description.
    *
    * @param descriptionList the description list
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   @Override
   public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate,
                                         descriptionList,
                                         Get.languageCoordinateService()
                                               .getFullySpecifiedConceptNid(),
                                         this);
   }

   /**
    * Gets the language concept nid.
    *
    * @return the language concept nid
    */
   @Override
   public int getLanguageConceptNid() {
      return this.languageConceptNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set language concept nid property.
    *
    * @param languageConceptSequenceProperty the language concept nid property
    * @return the change listener
    */
   public ChangeListener<Number> setLanguageConceptNidProperty(IntegerProperty languageConceptSequenceProperty) {
      final ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                               Number oldValue,
                                               Number newValue) -> {
               this.languageConceptNid = newValue.intValue();
            };

      languageConceptSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   public ChangeListener<ObservableLanguageCoordinate> setNextProrityLanguageCoordinateProperty(
           ObjectProperty<ObservableLanguageCoordinate> nextProrityLanguageCoordinateProperty) {
       
      final ChangeListener<ObservableLanguageCoordinate> listener = (ObservableValue<? extends ObservableLanguageCoordinate> observable,
                                               ObservableLanguageCoordinate oldValue,
                                               ObservableLanguageCoordinate newValue) -> {
               this.nextProrityLanguageCoordinate = ((ObservableLanguageCoordinateImpl)newValue).unwrap();
            };

      nextProrityLanguageCoordinateProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }
   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the preferred description.
    *
    * @param descriptionList the description list
    * @param stampCoordinate the stamp coordinate
    * @return the preferred description
    */
   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate,
                                         descriptionList,
                                         Get.languageCoordinateService()
                                               .getSynonymConceptNid(),
                                         this);
   }

   @Override
   public LatestVersion<DescriptionVersion> getDefinitionDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {

       LatestVersion<DescriptionVersion> definition = Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate,
                                         descriptionList,
                                         TermAux.DEFINITION_DESCRIPTION_TYPE.getNid(),
                                         this);
       
        if (definition.isPresent() && definition.get().getDescriptionTypeConceptNid() == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            return definition;
        }
        
       return new LatestVersion();
   }

   @Override
   public LanguageCoordinate deepClone() {
      LanguageCoordinateImpl newCoordinate = new LanguageCoordinateImpl(languageConceptNid,
                                 dialectAssemblagePreferenceList.clone(),
                                 descriptionTypePreferenceList.clone(),
                                 modulePreferenceList.clone());
      if (this.nextProrityLanguageCoordinate != null) {
          newCoordinate.nextProrityLanguageCoordinate = (LanguageCoordinateImpl) this.nextProrityLanguageCoordinate.deepClone();
      }
      return newCoordinate;
   }

    @Override
    public Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return Optional.ofNullable(this.nextProrityLanguageCoordinate);
    }

    public void setNextProrityLanguageCoordinate(LanguageCoordinate languageCoordinate) {
        this.nextProrityLanguageCoordinate = (LanguageCoordinateImpl) languageCoordinate;
    }

    @Override
    public int[] getModulePreferenceListForLanguage() {
        return modulePreferenceList;
    }
    
    
}

