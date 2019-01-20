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



package sh.isaac.api.coordinate;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 * The Enum PremiseType.
 *
 * @author kec
 */
public enum PremiseType {
   /**
    * Compute the taxonomy from stated axioms.
    */
   STATED("Stated", TermAux.STATED_PREMISE_TYPE),

   /**
    * Compute the taxonomy from inferred axioms.
    */
   INFERRED("Inferred", TermAux.INFERRED_PREMISE_TYPE);
   
   String displayName;
   ConceptSpecification premiseTypeConcept;

   private PremiseType(String displayName, ConceptSpecification premiseTypeConcept) {
      this.displayName = displayName;
      this.premiseTypeConcept = premiseTypeConcept;
   }

    public ConceptSpecification getPremiseTypeConcept() {
        return premiseTypeConcept;
    }
   
   public PremiseType next() {
      switch(this) {
         case INFERRED:
            return STATED;
         case STATED:
            return INFERRED;
         default:
            throw new UnsupportedOperationException("h Can't handle: " + this);
      }
   }

   @Override
   public String toString() {
      return displayName;
   }
   
   public static PremiseType fromConcept(ConceptSpecification conceptSpecification) {
      if (conceptSpecification.getNid() == TermAux.INFERRED_PREMISE_TYPE.getNid()) {
         return INFERRED;
      }
      if (conceptSpecification.getNid() == TermAux.STATED_PREMISE_TYPE.getNid()) {
         return STATED;
      }
      throw new IllegalStateException("PremiseType.fromConcept can't handle: " + conceptSpecification);
   }
}

