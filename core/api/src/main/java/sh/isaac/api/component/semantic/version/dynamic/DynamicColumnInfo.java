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



package sh.isaac.api.component.semantic.version.dynamic;

import java.util.Arrays;
import java.util.UUID;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicFloat;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;

/**
 * {@link DynamicColumnInfo}
 *
 * A user friendly class for containing the information parsed out of the Assemblage concepts which defines the Dynamic.
 * See the class description for {@link DynamicUsageDescription} for more details.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicColumnInfo
         implements Comparable<DynamicColumnInfo> {

   private transient String columnName;
   private transient String columnDescription;

   private UUID columnDescriptionConceptUUID;
   private int columnOrder;
   private UUID assemblageConcept;
   private DynamicDataType columnDataType;
   private DynamicData defaultData;
   private boolean columnRequired;
   private DynamicValidatorType[] validatorType;
   private DynamicData[] validatorData;

   /**
    * Useful for building up a new one step by step.
    */
   public DynamicColumnInfo() {}

   /**
    * calls {@link #DynamicColumnInfo(UUID, int, UUID, DynamicDataType, DynamicData, Boolean, DynamicValidatorType[], DynamicData[])}
    * with a null assemblage concept, null validator info.
    *
    * @param columnOrder the (0 indexed) column order
    * @param columnDescriptionConcept the column description concept
    * @param columnDataType the column data type
    * @param defaultData the default data
    * @param columnRequired the column required
    */
   public DynamicColumnInfo(int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicDataType columnDataType,
                                  DynamicData defaultData,
                                  Boolean columnRequired) {
      this(null, columnOrder, columnDescriptionConcept, columnDataType, defaultData, columnRequired, null, null);
   }

   /**
    * calls {@link #DynamicColumnInfo(UUID, int, UUID, DynamicDataType, DynamicData, Boolean, DynamicValidatorType[], DynamicData[])}
    * with a null assemblage concept, and a single array item for the validator info.
    *
    * @param columnOrder the (0 indexed) column order
    * @param columnDescriptionConcept the column description concept
    * @param columnDataType the column data type
    * @param defaultData the default data
    * @param columnRequired the column required
    * @param validatorType the validator type
    * @param validatorData the validator data
    */
   public DynamicColumnInfo(int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicDataType columnDataType,
                                  DynamicData defaultData,
                                  Boolean columnRequired,
                                  DynamicValidatorType validatorType,
                                  DynamicData validatorData) {
      this(null,
           columnOrder,
           columnDescriptionConcept,
           columnDataType,
           defaultData,
           columnRequired,
           (validatorType == null) ? null
                                   : new DynamicValidatorType[] { validatorType },
           (validatorData == null) ? null
                                   : new DynamicData[] { validatorData });
   }

   /**
    * calls {@link #DynamicColumnInfo(UUID, int, UUID, DynamicDataType, DynamicData, Boolean, DynamicValidatorType[], DynamicData[])}
    * with a null assemblage concept.
    *
    * @param columnOrder the (0 indexed) column order
    * @param columnDescriptionConcept the column description concept
    * @param columnDataType the column data type
    * @param defaultData the default data
    * @param columnRequired the column required
    * @param validatorType the validator type
    * @param validatorData the validator data
    */
   public DynamicColumnInfo(int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicDataType columnDataType,
                                  DynamicData defaultData,
                                  Boolean columnRequired,
                                  DynamicValidatorType[] validatorType,
                                  DynamicData[] validatorData) {
      this(null,
           columnOrder,
           columnDescriptionConcept,
           columnDataType,
           defaultData,
           columnRequired,
           validatorType,
           validatorData);
   }

   /**
    * Create this object by reading the columnName and columnDescription from the provided columnDescriptionConcept.
    *
    * If a suitable concept to use for the column Name/Description does not yet exist, see
    * {@link Frills#createNewDynamicSemanticColumnInfoConcept(String, String)}
    *
    * and pass the result in here.
    *
    * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
    * @param columnOrder - the column order as defined in the assemblage concept
    * @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
    * @param columnDataType - the data type as defined in the assemblage concept
    * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example,
    * if columnDataType is set to {@link DynamicDataType#FLOAT} then this field must be a {@link DynamicFloat}.
    * @param columnRequired - Is this column required when creating an instance of the refex?  True for yes, false or null for no.
    * @param validatorType - The Validator to use when creating an instance of this Refex.  Null for no validator
    * @param validatorData - The data required to execute the validatorType specified.  The format and type of this will depend on the
    * validatorType field.  See {@link DynamicValidatorType} for details on the valid data for this field.  Should be null when validatorType is null.
    */
   public DynamicColumnInfo(UUID assemblageConcept,
                                  int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicDataType columnDataType,
                                  DynamicData defaultData,
                                  Boolean columnRequired,
                                  DynamicValidatorType[] validatorType,
                                  DynamicData[] validatorData) {
      this.assemblageConcept            = assemblageConcept;
      this.columnOrder                  = columnOrder;
      this.columnDescriptionConceptUUID = columnDescriptionConcept;
      this.columnDataType               = columnDataType;
      this.defaultData                  = defaultData;
      this.columnRequired               = ((columnRequired == null) ? false
            : columnRequired);
      this.validatorType                = validatorType;
      this.validatorData                = validatorData;
   }

   @Override
   public int compareTo(DynamicColumnInfo o) {
      return Integer.compare(this.getColumnOrder(), o.getColumnOrder());
   }

   @Override
   public String toString() {
      return "DynamicColumnInfo [columnName_=" + this.columnName + ", columnDescription_=" +
             this.columnDescription + ", columnOrder_=" + this.columnOrder + ", assemblageConcept_=" +
             this.assemblageConcept + ", columnDataType_=" + this.columnDataType + ", defaultData_=" +
             this.defaultData + ", columnRequired_=" + this.columnRequired + ", validatorType_=" +
             Arrays.toString(this.validatorType) + ", validatorData_=" + Arrays.toString(this.validatorData) + "]";
   }

   /**
    * Read.
    */
   private void read() {
      final DynamicColumnUtility util = LookupService.get()
                                                           .getService(DynamicColumnUtility.class);

      if (util == null) {
         this.columnName        = "Unable to locate reader!";
         this.columnDescription = "Unable to locate reader!";
      } else {
         final String[] temp = util.readDynamicColumnNameDescription(this.columnDescriptionConceptUUID);

         this.columnName        = temp[0];
         this.columnDescription = temp[1];
      }
   }

   /**
    * Gets the assemblage concept.
    *
    * @return the UUID of the assemblage concept that this column data was read from
    * or null in the case where this column is not yet associated with an assemblage.
    */
   public UUID getAssemblageConcept() {
      return this.assemblageConcept;
   }

   /**
    * Sets the assemblage concept.
    *
    * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
    */
   public void setAssemblageConcept(UUID assemblageConcept) {
      this.assemblageConcept = assemblageConcept;
   }

   /**
    * Gets the column data type.
    *
    * @return The defined data type for this column of the semantic.  Note that this value will be identical to the {@link DynamicDataType}
    * returned by {@link DynamicData} EXCEPT for cases where this returns {@link DynamicDataType#POLYMORPHIC}.  In those cases, the
    * data type can only be determined by examining the actual member data in {@link DynamicData}
    */
   public DynamicDataType getColumnDataType() {
      return this.columnDataType;
   }

   /**
    * Sets the column data type.
    *
    * @param columnDataType - the data type as defined in the assemblage concept
    */
   public void setColumnDataType(DynamicDataType columnDataType) {
      this.columnDataType = columnDataType;
   }

   /**
    * Sets the column default data.
    *
    * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example,
    * if columnDataType is set to {@link DynamicDataType#FLOAT} then this field must be a {@link DynamicFloat}.
    */
   public void setColumnDefaultData(DynamicData defaultData) {
      this.defaultData = defaultData;
   }

   /**
    * Gets the column description.
    *
    * @return The user-friendly description of this column of data.  To be used by GUIs to provide a more detailed explanation of
    * the type of data found in this column.
    */
   public String getColumnDescription() {
      if (this.columnDescription == null) {
         read();
      }

      return this.columnDescription;
   }

   /**
    * Gets the column description concept.
    *
    * @return The UUID of the concept where the columnName and columnDescription were read from.
    */
   public UUID getColumnDescriptionConcept() {
      return this.columnDescriptionConceptUUID;
   }

   /**
    * Sets the column description concept.
    *
    * @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
    */
   public void setColumnDescriptionConcept(UUID columnDescriptionConcept) {
      this.columnDescriptionConceptUUID = columnDescriptionConcept;
      this.columnName                   = null;
      this.columnDescription            = null;
   }

   /**
    * Gets the column name.
    *
    * @return The user-friendly name of this column of data.  To be used by GUIs to label the data in this column.
    */
   public String getColumnName() {
      if (this.columnName == null) {
         read();
      }

      return this.columnName;
   }

   /**
    * Gets the column order.
    *
    * @return Defined the order in which the data columns will be stored, so that the column name / description can be aligned
    * with the {@link DynamicData} columns in the {@link DynamicVersion#getData(int)}.
    *
    * Note, this value is 0 indexed (It doesn't start at 1)
    */
   public int getColumnOrder() {
      return this.columnOrder;
   }

   /**
    * Sets the column order.
    *
    * @param columnOrder - the column order as defined in the assemblage concept
    */
   public void setColumnOrder(int columnOrder) {
      this.columnOrder = columnOrder;
   }

   /**
    * Checks if column required.
    *
    * @return When creating this refex, must this column be provided?
    */
   public boolean isColumnRequired() {
      return this.columnRequired;
   }

   /**
    * Sets the column required.
    *
    * @param columnRequired - Is this column required when creating an instance of the semantic?  True for yes, false or null for no.
    */
   public void setColumnRequired(boolean columnRequired) {
      this.columnRequired = columnRequired;
   }

   /**
    * Gets the default column value.
    *
    * @return the default value to use for this column, if no value is specified in a refex that is created using this column info
    */
   public DynamicData getDefaultColumnValue() {
      // Handle folks sending empty strings gracefully
      if ((this.defaultData != null) &&
            (this.defaultData instanceof DynamicString) &&
            ((DynamicString) this.defaultData).getDataString().length() == 0) {
         return null;
      }

      return this.defaultData;
   }

   /**
    * Gets the validator.
    *
    * @return The type of the validator(s) (if any) which must be used to validate user data before accepting the refex
    */
   public DynamicValidatorType[] getValidator() {
      return this.validatorType;
   }

   /**
    * Gets the validator data.
    *
    * @return the validator data
    */
   public DynamicData[] getValidatorData() {
      return this.validatorData;
   }

   /**
    * Sets the validator data.
    *
    * @param validatorData the new validator data
    */
   public void setValidatorData(DynamicData[] validatorData) {
      this.validatorData = validatorData;
   }

   /**
    * Sets the validator type.
    *
    * @param validatorType - The Validator(s) to use when creating an instance of this semantic.  Null for no validator
    */
   public void setValidatorType(DynamicValidatorType[] validatorType) {
      this.validatorType = validatorType;
   }
}