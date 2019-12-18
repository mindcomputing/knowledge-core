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
package sh.isaac.dbConfigBuilder.fx.fxUtil;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * Redirect a stream to a TextArea
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class StreamRedirect extends OutputStream
{
	private static Logger log = LogManager.getLogger();
	private TextArea ta_;
	private StringBuilder buffer = new StringBuilder();
	private String lastMessage = "";
	
	public StreamRedirect(TextArea ta)
	{
		ta_ = ta;
	}
	
	@Override
	public void write(int b) throws IOException
	{
		buffer.append((char)b);
	}

	@Override
	public void flush() throws IOException
	{
		StringBuilder newBuffer = new StringBuilder();
		final StringBuilder oldBuffer = buffer;
		buffer = newBuffer;
		
		String message = oldBuffer.toString();
		if (message.length() == 0)
		{
			return;
		}
		if (!message.equals(lastMessage)) //maven can be repetitive...since it thinks its writing to a console and backspacing.
		{
			String messageTrimmed = oldBuffer.toString().trim();
			if (messageTrimmed.length() > 0 && !messageTrimmed.equals("."))
			{
				log.info("Maven Execution: " + messageTrimmed);
			}
		
			//TODO clean this up when SOLOR fixed.
			if (!messageTrimmed.contains("logic.LogicalExpressionImpl"))  //Don't log these to the screen, currently a million of them going on in SOLOR.
			{
				Platform.runLater(() ->
				{
					ta_.appendText(message);
				});
			}
		}
		
		lastMessage = message;
	}

	@Override
	public void close() throws IOException
	{
		flush();
		super.close();
	}

}
