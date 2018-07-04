/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.component.concept;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;

/**
 *
 * @author kec
 */
public interface ObservableConceptSnapshotService extends SharedConceptSnapshotService {


      /**
    * Gets the concept snapshot.
    *
    * @param conceptNid of the concept to get the {@code ConceptSnapshot} for
    * @return a concept that internally uses the {@code StampCoordinate}
    * and {@code LanguageCoordinate} for
    */
   ConceptSnapshot getConceptSnapshot(int conceptNid);

   /**
    * Gets the concept snapshot.
    *
    * @param conceptSpecification specification of the concept to get the {@code ConceptSnapshot} for
    * @return a concept that internally uses the {@code StampCoordinate}
    * and {@code LanguageCoordinate} for
    */
   ConceptSnapshot getConceptSnapshot(ConceptSpecification conceptSpecification);

   /**
    * This method will try to return description types according to the type preferences
    * of the language coordinate, finally any description if there is no
    * preferred or fully specified description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    * @param conceptNid of the concept to get the description for
    * @return a Optional description for this concept.
    */
   LatestVersion<ObservableDescriptionVersion> getDescriptionOptional(int conceptNid);

   /**
    * Gets the fully specified description.
    *
    * @param conceptNid of the concept to get the description for
    * @return The fully specified description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<ObservableDescriptionVersion> getFullySpecifiedDescription(int conceptNid);

   /**
    * Gets the preferred description.
    *
    * @param conceptNid of the concept to get the description for
    * @return The preferred description for this concept. Optional in case
    * there is not description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<ObservableDescriptionVersion> getPreferredDescription(int conceptNid);
}
