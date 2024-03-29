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



package sh.isaac.api.chronicle;

import java.util.List;
import java.util.UUID;
import sh.isaac.api.Status;
import sh.isaac.api.commit.CommitStates;

/**
 *
 * @author kec
 */
public class CategorizedVersion
         implements Version {
   private final Version             delegate;
   private final CategorizedVersions<CategorizedVersion> categorizedVersions;

   public CategorizedVersion(Version delegate, CategorizedVersions<CategorizedVersion> categorizedVersions) {
      this.delegate            = delegate;
      this.categorizedVersions = categorizedVersions;
   }

   @Override
   public void addAdditionalUuids(UUID... uuids) {
      delegate.addAdditionalUuids(uuids);
   }
   
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      return delegate.equals(obj);
   }

   @Override
   public int hashCode() {
      return delegate.hashCode();
   }

   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      return delegate.makeAnalog(stampSequence);
   }

   @Override
   public String toString() {
      return delegate.toString();
   }

   @Override
   public String toUserString() {
      return delegate.toUserString();
   }

   @SuppressWarnings("unchecked")
   public <V extends Version> V unwrap() {
      return (V) delegate;
   }

   @Override
   public int getAuthorNid() {
      return delegate.getAuthorNid();
   }

   @Override
   public void setAuthorNid(int authorNid) {
      delegate.setAuthorNid(authorNid);
   }

   @Override
   public Chronology getChronology() {
      return delegate.getChronology();
   }

   @Override
   public CommitStates getCommitState() {
      return delegate.getCommitState();
   }

   @Override
   public int getModuleNid() {
      return delegate.getModuleNid();
   }

   @Override
   public void setModuleNid(int moduleNid) {
      delegate.setModuleNid(moduleNid);
   }

   @Override
   public int getNid() {
      return delegate.getNid();
   }

   @Override
   public int getPathNid() {
      return delegate.getPathNid();
   }

   @Override
   public void setPathNid(int pathNid) {
      delegate.setPathNid(pathNid);
   }

   @Override
   public UUID getPrimordialUuid() {
      return delegate.getPrimordialUuid();
   }

   @Override
   public int getStampSequence() {
      return delegate.getStampSequence();
   }

   @Override
   public Status getStatus() {
      return delegate.getStatus();
   }

   @Override
   public long getTime() {
      return delegate.getTime();
   }

   @Override
   public void setTime(long time) {
      delegate.setTime(time);
   }

   @Override
   public void setStatus(Status state) {
      delegate.setStatus(state);
   }

   @Override
   public boolean isUncommitted() {
      return delegate.isUncommitted();
   }

   @Override
   public List<UUID> getUuidList() {
      return delegate.getUuidList();
   }

   @Override
   public UUID[] getUuids() {
      return delegate.getUuids();
   }

   public VersionCategory getVersionCategory() {
      return categorizedVersions.getVersionCategory(this);
   }

   @Override
   public VersionType getSemanticType() {
      return delegate.getSemanticType();
   }
   
   public CategorizedVersions<CategorizedVersion> getCategorizedVersions() {
      return categorizedVersions;
   }   

   @Override
   public int getAssemblageNid() {
      return delegate.getAssemblageNid();
   }

    @Override
    public boolean deepEquals(Object other) {
        return delegate.deepEquals(other);
    }
}

