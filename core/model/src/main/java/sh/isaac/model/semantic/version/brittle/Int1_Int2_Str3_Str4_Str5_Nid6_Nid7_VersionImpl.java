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



package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl
        extends AbstractVersionImpl
         implements Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version {
   int    int1 = Integer.MAX_VALUE;
   int    int2 = Integer.MAX_VALUE;
   String str3 = null;
   String str4 = null;
   String str5 = null;
   int    nid6 = Integer.MAX_VALUE;
   int    nid7 = Integer.MAX_VALUE;

   public Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.int1 = data.getInt();
      this.int2 = data.getInt();
      this.str3 = data.getUTF();
      this.str4 = data.getUTF();
      this.str5 = data.getUTF();
      this.nid6 = data.getNid();
      this.nid7 = data.getNid();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putInt(this.int1);
      data.putInt(this.int2);
      data.putUTF(this.str3);
      data.putUTF(this.str4);
      data.putUTF(this.str5);
      data.putNid(this.nid6);
      data.putNid(this.nid7);
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl newVersion = new Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setInt1(this.int1);
      newVersion.setInt2(this.int2);
      newVersion.setStr3(this.str3);
      newVersion.setStr4(this.str4);
      newVersion.setStr5(this.str5);
      newVersion.setNid6(this.nid6);
      newVersion.setNid7(this.nid7);
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl another = (Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl) other;
      if (this.int1 != another.int1) {
         editDistance++;
      }
      if (this.int2 != another.int2) {
         editDistance++;
      }
      if (this.str3 == null ? another.str3 != null : !this.str3.equals(another.str3)) {
         editDistance++;
      }
      if (this.str4 == null ? another.str4 != null : !this.str4.equals(another.str4)) {
         editDistance++;
      }
      if (this.str5 == null ? another.str5 != null : !this.str5.equals(another.str5)) {
         editDistance++;
      }
      if (this.nid6 != another.nid6) {
         editDistance++;
      }
      if (this.nid7 != another.nid7) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   public int getInt1() {
      return int1;
   }

   @Override
   public void setInt1(int int1) {
      this.int1 = int1;
   }

   @Override
   public int getInt2() {
      return int2;
   }

   @Override
   public void setInt2(int int2) {
      this.int2 = int2;
   }

   @Override
   public int getNid6() {
      return nid6;
   }

   @Override
   public void setNid6(int nid6) {
      this.nid6 = nid6;
   }

   @Override
   public int getNid7() {
      return nid7;
   }

   @Override
   public void setNid7(int nid7) {
      this.nid7 = nid7;
   }

   @Override
   public String getStr3() {
      return str3;
   }

   @Override
   public void setStr3(String str3) {
      this.str3 = str3;
   }

   @Override
   public String getStr4() {
      return str4;
   }

   @Override
   public void setStr4(String str4) {
      this.str4 = str4;
   }

   @Override
   public String getStr5() {
      return str5;
   }

   @Override
   public void setStr5(String str5) {
      this.str5 = str5;
   }
}
