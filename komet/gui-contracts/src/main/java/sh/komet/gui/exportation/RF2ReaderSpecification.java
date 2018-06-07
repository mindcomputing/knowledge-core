package sh.komet.gui.exportation;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.util.UuidT5Generator;
import sh.komet.gui.manifold.Manifold;

/*
 * aks8m - 5/22/18
 */
public abstract class RF2ReaderSpecification implements ReaderSpecification {

    private final Manifold manifold;
    private static ObservableSnapshotService snapshotService;
    private final ExportLookUpCache exportLookUpCache;

    public RF2ReaderSpecification(Manifold manifold, ExportLookUpCache exportLookUpCache) {
        this.manifold = manifold;
        this.exportLookUpCache = exportLookUpCache;
        createSnapshotInstance(this.manifold);
    }

    private static void createSnapshotInstance(Manifold manifold){
        snapshotService = Get.observableSnapshotService(manifold);
    }

    public ObservableSnapshotService getSnapshotService() {
        return this.snapshotService;
    }

    StringBuilder getRF2CommonElements(Chronology chronology){

        int stampNid = 0;

        if(chronology instanceof ConceptChronology)
            stampNid = getSnapshotService().getObservableConceptVersion(chronology.getNid()).getStamps().findFirst().getAsInt();
        else if(chronology instanceof SemanticChronology)
            stampNid = getSnapshotService().getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();


        return new StringBuilder()
                .append(getIdString(chronology) + "\t")       //id
                .append(getTimeString(stampNid) + "\t")        //time
                .append(getActiveString(stampNid) + "\t")      //active
                .append(getModuleString(stampNid) + "\t");     //moduleId
    }

    String getIdString(Chronology chronology){

        if (this.exportLookUpCache.getSctidNids().contains(chronology.getNid())) {
            return lookUpIdentifierFromSemantic(this.snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), chronology);
        } else if (this.exportLookUpCache.getLoincNids().contains(chronology.getNid())) {
            final String loincId = lookUpIdentifierFromSemantic(this.snapshotService, MetaData.CODE____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromLoincId(loincId);
        } else if (this.exportLookUpCache.getRxnormNids().contains(chronology.getNid())) {
            final String rxnormId = lookUpIdentifierFromSemantic(this.snapshotService, MetaData.RXNORM_CUI____SOLOR.getNid(), chronology);
            return UuidT5Generator.makeSolorIdFromRxNormId(rxnormId);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(chronology.getPrimordialUuid());
        }
    }

    String getTimeString(int stampNid){
        return Long.toString(Get.stampService().getTimeForStamp(stampNid));
    }

    String getActiveString(int stampNid){
        return Get.stampService().getStatusForStamp(stampNid).isActive() ? "1" : "0";
    }

    String getModuleString(int stampNid){
        ConceptChronology moduleConcept = Get.concept(Get.stampService().getModuleNidForStamp(stampNid));
        if (this.exportLookUpCache.getSctidNids().contains(moduleConcept.getNid())) {
            return lookUpIdentifierFromSemantic(this.snapshotService, TermAux.SNOMED_IDENTIFIER.getNid(), moduleConcept);
        } else {
            return UuidT5Generator.makeSolorIdFromUuid(moduleConcept.getPrimordialUuid());
        }
    }

    private String lookUpIdentifierFromSemantic(ObservableSnapshotService snapshotService
            , int identifierAssemblageNid, Chronology chronology){

        LatestVersion<ObservableStringVersion> stringVersion =
                (LatestVersion<ObservableStringVersion>) snapshotService.getObservableSemanticVersion(
                        chronology.getSemanticChronologyList().stream()
                                .filter(semanticChronology -> semanticChronology.getAssemblageNid() == identifierAssemblageNid)
                                .findFirst().get().getNid()
                );

        return stringVersion.isPresent() ? stringVersion.get().getString() : "";
    }


}
