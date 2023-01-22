package com.chaosinmotion.coviddata.utils;

public class Utils
{
	/**
	 * Look up the field from the list of fields. Throws an exception if the
	 * field was not found.
	 * @param fields The first row of the data with the TOC
	 * @param field The name of the column we're interested in
	 * @return The index of the column with the data we want
	 */
	public static int lookup(String[] fields, String field, int defaultValue)
	{
		for (int i = 0; i < fields.length; ++i) {
			if (fields[i].equalsIgnoreCase(field)) return i;
		}
		if (defaultValue != -1) return defaultValue;

		throw new RuntimeException("Programmer is an idiot; field " + field + " not found");
	}

	public static int lookup(String[] fields, String field)
	{
		return lookup(fields,field,-1);
	}

	/**
	 * This uses the fields as a bitmap mask.
	 * @param fields
	 * @param field
	 * @return
	 */
	public static int fields(String[] fields, String field)
	{
		if (field.equalsIgnoreCase("None")) return 0;
		if (field.equalsIgnoreCase("N/A")) return 0;
		if (field.equalsIgnoreCase("")) return 0;

		String[] parts = field.split(":");
		int retValue = 0;
		int i;

		for (String part: parts) {
			for (i = 0; i < fields.length; ++i) {
				if (part.equalsIgnoreCase(fields[i])) {
					retValue |= 1 << i;
					break;
				}
			}
			if (i >= fields.length) {
				throw new RuntimeException("Programmer is an idiot; flag " + part + " not found");
			}
		}

		return retValue;
	}
}
