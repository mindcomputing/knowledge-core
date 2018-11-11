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



package sh.isaac.api.query.clauses;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.bootstrap.TermAux;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * <code>LeafClause</code> that returns the nid of the input refset if a kind of
 * the input concept is a member of the refset and returns an empty set if a
 * kind of the input concept is not a member of the refset.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class AssemblageContainsKindOfConcept
        extends LeafClause {
   /** The refset spec key. */
   @XmlElement
   LetItemKey assemblageSpecKey;

   /** The concept spec key. */
   @XmlElement
   LetItemKey conceptSpecKey;

   /** The view coordinate key. */
   @XmlElement
   LetItemKey stampCoordinateKey;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new refset contains kind of concept.
    */
   public AssemblageContainsKindOfConcept() {}

   /**
    * Instantiates a new refset contains kind of concept.
    *
    * @param enclosingQuery the enclosing query
    * @param assemblageSpecKey the refset spec key
    * @param conceptSpecKey the concept spec key
    * @param stampCoordinateKey the view coordinate key
    */
   public AssemblageContainsKindOfConcept(Query enclosingQuery,
                                      LetItemKey assemblageSpecKey,
                                      LetItemKey conceptSpecKey,
                                      LetItemKey stampCoordinateKey) {
      super(enclosingQuery);
      this.assemblageSpecKey     = assemblageSpecKey;
      this.conceptSpecKey    = conceptSpecKey;
      this.stampCoordinateKey = stampCoordinateKey;
   }

   //~--- methods -------------------------------------------------------------
    @Override
    public void resetResults() {
        // no cached data in task. 
    }

   /**
    * Compute possible components.
    *
    * @param incomingPossibleComponents the incoming possible components
    * @return the nid set
    */
   @Override
   public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
      throw new UnsupportedOperationException();

      // TODO FIX BACK UP
//    ManifoldCoordinate manifoldCoordinate = (ManifoldCoordinate) this.enclosingQuery.getLetDeclarations().get(stampCoordinateKey);
//    ConceptSpec refsetSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(assemblageSpecKey);
//    ConceptSpec conceptSpec = (ConceptSpec) this.enclosingQuery.getLetDeclarations().get(conceptSpecKey);
//
//
//    int parentNid = conceptSpec.getNid();
//    NidSet kindOfSet = Ts.get().isKindOfSet(parentNid, viewCoordinate);
//    int refsetNid = refsetSpec.getNid();
//    ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(viewCoordinate, refsetNid);
//    for (RefexVersionBI<?> rm : conceptVersion.getCurrentRefsetMembers(viewCoordinate)) {
//        if (kindOfSet.contains(rm.getReferencedComponentNid())) {
//            getResultsCache().add(refsetNid);
//        }
//    }
//
//    return getResultsCache();
   }

   public LetItemKey getAssemblageSpecKey() {
      return assemblageSpecKey;
   }

    public void setAssemblageSpecKey(LetItemKey assemblageSpecKey) {
        this.assemblageSpecKey = assemblageSpecKey;
    }

    public LetItemKey getConceptSpecKey() {
        return conceptSpecKey;
    }

    public void setConceptSpecKey(LetItemKey conceptSpecKey) {
        this.conceptSpecKey = conceptSpecKey;
    }

    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKey;
    }

    //~--- get methods ---------------------------------------------------------
    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKey = stampCoordinateKey;
    }

    /**
     * Gets the compute phases.
     *
     * @return the compute phases
     */
    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      // Nothing to do here
   }
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT;
    }
   

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT);
      whereClause.getLetKeys()
                 .add(this.assemblageSpecKey);
      whereClause.getLetKeys()
                 .add(this.conceptSpecKey);
      whereClause.getLetKeys()
                 .add(this.stampCoordinateKey);
      return whereClause;
   }
   
   @Override
   public ConceptSpecification getClauseConcept() {
      return TermAux.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE;
   }
   
}

