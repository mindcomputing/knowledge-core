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

package sh.isaac.api.index;

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

/**
 * A class for passing Author, Module and/or Path restrictions into lucene queries.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 *
 */
public class AmpRestriction {

   private NidSet authors;
   private NidSet modules;
   private NidSet paths;

   private AmpRestriction() {

   }

   public static AmpRestriction restrictAuthor(NidSet authors) {
      AmpRestriction ar = new AmpRestriction();
      ar.authors = authors;
      return ar;
   }

   public static AmpRestriction restrictModule(NidSet modules) {
      AmpRestriction ar = new AmpRestriction();
      ar.modules = modules;
      return ar;
   }

   public static AmpRestriction restrictPath(NidSet paths) {
      AmpRestriction ar = new AmpRestriction();
      ar.paths = paths;
      return ar;
   }

   public static AmpRestriction restrict(NidSet authors,
         NidSet modules,
         NidSet paths) {
      AmpRestriction ar = new AmpRestriction();
      ar.authors = authors;
      ar.modules = modules;
      ar.paths = paths;
      return ar;
   }
   
   /**
    * Build an AmpRestriction by extracting the modules and Path from the manifold coordinate
    * @param mc
    * @return
    */
   public static AmpRestriction restrict(ManifoldCoordinate mc) {
      AmpRestriction ar = new AmpRestriction();
      ar.authors = NidSet.EMPTY;
      ar.modules = mc.getModuleNids();
      ar.paths = NidSet.of(mc.getStampCoordinate().getStampPosition().getStampPathNid());
      return ar;
   }
   
   /**
    * Build an AmpRestriction by extracting the modules and Path from the manifold coordinate
    * @param sc
    * @return
    */
   public static AmpRestriction restrict(StampCoordinate sc) {
      AmpRestriction ar = new AmpRestriction();
      ar.authors = NidSet.EMPTY;
      ar.modules = sc.getModuleNids();
      ar.paths = NidSet.of(sc.getStampPosition().getStampPathNid());
      return ar;
   }

   public NidSet getAuthors() {
      return authors;
   }

   public NidSet getModules() {
      return modules;
   }

   public NidSet getPaths() {
      return paths;
   }
}
