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



package sh.isaac.model.logic.node;

//~--- JDK imports ------------------------------------------------------------


import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/10/14.
 */
public class LiteralNodeBoolean
        extends LiteralNode {
   /** The literal value. */
   boolean literalValue;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new literal node boolean.
    *
    * @param logicGraphVersion the logic graph version
    * @param literalValue the literal value
    */
   public LiteralNodeBoolean(LogicalExpressionImpl logicGraphVersion, boolean literalValue) {
      super(logicGraphVersion);
      this.literalValue = literalValue;
   }

   /**
    * Instantiates a new literal node boolean.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public LiteralNodeBoolean(LogicalExpressionImpl logicGraphVersion,
                             ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.literalValue = dataInputStream.getBoolean();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      if (!super.equals(o)) {
         return false;
      }

      final LiteralNodeBoolean that = (LiteralNodeBoolean) o;

      return this.literalValue == that.literalValue;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + (this.literalValue ? 1
            : 0);
      return result;
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
      return "Boolean literal[" + getNodeIndex() + nodeIdSuffix + "]" + this.literalValue +
             super.toString(nodeIdSuffix);
   }
   @Override
   public String toSimpleString() {
      return this.literalValue +  super.toSimpleString();
   }

    @Override
    public void addToBuilder(StringBuilder builder) {
            builder.append("\n       BooleanLiteral(");
            builder.append(this.literalValue);
            builder.append(")\n");
    }
   /**
    * Compare fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected int compareFields(LogicNode o) {
      final LiteralNodeBoolean that = (LiteralNodeBoolean) o;

      return Boolean.compare(this.literalValue, that.literalValue);
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), Boolean.toString(this.literalValue));
   }

   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   @Override
   protected void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
      super.writeData(dataOutput, dataTarget);
      dataOutput.putBoolean(this.literalValue);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the literal value.
    *
    * @return the literal value
    */
   public boolean getLiteralValue() {
      return this.literalValue;
   }

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.LITERAL_BOOLEAN;
   }
}

