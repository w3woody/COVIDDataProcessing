package com.chaosinmotion.coviddata.reports;

import com.chaosinmotion.coviddata.utils.Utils;
import com.chaosinmotion.coviddata.csv.CSVParser;
import com.chaosinmotion.coviddata.csv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Each report basically scours the health data set to figure out certain
 * things.
 *
 * This report scans the health data field to determine what percentage of
 * patients reported a health care visit in the VSafe data set, and what type
 * of visit did they make.
 *
 * Note we have to scan the data by patient ID, because we're interested in
 * the percentage of patients reporting a health care visit, not the percentage
 * of health care events are recorded in the table.
 *
 * This is done by building a data record counting for each patient the type
 * of visit they had from the field HEALTHCARE_VISITS, grouped by
 * REGISTRANT_CODE. We then accumulate the number of patients who reported
 * an event.
 */
public class Report1
{
	public static class Visit
	{
		// Emergency room or emergency department visit
		int er;
		// Hospitalization
		int hospital;
		// Outpatient clinic or urgent care clinic visit
		int outpatient;
		// Telehealth, virtual health, or email health consultation
		int telehealth;

		boolean hasVisit()
		{
			return (er != 0) || (hospital != 0) || (outpatient != 0) || (telehealth != 0);
		}
	}

	/**
	 * Run our report. Print our results.
	 * @throws IOException
	 */
	public static void run() throws IOException
	{
		HashMap<String,Visit> visitCount = new HashMap<>();

		/*
		 *	Before we go through this heavy lifting, see if we've cached the
		 * 	data first.
		 */
		File f = new File("cache/hinteraction.csv");
		if (f.exists()) {
			System.out.println("Reading cache");

			/*
			 *	Open our cache.
			 */

			FileReader fr = new FileReader(f);
			CSVParser parser = new CSVParser(fr);
			parser.readRow();			// skip toc

			for (;;) {
				String[] row = parser.readRow();
				if (row == null) break;

				Visit v = new Visit();
				v.er = Integer.parseInt(row[1]);
				v.hospital = Integer.parseInt(row[2]);
				v.outpatient = Integer.parseInt(row[3]);
				v.telehealth = Integer.parseInt(row[4]);

				visitCount.put(row[0],v);
			}

			fr.close();

			System.out.println("Finished reading cache");
		} else {
			/*
			 *	Open our big massive file, and track the TOC indexes for the
			 * 	fields I'm interested in
			 */
			FileReader fr = new FileReader("data/consolidated_health_checkin.csv");
			CSVParser parser = new CSVParser(fr);
			String[] firstRow = parser.readRow();

			int hvisit = Utils.lookup(firstRow,"HEALTHCARE_VISITS");
			int rindex = Utils.lookup(firstRow,"REGISTRANT_CODE");

			/*
			 *	Now run the rest. This all takes time, you know.
			 */

			int rowCount = 0;
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

				if (row.length < rindex) continue;		// Should never happen.
				String registrantCode = row[rindex];
				String healthVisit = (hvisit >= row.length) ? "" : row[hvisit];

				boolean er = false;
				boolean hospital = false;
				boolean outpatient = false;
				boolean telehealth = false;
				if (!healthVisit.isEmpty()) {
					String[] hflags = healthVisit.split(":");

					for (String h : hflags) {
						if (h.equalsIgnoreCase("Emergency room or emergency department visit")) {
							er = true;
						} else if (h.equalsIgnoreCase("Hospitalization")) {
							hospital = true;
						} else if (h.equalsIgnoreCase("Outpatient clinic or urgent care clinic visit")) {
							outpatient = true;
						} else if (h.equalsIgnoreCase("Telehealth, virtual health, or email health consultation")) {
							telehealth = true;
						} else {
							throw new RuntimeException("Programmer is an idiot; flag " + h + " not found");
						}
					}
				}

				/*
				 *	Now get the registrant's health flags and increment the
				 * 	appropriate fields.
				 */

				Visit v = visitCount.get(registrantCode);
				if (v == null) {
					v = new Visit();
					visitCount.put(registrantCode,v);
				}

				if (er) v.er++;
				if (hospital) v.hospital++;
				if (outpatient) v.outpatient++;
				if (telehealth) v.telehealth++;
			}

			fr.close();

			/*
			 *	Now write our cache. This will be much quicker to load later.
			 */

			FileWriter outfile = new FileWriter(f);
			CSVWriter writer = new CSVWriter(outfile);

			String[] row = new String[5];
			row[0] = "REGISTRANT_CODE";
			row[1] = "ER";
			row[2] = "HOSPITAL";
			row[3] = "OUTPATIENT";
			row[4] = "TELEHEALTH";
			writer.writeRow(row);

			for (Map.Entry<String,Visit> e: visitCount.entrySet()) {
				Visit v = e.getValue();

				row[0] = e.getKey();
				row[1] = Integer.toString(v.er);
				row[2] = Integer.toString(v.hospital);
				row[3] = Integer.toString(v.outpatient);
				row[4] = Integer.toString(v.telehealth);
				writer.writeRow(row);
			}

			writer.close();
		}



		/*
		 *	At this point we have a list of registrant codes and counts of
		 * 	their interactions with health care providers. Dump our results.
		 */

		int totalCount = visitCount.size();		// Total # of unique registrant codes

		int totalER = 0;
		int totalHospital = 0;
		int totalOutpatient = 0;
		int totalTelehealth = 0;
		int anyReaction = 0;

		for (Map.Entry<String,Visit> e: visitCount.entrySet()) {
			Visit v = e.getValue();

			if (v.er != 0) totalER++;
			if (v.hospital != 0) totalHospital++;
			if (v.outpatient != 0) totalOutpatient++;
			if (v.telehealth != 0) totalTelehealth++;

			if ((v.er != 0) || (v.hospital != 0) || (v.outpatient != 0) || (v.telehealth != 0)) {
				anyReaction++;
			}
		}

		System.out.println();
		System.out.println("Total registrants: " + totalCount);
		System.out.println("Any:               " + anyReaction);
		System.out.println("ER Visits:         " + totalER);
		System.out.println("Hospital Visits:   " + totalHospital);
		System.out.println("Outpatient Visits: " + totalOutpatient);
		System.out.println("Telehealth Visits: " + totalTelehealth);
	}
}
