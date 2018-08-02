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



package sh.isaac.model.semantic.types;

import java.nio.charset.StandardCharsets;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicStringImpl}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicStringImpl
        extends DynamicDataImpl
         implements DynamicString {
   /** The property. */
   private ObjectProperty<String> property;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic string impl.
    *
    * @param data the data
    */
   protected DynamicStringImpl(byte[] data) {
      super(data);
   }

   /**
    * Instantiates a new dynamic string impl.
    *
    * @param string the string
    */
   public DynamicStringImpl(String string) {
      super();

      if (string == null) {
         throw new RuntimeException("The string value cannot be null", null);
      }

      this.data = string.getBytes(StandardCharsets.UTF_8);
   }

   /**
    * Instantiates a new dynamic string impl.
    *
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   protected DynamicStringImpl(byte[] data, int assemblageSequence, int columnNumber) {
      super(data, assemblageSequence, columnNumber);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getDataObject() {
      return getDataString();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ReadOnlyObjectProperty<?> getDataObjectProperty() {
      return getDataStringProperty();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getDataString() {
      return new String(this.data, StandardCharsets.UTF_8);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public ReadOnlyObjectProperty<String> getDataStringProperty() {
      if (this.property == null) {
         this.property = new SimpleObjectProperty<>(null, getName(), getDataString());
      }

      return this.property;
   }
}

