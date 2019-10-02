/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.provider.query.lucene.indexers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.index.AuthorModulePathRestriction;
import sh.isaac.api.index.ComponentSearchResult;
import sh.isaac.api.index.IndexDescriptionQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import sh.isaac.provider.query.lucene.PerFieldAnalyzer;

/**
 * Lucene Manager which specializes in indexing descriptions.
 * 
 * This has been redesigned such that is now creates multiple columns within the index
 * 
 * There is a 'everything' column, which gets all descriptions, to support the standard search where you want to match on a text value
 * anywhere it appears.
 * 
 * There are 3 columns to support FULLY_QUALIFIED_NAME / Synonym / Definition - to support searching that subset of descriptions. There are
 * also data-defined columns to support extended definition types - for example - loinc description types - to support searching terminology
 * specific fields.
 * 
 * Each of the columns above is also x2, as everything is indexed both with a standard analyzer, and with a whitespace analyzer.
 * 
 * @author kec
 * @author aimeefurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "description index")
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class DescriptionIndexer extends LuceneIndexer
        implements IndexDescriptionQueryService {

   /** The Constant FIELD_INDEXED_STRING_VALUE. */
   private static final String FIELD_INDEXED_STRING_VALUE = "_string_content_";
   
   private static final String FIELD_INDEXED_DESCRIPTION_TYPE_NID = "_desc_type_nid_";
   
   private static final String FIELD_INDEXED_EXTENDED_DESCRIPTION_TYPE_UUID = "_extended_desc_type_uuid_";
   
   /** The Constant INDEX_NAME. */
   public static final String INDEX_NAME = "descriptions-index";
   
   /** The desc extended type sequence. */
   private int descExtendedTypeNid= 0;
   
   private HashSet<Integer> metadataConcepts = new HashSet<>();

   private DescriptionIndexer() throws IOException {
      super(INDEX_NAME);
   }

   @Override
   public void startBatchReindex() {
      super.startBatchReindex();
      if (LookupService.getCurrentRunLevel() >= LookupService.SL_L4 ) {
         LOG.info("Populating metadata lookup hash");
         populateMetadataCache(TermAux.SOLOR_METADATA.getNid());
         LOG.info("System contains " + metadataConcepts.size() + " metadata concepts");
      }
      else {
         //We will just index without the cache, which is slower, but still accurate.
         //Note, this may be a bug, if this reindex gets triggered on a DB with content during startup, because we now rely on the 
         //taxonomy provider.  We may have to move the startup-reindex to a later runlevel...
         LOG.info("Can't populate metadata lookup hash for this batch reindex, because the taxonomy provider isn't started yet");
      }
   }
   
   private void populateMetadataCache(int nid) {
     if (!metadataConcepts.add(nid)) {
        // some of the metadata is linked in multiple locations, we don't need to reprocess it.
        return;
     }
     for (int child : Get.taxonomyService().getAllTaxonomyChildren(nid)) {
         populateMetadataCache(child);
     }
   }
   
   @Override
   public void finishBatchReindex() {
      super.finishBatchReindex();
      //Don't keep this, as it will become outdated.  The code that uses it falls back to a different lookup when the structure isn't populated.
      metadataConcepts.clear();
   }

/**
    * {@inheritDoc}
    */
   @Override
   protected void addFields(Chronology chronicle, Document doc, Set<Integer> pathNids) {
      if (chronicle instanceof SemanticChronology) {
         final SemanticChronology semanticChronology = (SemanticChronology) chronicle;

         if (semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
            indexDescription(doc, semanticChronology, pathNids);
            incrementIndexedItemCount("Description");
         }
      }
   }

   /**
    * Index description.
    *
    * @param doc the doc
    * @param semanticChronology the semantic chronology
    */
   private void indexDescription(Document doc,SemanticChronology semanticChronology, Set<Integer> pathNids) {
      doc.add(new TextField(FIELD_SEMANTIC_ASSEMBLAGE_NID, semanticChronology.getAssemblageNid() + "", Field.Store.NO));

      String                      lastDescText     = null;
      String                      lastDescType     = null;

      boolean isMetadata = false;
      if (metadataConcepts.size() > 0) {
          isMetadata = metadataConcepts.contains(semanticChronology.getReferencedComponentNid());
      }
      
      //This is an if instead of an else, to guard against the metadataConcepts cache being emptied during a one-off index op.
      if (!isMetadata && metadataConcepts.size() == 0){
          isMetadata = Get.taxonomyService().wasEverKindOf(semanticChronology.getReferencedComponentNid(), TermAux.SOLOR_METADATA.getNid());
      }
      
      if (isMetadata) {
         doc.add(new TextField(FIELD_CONCEPT_IS_METADATA, FIELD_CONCEPT_IS_METADATA_VALUE, Field.Store.NO));
      }
      
      final Set<Integer> uniqueDescriptionTypes = new HashSet<>();

      for (final StampedVersion stampedVersion : semanticChronology.getVersionList()) {
         DescriptionVersion descriptionVersion = (DescriptionVersion) stampedVersion;

         // No need to index if the text is the same as the previous version.
         if ((lastDescText == null) || (lastDescType == null) || !lastDescText.equals(descriptionVersion.getText())) {
            // Add to the field that carries all text
            addField(doc, FIELD_INDEXED_STRING_VALUE, descriptionVersion.getText(), true);
            uniqueDescriptionTypes.add(descriptionVersion.getDescriptionTypeConceptNid());
            lastDescText = descriptionVersion.getText();
         }
      }
      
      for (Integer i : uniqueDescriptionTypes) {
         addField(doc, FIELD_INDEXED_DESCRIPTION_TYPE_NID, i.toString(), false);
      }

      final Set<String> uniqueExtensionTypes = new HashSet<>();

      Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(semanticChronology.getNid(), getDescriptionExtendedTypeNid()).forEach(nestedSemantic -> {
         for (Version nestedVersions : nestedSemantic.getVersionList()) {
            // this is a UUID, but we want to treat it as a string anyway
            uniqueExtensionTypes.add(((DynamicVersion<?>) nestedVersions).getData()[0].getDataObject().toString());
         }
      });

      for (String s : uniqueExtensionTypes) {
         addField(doc, FIELD_INDEXED_EXTENDED_DESCRIPTION_TYPE_UUID, s, false);
      }
   }


   /**
    * Adds the field.
    *
    * @param doc the doc
    * @param fieldName the field name
    * @param value the value
    * @param tokenize the tokenize
    */
   private void addField(Document doc, String fieldName, String value, boolean tokenize) {
      // index twice per field - once with the standard analyzer, once with the whitespace analyzer.
      if (tokenize) {
         doc.add(new TextField(fieldName, value, Field.Store.NO));
      }

      doc.add(new TextField(fieldName + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, value, Field.Store.NO));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean indexChronicle(Chronology chronicle) {
      if (chronicle instanceof SemanticChronology && ((SemanticChronology)chronicle).getVersionType() == VersionType.DESCRIPTION) {
         return true;
      }
      return false;
   }

   
   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcepts,
         Predicate<Integer> filter,
         AuthorModulePathRestriction amp,
         boolean metadataOnly,
         int[] descriptionTypes,
         int[] extendedDescriptionTypes,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      
      if (!prefixSearch && SemanticTags.containsSemanticTag(query))
      {
         //If they include a semantic tag, adjust their query so that the tag is not treated like a lucene grouping rule.
         //Note, grouping rules are still allowed, so long as they aren't at the very end of the query (so they don't look like a semantic tag)
         query = SemanticTags.stripSemanticTagIfPresent(query) + " \\(" + SemanticTags.findSemanticTagIfPresent(query).get() + "\\)";
      }

      Query q = buildTokenizedStringQuery(query, FIELD_INDEXED_STRING_VALUE, prefixSearch, metadataOnly);

      q = restrictToSemantic(q, assemblageConcepts);

      if (descriptionTypes != null && descriptionTypes.length > 0) {
         final BooleanQuery.Builder outerWrapQueryBuilder = new BooleanQuery.Builder();
         outerWrapQueryBuilder.add(q, Occur.MUST);
         
         final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
         for (Integer i : descriptionTypes)
         {
            innerQueryBuilder.add(new TermQuery(new Term(FIELD_INDEXED_DESCRIPTION_TYPE_NID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, i.toString())), Occur.SHOULD);
         }
         
         outerWrapQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
         q = outerWrapQueryBuilder.build();
      }
      
      if (extendedDescriptionTypes != null && extendedDescriptionTypes.length > 0) {
         final BooleanQuery.Builder outerWrapQueryBuilder = new BooleanQuery.Builder();
         outerWrapQueryBuilder.add(q, Occur.MUST);
         
         final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
         for (int i : extendedDescriptionTypes)
         {
            for (UUID uuid : Get.identifierService().getUuidsForNid(i))
            {
               innerQueryBuilder.add(new TermQuery(new Term(FIELD_INDEXED_EXTENDED_DESCRIPTION_TYPE_UUID + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, uuid.toString())), 
                     Occur.SHOULD);
            }
         }
         
         outerWrapQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
         q = outerWrapQueryBuilder.build();
      }
      List<SearchResult> results = search(q, filter, amp, pageNum, sizeLimit, targetGeneration);
      
      if (prefixSearch) {
         long time = System.currentTimeMillis();
         // Do some post search score manipulation to get relevant results closer to the top.

         // Compute the max score of all results.
         float maxScore = 0.0f;
         for (final SearchResult sr : results) {
            final float score = sr.getScore();

            if (score > maxScore) {
               maxScore = score;
            }
         }
         
         //A coordinate for best-effort readback of descriptions
         StampCoordinate sc = Get.configurationService().getGlobalDatastoreConfiguration().getDefaultStampCoordinate().makeCoordinateAnalog(Status.makeAnyStateSet());
         
         // normalize the scores between 0 and 1
         for (final SearchResult sr : results) {
            //This cast is safe, per the docs of the internal search
            ((ComponentSearchResult)sr).setScore(sr.getScore() / maxScore);
            
            //Look up the object, and fiddle the scores, depending on how good the match is.
            final Optional<? extends Chronology> chronology = Get.identifiedObjectService().getChronology(sr.getNid());
            
            if (chronology.isPresent() && chronology.get().getIsaacObjectType() == IsaacObjectType.SEMANTIC) {
               if (((SemanticChronology)chronology.get()).getVersionType() == VersionType.DESCRIPTION) {
                  
                  LatestVersion<DescriptionVersion> dv = chronology.get().getLatestVersion(sc);
                  if (dv.isPresent()) {
                     float adjustValue = 0f;
                     String matchingString = dv.get().getText().toLowerCase(Locale.ENGLISH);
                     String localQuery = query.trim().toLowerCase(Locale.ENGLISH);

                     if (matchingString.equals(localQuery)) {
                        // "exact match, bump by 2"
                        adjustValue = 2.0f;
                     }
                     else if (matchingString.startsWith(localQuery)) {
                        // "add 1, plus a bit more boost based on the length of the matches (shorter matches get more boost)"
                        adjustValue = 1.0f + (1.0f - ((float) (matchingString.length() - localQuery.length()) / (float) matchingString.length()));
                     }

                     if (adjustValue > 0f) {
                        ((ComponentSearchResult)sr).setScore(sr.getScore() + adjustValue);
                     }
                  }
               }
               else {
                  LOG.warn("Prefix match search got an unexpected result: {}", chronology);
               }
            }
            else {
               LOG.warn("Prefix match search got an unexpected result: {}", chronology);
            }
         }
         
         //Re-sort based on adjusted scores
         Collections.sort(results);
         
         LOG.debug("Time for prefix-search score manipulation: {}ms", System.currentTimeMillis() - time);
      }
      return results;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcepts,
         Predicate<Integer> filter,
         AuthorModulePathRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      
      return query(query, prefixSearch, assemblageConcepts, filter, amp, false, (int[]) null, null, pageNum, sizeLimit, targetGeneration);
   }
   
   public int getDescriptionExtendedTypeNid()
   {
      if (this.descExtendedTypeNid == 0)
      {
         this.descExtendedTypeNid = DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid();
      }
      return this.descExtendedTypeNid;
   }
}
