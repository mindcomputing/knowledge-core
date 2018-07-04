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



package sh.isaac.api.observable;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.coordinate.ManifoldCoordinate;

import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableChronologyService.
 *
 * @author kec
 */
@Contract
public interface ObservableChronologyService {
   /**
    * Gets the observable concept chronology.
    *
    * @param id either a nid or a concept nid
    * @return the ObservableConceptChronology with the provided id
    */
   ObservableConceptChronology getObservableConceptChronology(int id);

   /**
    * Gets the observable semantic chronology.
    *
    * @param id a nid 
    * @return the ObservableSemanticChronology with the provided id
    */
   ObservableSemanticChronology getObservableSemanticChronology(int id);
   /**
    * 
    * @param manifoldCoordinate the coordinate to determine the latest versions of the snapshot
    * @return the snapshot service. 
    */
   ObservableSnapshotService getObservableSnapshotService(ManifoldCoordinate manifoldCoordinate);
}

