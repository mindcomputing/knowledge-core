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

package sh.isaac.converters.sharedUtils.umlsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sh.isaac.converters.sharedUtils.sql.TerminologyFileReader;

/**
 * The Class UMLSFileReader.
 */
public class UMLSFileReader implements TerminologyFileReader
{
	private final BufferedReader reader;

	private List<String> nextLine;


	/**
	 * Instantiates a new UMLS file reader.
	 *
	 * @param reader the reader
	 */
	public UMLSFileReader(BufferedReader reader)
	{
		this.reader = reader;
	}

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see sh.isaac.converters.sharedUtils.sql.TerminologyFileReader#close()
	 */
	@Override
	public void close() throws IOException
	{
		this.reader.close();
	}

	/**
	 * Read next line.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void readNextLine() throws IOException
	{
		final String line = this.reader.readLine();

		if (line != null)
		{
			final String[] cols = line.split("\\|", -1);

			// remove the last because the files have a trailing separator, with no data after it
			this.nextLine = new ArrayList<>(cols.length - 1);

			for (final String s : cols)
			{
				if ((this.nextLine.size() == cols.length - 1) && (s == null || s.length() == 0))
				{
					break;
				}

				this.nextLine.add(s);
			}
		}
	}

	/**
	 * Gets the next row.
	 *
	 * @return the next row
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see sh.isaac.converters.sharedUtils.sql.TerminologyFileReader#getNextRow()
	 */
	@Override
	public List<String> getNextRow() throws IOException
	{
		if (this.nextLine == null)
		{
			readNextLine();
		}

		final List<String> temp = this.nextLine;

		this.nextLine = null;
		return temp;
	}

	/**
	 * Checks for next row.
	 *
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see sh.isaac.converters.sharedUtils.sql.TerminologyFileReader#hasNextRow()
	 */
	@Override
	public boolean hasNextRow() throws IOException
	{
		if (this.nextLine == null)
		{
			readNextLine();
		}

		return this.nextLine != null;
	}
}
