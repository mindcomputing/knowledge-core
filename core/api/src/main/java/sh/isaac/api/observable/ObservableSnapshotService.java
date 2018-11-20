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
package sh.isaac.api.observable;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.observable.concept.ObservableConceptVersion;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;

/**
 *
 * @author kec
 */
@Contract
public interface ObservableSnapshotService {

   /**
    * Gets the observable concept version.
    *
    * @param id a nid 
    * @return the ObservableConceptChronology with the provided id
    */
   LatestVersion<ObservableConceptVersion> getObservableConceptVersion(int id);

   /**
    * Gets the observable semantic version.
    *
    * @param id a nid 
    * @return the ObservableSemanticVersion with the provided id
    */
   LatestVersion<? extends ObservableSemanticVersion> getObservableSemanticVersion(int id);

   LatestVersion<? extends ObservableVersion> getObservableVersion(int id);

}
