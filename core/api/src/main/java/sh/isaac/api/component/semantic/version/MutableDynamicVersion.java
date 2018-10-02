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

package sh.isaac.api.component.semantic.version;

import java.util.List;
import java.util.function.BooleanSupplier;
import sh.isaac.api.Get;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

/**
 * The Interface MutableDynamicVersion.
 *
 * @author kec
 * @param <T> the generic type
 */
public interface MutableDynamicVersion<T extends MutableDynamicVersion<T>> extends MutableSemanticVersion, DynamicVersion<T>
{
	/**
	 * Set the data (if any) for the DynamicVersion.
	 *
	 * For a Dyanamic that is only establishing membership, there will be no data columns.
	 * 
	 * If there is one or more data columns associated with a DynamicVersion membership, then the type of each of data columns would
	 * be an extension of {@link DynamicData}
	 *
	 * @param data the new data
	 */
	public default void setData(DynamicData[] data)
	{
		boolean delayValidation = false;
		if (Get.configurationService().isInDBBuildMode(BuildMode.IBDF))
		{
			delayValidation = true;
			//TODO These delayed validations should be accessible somewhere, so that the code that put it in IBDF mode
			//could execute them later.  However, that only happens with the IBDFUtility DB build path, which is being deprecated
			//in favor of a runtime conversion pattern.  the Build Mode may go away entirely, after that conversion is complete.
			//These validations have never been properly executed in IBDF build mode.
		}
		setData(data, delayValidation);
	}

	/**
	 * Set the data (if any) for the DynamicVersion.
	 *
	 * For a Dyanamic that is only establishing membership, there will be no data columns.
	 * 
	 * If there is one or more data columns associated with a DynamicVersion membership, then the type of each of data columns would
	 * be an extension of {@link DynamicData}
	 * 
	 * This method allows certain data validations to be delayed, and is useful when loading content out-of-order. Any delayed validations
	 * are returned as a list of fuctions, it is the callers responsibility to execute those validations when the content is fully loaded.
	 *
	 * @param data the new data
	 * @param delayValidation
	 */
	public List<BooleanSupplier> setData(DynamicData[] data, boolean delayValidation);
}
