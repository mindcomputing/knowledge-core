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
package sh.isaac.api.query;

import java.util.OptionalInt;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.api.xml.ConceptSpecificationAdaptor;

/**
 * The SortSpecification provides the data necessary to convert a
 component nid into format for sorting results. The query 
 returns an array of 1 or more nids for each row, which needs to be processed into 
 the result set. Each nid is a member of an assemblage
 that was specified in FOR clause of the query. A list of SortSpecification 
 elements will expand the array of nids into an array of Strings that will be 
 * used to sort the rows prior to applying the attribute functions. 
 * 
 * @author kec
 */
@XmlRootElement(name = "SortSpecification")
@XmlAccessorType(value = XmlAccessType.NONE)
public class SortSpecification implements QueryFieldSpecification {
     /**
     * The index of the property on the version of the chronology to 
     * process for this function. The property index is based on the ordered 
     * list of properties provided by the ObservableVersion.getProperties() method. 
     */
    private final SimpleIntegerProperty propertyIndexProperty;

    /**
     * The name of the column to include in the results. 
     */
    private final SimpleStringProperty columnNameProperty;
    
    /**
     * The key for the stamp coordinate from which to determine the version of the 
     * chronology to process. The nid is obtained by finding the nid in the nid array 
     * which is a member of the specified assemblage. 
     */
    private final SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty;

    /**
     * The assemblage from which the nid to process must be an element of. 
     */
    private final SimpleIntegerProperty assemblageNidProperty;
    /**
     * Possibly null cell function to apply to the property value to generate the 
     * result value. 
     */
    
    private final SimpleObjectProperty<AttributeFunction> attributeFunctionProperty;
    
    /**
     * The concept that specifies the property on the version of the chronology to 
     * process for this function. Not used in result set generation. 
     */
    private final SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty;
    
    private final SimpleObjectProperty<TableColumn.SortType> sortTypeProperty;

    /**
     * No arg constructor for Jaxb. 
     */
    public SortSpecification() {
        this.attributeFunctionProperty = new SimpleObjectProperty();
        this.columnNameProperty = new SimpleStringProperty();
        this.assemblageNidProperty = new SimpleIntegerProperty();
        this.propertySpecificationProperty = new SimpleObjectProperty();
        this.propertyIndexProperty = new SimpleIntegerProperty();
        this.sortTypeProperty = new SimpleObjectProperty<>(TableColumn.SortType.ASCENDING);
        this.stampCoordinateKeyProperty  = new SimpleObjectProperty();
    }
    
    public SortSpecification(SortSpecification another) {
        this.attributeFunctionProperty = new SimpleObjectProperty(another.attributeFunctionProperty.get());
        this.columnNameProperty = new SimpleStringProperty(another.columnNameProperty.get());
        
        /**
         * The assemblage from which to select the nid to process. 
         */
        this.assemblageNidProperty = new SimpleIntegerProperty(another.assemblageNidProperty.get());
        this.propertySpecificationProperty = new SimpleObjectProperty(another.propertySpecificationProperty.get());
        this.propertyIndexProperty = new SimpleIntegerProperty(another.propertyIndexProperty.get());
        this.sortTypeProperty = new SimpleObjectProperty<>(another.sortTypeProperty.get());
        this.stampCoordinateKeyProperty  = new SimpleObjectProperty(another.getStampCoordinateKey());
    }
    
    public SortSpecification(
            AttributeFunction attributeFunction, String columnName, int assemblageNid,
            ConceptSpecification propertySpecification, TableColumn.SortType sortType, 
            LetItemKey stampCoordinateKey, int propertyIndex) {
        this.attributeFunctionProperty = new SimpleObjectProperty(attributeFunction);
        this.columnNameProperty = new SimpleStringProperty(columnName);
        this.assemblageNidProperty = new SimpleIntegerProperty(assemblageNid);
        this.propertySpecificationProperty = new SimpleObjectProperty(propertySpecification);
        this.sortTypeProperty = new SimpleObjectProperty<>(sortType);
        this.propertyIndexProperty = new SimpleIntegerProperty(propertyIndex);
        this.stampCoordinateKeyProperty = new SimpleObjectProperty(stampCoordinateKey);
    }
    
    @XmlElement
    @Override
    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKeyProperty.get();
    }

    @Override
    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKeyProperty.set(stampCoordinateKey);
    }

    @Override
    public SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty() {
        return stampCoordinateKeyProperty;
    }

    public SimpleObjectProperty<TableColumn.SortType> sortTypeProperty() {
        return sortTypeProperty;
    }

    @XmlElement
    public TableColumn.SortType getSortType() {
        return sortTypeProperty.get();
    }

    public void setSortType(TableColumn.SortType sortType) {
        sortTypeProperty.set(sortType);
    }


    @XmlElement
    @XmlJavaTypeAdapter(ConceptSpecificationAdaptor.class)
    @Override
    public ConceptSpecification getPropertySpecification() {
        return this.propertySpecificationProperty.get();
    }
    @Override
    public void setPropertySpecification(ConceptSpecification propertySpecification) {
        this.propertySpecificationProperty.set(propertySpecification);
    }
    
    @Override
    public SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty() {
        return this.propertySpecificationProperty;
    }

    @XmlAttribute
    @Override
    public Integer getPropertyIndex() {
        return this.propertyIndexProperty.get();
    }
    @Override
    public void setPropertyIndex(Integer propertyIndex) {
        this.propertyIndexProperty.set(propertyIndex);
    }
    
    @Override
    public SimpleIntegerProperty propertyIndexProperty() {
        return this.propertyIndexProperty;
    }

    @Override
    public int getAssemblageNid() {
        return this.assemblageNidProperty.get();
    }
    @Override
    public void setAssemblageNid(int assemblageNid) {
        this.assemblageNidProperty.set(assemblageNid);
    }
    
    @Override
    public SimpleIntegerProperty assemblageNidProperty() {
        return this.assemblageNidProperty;
    }
    
    @XmlElement
    @XmlJavaTypeAdapter(ConceptSpecificationAdaptor.class)
    @Override
     public ConceptSpecification getAssemblage() {
         if (this.assemblageNidProperty.get() == 0) {
             return null;
         }
        return new ConceptProxy(this.assemblageNidProperty.get());
    }
    @Override
     public void setAssemblage(ConceptSpecification specification) {
         this.assemblageNidProperty.set(specification.getNid());
        
    }

    @Override
    public void setAssemblageUuid(ConceptSpecification assemblageConceptSpecification) {
        setAssemblageNid(assemblageConceptSpecification.getNid());
    }

    @XmlElement(name = "attributeFunction")
    @Override
    public AttributeFunction getAttributeFunction() {
        return attributeFunctionProperty.get();
    }

    @Override
    public SimpleObjectProperty<AttributeFunction> attributeFunctionProperty() {
        return attributeFunctionProperty;
    }

    @Override
    public void setAttributeFunction(AttributeFunction attributeFunction) {
        this.attributeFunctionProperty.set(attributeFunction);
    }

    @XmlAttribute(name = "columnName")
    @Override
    public String getColumnName() {
        return columnNameProperty.get();
    }

    @Override
    public SimpleStringProperty columnNameProperty() {
        return columnNameProperty;
    }

    @Override
    public void setColumnName(String columnName) {
        this.columnNameProperty.set(columnName);
    }

    public int compare(int[] o1, int[] o2, Query q) {
        // Get index...
        int comparisonIndex = 0;
        if (o1.length != 1) {
            for (int i = 0; i < o1.length; i++) {
                OptionalInt optionalAssemblageNid = Get.identifierService().getAssemblageNid(o1[i]);
                if (optionalAssemblageNid.isPresent() &&
                        optionalAssemblageNid.getAsInt() == getAssemblageNid()) {
                    comparisonIndex = i;
                    break;
                }
            }
        }
        StampCoordinate stampCoordinate = (StampCoordinate) q.getLetDeclarations().get(getStampCoordinateKey());
        
        ObservableChronology o1Chronology = Get.observableChronologyService().getObservableChronology(o1[comparisonIndex]);
        LatestVersion<? extends ObservableVersion>  o1LatestVersion = o1Chronology.getLatestVersion(stampCoordinate);

        ObservableChronology o2Chronology = Get.observableChronologyService().getObservableChronology(o2[comparisonIndex]);
        LatestVersion<? extends ObservableVersion>  o2LatestVersion = o2Chronology.getLatestVersion(stampCoordinate);

        String o1String = getAttributeFunction().apply(
                o1LatestVersion.get().getProperties().get(getPropertyIndex()).getValue().toString(), 
                stampCoordinate, q);
        String o2String = getAttributeFunction().apply(
                o2LatestVersion.get().getProperties().get(getPropertyIndex()).getValue().toString(), 
                stampCoordinate, q);
        
        int comparison = NaturalOrder.compareStrings(o1String, o2String);
        if (getSortType() == TableColumn.SortType.ASCENDING) {
            return comparison;
        }
        return -comparison;
    }
    
    
}
