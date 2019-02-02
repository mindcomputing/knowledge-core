/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.search.extended;

import java.util.EnumSet;
import java.util.function.Predicate;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 * variable carrying class to help with search restrictions, create an
 * appropriate predicate to pass into the search APIs during a query
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class TimeStatusRestriction {

    Long afterTime;
    Long beforeTime;
    EnumSet<Status> allowedStatus;
    ManifoldCoordinate manifold;

    public TimeStatusRestriction(Long afterTime, Long beforeTime, EnumSet<Status> allowedStatus, ManifoldCoordinate manifold) {
        this.afterTime = afterTime;
        this.beforeTime = beforeTime;
        this.allowedStatus = allowedStatus;
        this.manifold = manifold;
    }

    public Long getAfterTime() {
        return afterTime;
    }

    public Long getBeforeTime() {
        return beforeTime;
    }

    public EnumSet<Status> getAllowedStates() {
        return allowedStatus;
    }

    public Predicate<Integer> getTimeStatusFilter() {
        if (afterTime == null && beforeTime == null && allowedStatus == null) {
            return null;
        } else {
            return new Predicate<Integer>() {
                @Override
                public boolean test(Integer nid) {
                    //Each integer passed in here is a semantic nid, of something that was indexed.  True 
                    //if we want it to be allowed for the query, false if not.
                    //We will return true, if there is any version which meets all present criteria.
                    SemanticChronology sc = Get.assemblageService().getSemanticChronology(nid);
                    LatestVersion<Version> latest = sc.getLatestVersion(manifold);
                    if (latest.isPresent()) {
                        Version v = latest.get();
                        if ((allowedStatus == null || allowedStatus.contains(v.getStatus()))
                                && (afterTime == null || v.getTime() > afterTime)
                                && (beforeTime == null || v.getTime() < beforeTime)) {
                            if (sc.getVersionType() == VersionType.DESCRIPTION) {
                                ConceptChronology concept = Get.concept(sc.getReferencedComponentNid());
                                LatestVersion<Version> latestConcept = concept.getLatestVersion(manifold);
                                if (latestConcept.isPresent()) {
                                    if (allowedStatus == null || allowedStatus.contains(latestConcept.get().getStatus())) {
                                        return true;
                                    }

                                }
                                return false;
                            }
                            return true;
                        }
                    }
                    return false;

                }
            };
        }
    }
}
