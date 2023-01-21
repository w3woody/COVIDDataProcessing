package com.chaosinmotion.coviddata.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * This class allows me to write CSV files. We use this format for caching
 * intermediate results, as it takes me several minutes to load and parse the
 * raw data.
 */
public class CSVWriter
{
	private BufferedWriter writer;

	public CSVWriter(Writer writer)
	{
		if (writer instanceof BufferedWriter) {
			this.writer = (BufferedWriter) writer;
		} else {
			this.writer = new BufferedWriter(writer);
		}
	}

	/**
	 * Escape the string for writing. This adds double quotes to the string
	 * and surrounds with quotes if it contains a comma. Note if this does
	 * not contain a double-quote or comma, the string is returned
	 * unmodified
	 * @param str The string to write
	 * @return The escaped string text that can be written
	 */
	private static String escapeString(String str)
	{
		boolean flag = false;

		if (-1 != str.indexOf(',')) flag = true;
		if (-1 != str.indexOf('"')) flag = true;

		if (!flag) return str;

		StringBuilder builder = new StringBuilder();
		builder.append('"');

		int i,len = str.length();
		for (i = 0; i < len; ++i) {
			char ch = str.charAt(i);
			if (ch == '"') {
				// double up quotes
				builder.append(ch);
			}
			builder.append(ch);
		}

		builder.append('"');

		return builder.toString();
	}

	public void writeRow(List<String> row) throws IOException
	{
		boolean first = true;
		for (String str: row) {
			if (first) first = false;
			else writer.write(',');

			writer.write(escapeString(str));
		}
		writer.newLine();
	}

	public void writeRow(String[] row) throws IOException
	{
		boolean first = true;
		for (String str: row) {
			if (first) first = false;
			else writer.write(',');

			writer.write(escapeString(str));
		}
		writer.newLine();
	}

	public void close() throws IOException
	{
		writer.close();
	}
}
