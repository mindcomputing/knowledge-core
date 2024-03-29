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
 */
package sh.isaac.provider.qa;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.qa.QAResults;
import sh.isaac.api.qa.QAService;
import sh.isaac.api.task.TimedTask;

/**
 * Simple QA service as a placeholder
 * @author darmbrust
 *
 */
@Service
@PerLookup
public class QAProvider implements QAService
{

	@Override
	public TimedTask<QAResults> runQA(ManifoldCoordinate coordinate)
	{
		SimpleQA sqa = new SimpleQA(coordinate);
		Get.workExecutors().getExecutor().execute(sqa);
		return sqa;
	}
	
	@Override
	public QAResults runQA(Version component, ManifoldCoordinate coordinate)
	{
		return new SimpleQA(coordinate).checkVersion(component);
	}
}
