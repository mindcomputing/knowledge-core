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
package sh.isaac.api;

import java.util.Collections;
//~--- JDK imports ------------------------------------------------------------
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;


//~--- enums ------------------------------------------------------------------
/**
 * The Enum Status.
 *
 * @author kec
 */
@XmlType(name = "Status")
@XmlEnum
public enum Status {
   /**
    * Currently inactive.
    */
   INACTIVE(false, "Inactive", "I", TermAux.INACTIVE_STATUS),
   /**
    * Currently active.
    */
   ACTIVE(true, "Active", "A", TermAux.ACTIVE_STATUS),
   /**
    * Not yet created.
    */
   PRIMORDIAL(false, "Primordial", "P", TermAux.PRIMORDIAL_STATUS),
   /**
    * Canceled prior to commit.
    */
   CANCELED(false, "Canceled", "C", TermAux.CANCELED_STATUS),
   /**
    * Withdrawn after being committed, but should no longer be used in snapshot computations.
    */
   WITHDRAWN(false, "Withdrawn", "W", TermAux.WITHDRAWN_STATUS);

   /**
    * The is active.
    */
   boolean isActive;

   /**
    * The name.
    */
   String name;

   /**
    * The abbreviation.
    */
   String abbreviation;
   
   /**
    * Concept specification for this status.
    */
   ConceptSpecification specifyingConcept;
   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new state.
    *
    * @param isActive the is active
    * @param name the name
    * @param abbreviation the abbreviation
    */
   Status(boolean isActive, String name, String abbreviation, ConceptSpecification specifyingConcept) {
      this.isActive = isActive;
      this.name = name;
      this.abbreviation = abbreviation;
      this.specifyingConcept = specifyingConcept;
   }

   //~--- methods -------------------------------------------------------------
   public static Status from(ConceptSpecification conceptSpecification) {
        for (Status status: values()) {
            if (status.specifyingConcept.equals(conceptSpecification)) {
                return status;
            }
        }
        throw new NoSuchElementException("No status matches: " + conceptSpecification);
   }
   /**
    * Inverse.
    *
    * @return the state
    */
   public Status inverse() {
      switch (this) {
         case ACTIVE:
            return INACTIVE;

         case INACTIVE:
            return ACTIVE;

         default:
            return this;
      }
   }

   public static EnumSet<Status> makeActiveOnlySet() {
      return EnumSet.of(ACTIVE);
   }

   public static EnumSet<Status> makeActiveAndInactiveSet() {
      return EnumSet.of(ACTIVE, INACTIVE);
   }

   public static EnumSet<Status> makeAnyStateSet() {
      return EnumSet.allOf(Status.class);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.name;
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the abbreviation.
    *
    * @return the abbreviation
    */
   public String getAbbreviation() {
      return this.abbreviation;
   }

   /**
    * Checks if active.
    *
    * @return true, if active
    */
   public boolean isActive() {
      return this.isActive;
   }

   public static boolean isActiveOnlySet(EnumSet<Status> setToTest) {
      if (setToTest.size() != 1) {
         return false;
      }

      return setToTest.contains(ACTIVE);
   }

   /**
    * Gets the boolean.
    *
    * @return the boolean
    */
   public boolean getBoolean() {
      return this.isActive;
   }

   /**
    * Gets the from boolean.
    *
    * @param isActive the is active
    * @return the from boolean
    */
   public static Status getFromBoolean(boolean isActive) {
      if (isActive) {
         return ACTIVE;
      }

      return INACTIVE;
   }

   public static Status fromZeroOneToken(String token) {
      switch (token) {
         case "1":
            return Status.ACTIVE;
         case "0":
            return Status.INACTIVE;
         default:
            throw new UnsupportedOperationException("Can't handle token: " + token);
      }
   }

    public ConceptSpecification getSpecifyingConcept() {
        return specifyingConcept;
    }
   
   public static final Set<Status> ACTIVE_ONLY_SET = Collections.unmodifiableSet(EnumSet.of(Status.ACTIVE));
   public static final Set<Status> ANY_STATUS_SET = Collections.unmodifiableSet(EnumSet.allOf(Status.class));
   public static final Set<Status> INACTIVE_STATUS_SET = Collections.unmodifiableSet(EnumSet.of(Status.INACTIVE, Status.CANCELED, Status.WITHDRAWN));
}
