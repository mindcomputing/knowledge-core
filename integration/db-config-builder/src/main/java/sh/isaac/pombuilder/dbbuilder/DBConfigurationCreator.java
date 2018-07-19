/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.pombuilder.dbbuilder;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.pom._4_0.Build;
import org.apache.maven.pom._4_0.Build.Plugins;
import org.apache.maven.pom._4_0.Dependency;
import org.apache.maven.pom._4_0.License;
import org.apache.maven.pom._4_0.Model;
import org.apache.maven.pom._4_0.Model.Dependencies;
import org.apache.maven.pom._4_0.Model.Licenses;
import org.apache.maven.pom._4_0.Model.Properties;
import org.apache.maven.pom._4_0.Parent;
import org.apache.maven.pom._4_0.Plugin;
import org.apache.maven.pom._4_0.Plugin.Executions;
import org.apache.maven.pom._4_0.PluginExecution;
import org.apache.maven.pom._4_0.PluginExecution.Configuration;
import org.apache.maven.pom._4_0.PluginExecution.Goals;
import org.apache.maven.pom._4_0.Scm;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.util.StringUtils;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.GitPublish;
import sh.isaac.provider.sync.git.gitblit.GitBlitUtils;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link DBConfigurationCreator}
 * Create a new maven pom project which when executed, will input a set if IBDF files, and build them into a runnable database for ISAAC systems.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DBConfigurationCreator
{
	/** The Constant LOG. */
	private static final Logger LOG = LogManager.getLogger();

	/** The Constant parentGroupId. */
	private static final String PARENT_GROUP_ID = "sh.isaac.integration";

	/** The Constant parentArtifactId. */
	public static final String PARENT_ARTIFIACT_ID = "db-builder";

	/** The Constant groupId. */
	public static final String GROUP_ID = "sh.isaac.db";

	// ~--- methods -------------------------------------------------------------

	/**
	 * Construct a new DB builder project which is executable via maven.
	 *
	 * @param name - The name to use for the maven artifact that will result from executing this generated pom file.
	 * @param version - The version to use for the maven artifact that will result from executing this generated pom file.
	 * @param description - Describe the purpose / contents of the database being constructed
	 * @param resultClassifier - The (optional) maven classifer to use for the maven artifact that will result from executing this generated pom file.
	 * @param classify - True to classify the content with the snorocket classifer as part of the database build, false to skip classification.
	 * @param ibdfFiles - The set of IBDF files to be included in the DB. Do not include the metadata IBDF file from ISAAC, it is always included.
	 * @param metadataVersion - The version of the metadata content to include in the DB
	 * @param dbImplementation - optional - which type of DB should we build.  If not provided, uses system default.
	 * @param gitRepositoryURL - optional - The URL to publish this built project to.  If not provided, the project is not published.
	 * @param gitUsername - optional - The username to utilize to publish this project
	 * @param gitPassword - optional - the git password
	 * @param workingFolder - optional - if provided, the working files are created inside this folder.  If not provided, uses a system temp folder.
	 * @param deleteAfterPublish - true if the content created inside of workingFolder should be deleted after a successful publish
	 * @return the tag created in the repository that carries the created project
	 * @throws Exception the exception
	 */
	public static String createDBConfiguration(String name, String version, String description, String resultClassifier, boolean classify, IBDFFile[] ibdfFiles,
			String metadataVersion, DatabaseImplementation dbImplementation, String gitRepositoryURL, String gitUsername, char[] gitPassword, File workingFolder, boolean deleteAfterPublish) throws Exception
	{
		LOG.info("Creating a db configuration name: {} version: {} description: {}, with a classifier: '{}' on '{}' and the classify flag set to {}", name,
				version, description, resultClassifier, gitRepositoryURL, classify);

		try
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("metadataVersion: {}, IBDF Files: {}", metadataVersion, Arrays.toString(ibdfFiles));
			}

			final Model model = new Model();

			model.setModelVersion("4.0.0");

			final Parent parent = new Parent();

			parent.setGroupId(PARENT_GROUP_ID);
			parent.setArtifactId(PARENT_ARTIFIACT_ID);
			parent.setVersion(metadataVersion);
			model.setParent(parent);
			model.setGroupId(GROUP_ID);
			model.setArtifactId(name);
			model.setVersion(version);
			model.setName(PARENT_ARTIFIACT_ID + ": " + name);
			model.setPackaging("pom");
			model.setDescription(description);

			final Scm scm = new Scm();

			scm.setUrl(StringUtils.isNotBlank(gitRepositoryURL) ? GitBlitUtils.constructChangesetRepositoryURL(gitRepositoryURL) : "");
			scm.setTag(GROUP_ID + "/" + name + "/" + version);
			model.setScm(scm);

			final Properties properties = new Properties();

			properties.setInParent("false");

			if ((resultClassifier != null) && (resultClassifier.length() > 0))
			{
				properties.setResultArtifactClassifier(resultClassifier);
			}

			model.setProperties(properties);

			final Licenses licenses = new Licenses();
			final License l = new License();

			l.setName("The Apache Software License, Version 2.0");
			l.setUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
			l.setDistribution("repo");
			l.setComments("Copyright Notice\n" + "                This is a work of the U.S. Government and is not subject to copyright\n"
					+ "                protection in the United States. Foreign copyrights may apply.");
			licenses.getLicense().add(l);

			// TODO extract licenses from IBDF file(s), include here.
			model.setLicenses(licenses);

			final Dependencies dependencies = new Dependencies();
			Dependency dependency = new Dependency();

			dependency.setGroupId("sh.isaac.core");
			dependency.setArtifactId("metadata");
			dependency.setClassifier("all");
			dependency.setVersion(metadataVersion);
			dependency.setType("ibdf.zip");
			dependency.setOptional(true);
			dependencies.getDependency().add(dependency);

			for (final IBDFFile ibdf : ibdfFiles)
			{
				dependency = new Dependency();
				dependency.setGroupId(ibdf.getGroupId());
				dependency.setArtifactId(ibdf.getArtifactId());
				dependency.setVersion(ibdf.getVersion());

				if (ibdf.hasClassifier())
				{
					dependency.setClassifier(ibdf.hasClassifier() ? ibdf.getClassifier() : "");
				}

				dependency.setType("ibdf.zip");
				dependency.setOptional(true);
				dependency.setScope("compile");
				dependencies.getDependency().add(dependency);
			}

			model.setDependencies(dependencies);

			final Build build = new Build();
			final Plugins plugins = new Plugins();
			Plugin plugin = new Plugin();

			plugin.setGroupId("org.apache.maven.plugins");
			plugin.setArtifactId("maven-dependency-plugin");

			Executions executions = new Executions();

			// Extract dependencies
			PluginExecution pe = new PluginExecution();

			pe.setId("extract-ibdf");
			pe.setPhase("generate-resources");

			Goals goals = new Goals();

			goals.getGoal().add("unpack-dependencies");
			pe.setGoals(goals);

			Configuration configuration = new Configuration();
			final StringBuilder sb = new StringBuilder();

			for (final IBDFFile ibdf : ibdfFiles)
			{
				sb.append(ibdf.getArtifactId());
				sb.append(",");
			}

			sb.append("metadata");
			configuration.setIncludeArtifactIds(sb.toString());
			configuration.setOutputDirectory("${project.build.directory}/data");
			pe.setConfiguration(configuration);
			executions.getExecution().add(pe);
			plugin.setExecutions(executions);
			plugins.getPlugin().add(plugin);

			// new plugin
			plugin = new Plugin();
			plugin.setGroupId("sh.isaac.core");
			plugin.setArtifactId("mojo");
			plugin.setDependencies(new org.apache.maven.pom._4_0.Plugin.Dependencies());
			
			Dependency pd = new Dependency();
			pd.setGroupId("sh.isaac.core");
			pd.setArtifactId("log-config");
			pd.setVersion(metadataVersion);
			pd.setScope("compile");
			
			plugin.getDependencies().getDependency().add(pd);
			executions = new Executions();

			// setup isaac
			pe = new PluginExecution();
			pe.setId("setup-isaac");
			goals = new Goals();
			goals.getGoal().add("setup-isaac");
			goals.getGoal().add("count-concepts");
			pe.setGoals(goals);
			configuration = new Configuration();
			configuration.setDataStoreLocation("${project.build.directory}/${project.build.finalName}${resultArtifactClassifierWithLeadingHyphen}.data/");
			configuration.setDbBuildMode(BuildMode.DB.name());
			configuration.setDbImplementation(dbImplementation == null ? DatabaseImplementation.DEFAULT.name() : dbImplementation.name());
			pe.setConfiguration(configuration);
			executions.getExecution().add(pe);

			// load termstore
			pe = new PluginExecution();
			pe.setId("load-termstore");
			goals = new Goals();
			goals.getGoal().add("load-termstore");
			pe.setGoals(goals);
			configuration = new Configuration();
			configuration.setIbdfFileFolder("${project.build.directory}/data/");
			pe.setConfiguration(configuration);
			executions.getExecution().add(pe);

			// count
			pe = new PluginExecution();
			pe.setId("count-after-load");
			goals = new Goals();
			goals.getGoal().add("count-concepts");
			pe.setGoals(goals);
			executions.getExecution().add(pe);

			// classify
			if (classify)
			{
				pe = new PluginExecution();
				pe.setId("classify");
				goals = new Goals();
				goals.getGoal().add("quasi-mojo-executor");
				pe.setGoals(goals);
				configuration = new Configuration();
				configuration.setQuasiMojoName("full-classification");
				pe.setConfiguration(configuration);
				executions.getExecution().add(pe);
			}

			// index and shutdown
			pe = new PluginExecution();
			pe.setId("shutdown");
			goals = new Goals();
			// goals.getGoal()
			// .add("stop-heap-ticker");
			// goals.getGoal()
			// .add("stop-tasks-ticker");
			goals.getGoal().add("shutdown-isaac");
			pe.setGoals(goals);
			executions.getExecution().add(pe);
			plugin.setExecutions(executions);
			plugins.getPlugin().add(plugin);
			build.setPlugins(plugins);
			model.setBuild(build);
			
			File baseFolder;
			if (workingFolder != null)
			{
				baseFolder = new File(workingFolder, PARENT_ARTIFIACT_ID);
				FileUtil.recursiveDelete(baseFolder);
			}
			else
			{
				baseFolder = Files.createTempDirectory(PARENT_ARTIFIACT_ID).toFile();
			}
			baseFolder.mkdirs();

			FileUtil.writePomFile(model, baseFolder);
			FileUtil.writeFile("dbProjectTemplate", "DOTgitattributes", baseFolder);
			FileUtil.writeFile("dbProjectTemplate", "DOTgitignore", baseFolder);
			FileUtil.writeFile("shared", "LICENSE.txt", baseFolder);
			FileUtil.writeFile("shared", "NOTICE.txt", baseFolder);
			FileUtil.writeFile("dbProjectTemplate", "src/assembly/isaac.xml", baseFolder);
			FileUtil.writeFile("dbProjectTemplate", "src/assembly/MANIFEST.MF", baseFolder);
			
			if (StringUtils.isNotBlank(gitRepositoryURL)) 
			{
				GitPublish.publish(baseFolder, gitRepositoryURL, gitUsername, gitPassword, scm.getTag());
				if (deleteAfterPublish)
				{
					try
					{
						FileUtil.recursiveDelete(baseFolder);
					}
					catch (final Exception e)
					{
						LOG.error("Problem cleaning up temp folder " + baseFolder, e);
					}
				}
			}

			final String tag = scm.getTag();

			return tag;
		}
		catch (Exception e)
		{
			LOG.error("createDBConfiguration failed with ", e);
			throw e;
		}
	}
}
