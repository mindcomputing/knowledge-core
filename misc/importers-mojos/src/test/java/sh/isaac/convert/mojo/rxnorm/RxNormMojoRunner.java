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
package sh.isaac.convert.mojo.rxnorm;

import java.io.File;
import java.util.Arrays;
import org.apache.maven.plugin.MojoExecutionException;
import javafx.application.Platform;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         Just a runner class in the test package, so that eclipse launches with a classpath that includes test libraries, which makes
 *         logging work to the console.
 */
public class RxNormMojoRunner extends RxNormImportMojoDirect
{

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws MojoExecutionException the mojo execution exception
	 */
	public static void main(String[] args) throws MojoExecutionException
	{
		final RxNormMojoRunner mojo = new RxNormMojoRunner();

		try
		{
			mojo.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
			mojo.inputFileLocationPath= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src").toPath();
			mojo.converterVersion = "foo";
			mojo.converterOutputArtifactVersion = "bar";
			mojo.converterSourceArtifactVersion = "foooo";
			mojo.converterOutputArtifactId = "rxnorm-ibdf";
			mojo.sabsToInclude = Arrays.asList(new String[] { "VANDF", "NDDF", "MMSL", "ATC", "NDFRT", "SNOMEDCT_US" });
			mojo.execute();
		}
		finally
		{
			Platform.exit();
		}
	}
}
