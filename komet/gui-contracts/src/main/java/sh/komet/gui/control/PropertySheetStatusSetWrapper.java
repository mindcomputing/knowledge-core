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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class PropertySheetStatusSetWrapper implements PropertySheet.Item {

    private final SetProperty<Status> statusSetProperty;
    private final String name;
    private ConceptSpecification propertySpecification = null;

    public PropertySheetStatusSetWrapper(String name, SetProperty<Status> statusSetProperty) {
        if (statusSetProperty == null) {
            throw new NullPointerException("statusSetProperty cannot be null");
        }
        this.name = name;
        this.statusSetProperty = statusSetProperty;
    }

    public PropertySheetStatusSetWrapper(Manifold manifold,
            SetProperty<Status> statusSetProperty) {
        this(manifold.getPreferredDescriptionText(new ConceptProxy(statusSetProperty.getName()),
                ConceptSpecification.getNameFromExternalString(statusSetProperty.getName())),
                statusSetProperty);
    }
    public ConceptSpecification getSpecification() {
        if (this.propertySpecification == null) {
            this.propertySpecification = new ConceptProxy(this.statusSetProperty.getName());
        }
        return this.propertySpecification;
    }

    @Override
    public Class<?> getType() {
        return ObservableSet.class;
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
        return "Specify the set of allowed status values";
    }

    @Override
    public ObservableSet<Status> getValue() {
        return statusSetProperty.get();
    }

    @Override
    public void setValue(Object value) {
        statusSetProperty.set((ObservableSet<Status>) value);
    }
    
    public List<ObservableSet<Status>> getAllowedValues() {
       ObservableSet[] allowedValues = {
           FXCollections.observableSet(Status.ACTIVE_ONLY_SET),
           FXCollections.observableSet(Status.ANY_STATUS_SET),
           FXCollections.observableSet(Status.INACTIVE_STATUS_SET)
           
       }; 
       return Arrays.asList(allowedValues);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(statusSetProperty);
    }

}
