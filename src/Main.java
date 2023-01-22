import com.chaosinmotion.coviddata.csv.CSVParser;
import com.chaosinmotion.coviddata.reports.Report1;
import com.chaosinmotion.coviddata.reports.Report2;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Main
{
	/**
	 * Test routine to dump the table of contents of a CSV file.
	 * @param filename
	 * @throws IOException
	 */
	private static void dumpTOC(String filename) throws IOException
	{
		FileReader fr = new FileReader(filename);
		CSVParser parser = new CSVParser(fr);
		String[] firstRow = parser.readRow();
		fr.close();

		System.out.println(filename);
		for (String str: firstRow) {
			System.out.println("    " + str);
		}
		System.out.println();
	}

	/**
	 * This internal routine is used to dump the table of contents of all of
	 * the files in the directory, in order to understand their structure.
	 * The files I downloaded have the structure documented in the readme
	 * file.
	 */
	private static void dumpAllTOC() throws IOException
	{
		System.out.println("Dumping TOC");
		dumpTOC("data/Consolidated_health_checkin_u3[1].csv");
		dumpTOC("data/consolidated_health_checkin.csv");
		dumpTOC("data/consolidated_race_ethnicity[1].csv");
		dumpTOC("data/consolidated_registrants[1].csv");
		dumpTOC("data/consolidated_vaccinations[1].csv");
	}

	/**
	 * This routine is used to scrape the health checkin data to determine the
	 * various values entered per field.
	 */

	private static void scrapeHealthData() throws IOException
	{
		int rowCount = 0;

		FileReader fr = new FileReader("data/consolidated_health_checkin.csv");
		CSVParser parser = new CSVParser(fr);
		String[] firstRow = parser.readRow();

		// Build a list of found values in each of the columns from 6 to EOF
		HashSet<String>[] found = new HashSet[firstRow.length-6];
		for (int i = 6; i < firstRow.length; ++i) {
			found[i-6] = new HashSet<>();
		}

		for (;;) {
			String[] row = parser.readRow();
			if (row == null) break;

			++rowCount;
			if ((rowCount % 1000000) == 0) {
				if ((rowCount % 10000000) == 0) {
					System.out.print("+");
				} else {
					System.out.print("-");
				}
				if ((rowCount % 100000000) == 0) {
					System.out.println();
				}
				System.out.flush();
			}

			for (int i = 6; i < firstRow.length; ++i) {
				if (i == 10) continue;
				if (i == 14) continue;

				String contents = (i < row.length) ? row[i] : "";

				if (found[i-6] != null) {
					found[i-6].add(contents);

					int size = found[i - 6].size();
					if (size > 1000) {
						// Too many values; stop tracking
						found[i - 6] = null;
					}
				}
			}

			// Special: Pull apart row 10, 14
			String r = row[14];
			String[] rparts = r.split(":");
			if (found[14-6] != null) {
				for (String rpart: rparts) found[14-6].add(rpart);
			}

			r = row[10];
			rparts = r.split(":");
			if (found[10-6] != null) {
				for (String rpart: rparts) found[10-6].add(rpart);
			}
		}

		System.out.println();
		System.out.println("# rows " + rowCount);
		System.out.println();

		for (int i = 6; i < firstRow.length; ++i) {
			if (found[i-6] == null) continue;
			System.out.println("Column " + i + ": " + firstRow[i]);
			for (String str: found[i-6]) {
				System.out.println("    " + str);
			}
		}
	}

	public static void main(String[] args)
	{
		try {
			/*
			 *	Preliminaries: dump allowed values. This allows me to sort
			 * 	out what's going on inside the data files.
			 */
//			dumpAllTOC();
//			scrapeHealthData();
			Report1.run();
			Report2.run();
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}