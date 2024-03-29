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
package sh.komet.gui.contract;

import org.jvnet.hk2.annotations.Contract;
import sh.komet.gui.interfaces.DetailAspectNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Contract
public interface DetailAspectFactory extends NodeFactory {
   /**
    * 
    * @param manifold the manifold that determines the current coordinates and focus. 
    * @return the detail node, after it has been added to the parent. 
    */
   DetailAspectNode createDetailAspectNode(Manifold manifold);
   
}
