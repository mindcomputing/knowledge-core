/*
 * Copyright 2019 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.solor.direct.ho;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.ModelGet;
import static sh.isaac.solor.direct.ho.HoDirectImporter.ALLERGEN_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_CCS_MULTI_1_ICD_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_CCS_MULTI_2_ICD_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_CCS_SINGLE_ICD_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_ICD10CM_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_ICD10PCS_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_ICF_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_ICPC_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_LEGACY_IS_A;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_MDC_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_MESH_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_RADLEX_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_RXCUI_MAP;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HUMAN_DX_MODULE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.INEXACT_SNOMED_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.IS_CATEGORY_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.IS_DIAGNOSIS_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.LEGACY_HUMAN_DX_ROOT_CONCEPT;
import static sh.isaac.solor.direct.ho.HoDirectImporter.REFID_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.SNOMED_MAP_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.SNOMED_SIB_CHILD_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HDX_ENTITY_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.LEGACY_HUMAN_DX_MODULE;

/**
 *
 * @author kec
 */
public class HoWriter extends TimedTaskWithProgressTracker<Void> {
    //Name	ref id	Parents	Abbreviations	Description	Is Diagnosis?	Is category?	Deprecated	icd_10_cm	icd_10_pcs	icd_9_cm	
    //icf	icpc	loinc	mdc	mesh	radlex	rx_cui	snomed_ct	ccs-single_category_icd_10	ccs-multi_level_1_icd_10	ccs-multi_level_2_icd_10
    //Inexact RxNormS	Sibling/Child	Inexact SNOMED	Sibling/Child

    //Name	
    public static final int NAME = 0;
    //ref id	
    public static final int REFID = 1;
    //Parent Names	
    public static final int PARENT_NAMES = 2;
    //Parent Ref IDs	
    public static final int PARENT_REF_IDS = 3;
    //Mapped to Allergen?	
    public static final int MAPPED_TO_ALLERGEN = 4;
    //Abbreviations	
    public static final int ABBREVIATIONS = 5;
    //Description	
    public static final int DESCRIPTION = 6;
    //Is Diagnosis?	
    public static final int IS_DIAGNOSIS = 7;
    //Is category?	
    public static final int IS_CATEGORY = 8;
    //Deprecated	
    public static final int DEPRECATED = 9;
    //icd_10_cm	
    public static final int ICD10CM = 10;
    //icd_10_pcs	
    public static final int ICD10PCS = 11;
    //icd_9_cm	
    public static final int ICD9CM = 12;
    //icf	
    public static final int ICF = 13;
    //icpc	
    public static final int ICPC = 14;
    //loinc	
    public static final int LOINC = 15;
    //mdc	
    public static final int MDC = 16;
    //mesh	
    public static final int MESH = 17;
    //radlex	
    public static final int RADLEX = 18;
    //rx_cui	
    public static final int RXCUI = 19;
    //snomed_ct	
    public static final int SNOMEDCT = 20;
    //ccs-single_category_icd_10	
    public static final int CCS_SINGLE_CAT_ICD = 21;
    //ccs-multi_level_1_icd_10	
    public static final int CCS_MULTI_LEVEL_1_ICD = 22;
    //ccs-multi_level_2_icd_10	
    public static final int CCS_MULTI_LEVEL_2_ICD = 23;
    //Inexact RxNorm	
    public static final int INEXACT_RXNORM = 24;
    //RxNorm Sibling/Child	
    public static final int RXNORM_SIB_CHILD = 25;
    //Inexact SNOMED	
    public static final int INEXACT_SNOMED_1 = 26;
    //SNOMED Sibling/Child	
    public static final int SNOMED_SIB_CHILD_1 = 27;
    //Inexact SNOMED	
    public static final int INEXACT_SNOMED_2 = 28;
    //SNOMED Sibling/Child	
    public static final int SNOMED_SIB_CHILD_2 = 29;
    //Inexact SNOMED	
    public static final int INEXACT_SNOMED_3 = 30;
    //SNOMED Sibling/Child	
    public static final int SNOMED_SIB_CHILD_3 = 31;

    private final List<String[]> hoRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final long commitTime;
    private final IdentifierService identifierService = Get.identifierService();
    private final AssemblageService assemblageService = Get.assemblageService();
    private final HoDirectImporter importer;
    private final ConceptProxy allergyToSubstance = new ConceptProxy("Allergy to substance (disorder)", UUID.fromString("dddb93ba-3e25-313d-b3be-5081a91cce37"));
    private final ConceptProxy after = new ConceptProxy("After (attribute)", UUID.fromString("fb6758e0-442c-3393-bb2e-ff536711cde7"));
    private final ConceptProxy allergicSensitization = new ConceptProxy("Allergic sensitization (disorder)", UUID.fromString("3944bbe7-9080-3d20-9466-3302fcfcd403"));
    private final ConceptProxy causativeAgent = new ConceptProxy("Causative agent (attribute)", UUID.fromString("f770e2d8-91e6-3c55-91be-f794ee835265"));

    public static UUID refidToUuid(String refid) {
        return UuidT5Generator.get(LEGACY_HUMAN_DX_ROOT_CONCEPT.getPrimordialUuid(), refid);
    }

    public static int refidToNid(String refid) {
        return Get.nidWithAssignment(UuidT5Generator.get(LEGACY_HUMAN_DX_ROOT_CONCEPT.getPrimordialUuid(), refid));
    }

    public static UUID refidToSolorUuid(String refid) {
        return UuidT5Generator.get(HUMAN_DX_MODULE.getPrimordialUuid(), refid);
    }

    public static int refidToSolorNid(String refid) {
        return Get.nidWithAssignment(UuidT5Generator.get(HUMAN_DX_MODULE.getPrimordialUuid(), refid));
    }

    public HoWriter(List<String[]> hoRecords,
            Semaphore writeSemaphore, String message, long commitTime, HoDirectImporter importer) {
        this.hoRecords = hoRecords;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        this.commitTime = commitTime;
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing LOINC batch of size: " + hoRecords.size());
        updateMessage(message);
        addToTotalWork(hoRecords.size());
        Get.activeTasks().add(this);
        this.importer = importer;
    }

    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer : indexers) {
            indexer.indexNow(chronicle);
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            HashSet<String> allergenParents = new HashSet<>();
            allergenParents.add("1");
            allergenParents.add("3239");
            allergenParents.add("13592");

            ConceptService conceptService = Get.conceptService();
            StampService stampService = Get.stampService();
            int authorNid = TermAux.USER.getNid();
            int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
            int moduleNid = HUMAN_DX_MODULE.getNid();
            int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();

            List<String[]> noSuchElementList = new ArrayList<>();
            HashMap<String, String> nameRefidMap = new HashMap<>();
            for (String[] hoRec : hoRecords) {
                nameRefidMap.put(hoRec[NAME], hoRec[REFID]);

            }

            // All deprecated records are filtered out already
            for (String[] hoRec : hoRecords) {
                if (hoRec.length < SNOMED_SIB_CHILD_3 + 1) {
                    String[] newRec = new String[SNOMED_SIB_CHILD_3 + 1];
                    Arrays.fill(newRec, "");
                    for (int i = 0; i < hoRec.length; i++) {
                        newRec[i] = hoRec[i];
                    }
                    hoRec = newRec;
                }
                hoRec = clean(hoRec);
                try {

                    int recordStamp = stampService.getStampSequence(Status.ACTIVE, commitTime, authorNid, moduleNid, pathNid);
                    int legacyStamp = stampService.getStampSequence(Status.ACTIVE, commitTime, authorNid, LEGACY_HUMAN_DX_MODULE.getNid(), pathNid);
                    int inactiveLegacyStamp = stampService.getStampSequence(Status.INACTIVE, commitTime, authorNid, LEGACY_HUMAN_DX_MODULE.getNid(), pathNid);
                    // See if the concept is created (from the SNOMED/LOINC expressions. 
                    UUID conceptUuid = refidToUuid(hoRec[REFID]);
                    int conceptNid = Get.nidWithAssignment(conceptUuid);
                    Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptUuid);
                    if (!optionalConcept.isPresent()) {

                        int[] parentNids = new int[]{LEGACY_HUMAN_DX_ROOT_CONCEPT.getNid()};
                        if (hoRec[PARENT_NAMES] != null & !hoRec[PARENT_NAMES].isEmpty()) {
                            String[] parentRefIds = hoRec[PARENT_REF_IDS].split("; ");
                            parentNids = new int[parentRefIds.length];
                            for (int i = 0; i < parentRefIds.length; i++) {
                                String refId = parentRefIds[i];
                                if (allergenParents.contains(refId)) {
                                    if (Boolean.valueOf(hoRec[MAPPED_TO_ALLERGEN])) {
                                        // Add allergy concept...
                                        LOG.info("Allergen record: " + Arrays.asList(hoRec));
                                        addAllergy(hoRec[NAME], recordStamp, hoRec);
                                    }
                                } else if (Boolean.valueOf(hoRec[MAPPED_TO_ALLERGEN])) {
                                    LOG.info("Allergen record, no allergy parent: " + Arrays.asList(hoRec));
                                }
                                UUID parentUuid = refidToUuid(refId);
                                parentNids[i] = Get.nidWithAssignment(parentUuid);
                                ModelGet.identifierService().setupNid(parentNids[i], TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid(), IsaacObjectType.CONCEPT, VersionType.CONCEPT);
                            }
                        }
                        // Need to create new concept, and a stated definition...
                        buildConcept(conceptUuid, hoRec[NAME], recordStamp, legacyStamp, inactiveLegacyStamp, parentNids, hoRec);

//                        LogicalExpressionBuilder builder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
//                        
//                        ConceptAssertion[] conceptAssertions = new ConceptAssertion[parentNids.length];
//                        for (int i = 0; i < conceptAssertions.length; i++) {
//                            conceptAssertions[i] = builder.conceptAssertion(parentNids[i]);
//                        }
//                        builder.necessarySet(builder.and(conceptAssertions));
//                        LogicalExpression logicalExpression = builder.build();
//                        logicalExpression.getNodeCount();
//                        addLogicGraph(conceptUuid, hoRec[NAME],
//                                logicalExpression);
                    }
                    // make regular descriptions

//                    String shortName = hoRec[SHORTNAME];
//                    if (shortName == null || shortName.isEmpty()) {
//                        shortName = fullyQualifiedName + " with no sn";
//                    }
//
//                    addDescription(hoRec, shortName, TermAux.REGULAR_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp);
                } catch (NoSuchElementException ex) {
                    noSuchElementList.add(new String[]{ex.getMessage()});
                    noSuchElementList.add(hoRec);
                }
                completedUnitOfWork();
            }
            if (!noSuchElementList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String[] item : noSuchElementList) {
                    for (String field : item) {
                        sb.append(field);
                        sb.append("|");
                    }
                    sb.append("\n");
                }
                LOG.error("Continuing after import failed with no such element exception for record count: " + noSuchElementList.size()
                        + "\n\n" + sb.toString());
            }
            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }

    protected void buildConcept(UUID conceptUuid, String conceptName, int stamp, int legacyStamp, int inactiveLegacyStamp, int[] parentConceptNids, String[] hoRec) throws IllegalStateException, NoSuchElementException {

        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        int conceptNid = Get.nidForUuids(conceptUuid);

        ConceptAssertion[] parents = new ConceptAssertion[parentConceptNids.length];
        for (int i = 0; i < parentConceptNids.length; i++) {
            parents[i] = eb.conceptAssertion(parentConceptNids[i]);
        }

        eb.necessarySet(eb.and(parents));
        if (!hoRec[SNOMEDCT].isEmpty()) {
            if (hoRec[INEXACT_SNOMED_1].isEmpty()
                    && hoRec[SNOMED_SIB_CHILD_1].isEmpty()
                    && !hoRec[SNOMEDCT].contains(".")) {
                UUID snomedUuid = UuidT3Generator.fromSNOMED(hoRec[SNOMEDCT]);
                if (Get.identifierService().hasUuid(snomedUuid)) {
                    int snomedNid = Get.nidForUuids(snomedUuid);

                    addItemsIfNew(hoRec, ICD10PCS, snomedNid, HDX_ICD10PCS_MAP, stamp);
                    addItemsIfNew(hoRec, ICF, snomedNid, HDX_ICF_MAP, stamp);
                    addItemsIfNew(hoRec, ICPC, snomedNid, HDX_ICPC_MAP, stamp);
                    addItemsIfNew(hoRec, MDC, snomedNid, HDX_MDC_MAP, stamp);
                    addItemsIfNew(hoRec, MESH, snomedNid, HDX_MESH_MAP, stamp);
                    addItemsIfNew(hoRec, RADLEX, snomedNid, HDX_RADLEX_MAP, stamp);
                    addItemsIfNew(hoRec, RXCUI, snomedNid, HDX_RXCUI_MAP, stamp);
                    addItemsIfNew(hoRec, CCS_SINGLE_CAT_ICD, snomedNid, HDX_CCS_SINGLE_ICD_MAP, stamp);
                    addItemsIfNew(hoRec, CCS_MULTI_LEVEL_1_ICD, snomedNid, HDX_CCS_MULTI_1_ICD_MAP, stamp);
                    addItemsIfNew(hoRec, CCS_MULTI_LEVEL_2_ICD, snomedNid, HDX_CCS_MULTI_2_ICD_MAP, stamp);
                    addDescriptionsIfNew(hoRec, snomedNid, stamp);

                } else {
                    LOG.info("No concept for: |" + hoRec[SNOMEDCT] + "|" + snomedUuid.toString());
                }
            } else if (!hoRec[INEXACT_SNOMED_1].isEmpty()) {
                LOG.error("SNOMED and inexact populated: " + Arrays.asList(hoRec));
            }
        } else if (!hoRec[INEXACT_SNOMED_1].isEmpty()) {
            // '22325002'	'Child'	8510008'	'Child'
            // '16737003'	'Sibling'
            // '216757002'	'Parent' - Remember to process a parent relationship like a sibling one
            switch (hoRec[SNOMED_SIB_CHILD_1]) {
                case "Child":
                    addChild(conceptName, stamp, hoRec);
                    break;
                case "Sibling":
                case "Parent":
                    addSibling(conceptName, stamp, hoRec);
                    break;
            }
        }

        ConceptBuilderService builderService = Get.conceptBuilderService();
        String[] parentNames = hoRec[PARENT_NAMES].split("; ");
        String[] abbreviations = hoRec[ABBREVIATIONS].split("; ");
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "HO " + parentNames[0],
                eb.build(),
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(conceptUuid);
        for (int parentNid : parentConceptNids) {
            builder.addComponentSemantic(new ConceptProxy(parentNid), HDX_LEGACY_IS_A);
        }

        addMaps(hoRec, builder);
        // white-coated tongue; white coating on tongue; tongue with white coating
        for (int i = 0; i < abbreviations.length; i++) {
            if (!abbreviations[i].isEmpty()) {
                builder.addDescription(abbreviations[i], MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR);
            }
        }

        if (!hoRec[REFID].isEmpty()) {
            builder.addStringSemantic(hoRec[REFID], REFID_ASSEMBLAGE);
        }
        if (!hoRec[MAPPED_TO_ALLERGEN].isEmpty()) {
            builder.addStringSemantic(hoRec[MAPPED_TO_ALLERGEN], ALLERGEN_ASSEMBLAGE);
        }
        if (!hoRec[IS_DIAGNOSIS].isEmpty()) {
            builder.addStringSemantic(hoRec[IS_DIAGNOSIS], IS_DIAGNOSIS_ASSEMBLAGE);
        }
        if (!hoRec[IS_CATEGORY].isEmpty()) {
            builder.addStringSemantic(hoRec[IS_CATEGORY], IS_CATEGORY_ASSEMBLAGE);
        }
        if (!hoRec[SNOMEDCT].isEmpty()) {
            if (!hoRec[MAPPED_TO_ALLERGEN].isEmpty() && Boolean.parseBoolean(hoRec[MAPPED_TO_ALLERGEN])) {
                // The SNOMED record is not the correct record, since it is mapped to an allergen, and 
                // will thus have an allegy concept created for it. 
                if (snomedAllergyMap.containsKey(hoRec[SNOMEDCT])) {
                   SemanticBuilder semanticBuilder = Get.semanticBuilderService()
                            .getComponentSemanticBuilder(snomedAllergyMap.get(hoRec[SNOMEDCT]), conceptNid, HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getNid());
                    List<Chronology> builtObjects = new ArrayList<>();
                    semanticBuilder.build(stamp, builtObjects);
                    buildAndIndex(semanticBuilder, stamp, hoRec);
                }
            } else {
                UUID snomedUuid = UuidT3Generator.fromSNOMED(hoRec[SNOMEDCT]);
                if (Get.identifierService().hasUuid(snomedUuid)) {
                    int snomedNid = Get.nidForUuids(snomedUuid);
                    builder.addStringSemantic(hoRec[SNOMEDCT], SNOMED_MAP_ASSEMBLAGE);
                    // Add reverse semantic
                    addReverseSemantic(hoRec, conceptNid, snomedNid, stamp);
                    
                   SemanticBuilder semanticBuilder = Get.semanticBuilderService()
                            .getComponentSemanticBuilder(snomedNid, conceptNid, HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getNid());
                    List<Chronology> builtObjects = new ArrayList<>();
                    semanticBuilder.build(stamp, builtObjects);
                    buildAndIndex(semanticBuilder, stamp, hoRec);
                } else {
                    throw new NoSuchElementException("No identifier for: " + hoRec[SNOMEDCT]);
                }
            }
        }
        if (!hoRec[INEXACT_SNOMED_1].isEmpty()) {
            builder.addStringSemantic(hoRec[INEXACT_SNOMED_1], INEXACT_SNOMED_ASSEMBLAGE);
        }
        if (!hoRec[SNOMED_SIB_CHILD_1].isEmpty()) {
            builder.addStringSemantic(hoRec[SNOMED_SIB_CHILD_1], SNOMED_SIB_CHILD_ASSEMBLAGE);
        }
        if (!hoRec[INEXACT_SNOMED_2].isEmpty()) {
            builder.addStringSemantic(hoRec[INEXACT_SNOMED_2], INEXACT_SNOMED_ASSEMBLAGE);
        }
        if (!hoRec[SNOMED_SIB_CHILD_2].isEmpty()) {
            builder.addStringSemantic(hoRec[SNOMED_SIB_CHILD_2], SNOMED_SIB_CHILD_ASSEMBLAGE);
        }
        if (!hoRec[INEXACT_SNOMED_3].isEmpty()) {
            builder.addStringSemantic(hoRec[INEXACT_SNOMED_3], INEXACT_SNOMED_ASSEMBLAGE);
        }
        if (!hoRec[SNOMED_SIB_CHILD_3].isEmpty()) {
            builder.addStringSemantic(hoRec[SNOMED_SIB_CHILD_3], SNOMED_SIB_CHILD_ASSEMBLAGE);
        }
        if (!hoRec[DEPRECATED].isEmpty() && Boolean.parseBoolean(hoRec[DEPRECATED])) {
            buildAndIndex(builder, inactiveLegacyStamp, hoRec);
        } else {
            buildAndIndex(builder, legacyStamp, hoRec);
        }

    }

    private void addReverseSemantic(String[] hoRec, int legacyHdxNid, int solorNid, int stamp) throws NoSuchElementException, IllegalStateException {
        int assemblageConceptNid = HDX_ENTITY_ASSEMBLAGE.getNid();
        //int componentNid,

        SemanticBuilder semanticBuilder = Get.semanticBuilderService().getComponentSemanticBuilder(legacyHdxNid, solorNid, assemblageConceptNid);
        List<Chronology> builtObjects = new ArrayList<>();
        semanticBuilder.build(stamp, builtObjects);
        buildAndIndex(semanticBuilder, stamp, hoRec);
    }

    protected void buildAndIndex(IdentifiedComponentBuilder builder, int stamp, String[] hoRec) throws IllegalStateException {
        List<Chronology> builtObjects = new ArrayList<>();
        builder.build(stamp, builtObjects);
        for (Chronology chronology : builtObjects) {
            Get.identifiedObjectService().putChronologyData(chronology);
            if (chronology.getVersionType() == VersionType.LOGIC_GRAPH) {
                try {
                    Get.taxonomyService().updateTaxonomy((SemanticChronology) chronology);
                } catch (RuntimeException e) {
                    LOG.error("Processing " + Arrays.toString(hoRec), e);
                }
            }
            index(chronology);
        }
    }

    private String[] clean(String[] hoRec) {
        for (int i = 0; i < hoRec.length; i++) {
            hoRec[i] = hoRec[i].trim();
            if (hoRec[i].startsWith("'")) {
                hoRec[i] = hoRec[i].substring(1);
            }
            if (hoRec[i].endsWith("'")) {
                hoRec[i] = hoRec[i].substring(0, hoRec[i].length() - 1);
            }
            if (hoRec[i].startsWith("\"")) {
                hoRec[i] = hoRec[i].substring(1);
            }
            if (hoRec[i].endsWith("\"")) {
                hoRec[i] = hoRec[i].substring(0, hoRec[i].length() - 1);
            }
        }
        return hoRec;
    }

    private void addChild(String conceptName, int stamp, String[] hoRec) {
        List<Integer> parentConceptNidList = new ArrayList<>();
        if (!hoRec[INEXACT_SNOMED_1].isEmpty()) {
            parentConceptNidList.add(getNidForSCTID(hoRec[INEXACT_SNOMED_1]));
        }
        if (!hoRec[INEXACT_SNOMED_2].isEmpty()) {
            parentConceptNidList.add(getNidForSCTID(hoRec[INEXACT_SNOMED_2]));
        }
        if (!hoRec[INEXACT_SNOMED_3].isEmpty()) {
            parentConceptNidList.add(getNidForSCTID(hoRec[INEXACT_SNOMED_3]));
        }
        int[] parentConceptNids = new int[parentConceptNidList.size()];
        for (int i = 0; i < parentConceptNids.length; i++) {
            parentConceptNids[i] = parentConceptNidList.get(i);
        }
        HdxConceptHash hdxConceptHash = new HdxConceptHash(conceptName, parentConceptNids, hoRec[REFID]);
        UUID conceptUuid = refidToSolorUuid(hoRec[REFID]);
        int conceptNid = Get.nidWithAssignment(conceptUuid);
        if (this.importer.getHdxSolorConcepts().containsKey(hdxConceptHash)) {
            addReverseSemantic(hoRec, refidToNid(hoRec[REFID]), this.importer.getHdxSolorConcepts().get(hdxConceptHash).getNid(), stamp);
            //builder.addStringSemantic(hoRec[REFID], REFID_ASSEMBLAGE);
            addItemsIfNew(hoRec, ICD10PCS, conceptNid, HDX_ICD10PCS_MAP, stamp);
            addItemsIfNew(hoRec, ICF, conceptNid, HDX_ICF_MAP, stamp);
            addItemsIfNew(hoRec, ICPC, conceptNid, HDX_ICPC_MAP, stamp);
            addItemsIfNew(hoRec, MDC, conceptNid, HDX_MDC_MAP, stamp);
            addItemsIfNew(hoRec, MESH, conceptNid, HDX_MESH_MAP, stamp);
            addItemsIfNew(hoRec, RADLEX, conceptNid, HDX_RADLEX_MAP, stamp);
            addItemsIfNew(hoRec, RXCUI, conceptNid, HDX_RXCUI_MAP, stamp);
            addItemsIfNew(hoRec, CCS_SINGLE_CAT_ICD, conceptNid, HDX_CCS_SINGLE_ICD_MAP, stamp);
            addItemsIfNew(hoRec, CCS_MULTI_LEVEL_1_ICD, conceptNid, HDX_CCS_MULTI_1_ICD_MAP, stamp);
            addItemsIfNew(hoRec, CCS_MULTI_LEVEL_2_ICD, conceptNid, HDX_CCS_MULTI_2_ICD_MAP, stamp);

        } else {
            this.importer.getHdxSolorConcepts().put(hdxConceptHash, new ConceptProxy(conceptName, conceptUuid));

            LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();

            ConceptAssertion[] parents = new ConceptAssertion[parentConceptNidList.size()];
            for (int i = 0; i < parentConceptNidList.size(); i++) {
                parents[i] = eb.conceptAssertion(parentConceptNidList.get(i));
            }

            eb.necessarySet(eb.and(parents));
            ConceptBuilderService builderService = Get.conceptBuilderService();
            ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                    "HDX",
                    eb.build(),
                    TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
            builder.setPrimordialUuid(conceptUuid);
            builder.addStringSemantic(hoRec[REFID], REFID_ASSEMBLAGE);
            builder.addAssemblageMembership(HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE);
            builder.getDescriptionBuilders().forEach(descriptionBuilder -> {
                descriptionBuilder.addAssemblageMembership(HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE);
            });

            addMaps(hoRec, builder);
            buildAndIndex(builder, stamp, hoRec);
            //
            if (!Boolean.valueOf(hoRec[MAPPED_TO_ALLERGEN])) {
                SemanticBuilder semanticBuilder = Get.semanticBuilderService().getComponentSemanticBuilder(conceptNid, refidToNid(hoRec[REFID]), HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getNid());
                List<Chronology> builtObjects = new ArrayList<>();
                semanticBuilder.build(stamp, builtObjects);
                buildAndIndex(semanticBuilder, stamp, hoRec);
                addReverseSemantic(hoRec, refidToNid(hoRec[REFID]), conceptNid, stamp);
            }

        }
    }

    private int getNidForSCTID(String sctid) {
        UUID snomedUuid = UuidT3Generator.fromSNOMED(sctid);
        return Get.nidForUuids(snomedUuid);
    }

    HashMap<String, Integer> snomedAllergyMap = new HashMap<>();

    private void addAllergy(String conceptName, int stamp, String[] hoRec) {
        // see if allergy already added...
        if (snomedAllergyMap.containsKey(hoRec[SNOMEDCT])) {
            // already added, add info to existing concept. 
            int existingAllergyNid = snomedAllergyMap.get(hoRec[SNOMEDCT]);
            addReverseSemantic(hoRec, refidToNid(hoRec[REFID]), existingAllergyNid, stamp);
            buildAndIndex(Get.semanticBuilderService().getStringSemanticBuilder(hoRec[REFID], existingAllergyNid, REFID_ASSEMBLAGE.getNid()), stamp, hoRec);
        } else {

            UUID conceptUuid = refidToSolorUuid(hoRec[REFID]);

            // Parent is 419199007 |Allergy to substance (finding)|
            //   Has realization →  Allergic process
            //   Causative agent →  Substance 
            LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
            eb.sufficientSet(eb.and(eb.conceptAssertion(allergyToSubstance), eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                    eb.and(eb.someRole(after, eb.conceptAssertion(allergicSensitization)),
                            eb.someRole(causativeAgent, eb.conceptAssertion(getNidForSCTID(hoRec[SNOMEDCT])))))));

            ConceptBuilderService builderService = Get.conceptBuilderService();
            ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                    "HDX",
                    eb.build(),
                    TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
            builder.setPrimordialUuid(conceptUuid);
            builder.addStringSemantic(hoRec[REFID], REFID_ASSEMBLAGE);
            builder.addAssemblageMembership(HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE);
            builder.getDescriptionBuilders().forEach(descriptionBuilder -> {
                descriptionBuilder.addAssemblageMembership(HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE);
            });

            int conceptNid = builder.getNid();
            buildAndIndex(builder, stamp, hoRec);
            snomedAllergyMap.put(hoRec[SNOMEDCT], conceptNid);
            //
            SemanticBuilder semanticBuilder = Get.semanticBuilderService()
                    .getComponentSemanticBuilder(refidToSolorNid(hoRec[REFID]),
                            refidToNid(hoRec[REFID]), HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getNid());
            List<Chronology> builtObjects = new ArrayList<>();
            semanticBuilder.build(stamp, builtObjects);
            buildAndIndex(semanticBuilder, stamp, hoRec);
            addReverseSemantic(hoRec, refidToNid(hoRec[REFID]), conceptNid, stamp);
        }

    }

    private void addSibling(String conceptName, int stamp, String[] hoRec) {
        UUID conceptUuid = refidToSolorUuid(hoRec[REFID]);
        int[] parentConceptNids = new int[]{getNidForSCTID(hoRec[INEXACT_SNOMED_1])};
        HdxConceptHash hdxConceptHash = new HdxConceptHash(conceptName, parentConceptNids, hoRec[REFID]);

        if (this.importer.getHdxSolorConcepts().containsKey(hdxConceptHash)) {
            addReverseSemantic(hoRec, refidToNid(hoRec[REFID]), this.importer.getHdxSolorConcepts().get(hdxConceptHash).getNid(), stamp);
            //builder.addStringSemantic(hoRec[REFID], REFID_ASSEMBLAGE);
            int conceptNid = Get.nidForUuids(conceptUuid);
            addItemsIfNew(hoRec, ICD10PCS, conceptNid, HDX_ICD10PCS_MAP, stamp);
            addItemsIfNew(hoRec, ICF, conceptNid, HDX_ICF_MAP, stamp);
            addItemsIfNew(hoRec, ICPC, conceptNid, HDX_ICPC_MAP, stamp);
            addItemsIfNew(hoRec, MDC, conceptNid, HDX_MDC_MAP, stamp);
            addItemsIfNew(hoRec, MESH, conceptNid, HDX_MESH_MAP, stamp);
            addItemsIfNew(hoRec, RADLEX, conceptNid, HDX_RADLEX_MAP, stamp);
            addItemsIfNew(hoRec, RXCUI, conceptNid, HDX_RXCUI_MAP, stamp);
            addItemsIfNew(hoRec, CCS_SINGLE_CAT_ICD, conceptNid, HDX_CCS_SINGLE_ICD_MAP, stamp);
            addItemsIfNew(hoRec, CCS_MULTI_LEVEL_1_ICD, conceptNid, HDX_CCS_MULTI_1_ICD_MAP, stamp);
            addItemsIfNew(hoRec, CCS_MULTI_LEVEL_2_ICD, conceptNid, HDX_CCS_MULTI_2_ICD_MAP, stamp);
        } else {
            this.importer.getHdxSolorConcepts().put(hdxConceptHash, new ConceptProxy(conceptName, conceptUuid));
            int[] parentNids = this.importer.getTaxonomy().getTaxonomyParentConceptNids(parentConceptNids[0]);
            ConceptAssertion[] parents = new ConceptAssertion[parentNids.length];
            LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
            for (int i = 0; i < parentNids.length; i++) {
                parents[i] = eb.conceptAssertion(parentNids[i]);
            }

            eb.necessarySet(eb.and(parents));
            ConceptBuilderService builderService = Get.conceptBuilderService();
            ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                    "HDX",
                    eb.build(),
                    TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
            builder.setPrimordialUuid(conceptUuid);
            builder.addStringSemantic(hoRec[REFID], REFID_ASSEMBLAGE);
            builder.addAssemblageMembership(HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE);
            builder.getDescriptionBuilders().forEach(descriptionBuilder -> {
                descriptionBuilder.addAssemblageMembership(HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE);
            });

            int conceptNid = builder.getNid();
            addMaps(hoRec, builder);
            buildAndIndex(builder, stamp, hoRec);
            //
            SemanticBuilder semanticBuilder = Get.semanticBuilderService()
                    .getComponentSemanticBuilder(refidToSolorNid(hoRec[REFID]),
                            refidToNid(hoRec[REFID]), HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getNid());
            List<Chronology> builtObjects = new ArrayList<>();
            semanticBuilder.build(stamp, builtObjects);

            buildAndIndex(semanticBuilder, stamp, hoRec);
            addReverseSemantic(hoRec, refidToNid(hoRec[REFID]), conceptNid, stamp);
        }
    }

    private void addMaps(String[] hoRec, ConceptBuilder builder) {
        if (!hoRec[ICD10CM].isEmpty()) {
            String[] dataArray = hoRec[ICD10CM].split("; ");
            for (String data : dataArray) {
                builder.addStringSemantic(data, HDX_ICD10CM_MAP);
            }
        }
        addItems(hoRec, ICD10PCS, builder, HDX_ICD10PCS_MAP);
        addItems(hoRec, ICF, builder, HDX_ICF_MAP);
        addItems(hoRec, ICPC, builder, HDX_ICPC_MAP);
        addItems(hoRec, MDC, builder, HDX_MDC_MAP);
        addItems(hoRec, MESH, builder, HDX_MESH_MAP);
        addItems(hoRec, RADLEX, builder, HDX_RADLEX_MAP);
        addItems(hoRec, RXCUI, builder, HDX_RXCUI_MAP);
        addItems(hoRec, CCS_SINGLE_CAT_ICD, builder, HDX_CCS_SINGLE_ICD_MAP);
        addItems(hoRec, CCS_MULTI_LEVEL_1_ICD, builder, HDX_CCS_MULTI_1_ICD_MAP);
        addItems(hoRec, CCS_MULTI_LEVEL_2_ICD, builder, HDX_CCS_MULTI_2_ICD_MAP);
    }

    private void addItems(String[] hoRec, int index, ConceptBuilder builder, ConceptSpecification assemblage) {
        if (!hoRec[index].isEmpty()) {
            String[] dataArray = hoRec[index].split("; ");
            for (String data : dataArray) {
                builder.addStringSemantic(data, assemblage);
            }
        }
    }

    private void addItemsIfNew(String[] hoRec, int index, int conceptNid, ConceptSpecification assemblage, int stamp) {
        HashSet<String> existing = new HashSet();
        Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(conceptNid, assemblage.getNid())
                .forEach(semanticChronology -> {
                    for (Version v : semanticChronology.getVersionList()) {
                        existing.add(((StringVersion) v).getString());
                    }
                });

        if (!hoRec[index].isEmpty()) {
            String[] dataArray = hoRec[index].split("; ");
            for (String data : dataArray) {
                if (!existing.contains(data)) {
                    SemanticBuilder<? extends SemanticChronology> b = Get.semanticBuilderService().getStringSemanticBuilder(data, conceptNid, assemblage.getNid());
                    buildAndIndex(b, stamp, hoRec);
                }
            }
        }
    }

    private void addDescriptionsIfNew(String[] hoRec, int snomedNid, int stamp) {
        if (Boolean.valueOf(hoRec[MAPPED_TO_ALLERGEN])) {
            return;
        }
        HashSet<String> existing = new HashSet();
        Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(snomedNid, TermAux.ENGLISH_LANGUAGE.getNid())
                .forEach(semanticChronology -> {
                    for (Version v : semanticChronology.getVersionList()) {
                        existing.add(((DescriptionVersion) v).getText());
                    }
                });

        String[] abbreviations = hoRec[ABBREVIATIONS].split("; ");
        for (int i = 0; i < abbreviations.length; i++) {
            if (!abbreviations[i].isEmpty() && !existing.contains(abbreviations[i])) {
                SemanticBuilder<? extends SemanticChronology> b = Get.semanticBuilderService().getDescriptionBuilder(
                        MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getNid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid(), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                        abbreviations[i], snomedNid);
                b.addSemantic(Get.semanticBuilderService().getMembershipSemanticBuilder(b.getNid(), HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE.getNid()));
                buildAndIndex(b, stamp, hoRec);
            }
        }
        if (!existing.contains(hoRec[NAME])) {
            SemanticBuilder<? extends SemanticChronology> b = Get.semanticBuilderService().getDescriptionBuilder(
                    MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getNid(), MetaData.ENGLISH_LANGUAGE____SOLOR.getNid(), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                    hoRec[NAME], snomedNid);
            b.addSemantic(Get.semanticBuilderService().getMembershipSemanticBuilder(b.getNid(), HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE.getNid()));
            buildAndIndex(b, stamp, hoRec);
        }

    }

}
