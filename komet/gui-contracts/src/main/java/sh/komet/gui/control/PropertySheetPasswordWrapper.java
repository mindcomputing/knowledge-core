/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.control;

import java.util.Optional;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetPasswordWrapper implements PropertySheet.Item {

   private final String name;
   private final StringProperty textProperty;

   public PropertySheetPasswordWrapper(String name, StringProperty textProperty) {
      this.name = name;
      this.textProperty = textProperty;
   }

   public PropertySheetPasswordWrapper(Manifold manifold,
           StringProperty textProperty) {
      this(manifold.getPreferredDescriptionText(new ConceptProxy(textProperty.getName())), 
              textProperty);
   }

   @Override
   public Class<?> getType() {
      return String.class;
   }

   @Override
   public String getCategory() {
      return null;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getDescription() {
      return "Enter the text for the version you wish to create";
   }

   @Override
   public String getValue() {
      return textProperty.get();
   }

   @Override
   public void setValue(Object value) {
      textProperty.setValue((String) value);
   }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(textProperty);
   }
   
}
