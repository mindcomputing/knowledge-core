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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.provider.logic.csiro.axioms;

//~--- JDK imports ------------------------------------------------------------

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

//~--- non-JDK imports --------------------------------------------------------

//TODO move to CSIRO specific module
import au.csiro.ontology.Factory;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.ConceptInclusion;
import au.csiro.ontology.model.Feature;
import au.csiro.ontology.model.Literal;
import au.csiro.ontology.model.Operator;
import au.csiro.ontology.model.Role;
import sh.isaac.api.DataSource;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.EclipseIntObjectMap;
import sh.isaac.model.collections.IntObjectMap;
import sh.isaac.model.collections.SpinedIntObjectMap;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.LiteralNodeBoolean;
import sh.isaac.model.logic.node.LiteralNodeDouble;
import sh.isaac.model.logic.node.LiteralNodeInstant;
import sh.isaac.model.logic.node.LiteralNodeInteger;
import sh.isaac.model.logic.node.LiteralNodeString;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;

//~--- classes ----------------------------------------------------------------

/**
 * The Class GraphToAxiomTranslator.
 *
 * @author kec
 */
public class GraphToAxiomTranslator {
   /** The axioms. */
   Set<Axiom> axioms = new ConcurrentSkipListSet<>();

   /** The nid logic concept map. */
   IntObjectMap<Concept> nidLogicConceptMap;


   /** The sequence logic role map. */
   ConcurrentHashMap<Integer, Role> nidLogicRoleMap = new ConcurrentHashMap<>();

   /** The sequence logic feature map. */
   ConcurrentHashMap<Integer, Feature> nidLogicFeatureMap = new ConcurrentHashMap<>();

   /** The loaded concepts. */
   ConcurrentSkipListSet<Integer> loadedConceptNids = new ConcurrentSkipListSet<>();

   public GraphToAxiomTranslator() {
      nidLogicConceptMap = ModelGet.dataStore().implementsSequenceStore() ? new SpinedIntObjectMap<>() : new EclipseIntObjectMap<>();
   }

   /**
    * Clear.
    */
   public void clear() {
      this.axioms.clear();
      this.nidLogicRoleMap.clear();
      this.nidLogicFeatureMap.clear();
      this.nidLogicConceptMap.clear();
      this.loadedConceptNids.clear();
   }

   /**
    * Translates the logicGraphSemantic into a set of axioms, and adds those axioms
 to the internal set of axioms.
    *
    * @param logicGraphSemantic the logic graph semantic
    */
   public void convertToAxiomsAndAdd(LogicGraphVersion logicGraphSemantic) {
      if (logicGraphSemantic.getReferencedComponentNid() >= 0) {
         throw new IllegalStateException("Referenced component nid must be negative: " + logicGraphSemantic.getReferencedComponentNid());
      }
      this.loadedConceptNids.add(logicGraphSemantic.getReferencedComponentNid());

      final LogicalExpressionImpl logicGraph = new LogicalExpressionImpl(logicGraphSemantic.getGraphData(),
                                                                                   DataSource.INTERNAL);

      generateAxioms(logicGraph.getRoot(), logicGraphSemantic.getReferencedComponentNid(), logicGraph);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "GraphToAxiomTranslator{" + "axioms=" + this.axioms.size() + ", nidLogicConceptMap=" +
             this.nidLogicConceptMap.size() + ", sequenceLogicRoleMap=" +
             this.nidLogicRoleMap.size() + ", sequenceLogicFeatureMap=" + this.nidLogicFeatureMap.size() +
             '}';
   }

   /**
    * Generate axioms.
    *
    * @param logicNode the logic node
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    * @return the optional
    */
   private Optional<Concept> generateAxioms(LogicNode logicNode,
         int conceptNid,
         LogicalExpressionImpl logicGraph) {
      switch (logicNode.getNodeSemantic()) {
      case AND:
         return processAnd((AndNode) logicNode, conceptNid, logicGraph);

      case CONCEPT:
         final ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) logicNode;

         return Optional.of(getConcept(conceptNode.getConceptNid()));

      case DEFINITION_ROOT:
         processRoot(logicNode, conceptNid, logicGraph);
         break;

      case DISJOINT_WITH:
         throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

      case FEATURE:
         return processFeatureNode((FeatureNodeWithNids) logicNode, conceptNid, logicGraph);

      case NECESSARY_SET:
         processNecessarySet((NecessarySetNode) logicNode, conceptNid, logicGraph);
         break;

      case OR:
         throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

      case ROLE_ALL:
         throw new UnsupportedOperationException("Not supported by SnoRocket/EL++.");

      case ROLE_SOME:
         return processRoleNodeSome((RoleNodeSomeWithNids) logicNode, conceptNid, logicGraph);

      case SUBSTITUTION_BOOLEAN:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_CONCEPT:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_FLOAT:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_INSTANT:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_INTEGER:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUBSTITUTION_STRING:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case SUFFICIENT_SET:
         processSufficientSet((SufficientSetNode) logicNode, conceptNid, logicGraph);
         break;

      case TEMPLATE:
         throw new UnsupportedOperationException("Supported, but not yet implemented.");

      case LITERAL_BOOLEAN:
      case LITERAL_FLOAT:
      case LITERAL_INSTANT:
      case LITERAL_INTEGER:
      case LITERAL_STRING:
         throw new UnsupportedOperationException("Expected concept logicNode, found literal logicNode: " + logicNode +
               " Concept: " + conceptNid + " graph: " + logicGraph);

      default:
         throw new UnsupportedOperationException("ar Can't handle: " + logicNode.getNodeSemantic());
      }

      return Optional.empty();
   }

   /**
    * Generate literals.
    *
    * @param logicNode the logic node
    * @param c the c
    * @param logicGraph the logic graph
    * @return the optional
    */
   @SuppressWarnings("deprecation")
   private Optional<Literal> generateLiterals(LogicNode logicNode, Concept c, LogicalExpressionImpl logicGraph) {
      switch (logicNode.getNodeSemantic()) {
      case LITERAL_BOOLEAN:
         final LiteralNodeBoolean literalNodeBoolean = (LiteralNodeBoolean) logicNode;

         return Optional.of(Factory.createBooleanLiteral(literalNodeBoolean.getLiteralValue()));

      case LITERAL_FLOAT:
         final LiteralNodeDouble literalNodeFloat = (LiteralNodeDouble) logicNode;

         return Optional.of(Factory.createFloatLiteral((float) literalNodeFloat.getLiteralValue()));

      case LITERAL_INSTANT:
         final LiteralNodeInstant literalNodeInstant = (LiteralNodeInstant) logicNode;
         final Calendar           calendar           = Calendar.getInstance();

         calendar.setTimeInMillis(literalNodeInstant.getLiteralValue()
               .toEpochMilli());
         return Optional.of(Factory.createDateLiteral(calendar));

      case LITERAL_INTEGER:
         final LiteralNodeInteger literalNodeInteger = (LiteralNodeInteger) logicNode;

         return Optional.of(Factory.createIntegerLiteral(literalNodeInteger.getLiteralValue()));

      case LITERAL_STRING:
         final LiteralNodeString literalNodeString = (LiteralNodeString) logicNode;

         return Optional.of(Factory.createStringLiteral(literalNodeString.getLiteralValue()));

      default:
         throw new UnsupportedOperationException("Expected literal logicNode, found: " + logicNode + " Concept: " + c +
               " graph: " + logicGraph);
      }
   }

   /**
    * Process and.
    *
    * @param andNode the and node
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    * @return the optional
    */
   private Optional<Concept> processAnd(AndNode andNode, int conceptNid, LogicalExpressionImpl logicGraph) {
      final LogicNode[] childrenLogicNodes  = andNode.getChildren();
      final Concept[]   conjunctionConcepts = new Concept[childrenLogicNodes.length];

      for (int i = 0; i < childrenLogicNodes.length; i++) {
         conjunctionConcepts[i] = generateAxioms(childrenLogicNodes[i], conceptNid, logicGraph).get();
      }

      return Optional.of(Factory.createConjunction(conjunctionConcepts));
   }

   /**
    * Process feature node.
    *
    * @param featureNode the feature node
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    * @return the optional
    */
   private Optional<Concept> processFeatureNode(FeatureNodeWithNids featureNode,
         int conceptNid,
         LogicalExpressionImpl logicGraph) {
      final Feature     theFeature = getFeature(featureNode.getTypeConceptNid());
      final LogicNode[] children   = featureNode.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("FeatureNode can only have one child. Concept: " + conceptNid + " graph: " +
                                         logicGraph);
      }

      final Optional<Literal> optionalLiteral = generateLiterals(children[0], getConcept(conceptNid), logicGraph);

      if (optionalLiteral.isPresent()) {
         switch (featureNode.getOperator()) {
         case EQUALS:
            return Optional.of(Factory.createDatatype(theFeature, Operator.EQUALS, optionalLiteral.get()));

         case GREATER_THAN:
            return Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN, optionalLiteral.get()));

         case GREATER_THAN_EQUALS:
            return Optional.of(Factory.createDatatype(theFeature, Operator.GREATER_THAN_EQUALS, optionalLiteral.get()));

         case LESS_THAN:
            return Optional.of(Factory.createDatatype(theFeature, Operator.LESS_THAN, optionalLiteral.get()));

         case LESS_THAN_EQUALS:
            return Optional.of(Factory.createDatatype(theFeature, Operator.LESS_THAN_EQUALS, optionalLiteral.get()));

         default:
            throw new UnsupportedOperationException(featureNode.getOperator().toString());
         }
      }

      throw new UnsupportedOperationException("Child of FeatureNode node cannot return null concept. Concept: " +
            conceptNid + " graph: " + logicGraph);
   }

   /**
    * Process necessary set.
    *
    * @param necessarySetNode the necessary set node
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    */
   private void processNecessarySet(NecessarySetNode necessarySetNode,
                                    int conceptNid,
                                    LogicalExpressionImpl logicGraph) {
      final LogicNode[] children = necessarySetNode.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("necessarySetNode can only have one child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      if (!(children[0] instanceof AndNode)) {
         throw new IllegalStateException("necessarySetNode can only have AND for a child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      final Optional<Concept> conjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph);

      if (conjunctionConcept.isPresent()) {
         this.axioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
      } else {
         throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }
   }

   /**
    * Process role node some.
    *
    * @param roleNodeSome the role node some
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    * @return the optional
    */
   private Optional<Concept> processRoleNodeSome(RoleNodeSomeWithNids roleNodeSome,
         int conceptNid,
         LogicalExpressionImpl logicGraph) {
      final Role        theRole  = getRole(roleNodeSome.getTypeConceptNid());
      final LogicNode[] children = roleNodeSome.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("RoleNodeSome can only have one child. Concept: " + conceptNid + " graph: " +
                                         logicGraph);
      }

      final Optional<Concept> restrictionConcept = generateAxioms(children[0], conceptNid, logicGraph);

      if (restrictionConcept.isPresent()) {
         return Optional.of(Factory.createExistential(theRole, restrictionConcept.get()));
      }

      throw new UnsupportedOperationException("Child of role node can not return null concept. Concept: " +
            conceptNid + " graph: " + logicGraph);
   }

   /**
    * Process root.
    *
    * @param logicNode the logic node
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    * @throws IllegalStateException the illegal state exception
    */
   private void processRoot(LogicNode logicNode,
                            int conceptNid,
                            LogicalExpressionImpl logicGraph)
            throws IllegalStateException {
      final RootNode rootNode = (RootNode) logicNode;

      for (final LogicNode child: rootNode.getChildren()) {
         final Optional<Concept> axiom = generateAxioms(child, conceptNid, logicGraph);

         if (axiom.isPresent()) {
            throw new IllegalStateException("Children of root logicNode should not return axioms. Concept: " +
                                            conceptNid + " graph: " + logicGraph);
         }
      }
   }

   /**
    * Process sufficient set.
    *
    * @param sufficientSetNode the sufficient set node
    * @param conceptNid the concept nid
    * @param logicGraph the logic graph
    */
   private void processSufficientSet(SufficientSetNode sufficientSetNode,
                                     int conceptNid,
                                     LogicalExpressionImpl logicGraph) {
      final LogicNode[] children = sufficientSetNode.getChildren();

      if (children.length != 1) {
         throw new IllegalStateException("SufficientSetNode can only have one child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      if (!(children[0] instanceof AndNode)) {
         throw new IllegalStateException("SufficientSetNode can only have AND for a child. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }

      final Optional<Concept> conjunctionConcept = generateAxioms(children[0], conceptNid, logicGraph);

      if (conjunctionConcept.isPresent()) {
         this.axioms.add(new ConceptInclusion(getConcept(conceptNid), conjunctionConcept.get()));
         this.axioms.add(new ConceptInclusion(conjunctionConcept.get(), getConcept(conceptNid)));
      } else {
         throw new IllegalStateException("Child node must return a conjunction concept. Concept: " + conceptNid +
                                         " graph: " + logicGraph);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the axioms.
    *
    * @return the axioms
    */
   public Set<Axiom> getAxioms() {
      return this.axioms;
   }

   /**
    * Gets the concept.
    *
    * @param name the name
    * @return the concept
    */
   private Concept getConcept(int name) {
      final Optional<Concept> optionalConcept = this.nidLogicConceptMap.getOptional(name);

      if (optionalConcept.isPresent()) {
         return optionalConcept.get();
      }
      Concept concept = Factory.createNamedConcept(Integer.toString(name));
      this.nidLogicConceptMap.put(name, concept);
      return concept;
   }

   /**
    * Gets the feature.
    *
    * @param name the name
    * @return the feature
    */
   private Feature getFeature(int name) {

      final Feature feature = this.nidLogicFeatureMap.get(name);

      if (feature != null) {
         return feature;
      }

      this.nidLogicFeatureMap.putIfAbsent(name, Factory.createNamedFeature(Integer.toString(name)));
      return this.nidLogicFeatureMap.get(name);
   }

   /**
    * Gets the loaded concepts.
    *
    * @return the loaded concepts
    */
   public Set<Integer> getLoadedConcepts() {
      return this.loadedConceptNids;
   }

   /**
    * Gets the role.
    *
    * @param name the name
    * @return the role
    */
   private Role getRole(int name) {
 
      final Role role = this.nidLogicRoleMap.get(name);

      if (role != null) {
         return role;
      }

      this.nidLogicRoleMap.putIfAbsent(name, Factory.createNamedRole(Integer.toString(name)));
      return this.nidLogicRoleMap.get(name);
   }
}

