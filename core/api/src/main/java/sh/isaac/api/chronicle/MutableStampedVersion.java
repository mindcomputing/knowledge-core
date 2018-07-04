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



package sh.isaac.api.chronicle;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Status;
import sh.isaac.api.identity.StampedVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface MutableStampedVersion.
 *
 * @author kec
 */
public interface MutableStampedVersion
        extends StampedVersion {
   /**
    * Sets the state.
    * 
    * @param state the new Status
    */
   void setStatus(Status state);
   /**
    * Sets the author nid.
    *
    * @param authorNid the new author nid
    */
   void setAuthorNid(int authorNid);

   /**
    * Sets the module nid.
    *
    * @param moduleNid the new module nid
    */
   void setModuleNid(int moduleNid);

   /**
    * Sets the path nid.
    *
    * @param pathNid the new path nid
    */
   void setPathNid(int pathNid);

   /**
    * Sets the time.
    *
    * @param time the new time
    */
   void setTime(long time);
}

