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

package sh.isaac.convert.mojo.sopt.data;

/**
 * 
 * {@link SOPTValueSetColumnsV1}
 * 
 * Version 1 Column headers for SOPT import file
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 *
 */
public enum SOPTValueSetColumnsV1
{
	ValueSetName("Value Set Name"), 
	ValueSetCode("Value Set Code"), 
	ValueSetOID("Value Set OID"), 
	ValueSetVersion("Value Set Version"), 
	ValueSetDefinition("Value Set Definition"), 
	ValueSetStatus("Value Set Status"), 
	ValueSetUpdatedDate("VS Last Updated Date"), 
	ValueSetReleaseComments("VS Release Comments"),
	ValueSetConceptDefinitionText("Value Set Concept Definition Text");  //added in V8 (though probably in error, on this tab)

	final private String columnName;

	private SOPTValueSetColumnsV1(String name)
	{
		this.columnName = name;
	}

	public String toString()
	{
		return columnName;
	}

	/**
	 * @param stringCellValue
	 * @return the matching enum type
	 */
	public static SOPTValueSetColumnsV1 parse(String stringCellValue)
	{
		for (SOPTValueSetColumnsV1 col : SOPTValueSetColumnsV1.values())
		{
			if (col.columnName.equalsIgnoreCase(stringCellValue))
			{
				return col;
			}
		}
		throw new RuntimeException("Can't locate known column for '" + stringCellValue + "'");
	}
}
