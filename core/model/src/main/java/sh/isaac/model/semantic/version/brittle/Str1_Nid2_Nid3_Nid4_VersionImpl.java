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
package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Nid2_Nid3_Nid4_Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class Str1_Nid2_Nid3_Nid4_VersionImpl 
        extends AbstractVersionImpl
         implements Str1_Nid2_Nid3_Nid4_Version {
   String str1 = null;
   int    nid2 = Integer.MAX_VALUE;
   int    nid3 = Integer.MAX_VALUE;
   int    nid4 = Integer.MAX_VALUE;

   public Str1_Nid2_Nid3_Nid4_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Str1_Nid2_Nid3_Nid4_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.str1 = data.getUTF();
      this.nid2 = data.getNid();
      this.nid3 = data.getNid();
      this.nid4 = data.getNid();
   }

   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putUTF(this.str1);
      data.putNid(this.nid2);
      data.putNid(this.nid3);
      data.putNid(this.nid4);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final Str1_Nid2_Nid3_Nid4_VersionImpl newVersion = new Str1_Nid2_Nid3_Nid4_VersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setStr1(this.str1);
      newVersion.setNid2(this.nid2);
      newVersion.setNid3(this.nid3);
      newVersion.setNid4(this.nid4);
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Str1_Nid2_Nid3_Nid4_VersionImpl another = (Str1_Nid2_Nid3_Nid4_VersionImpl) other;
      if (this.str1 == null ? another.str1 != null : !this.str1.equals(another.str1)) {
         editDistance++;
      }
      if (this.nid2 != another.nid2) {
         editDistance++;
      }
      if (this.nid3 != another.nid3) {
         editDistance++;
      }
      if (this.nid4 != another.nid4) {
         editDistance++;
      }

      return editDistance;
   }

   @Override
   public int getNid3() {
      return nid3;
   }

   @Override
   public void setNid3(int nid3) {
      this.nid3 = nid3;
   }

   @Override
   public int getNid4() {
      return nid4;
   }

   @Override
   public void setNid4(int nid4) {
      this.nid4 = nid4;
   }

   @Override
   public String getStr1() {
      return str1;
   }

   @Override
   public void setStr1(String str1) {
      this.str1 = str1;
   }

   @Override
   public int getNid2() {
      return nid2;
   }

   @Override
   public void setNid2(int nid) {
      this.nid2 = nid;
   }
}