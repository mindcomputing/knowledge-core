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
import java.util.HashMap;
import java.util.Map;
import sh.isaac.api.bootstrap.TermAux;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------
/**
 * Computes the exclusive disjunction between the result sets of each
 * <code>ChildClause</code>.
 *
 * @author dylangrald
 */
public class Xor
        extends ParentClause {

    /**
     * Default no arg constructor for Jaxb.
     */
    public Xor() {
        super();
    }

    /**
     * Instantiates a new xor.
     *
     * @param enclosingQuery the enclosing query
     * @param clauses the clauses
     */
    public Xor(Query enclosingQuery, Clause... clauses) {
        super(enclosingQuery, clauses);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.XOR;
    }

    /**
     * Compute components.
     *
     * @param incomingComponents the incoming components
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> incomingComponents) {
        final NidSet xorSet = new NidSet();

        getChildren().stream().forEach((c) -> {
            xorSet.xor(c.computeComponents(incomingComponents).get(c.getAssemblageForIteration()));
            setAssemblageForIteration(c.getAssemblageForIteration());
        });
        HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(incomingComponents);
        resultsMap.put(this.getAssemblageForIteration(), xorSet);
        return resultsMap;
    }

    /**
     * Compute possible components.
     *
     * @param incomingPossibleComponents the incoming possible components
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
        final NidSet unionSet = new NidSet();

        getChildren().stream().forEach((c) -> {
            unionSet.or(c.computePossibleComponents(incomingPossibleComponents).get(c.getAssemblageForIteration()));
            setAssemblageForIteration(c.getAssemblageForIteration());
        });
        HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(incomingPossibleComponents);
        resultsMap.put(this.getAssemblageForIteration(), unionSet);
        return resultsMap;
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.XOR);

        for (final Clause clause : getChildren()) {
            whereClause.getChildren()
                    .add(clause.getWhereClause());
        }

        return whereClause;
    }

    @Override
    public ConceptSpecification getClauseConcept() {
        return TermAux.XOR_QUERY_CLAUSE;
    }

    @Override
    public Clause[] getAllowedSubstutitionClauses() {
        return getParentClauses();
    }

    @Override
    public Clause[] getAllowedChildClauses() {
        return getAllClauses();
    }

    @Override
    public Clause[] getAllowedSiblingClauses() {
        return getAllClauses();
    }

}
