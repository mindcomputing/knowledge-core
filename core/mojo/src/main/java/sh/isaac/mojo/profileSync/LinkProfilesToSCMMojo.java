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



package sh.isaac.mojo.profileSync;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.sync.SyncFiles;
import sh.isaac.mojo.Headless;

//~--- classes ----------------------------------------------------------------

/**
 * Goal which reads a app.xml file to find the specified SCM URL and type for the profiles / changeset
 * syncing service - and pulls down any existing profiles - if present.
 *
 * See {@link SyncFiles#linkAndFetchFromRemote(String, String, String)} for specific details on the
 * behavior of this linking process.
 *
 * See {@link ProfilesMojoBase} for details on how credentials are handled.
 * Keep this in a phase earlier than GenerateUsersMojo
 */
@Service(name = "get-and-link-profiles-scm")
public class LinkProfilesToSCMMojo
        extends ProfilesMojoBase {
   /**
    * Instantiates a new link profiles to SCM mojo.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   public LinkProfilesToSCMMojo()
            throws MojoExecutionException {
      super();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      Headless.setHeadless();
      super.execute();

      if (skipRun()) {
         return;
      }

      try {
         getLog().info("Configuring " + this.userProfileFolderLocation.getAbsolutePath() + " for SCM management");
         this.userProfileFolderLocation.mkdirs();
         getProfileSyncImpl().linkAndFetchFromRemote(getURL(), getUsername(), getPassword());
         getLog().info("Done Configuring SCM for profiles");
      } catch (final Exception e) {
         throw new MojoExecutionException("Unexpected error configuring SCM for the profiles", e);
      }
   }
}

