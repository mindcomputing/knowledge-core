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



package sh.isaac.api.chronicle;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;

import java.util.Locale;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum VersionType.
 *
 * @author kec
 */
public enum VersionType {
   /** The member. */
   MEMBER((byte) 0, "Member", "MBR"),

   /** A component nid version*/
   COMPONENT_NID((byte) 1, "Component Nid", "REF"),

   /** A long version. */
   LONG((byte) 2, "Long", "INT"),

   /** A logic graph version. */
   LOGIC_GRAPH((byte) 4, "Logic Graph", "DEF"),

   /** A string version. */
   STRING((byte) 5, "String", "STR"),

   /** A dynamic version. */
   DYNAMIC((byte) 6, "Dynamic", "DYN"),

   /** A description version. */
   DESCRIPTION((byte) 7, "Description", "DESC"),

   /* A relationship adaptor version -- deprecated/removed. */
   //RELATIONSHIP_ADAPTOR((byte) 8, "Relationship Adapter"),
   
   /** A concept version */
   CONCEPT((byte) 9, "Concept", "CON"),
   
   /** An RF2 relationship for backwards compatibility. */
   RF2_RELATIONSHIP((byte) 10, "RF2 Relationship", "REL"),
   
   /** An LOINC record. */
   LOINC_RECORD((byte) 11, "LOINC Record", "LOINC"),
   
   // Ideally, all of the below would be represented as dynamic semantics, 
   // but quick, removable implementation for now. 
   
   MEASURE_CONSTRAINTS((byte) (Byte.MAX_VALUE - 13), "Measure constraints", "Measure constraints"),
   
   Str1_Nid2_Nid3_Nid4((byte) (Byte.MAX_VALUE - 12), "String Component Component Component", "Str1_C2_C3_C4"),
   
   Str1_Str2_Nid3_Nid4_Nid5((byte) (Byte.MAX_VALUE - 11), "String String Component Component Component", "Str1_Str2_C3_C4_C5"),
   
   Nid1_Nid2((byte) (Byte.MAX_VALUE - 10), "Component Component", "C1_C2"),
   
   Nid1_Nid2_Int3((byte) (Byte.MAX_VALUE - 9), "Component Component Integer", "C1_C2_Int3"),
   
   Nid1_Nid2_Str3((byte) (Byte.MAX_VALUE - 8), "Component Component String", "C1_C2_Str3"),
   
   Nid1_Int2((byte) (Byte.MAX_VALUE - 7), "Component Integer", "C1_Int2"),
   
   Nid1_Str2((byte) (Byte.MAX_VALUE - 6), "Component String", "C1_Str2"),

   Nid1_Int2_Str3_Str4_Nid5_Nid6((byte) (Byte.MAX_VALUE - 5), "Component Integer String String Component Component", "C1_Int2_Str3_Str4_C5_C6"),
   
   Int1_Int2_Str3_Str4_Str5_Nid6_Nid7((byte) (Byte.MAX_VALUE - 4), "Integer Integer String String String Component Component", "Int1_Int2_Str3_Str4_Str5_C6_C7"),
   
   Str1_Str2((byte) (Byte.MAX_VALUE - 3), "String String", "Str1_Str2"),

   Str1_Str2_Nid3_Nid4((byte) (Byte.MAX_VALUE - 2), "String String Component Component", "Str1_Str2_C3_C4"),
   
   Str1_Str2_Str3_Str4_Str5_Str6_Str7((byte) (Byte.MAX_VALUE - 1), "String String String String String String String", "Str1_Str2_Str3_Str4_Str5_Str6_Str7"),
   
   /** An unknown type of version. */
   UNKNOWN(Byte.MAX_VALUE, "Unknown", "UNKNOWN");

   /** The semantic token. */
   final byte versionTypeToken;

   /** The nice name. */
   final String niceName;

   /** The what name for use in the what column of tables. */
   final String whatName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new semantic type.
    *
    * @param versionTypeToken the semantic token
    * @param niceName the nice name
    */
   private VersionType(byte versionTypeToken, String niceName, String whatName) {
      this.versionTypeToken = versionTypeToken;
      this.niceName   = niceName;
      this.whatName   = whatName;
   }

   //~--- methods -------------------------------------------------------------

   public String getWhatName() {
      return whatName;
   }

   /**
    * Parses the.
    *
    * @param nameOrEnumId the name or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the version type
    */
   public static VersionType parse(String nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
         return null;
      }

      final String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH)
              .trim();
      
      if (StringUtils.isBlank(clean)) {
         return null;
      }

      for (final VersionType ct: values()) {
         if (ct.name().toLowerCase(Locale.ENGLISH).equals(clean) ||
                 ct.niceName.toLowerCase(Locale.ENGLISH).equals(clean) ||
                 (ct.ordinal() + "").equals(clean)) {
            return ct;
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine VersionType from " + nameOrEnumId);
      }

      return UNKNOWN;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.niceName;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the from token.
    *
    * @param token the token
    * @return the from token
    */
   public static VersionType getFromToken(byte token) {
      switch (token) {
      case 0:
         return MEMBER;

      case 1:
         return COMPONENT_NID;

      case 2:
         return LONG;

      case 4:
         return LOGIC_GRAPH;

      case 5:
         return STRING;

      case 6:
         return DYNAMIC;

      case 7:
         return DESCRIPTION;

      case 9:
         return CONCEPT;

      case 10:
         return RF2_RELATIONSHIP;
      case 11:
          return LOINC_RECORD;
         
   // Ideally, all of the below would be represented as dynamic semantics, 
   // but quick, removable implementation for now. 
      case Byte.MAX_VALUE - 12:
         return Str1_Nid2_Nid3_Nid4;

      case Byte.MAX_VALUE - 11:
         return Str1_Str2_Nid3_Nid4_Nid5;

      case Byte.MAX_VALUE - 10:
         return Nid1_Nid2;

      case Byte.MAX_VALUE - 9:
         return Nid1_Nid2_Int3;

      case Byte.MAX_VALUE - 8:
         return Nid1_Nid2_Str3;

      case Byte.MAX_VALUE - 7:
         return Nid1_Int2;

      case Byte.MAX_VALUE - 6:
         return Nid1_Str2;

      case Byte.MAX_VALUE - 5:
         return Nid1_Int2_Str3_Str4_Nid5_Nid6;

      case Byte.MAX_VALUE - 4:
         return Int1_Int2_Str3_Str4_Str5_Nid6_Nid7;

      case Byte.MAX_VALUE - 3:
         return Str1_Str2;

      case Byte.MAX_VALUE - 2:
         return Str1_Str2_Nid3_Nid4;

      case Byte.MAX_VALUE - 1:
         return Str1_Str2_Str3_Str4_Str5_Str6_Str7;
         
      default:
         throw new UnsupportedOperationException("d Can't handle: " + token);
      }
   }

   /**
    * Gets the observable semantic version class.
    *
    * @return the observable semantic version class
    */
      public Class<? extends ObservableSemanticVersion> getObservableSemanticVersionClass() {
      switch (this) {
      case COMPONENT_NID:
         return ObservableComponentNidVersion.class;

      case DESCRIPTION:
         return ObservableDescriptionVersion.class;

      case MEMBER:
         return ObservableSemanticVersion.class;

      case DYNAMIC:

      // TODO implement Observable pattern
      case LOGIC_GRAPH:

      // TODO implement Observable pattern
      case LONG:

      // TODO implement Observable pattern
      case STRING:

      // TODO implement Observable pattern
      default:
         throw new RuntimeException("f Can't handle: " + this);
      }
   }

   /**
    * Gets the version type token.
    *
    * @return the version type token
    */
   public byte getVersionTypeToken() {
      return this.versionTypeToken;
   }

   /**
    * Gets the version class.
    *
    * @return the version class
    */
      public Class<? extends SemanticVersion> getVersionClass() {
      switch (this) {
      case COMPONENT_NID:
         return ComponentNidVersion.class;

      case DESCRIPTION:
         return DescriptionVersion.class;

      case MEMBER:
         return SemanticVersion.class;

      case DYNAMIC:
         return DynamicVersion.class;

      case LOGIC_GRAPH:
         return LogicGraphVersion.class;

      case LONG:
         return LongVersion.class;

      case STRING:
         return StringVersion.class;

      default:
         throw new RuntimeException("g Can't handle: " + this);
      }
   }
}

