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



package sh.isaac.utility;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.UUID;
import sh.isaac.MetaData;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.LanguageCode;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 * It would be nice if these were part of the LanguageCode class itself... but there are dependency problems preventing that.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LanguageMap {
   /**
    * Gets the concept for language code.
    *
    * @param lc the lc
    * @return the concept for language code
    */
   public static ConceptSpecification getConceptForLanguageCode(LanguageCode lc) {
      switch (lc) {
      case CS:
         return MetaData.CZECH_LANGUAGE____SOLOR;
      case EN:
      case EN_AU:
      case EN_BZ:
      case EN_CA:
      case EN_GB:
      case EN_IE:
      case EN_JM:
      case EN_NZ:
      case EN_TT:
      case EN_US:
      case EN_ZA:
         return MetaData.ENGLISH_LANGUAGE____SOLOR;

      case ES:
      case ES_AR:
      case ES_BO:
      case ES_CL:
      case ES_CO:
      case ES_CR:
      case ES_DO:
      case ES_EC:
      case ES_ES:
      case ES_GT:
      case ES_HN:
      case ES_NI:
      case ES_MX:
      case ES_PA:
      case ES_PE:
      case ES_PY:
      case ES_SV:
      case ES_UY:
      case ES_VE:
         return MetaData.SPANISH_LANGUAGE____SOLOR;

      case DA:
      case DA_DK:
         return MetaData.DANISH_LANGUAGE____SOLOR;

      case DE:
    	  return MetaData.GERMAN_LANGUAGE____SOLOR;
      case FR:
      case FR_BE:
      case FR_CA:
      case FR_CH:
      case FR_FR:
      case FR_LU:
      case FR_MC:
         return MetaData.FRENCH_LANGUAGE____SOLOR;
      case GA:
         return MetaData.IRISH_LANGUAGE____SOLOR;
      case IT:
          return MetaData.ITALIAN_LANGUAGE____SOLOR;
      case KO:
         return MetaData.KOREAN_LANGUAGE____SOLOR;
      case LT:
      case LT_LT:
         return MetaData.LITHUANIAN_LANGUAGE____SOLOR;

      case NL:
         return MetaData.DUTCH_LANGUAGE____SOLOR;

      case PL:
         return MetaData.POLISH_LANGUAGE____SOLOR;
         
      case RU:
         return MetaData.RUSSIAN_LANGUAGE____SOLOR;

      case SV:
      case SV_FI:
      case SV_SE:
         return MetaData.SWEDISH_LANGUAGE____SOLOR;

      case ZH:
      case ZH_CHS:
      case ZH_CHT:
      case ZH_CN:
      case ZH_HK:
      case ZH_MO:
      case ZH_SG:
      case ZH_TW:
         return MetaData.CHINESE_LANGUAGE____SOLOR;

      case ZZ:
      default:
         throw new RuntimeException("Unmapped Language Code " + lc);
      }
   }
   
   /**
    * Gets the concept dialect for language code.
    * Note, this method is really incomplete.
    * TODO get rid of all of this mapping, and just properly load an iso table as part of the metadata...
    *
    * @param lc the lc
    * @return the concept for dialect language code
    */
   public static ConceptSpecification getConceptDialectForLanguageCode(LanguageCode lc) {
      switch (lc) {
      case CS:
          return MetaData.CZECH_DIALECT____SOLOR;
      case EN_GB:
          return MetaData.GB_ENGLISH_DIALECT____SOLOR;
      case EN_US:
    	  return MetaData.US_ENGLISH_DIALECT____SOLOR;
      case EN:
      case EN_AU:
      case EN_BZ:
      case EN_CA:
      case EN_IE:
      case EN_JM:
      case EN_NZ:
      case EN_TT:
      case EN_ZA:
         return MetaData.ENGLISH_DIALECT_ASSEMBLAGE____SOLOR;
      case ES:
      case ES_AR:
      case ES_BO:
      case ES_CL:
      case ES_CO:
      case ES_CR:
      case ES_DO:
      case ES_EC:
      case ES_ES:
      case ES_GT:
      case ES_HN:
      case ES_NI:
      case ES_MX:
      case ES_PA:
      case ES_PE:
      case ES_PY:
      case ES_SV:
      case ES_UY:
      case ES_VE:
         return MetaData.SPANISH_DIALECT_ASSEMBLAGE____SOLOR;
      case GA:
          return MetaData.IRISH_DIALECT____SOLOR;
      case KO:
          return MetaData.KOREAN_DIALECT____SOLOR;
      case FR:
      case FR_BE:
      case FR_CA:
      case FR_CH:
      case FR_FR:
      case FR_LU:
      case FR_MC:
         return MetaData.FRENCH_DIALECT____SOLOR;
      case RU:
          return MetaData.RUSSIAN_DIALECT____SOLOR;
      case PL:
         return MetaData.POLISH_DIALECT____SOLOR;
      case DA:
      case DA_DK:
      case LT:
      case LT_LT:
      case NL:
      case SV:
      case SV_FI:
      case SV_SE:
      case ZH:
      case ZH_CHS:
      case ZH_CHT:
      case ZH_CN:
      case ZH_HK:
      case ZH_MO:
      case ZH_SG:
      case ZH_TW:
      case ZZ:
      default:
         throw new RuntimeException("Unmapped Language Dialect " + lc);
      }
   }
   
   

   /**
    * Gets the language code for UUID.
    *
    * @param uuid the uuid
    * @return the language code for UUID
    */
   public static Optional<LanguageCode> getLanguageCodeForUUID(UUID uuid) {
      for (final LanguageCode lc: LanguageCode.values()) {
         if (lc == LanguageCode.ZZ) {
            continue;
         }

         for (final UUID itemUuid: getConceptForLanguageCode(lc).getUuids()) {
            if (itemUuid.equals(uuid)) {
               return Optional.of(lc);
            }
         }
      }

      return Optional.empty();
   }
}

