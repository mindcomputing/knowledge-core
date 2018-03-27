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
package sh.isaac.solor.rf2.direct;

import java.util.Objects;
import sh.isaac.solor.ContentProvider;

/**
 *
 * @author kec
 */
public class ImportSpecification implements Comparable<ImportSpecification>{
   final ImportStreamType streamType;
   final ContentProvider contentProvider;
   
   public ImportSpecification(ContentProvider contentProvider, ImportStreamType streamType) {
         this.streamType = streamType;
         this.contentProvider = contentProvider;
   }
   
   @Override
   public int hashCode() {
      int hash = 7;
      hash = 37 * hash + Objects.hashCode(this.streamType);
      hash = 37 * hash + Objects.hashCode(this.contentProvider.getStreamSourceName());
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final ImportSpecification other = (ImportSpecification) obj;
      if (this.streamType != other.streamType) {
          return false;
      }
      return Objects.equals(this.contentProvider.getStreamSourceName(), other.contentProvider.getStreamSourceName());
   }

   @Override
   public int compareTo(ImportSpecification o) {
      if (this.streamType != o.streamType) {
         return this.streamType.compareTo(o.streamType);
      }
      return this.contentProvider.getStreamSourceName().compareTo(o.contentProvider.getStreamSourceName().toString());
   }

   @Override
   public String toString() {
      return "ImportSpecification{" + this.contentProvider.getStreamSourceName() + ", " + streamType + '}';
   }
}
