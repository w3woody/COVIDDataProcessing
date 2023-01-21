package com.chaosinmotion.coviddata.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.Buffer;
import java.util.ArrayList;

/**
 * The CSVParser class provides a way to parse the data stored in a CSV table.
 *
 * The rows are read, one at a time, and returned. When we reach the end, a
 * null pointer is returned. No attempt is made to assure the length of each
 * row is the same.
 *
 * Note we parse according to the rules of RFC-4180.
 *
 * https://datatracker.ietf.org/doc/html/rfc4180
 */
public class CSVParser
{
	private BufferedReader reader;

	/*
	 *	Internal buffer replacing StringBuilder, because we need this to be
	 * 	fast. Basically we never reclaim our memory as we build our strings.
	 *
	 * 	The theory is that this will be a lot faster than using StringBuilder
	 * 	in that once we're underway, we never need to allocate or reclaim
	 * 	memory beyond allocating strings as we build them.
	 */
	private int pos;
	private char[] buffer;

	public CSVParser(Reader reader) throws IOException
	{
		if (reader instanceof BufferedReader) {
			this.reader = (BufferedReader) reader;
		} else {
			this.reader = new BufferedReader(reader);
		}

		pos = 0;
		buffer = new char[1024];
	}

	/**
	 * Append charater to our internal buffer
	 * @param ch
	 */
	private void appendBuffer(char ch)
	{
		if (pos >= buffer.length) {
			char[] resize = new char[buffer.length * 2];
			System.arraycopy(buffer,0,resize,0,pos);
			buffer = resize;
		}
		buffer[pos++] = ch;
	}

	private String getBufferAsString()
	{
		return new String(buffer,0,pos);
	}

	private void clearBuffer()
	{
		pos = 0;
	}

	/**
	 * This reads a row and converts the contents into separate strings,
	 * escaping quotes and commas.
	 * @return The array read at this location, or null if at EOF
	 * @throws IOException
	 */
	public String[] readRow() throws IOException
	{
		boolean atStartOfLine = true;
		boolean inQuote = false;
		int ch;
		ArrayList<String> list = new ArrayList<>();

		clearBuffer();

		for (;;) {
			ch = reader.read();
			if (ch == -1) {
				if (!atStartOfLine) {
					list.add(getBufferAsString());
					clearBuffer();
					return list.toArray(new String[list.size()]);
				} else {
					return null;
				}
			}
			atStartOfLine = false;

			if (inQuote) {
				/*
				 *	NOTE: If we are in a quote, we allow all characters but
				 * 	quotes to pass through unchanged. That includes EOL and
				 * 	comma characters. We also do not collapse \r\n or \n\r
				 * 	sequences during parsing.
				 */
				if (ch == '"') {
					// This could either be a closed quote or an escaped
					// quote. Scan ahead one character to determine what
					// we're looking at.
					reader.mark(1);
					ch = reader.read();
					if (ch == '"') {
						appendBuffer((char)ch);
					} else {
						// Close quote. Rewind, close quote.
						reader.reset();
						inQuote = false;
					}
				} else {
					/*
					 *	Anything else appends the character. Note we append
					 * 	the code point to handle extended UTF-16 characters.
					 */
					appendBuffer((char)ch);
				}
			} else {
				if ((ch == ',') || (ch == '\n') || (ch == '\r')) {
					/*
					 *	At end of component or end of line. Save and store
					 */

					list.add(getBufferAsString());
					clearBuffer();

					/*
					 *	Collapse \r\n or \n\r sequences.
					 */
					if ((ch == '\n') || (ch == '\r')) {
						// Swallow the next character if we see /r/n or /n/r
						reader.mark(1);
						int ch2 = reader.read();
						if (((ch2 != '\n') && (ch2 != '\r')) || (ch == ch2)) {
							// this is not an \n\r or \r\n pair. Rewind
							reader.reset();
						}

						// Now return the row.
						return list.toArray(new String[list.size()]);
					}
				} else if (ch == '"') {
					inQuote = true;
				} else {
					appendBuffer((char)ch);
				}
			}
		}
	}
}
