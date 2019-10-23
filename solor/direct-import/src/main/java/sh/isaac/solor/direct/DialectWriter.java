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



package sh.isaac.solor.direct;

//~--- JDK imports ------------------------------------------------------------

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

//~--- non-JDK imports --------------------------------------------------------


import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class DialectWriter
        extends TimedTaskWithProgressTracker<Void> {
   /*
    * id      effectiveTime   active  moduleId        refsetId        referencedComponentId   acceptabilityId
    * 80000517-8513-5ca0-a44c-dc66f3c3a1c6    20080731        1       900000000000207008      900000000000508004      2743026013      900000000000548007
    * 80000755-c5d9-5bd8-bb64-ab8236d240d7    20020131        1       900000000000207008      900000000000509007      2320010 900000000000548007
    * 8000095c-e40d-56d2-9432-7f9a716d60d2    20020131        1       900000000000207008      900000000000509007      99175018        900000000000548007
    */
   private static final int                               DIALECT_UUID                    = 0;
   private static final int                               EFFECTIVE_TIME_INDEX            = 1;
   private static final int                               ACTIVE_INDEX                    = 2;  // 0 == false, 1 == true
   private static final int                               MODULE_SCTID_INDEX              = 3;
   private static final int                               ASSEMBLAGE_SCT_ID_INDEX         = 4;
   private static final int                               REFERENCED_COMPONENT_SCT_ID_INDEX = 5;
   private static final int                               ACCEPTABILITY_SCTID             = 6;

   //~--- fields --------------------------------------------------------------

   private final List<String[]>     dialectRecords;
   private final Semaphore          writeSemaphore;
   private final List<IndexBuilderService> indexers;
   private final ImportType importType;

   //~--- constructors --------------------------------------------------------

   public DialectWriter(List<String[]> dialectRecords, Semaphore writeSemaphore, 
           String message, ImportType importType) {
      this.dialectRecords = dialectRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      updateTitle("Importing dialect batch of size: " + dialectRecords.size());
      updateMessage(message);
      addToTotalWork(dialectRecords.size());
      this.importType = importType;
      Get.activeTasks()
         .add(this);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected Void call()
            throws Exception {
      try {
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService      stampService      = Get.stampService();
         int               authorNid         = TermAux.USER.getNid();
         int               pathNid           = TermAux.DEVELOPMENT_PATH.getNid();

         for (String[] descriptionRecord: dialectRecords) {
            final Status state = Status.fromZeroOneToken(descriptionRecord[ACTIVE_INDEX]);
            if (state == Status.INACTIVE && importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                continue;
            }
            UUID referencedComponentUuid = UuidT3Generator.fromSNOMED(descriptionRecord[REFERENCED_COMPONENT_SCT_ID_INDEX]);
            if (importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                if (!identifierService.hasUuid(referencedComponentUuid)) {
                    // if description was not imported because inactive, or inactive concept then skip
                    continue;
                }
            }
            UUID   elementUuid         = UUID.fromString(descriptionRecord[DIALECT_UUID]);
            UUID   moduleUuid          = UuidT3Generator.fromSNOMED(descriptionRecord[MODULE_SCTID_INDEX]);
            UUID   assemblageUuid      = UuidT3Generator.fromSNOMED(descriptionRecord[ASSEMBLAGE_SCT_ID_INDEX]);
            UUID   acceptabilityUuid   = UuidT3Generator.fromSNOMED(descriptionRecord[ACCEPTABILITY_SCTID]);

            // '2011-12-03T10:15:30Z'
            TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(
                                            DirectImporter.getIsoInstant(descriptionRecord[EFFECTIVE_TIME_INDEX]));
            long time = accessor.getLong(INSTANT_SECONDS) * 1000;

            // add to dialect assemblage
            int moduleNid            = identifierService.getNidForUuids(moduleUuid);
            int assemblageNid        = identifierService.getNidForUuids(assemblageUuid);
            int referencedComponentNid = identifierService.getNidForUuids(referencedComponentUuid);
            int acceptabilityNid     = identifierService.getNidForUuids(acceptabilityUuid);
            SemanticChronologyImpl dialectToWrite = new SemanticChronologyImpl(
                                                        VersionType.COMPONENT_NID,
                                                              elementUuid,
                                                              assemblageNid,
                                                              referencedComponentNid);
            int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
            ComponentNidVersionImpl dialectVersion = dialectToWrite.createMutableVersion(conceptStamp);

            dialectVersion.setComponentNid(acceptabilityNid);
            index(dialectToWrite);
            assemblageService.writeSemanticChronology(dialectToWrite);
            completedUnitOfWork();
         }

         return null;
      } finally {
         this.writeSemaphore.release();

         Get.activeTasks()
            .remove(this);
      }
   }

   private void index(Chronology chronicle) {
      for (IndexBuilderService indexer: indexers) {
         indexer.indexNow(chronicle);
      }
   }
}

