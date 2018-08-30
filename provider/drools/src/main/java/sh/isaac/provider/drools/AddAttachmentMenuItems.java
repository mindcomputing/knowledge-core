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
package sh.isaac.provider.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.task.OptionalWaitTask;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

/**
 *
 * @author kec
 */
public class AddAttachmentMenuItems {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

    final List<MenuItem> menuItems = new ArrayList<>();
    final Manifold manifold;
    final ObservableCategorizedVersion categorizedVersion;
    final BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer;

    public AddAttachmentMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
            BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer) {
        this.manifold = manifold;
        this.categorizedVersion = categorizedVersion;
        this.newAttachmentConsumer = newAttachmentConsumer;
    }

    public void sortMenuItems() {
        // TODO
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public VersionType getVersionType() {
        return this.categorizedVersion.getSemanticType();
    }

    public ConceptSpecification getAssemblageSpec() {
        return Get.conceptSpecification(this.categorizedVersion.getAssemblageNid());
    }

    public PropertySheetMenuItem makePropertySheetMenuItem(String menuText, ConceptSpecification assemblageSpecification) {
        PropertySheetMenuItem propertySheetMenuItem = new PropertySheetMenuItem(manifold, categorizedVersion, false);
        MenuItem menuItem = new MenuItem(menuText);
        menuItem.setOnAction((event) -> {
            try {
                SemanticChronology newChronology = makeNewChronology(assemblageSpecification);

                ObservableSemanticChronology newObservableChronology = Get.observableChronologyService().getObservableSemanticChronology(newChronology.getNid());
                CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = newObservableChronology.getCategorizedVersions(manifold);
                ObservableVersion newVersion = categorizedVersions.getUncommittedVersions().get(0).getObservableVersion();

                propertySheetMenuItem.setVersionInFlight(newVersion);

                propertySheetMenuItem.prepareToExecute();
                newAttachmentConsumer.accept(propertySheetMenuItem, assemblageSpecification);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(AddAttachmentMenuItems.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        menuItems.add(menuItem);
        return propertySheetMenuItem;
    }

    protected SemanticChronology makeNewChronology(ConceptSpecification assemblageSpecification) throws NoSuchElementException, InterruptedException, IllegalStateException, ExecutionException {
        OptionalInt optionalSemanticConceptNid = Get.assemblageService().getSemanticTypeConceptForAssemblage(assemblageSpecification, manifold);
        if (optionalSemanticConceptNid.isPresent()) {
            int semanticTypeNid = optionalSemanticConceptNid.getAsInt();
            if (semanticTypeNid == MetaData.CONCEPT_SEMANTIC____SOLOR.getNid()
                    || semanticTypeNid == MetaData.COMPONENT_SEMANTIC____SOLOR.getNid()) {
                
                SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService().getComponentSemanticBuilder(TermAux.UNINITIALIZED_COMPONENT_ID.getNid(),
                        this.categorizedVersion.getNid(),
                        assemblageSpecification.getNid());
                OptionalWaitTask<? extends SemanticChronology> buildTask = builder.build(manifold.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
                // this step does an add uncommitted...
                SemanticChronology newChronology = buildTask.get();
                return newChronology;
            } else if (semanticTypeNid == MetaData.INTEGER_SEMANTIC____SOLOR.getNid()) {
                SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService().getLongSemanticBuilder(-1,
                        this.categorizedVersion.getNid(),
                        assemblageSpecification.getNid());
                OptionalWaitTask<? extends SemanticChronology> buildTask = builder.build(manifold.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
                // this step does an add uncommitted...
                SemanticChronology newChronology = buildTask.get();
                return newChronology;
            } else if (semanticTypeNid == MetaData.MEMBERSHIP_SEMANTIC____SOLOR.getNid()) {
                SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService().getMembershipSemanticBuilder(
                        this.categorizedVersion.getNid(),
                        assemblageSpecification.getNid());
                OptionalWaitTask<? extends SemanticChronology> buildTask = builder.build(manifold.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
                // this step does an add uncommitted...
                SemanticChronology newChronology = buildTask.get();
                return newChronology;
            } else if (semanticTypeNid == MetaData.STRING_SEMANTIC____SOLOR.getNid()) {
                SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService().getStringSemanticBuilder("",
                        this.categorizedVersion.getNid(),
                        assemblageSpecification.getNid());
                OptionalWaitTask<? extends SemanticChronology> buildTask = builder.build(manifold.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
                // this step does an add uncommitted...
                SemanticChronology newChronology = buildTask.get();
                return newChronology;
            } else {
                throw new UnsupportedOperationException("Can't handle: " + Get.conceptDescriptionText(semanticTypeNid));
            }
        }

        LOG.warn("No semantic type defined for assemblge: " + Get.conceptDescriptionText(assemblageSpecification.getNid()));
        SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService().getStringSemanticBuilder("",
                this.categorizedVersion.getNid(),
                assemblageSpecification.getNid());
        OptionalWaitTask<? extends SemanticChronology> buildTask = builder.build(manifold.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
        // this step does an add uncommitted...
        SemanticChronology newChronology = buildTask.get();
        return newChronology;
    }

}
