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
package sh.komet.scripting.javascript;

import java.util.function.Consumer;
import javafx.scene.Node;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.LookupService;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Service(name = "Javascript View Provider")
@RunLevel(value = LookupService.SL_L5_ISAAC_STARTED_RUNLEVEL)
public class JavascriptViewFactory implements ExplorationNodeFactory {

    @Override
    public ExplorationNode createExplorationNode(Manifold manifold, Consumer<Node> nodeConsumer) {
      JavascriptViewProvider javascriptViewProvider = new JavascriptViewProvider(manifold);
      nodeConsumer.accept(javascriptViewProvider.getNode());
      return javascriptViewProvider;
    }

    @Override
    public String getMenuText() {
        return "Javascript";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.JAVASCRIPT.getIconographic();
    }
    
}