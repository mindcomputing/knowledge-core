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
package sh.isaac.model.builder;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------
import javafx.concurrent.Task;

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.api.chronicle.Chronology;

//~--- classes ----------------------------------------------------------------
/**
 * The Class ConceptBuilderImpl.
 *
 * @author kec
 */
public class ConceptBuilderImpl
        extends ComponentBuilder<ConceptChronology>
        implements ConceptBuilder {

   /**
    * The description builders.
    */
   private final List<DescriptionBuilder<?, ?>> descriptionBuilders = new ArrayList<>();

   /**
    * The logical expression builders.
    */
   private final List<LogicalExpressionBuilder> logicalExpressionBuilders = new ArrayList<>();

   /**
    * The logical expressions.
    */
   private final List<LogicalExpression> logicalExpressions = new ArrayList<>();

   /**
    * The fsn description builder.
    */
   private transient DescriptionBuilder<?, ?> fsnDescriptionBuilder = null;

   /**
    * The preferred description builder.
    */
   private transient DescriptionBuilder<?, ?> preferredDescriptionBuilder = null;

   /**
    * The concept name.
    */
   private final String conceptName;

   /**
    * The semantic tag.
    */
   private final String semanticTag;

   /**
    * The default language for descriptions.
    */
   private final ConceptSpecification defaultLanguageForDescriptions;

   /**
    * The default dialect assemblage for descriptions.
    */
   private final ConceptSpecification defaultDialectAssemblageForDescriptions;

   /**
    * The default logic coordinate.
    */
   private final LogicCoordinate defaultLogicCoordinate;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new concept builder ochre impl.
    *
    * @param conceptName - Optional - if specified, a FSN will be created using this value (but see additional
    * information on semanticTag)
    * @param semanticTag - Optional - if specified, conceptName must be specified, and two descriptions will be created
    * using the following forms: FSN: - "conceptName (semanticTag)" Preferred: "conceptName" If not specified: If the
    * specified FSN contains a semantic tag, the FSN will be created using that value. A preferred term will be created
    * by stripping the supplied semantic tag. If the specified FSN does not contain a semantic tag, no preferred term
    * will be created.
    * @param logicalExpression - Optional
    * @param defaultLanguageForDescriptions - Optional - used as the language for the created FSN and preferred term
    * @param defaultDialectAssemblageForDescriptions - Optional - used as the language for the created FSN and preferred
    * term
    * @param defaultLogicCoordinate - Optional - used during the creation of the logical expression, if either a
    * logicalExpression is passed, or if @link {@link #addLogicalExpression(LogicalExpression)} or
    * {@link #addLogicalExpressionBuilder(LogicalExpressionBuilder)} are used later.
    */
   public ConceptBuilderImpl(String conceptName,
           String semanticTag,
           LogicalExpression logicalExpression,
           ConceptSpecification defaultLanguageForDescriptions,
           ConceptSpecification defaultDialectAssemblageForDescriptions,
           LogicCoordinate defaultLogicCoordinate) {
      this.conceptName = conceptName;
      this.semanticTag = semanticTag;
      this.defaultLanguageForDescriptions = defaultLanguageForDescriptions;
      this.defaultDialectAssemblageForDescriptions = defaultDialectAssemblageForDescriptions;
      this.defaultLogicCoordinate = defaultLogicCoordinate;

      if (logicalExpression != null) {
         this.logicalExpressions.add(logicalExpression);
      }
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Adds the description.
    *
    * @param descriptionBuilder the description builder
    * @return the concept builder
    */
   @Override
   public ConceptBuilder addDescription(DescriptionBuilder<?, ?> descriptionBuilder) {
      this.descriptionBuilders.add(descriptionBuilder);
      return this;
   }

   /**
    * Adds the description.
    *
    * @param value the value
    * @param descriptionType the description type
    * @return the concept builder
    */
   @Override
   public ConceptBuilder addDescription(String value, ConceptSpecification descriptionType) {
      if ((this.defaultLanguageForDescriptions == null) || (this.defaultDialectAssemblageForDescriptions == null)) {
         throw new IllegalStateException("language and dialect are required if a concept name is provided");
      }

      if (!this.conceptName.equals(value)) {
         this.descriptionBuilders.add(LookupService.getService(DescriptionBuilderService.class)
                 .getDescriptionBuilder(value, this, descriptionType, this.defaultLanguageForDescriptions)
                 .addAcceptableInDialectAssemblage(this.defaultDialectAssemblageForDescriptions));
      }

      return this;
   }

   /**
    * Adds the logical expression.
    *
    * @param logicalExpression the logical expression
    * @return the concept builder
    */
   @Override
   public ConceptBuilder addLogicalExpression(LogicalExpression logicalExpression) {
      this.logicalExpressions.add(logicalExpression);
      return this;
   }

   /**
    * Adds the logical expression builder.
    *
    * @param logicalExpressionBuilder the logical expression builder
    * @return the concept builder
    */
   @Override
   public ConceptBuilder addLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder) {
      this.logicalExpressionBuilders.add(logicalExpressionBuilder);
      return this;
   }

   /**
    * Sets the logical expression. This method erases any previous logical expressions.
    *
    * @param logicalExpression the logical expression
    * @return the concept builder
    */
   @Override
   public ConceptBuilder setLogicalExpression(LogicalExpression logicalExpression) {
      this.logicalExpressions.clear();
      this.logicalExpressions.add(logicalExpression);
      return this;
   }

   /**
    * Sets the logical expression builder. This method erases previous logical expression builders.
    *
    * @param logicalExpressionBuilder the logical expression builder
    * @return the concept builder
    */
   @Override
   public ConceptBuilder setLogicalExpressionBuilder(LogicalExpressionBuilder logicalExpressionBuilder) {
      this.logicalExpressionBuilders.clear();
      this.logicalExpressionBuilders.add(logicalExpressionBuilder);
      return this;
   }

   /**
    * Builds the.
    *
    * @param stampCoordinate the stamp coordinate
    * @param builtObjects the built objects
    * @return the concept chronology
    * @throws IllegalStateException the illegal state exception
    */
   @Override
   public ConceptChronology build(int stampCoordinate,
           List<Chronology<? extends StampedVersion>> builtObjects)
           throws IllegalStateException {
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
              .getConcept(getUuids());

      conceptChronology.createMutableVersion(stampCoordinate);
      builtObjects.add(conceptChronology);

      if (getFullySpecifiedDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getFullySpecifiedDescriptionBuilder());
      }

      if (getPreferredDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getPreferredDescriptionBuilder());
      }

      this.descriptionBuilders.forEach((builder) -> {
         builder.build(stampCoordinate, builtObjects);
      });

      if ((this.defaultLogicCoordinate == null)
              && ((this.logicalExpressions.size() > 0) || (this.logicalExpressionBuilders.size() > 0))) {
         throw new IllegalStateException("A logic coordinate is required when a logical expression is passed");
      }

      final SememeBuilderService builderService = LookupService.getService(SememeBuilderService.class);

      for (final LogicalExpression logicalExpression : this.logicalExpressions) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(logicalExpression,
                 this,
                 this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      for (final LogicalExpressionBuilder builder : this.logicalExpressionBuilders) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(builder.build(),
                 this,
                 this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      this.sememeBuilders.forEach((builder) -> builder.build(stampCoordinate, builtObjects));
      return conceptChronology;
   }

   /**
    * Builds the.
    *
    * @param editCoordinate the edit coordinate
    * @param changeCheckerMode the change checker mode
    * @param builtObjects the built objects
    * @return the optional wait task
    * @throws IllegalStateException the illegal state exception
    */
   @Override
   public OptionalWaitTask<ConceptChronology> build(EditCoordinate editCoordinate,
           ChangeCheckerMode changeCheckerMode,
           List<Chronology<? extends StampedVersion>> builtObjects)
           throws IllegalStateException {
      final ArrayList<OptionalWaitTask<?>> nestedBuilders = new ArrayList<>();
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
              .getConcept(getUuids());

      conceptChronology.createMutableVersion(this.state, editCoordinate);
      builtObjects.add(conceptChronology);

      if (getFullySpecifiedDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getFullySpecifiedDescriptionBuilder());
      }

      if (getPreferredDescriptionBuilder() != null) {
         this.descriptionBuilders.add(getPreferredDescriptionBuilder());
      }

      this.descriptionBuilders.forEach((builder) -> {
         nestedBuilders.add(builder.build(editCoordinate,
                 changeCheckerMode,
                 builtObjects));
      });

      if ((this.defaultLogicCoordinate == null)
              && ((this.logicalExpressions.size() > 0) || (this.logicalExpressionBuilders.size() > 0))) {
         throw new IllegalStateException("A logic coordinate is required when a logical expression is passed");
      }

      final SememeBuilderService builderService = LookupService.getService(SememeBuilderService.class);

      for (final LogicalExpression logicalExpression : this.logicalExpressions) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(logicalExpression,
                 this,
                 this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      for (final LogicalExpressionBuilder builder : this.logicalExpressionBuilders) {
         this.sememeBuilders.add(builderService.getLogicalExpressionSememeBuilder(builder.build(),
                 this,
                 this.defaultLogicCoordinate.getStatedAssemblageSequence()));
      }

      this.sememeBuilders.forEach((builder) -> nestedBuilders.add(builder.build(editCoordinate,
              changeCheckerMode,
              builtObjects)));

      Task<Void> primaryNested;

      if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
         primaryNested = Get.commitService()
                 .addUncommitted(conceptChronology);
      } else {
         primaryNested = Get.commitService()
                 .addUncommittedNoChecks(conceptChronology);
      }

      return new OptionalWaitTask<>(primaryNested, conceptChronology, nestedBuilders);
   }

   /**
    * Merge from spec.
    *
    * @param conceptSpec the concept spec
    * @return the concept builder
    */
   @Override
   public ConceptBuilder mergeFromSpec(ConceptSpecification conceptSpec) {
      setPrimordialUuid(conceptSpec.getPrimordialUuid());
      addUuids(conceptSpec.getUuids());

      if (!this.conceptName.equals(conceptSpec.getFullySpecifiedConceptDescriptionText())) {
         addDescription(conceptSpec.getFullySpecifiedConceptDescriptionText(), TermAux.SYNONYM_DESCRIPTION_TYPE);
      }

      return this;
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the concept description text.
    *
    * @return the concept description text
    */
   @Override
   public String getFullySpecifiedConceptDescriptionText() {
      return this.conceptName;
   }

   /**
    * Gets the fully specified description builder.
    *
    * @return the fully specified description builder
    */
   @Override
   public DescriptionBuilder<?, ?> getFullySpecifiedDescriptionBuilder() {
      synchronized (this) {
         if ((this.fsnDescriptionBuilder == null) && StringUtils.isNotBlank(this.conceptName)) {
            final StringBuilder descriptionTextBuilder = new StringBuilder();

            descriptionTextBuilder.append(this.conceptName);

            if (StringUtils.isNotBlank(this.semanticTag)) {
               if ((this.conceptName.lastIndexOf('(') > 0)
                       && (this.conceptName.lastIndexOf(')') == this.conceptName.length() - 1)) {
                  // semantic tag already added. 
               } else {
                  descriptionTextBuilder.append(" (");
                  descriptionTextBuilder.append(this.semanticTag);
                  descriptionTextBuilder.append(")");
               }

            }

            if ((this.defaultLanguageForDescriptions == null)
                    || (this.defaultDialectAssemblageForDescriptions == null)) {
               throw new IllegalStateException("language and dialect are required if a concept name is provided");
            }

            this.fsnDescriptionBuilder = LookupService.getService(DescriptionBuilderService.class)
                    .getDescriptionBuilder(descriptionTextBuilder.toString(),
                            this,
                            TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE,
                            this.defaultLanguageForDescriptions)
                    .addPreferredInDialectAssemblage(this.defaultDialectAssemblageForDescriptions);
         }
      }

      return this.fsnDescriptionBuilder;
   }

   /**
    * Gets the synonym preferred description builder.
    *
    * @return the synonym preferred description builder
    */
   @Override
   public DescriptionBuilder<?, ?> getPreferredDescriptionBuilder() {
      synchronized (this) {
         if (this.preferredDescriptionBuilder == null) {
            if ((this.defaultLanguageForDescriptions == null)
                    || (this.defaultDialectAssemblageForDescriptions == null)) {
               throw new IllegalStateException("language and dialect are required if a concept name is provided");
            }

            String prefName = null;

            if (StringUtils.isNotBlank(this.semanticTag)) {
               prefName = this.conceptName;
            } else if ((this.conceptName.lastIndexOf('(') > 0)
                    && (this.conceptName.lastIndexOf(')') == this.conceptName.length())) {
               // they didn't provide a stand-alone semantic tag.  If they included a semantic tag in what they provided, strip it.
               // If not, don't create a preferred term, as it would just be identical to the FSN.
               prefName = this.conceptName.substring(0, this.conceptName.lastIndexOf('('))
                       .trim();
            }

            if (prefName != null) {
               this.preferredDescriptionBuilder = LookupService.getService(DescriptionBuilderService.class)
                       .getDescriptionBuilder(prefName,
                               this,
                               TermAux.SYNONYM_DESCRIPTION_TYPE,
                               this.defaultLanguageForDescriptions)
                       .addPreferredInDialectAssemblage(this.defaultDialectAssemblageForDescriptions);
            }
         }
      }

      return this.preferredDescriptionBuilder;
   }

   @Override
   public Optional<String> getPreferedConceptDescriptionText() {
      DescriptionBuilder<?, ?> descriptionBuilder = getPreferredDescriptionBuilder();
      return Optional.of(descriptionBuilder.getDescriptionText());
   }
}
