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



package sh.isaac.model.logic.node.internal;

//~--- JDK imports ------------------------------------------------------------


import java.util.Arrays;
import org.apache.mahout.math.set.OpenIntHashSet;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.ConnectorNode;
import sh.isaac.model.logic.node.external.TypedNodeWithUuids;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/9/14.
 */
public abstract class TypedNodeWithNids
        extends ConnectorNode {
   /** The type concept nid. */
   int typeConceptNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new typed node with sequences.
    *
    * @param externalForm the external form
    */
   public TypedNodeWithNids(TypedNodeWithUuids externalForm) {
      super(externalForm);
      this.typeConceptNid = Get.identifierService()
                                    .getNidForUuids(externalForm.getTypeConceptUuid());
   }

   /**
    * Instantiates a new typed node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public TypedNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                 ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.typeConceptNid = dataInputStream.getInt();
   }

   /**
    * Instantiates a new typed node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptId the type concept id
    * @param child the child
    */
   public TypedNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                 int typeConceptId,
                                 AbstractLogicNode child) {
      super(logicGraphVersion, child);
      this.typeConceptNid = typeConceptId;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the concepts referenced by node.
    *
    * @param conceptSequenceSet the concept nid set
    */
   @Override
   public void addConceptsReferencedByNode(OpenIntHashSet conceptSequenceSet) {
      super.addConceptsReferencedByNode(conceptSequenceSet);
      conceptSequenceSet.add(this.typeConceptNid);
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final TypedNodeWithNids other = (TypedNodeWithNids) obj;

      if (this.typeConceptNid != other.typeConceptNid) {
         return false;
      }

      return super.equals(obj);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 97 * hash + this.typeConceptNid;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString("");
   }

   /**
    * To string.
    *
    * @param nodeIdSuffix the node id suffix
    * @return the string
    */
   @Override
   public String toString(String nodeIdSuffix) {
      return " " + Get.conceptDescriptionText(this.typeConceptNid) + " <" +
             this.typeConceptNid + ">" + super.toString(nodeIdSuffix);
   }
   @Override
   public String toSimpleString() {
      return " " + Get.defaultCoordinate().getPreferredDescriptionText(this.typeConceptNid) + " " + super.toSimpleString();
   }

   /**
    * Compare node fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected final int compareNodeFields(LogicNode o) {
      // node semantic already determined equals.
      final TypedNodeWithNids other = (TypedNodeWithNids) o;

      if (this.typeConceptNid != other.typeConceptNid) {
         return Integer.compare(this.typeConceptNid, other.typeConceptNid);
      }

      return compareTypedNodeFields(o);
   }

   /**
    * Compare typed node fields.
    *
    * @param o the o
    * @return the int
    */
   protected abstract int compareTypedNodeFields(LogicNode o);

   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   @Override
   protected void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
      super.writeData(dataOutput, dataTarget);
      dataOutput.putInt(this.typeConceptNid);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the only child.
    *
    * @return the only child
    */
   public LogicNode getOnlyChild() {
      final LogicNode[] children = getChildren();

      if (children.length == 1) {
         return children[0];
      }

      throw new IllegalStateException("Typed nodes can have only one child. Found: " + Arrays.toString(children));
   }

   /**
    * Gets the type concept nid.
    *
    * @return the type concept nid
    */
   public int getTypeConceptNid() {
      return this.typeConceptNid;
   }
   public void setTypeConceptNid(int typeConceptNid) {
      this.typeConceptNid = typeConceptNid;
   }
}

