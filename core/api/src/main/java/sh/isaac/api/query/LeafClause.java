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



package sh.isaac.api.query;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 * A leaf in the computation tree of clauses of a query. A LeafClause
 * cannot have any child clauses. Implementations of LeafClause should cache
 * their results in the resultsCache, which is then passed on to the clauses
 * that enclose the LeafClause.
 *
 * @author kec
 */
@XmlRootElement(name = "leaf")
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class LeafClause
        extends Clause {

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new leaf clause.
    */
   protected LeafClause() {
      super();
   }

   /**
    * Instantiates a new leaf clause.
    *
    * @param enclosingQuery the enclosing query
    */
   public LeafClause(Query enclosingQuery) {
      super(enclosingQuery);
   }

   //~--- methods -------------------------------------------------------------


   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children.
    *
    * @return the children
    */
   @Override
   public List<Clause> getChildren() {
      return Collections.emptyList();
   }

   @Override
   public Clause[] getAllowedChildClauses() {
      return new Clause[0];
   }

   @Override
   public Clause[] getAllowedSubstutitionClauses() {
      return new Clause[0];
   }

   @Override
   public Clause[] getAllowedSiblingClauses() {
      return new Clause[0];
   }

    @Override
    public Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> incomingComponents) {
        return incomingComponents;
    }

    @Override
    public void resetResults() {
        // nothing to do...
    }
     
}

