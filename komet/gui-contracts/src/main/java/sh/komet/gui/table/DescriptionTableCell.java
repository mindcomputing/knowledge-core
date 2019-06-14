package sh.komet.gui.table;

import java.util.HashSet;
import java.util.Set;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.interfaces.DraggableWithImage;

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

/**
 *
 * @author kec
 * @param <T> The type for a row in the table
 */
public class DescriptionTableCell<T extends IdentifiedObject> extends TableCell<T, String> 
        implements DraggableWithImage {

   private double   dragOffset = 0;

   public DescriptionTableCell() {
      this.setOnDragDetected(new DragDetectedCellEventHandler());
      this.setOnDragDone(new DragDoneEventHandler());
   }

   @Override
   protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty); 
      setText(item);
   }

   @Override
   public Image getDragImage() {
      SnapshotParameters snapshotParameters = new SnapshotParameters();
      //snapshotParameters.setFill(Color.BISQUE);
      dragOffset = 0;

      double width  = this.getWidth();
      double height = this.getHeight();
      
      Transform pointTransform = this.getLocalToParentTransform();
      pointTransform = pointTransform.createConcatenation(this.getParent().getLocalToParentTransform());

      try {
         snapshotParameters.setTransform(pointTransform.createInverse());
      } catch (NonInvertibleTransformException ex) {
         throw new RuntimeException(ex);
      }
      
      Point2D pointInTable = pointTransform.deltaTransform(0, 0);
      snapshotParameters.setViewport(new Rectangle2D(dragOffset -2, pointInTable.getY(), width, height));
      Image image = this.getParent().getParent().snapshot(snapshotParameters, null);
      //return this.getScene().snapshot(null);
      return image;
      //return snapshot(null, null);
   }

   @Override
   public double getDragViewOffsetX() {
      return dragOffset;
   }
   
}
