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
package sh.isaac.api.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.observable.ObservableConceptProxy;

/**
 *
 * @author kec
 */
public class ObservableConceptSpecificationAdaptor extends XmlAdapter<ConceptProxy, ObservableConceptProxy> {

    @Override
    public ObservableConceptProxy unmarshal(ConceptProxy v) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConceptProxy marshal(ObservableConceptProxy v) throws Exception {
        return new ConceptProxy(v.getNid());
    }
    
}
