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
package sh.komet.gui.provider.concept.builder;

import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.DetailNodeFactory;
import sh.komet.gui.contract.DetailType;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;

/**
 *
 * @author kec
 */
@Service(name = "Logic Detail Provider")
@Singleton
public class ConceptBuilderProviderFactory implements DetailNodeFactory {

    @Override
    public DetailType getSupportedType() {
        return DetailType.Builder;
    }

    @Override
    public DetailNode createNode(Manifold manifold, IsaacPreferences preferencesNode) {
      return new ConceptBuilderNode(manifold);
    }

    @Override
    public String getMenuText() {
        return "Concept builder";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.NEW_CONCEPT.getIconographic();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public ManifoldGroup[] getDefaultManifoldGroups() {
        return new ManifoldGroup[] {ManifoldGroup.UNLINKED};
    }

   /** 
    * {@inheritDoc}
    */
   @Override
   public PanelPlacement getPanelPlacement() {
      return PanelPlacement.CENTER;
   }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.CONCEPT_BUILDER_PANEL____SOLOR;
    }
}
