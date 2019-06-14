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



/*
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
package sh.isaac.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.index.IndexBuilderService;

//~--- classes ----------------------------------------------------------------

/**
 * Goal which indexes a database using indexer services on the classpath.
 */
@Mojo(
   name         = "index-termstore",
   defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class IndexTermstore
        extends AbstractMojo {
   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      try {
         Headless.setHeadless();
         final List<IndexBuilderService> indexers     = LookupService.get()
                                                                .getAllServices(IndexBuilderService.class);
         final List<String>         indexerNames = new ArrayList<>();

         indexers.stream()
                 .forEach((IndexBuilderService i) -> indexerNames.add(i.getIndexerName()));
         getLog().info("Found indexers: " + indexerNames);
         getLog().info("Starting indexing. ");
         Get.startIndexTask((Class<? extends IndexBuilderService>[]) null)
            .get();
         getLog().info("Finished indexing. ");
      } catch (final Exception ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      }
   }
}

