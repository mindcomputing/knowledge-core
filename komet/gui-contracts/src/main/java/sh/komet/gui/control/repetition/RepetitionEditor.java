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
package sh.komet.gui.control.repetition;

import javafx.scene.Node;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.statement.Repetition;
import sh.isaac.model.statement.RepetitionImpl;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class RepetitionEditor implements PropertyEditor<Repetition>{

    private final RepetitionPropertySheet repPropertySheet;
    private RepetitionImpl repetition;
    public RepetitionEditor(Manifold manifold) {
        repPropertySheet = new RepetitionPropertySheet(manifold);
        repPropertySheet.getPropertySheet().setPropertyEditorFactory(new PropertyEditorFactory(manifold));
    }

    @Override
    public Node getEditor() {
        return repPropertySheet.getPropertySheet();
    }

    @Override
    public Repetition getValue() {
        return repetition;
    }

    @Override
    public void setValue(Repetition value) {
        if (this.repetition != null) {
            this.repetition.eventDurationProperty().unbind();
            this.repetition.eventFrequencyProperty().unbind();
            this.repetition.periodDurationProperty().unbind();
            this.repetition.periodStartProperty().unbind();
        }
        repPropertySheet.setRepition((RepetitionImpl) value);
        this.repetition = (RepetitionImpl) value;
    }
    
}
