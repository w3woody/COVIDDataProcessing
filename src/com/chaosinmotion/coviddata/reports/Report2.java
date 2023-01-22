package com.chaosinmotion.coviddata.reports;

import com.chaosinmotion.coviddata.utils.GregorianDate;
import com.chaosinmotion.coviddata.utils.Utils;
import com.chaosinmotion.coviddata.csv.CSVParser;

import java.io.*;
import java.nio.Buffer;
import java.text.ParseException;
import java.util.*;

/**
 * Once we have the table giving the number of visits to a point of care
 * service for each reporter, we can now scan through the data and get
 * some other interesting information by looking through the history of those
 * who sought care.
 *
 * This requires Report1 to have been run, so we can load the cached values
 * for health care interaction.
 */
public class Report2
{
	private static String[] reaction = { "", "Mild", "Moderate", "Severe" };
	private static String[] healthNow = { "", "Excellent", "Good", "Fair", "Poor" };
	private static String[] feeling = { "", "Poor", "Fair", "Good" };
	private static String[] healthImpact = {
			"Be unable to do their normal daily activities",
			"Be unable to work",
			"Be unable to do your normal daily activities",
			"Get care from a doctor or other healthcare professional",
			"Be unable to work or attend school"
	};
	private static String[] healthChange = { "", "Worse", "About the same", "Better" };
	private static String[] healthVisit = {
		"Emergency room or emergency department visit",
		"Hospitalization",
		"Outpatient clinic or urgent care clinic visit",
		"Telehealth, virtual health, or email health consultation"
	};
	private static String[] pregnantStatus = {		// default == 0
		"I don't know", "Yes", "No"
	};
	private static String[] siteReaction = {
		"Pain", "Redness", "Swelling", "Itching"
	};
	private static String[] systemicReation = {
		"Abdominal pain",
		"Chills",
		"Diarrhea",
		"Fatigue or tiredness",
		"Headache",
		"Joint pains",
		"Muscle or body aches",
		"Nausea",
		"Rash, not including the immediate area around the injection site",
		"Vomiting",
	};
	private static String[] yesno = { "No", "Yes" };

	/**
	 * Provide a compact representation so we can quickly scan for stuff.
	 */
	public static class Report
	{
		static final byte versionID = 1;

		// For ABDOMINAL_PAIN, CHILLS, DIARRHEA, FATIGUE, HEADACHE, ITCHING,
		// JOINT\_PAINS, MUSCLE\_OR\_BODY\_ACHES, NAUSEA, PAIN,
		// RASH\_OUTSIDE\_INJECTION\_SITE, REDNESS, SWELLING and VOMITING
		private static final int REACTION_NONE = 0;
		private static final int REACTION_MILD = 1;
		private static final int REACTION_MODERATE = 2;
		private static final int REACTION_SEVERE = 3;

		private static final int HEALTH_NOW_NONE = 0;
		private static final int HEALTH_NOW_EXCELLENT = 1;
		private static final int HEALTH_NOW_GOOD = 2;
		private static final int HEALTH_NOW_FAIR = 3;
		private static final int HEALTH_NOW_POOR = 4;

		private static final int HEALTH_COMPARISON_NONE = 0;
		private static final int HEALTH_COMPARISON_WORSE = 1;
		private static final int HEALTH_COMPARISON_SAME = 2;
		private static final int HEALTH_COMPARISON_BETTER = 3;

		private static final int HEALTHCARE_VISIT_ER = 1;
		private static final int HEALTHCARE_VISIT_HOSPITAL = 2;
		private static final int HEALTHCARE_VISIT_OUTPATIENT = 4;
		private static final int HEALTHCARE_VISIT_TELEHEALTH = 8;

		private static final int PREGNANT_UNKNOWN = 0;
		private static final int PREGNANT_YES = 1;
		private static final int PREGNANT_NO = 0;

		private static final int SITE_REACTION_PAIN = 1;
		private static final int SITE_REACTION_REDNESS = 2;
		private static final int SITE_REACTION_SWELLING = 4;
		private static final int SITE_REACTION_ITCHING = 8;

		GregorianDate date;			// Date of this report
		byte ABDOMINAL_PAIN;
		byte CHILLS;
		byte DIARRHEA;
		byte FATIGUE;
		byte FEELING_TODAY;
		boolean FEVER;
		boolean HAD_SYMPTOMS;
		byte HEADACHE;
		byte HEALTH_IMPACT;
		byte HEALTH_NOW;
		byte HEALTH_NOW_COMPARISON;
		boolean VACCINE_CAUSED_HEALTH_ISSUES;
		byte HEALTHCARE_VISITS;
		byte ITCHING;
		byte JOINT_PAINS;
		byte MUSCLE_OR_BODY_ACHES;
		byte NAUSEA;
		byte PAIN;
		byte PREGNANT;
		boolean PREGNANCY_TEST;		// PREGNANT_XXX
		byte RASH_OUTSIDE_INJECTION_SITE;
		byte REDNESS;
		byte SITE_REACTION;			// SITE_REACTION_XXX
		byte SWELLING;
		short SYSTEMIC_REACTION;		// Systemic reaction fields
//		short TEMPERATURE_CELSIUS;		// # * 10, so 38.0 -> 380
//		short TEMPERATURE_FAHRENHEIT;
//		byte TEMPERATURE_READING;
		boolean TESTED_POSITIVE;
		GregorianDate TESTED_POSITIVE_DATE;
		byte VOMITING;

		public Report(String[] row) throws ParseException
		{
			date = new GregorianDate(row[3]);		// started on

			ABDOMINAL_PAIN = (byte)Utils.lookup(reaction,row[6]);
			CHILLS = (byte)Utils.lookup(reaction,row[7]);
			DIARRHEA = (byte)Utils.lookup(reaction,row[8]);
			FATIGUE = (byte)Utils.lookup(reaction,row[9]);
			FEELING_TODAY = (byte)Utils.lookup(feeling,row[10]);

			FEVER = Utils.lookup(yesno,row[11],0) == 1;
			HAD_SYMPTOMS = Utils.lookup(yesno,row[12],0) == 1;

			HEADACHE = (byte)Utils.lookup(reaction,row[13]);
			HEALTH_IMPACT = (byte)Utils.fields(healthImpact,row[14]);
			HEALTH_NOW = (byte)Utils.lookup(healthNow,row[15]);
			HEALTH_NOW_COMPARISON = (byte)Utils.lookup(healthChange,row[16]);
			VACCINE_CAUSED_HEALTH_ISSUES = Utils.lookup(yesno,row[17],0) == 1;
			HEALTHCARE_VISITS = (byte)Utils.fields(healthVisit,row[18]);
			ITCHING = (byte)Utils.lookup(reaction,row[19]);

			JOINT_PAINS = (byte)Utils.lookup(reaction,row[20]);
			MUSCLE_OR_BODY_ACHES = (byte)Utils.lookup(reaction,row[21]);
			NAUSEA = (byte)Utils.lookup(reaction,row[22]);
			PAIN = (byte)Utils.lookup(reaction,row[23]);

			PREGNANT = (byte)Utils.lookup(pregnantStatus,row[24],0);
			PREGNANCY_TEST = Utils.lookup(yesno,row[25],0) == 1;

			RASH_OUTSIDE_INJECTION_SITE = (byte)Utils.lookup(reaction,row[26]);
			REDNESS = (byte)Utils.lookup(reaction,row[27]);
			SITE_REACTION = (byte)Utils.fields(siteReaction,row[28]);
			SWELLING = (byte)Utils.lookup(reaction,row[29]);

			SYSTEMIC_REACTION = (short)Utils.fields(systemicReation,row[30]);
			TESTED_POSITIVE = Utils.lookup(yesno,row[34],0) == 1;

			String testDate = row[35];
			if (!testDate.equalsIgnoreCase("")) {
				TESTED_POSITIVE_DATE = new GregorianDate(row[35]);
			}

			VOMITING = (byte)Utils.lookup(reaction,row[36]);
		}

		public Report(DataInput input) throws IOException
		{
			byte v = input.readByte();
			if (v != versionID) throw new IOException("Sync Error");

			date = new GregorianDate(input.readInt());

			ABDOMINAL_PAIN = input.readByte();
			CHILLS = input.readByte();
			DIARRHEA = input.readByte();
			FATIGUE = input.readByte();
			FEELING_TODAY = input.readByte();
			FEVER = input.readBoolean();
			HAD_SYMPTOMS = input.readBoolean();
			HEADACHE = input.readByte();
			HEALTH_IMPACT = input.readByte();
			HEALTH_NOW = input.readByte();
			HEALTH_NOW_COMPARISON = input.readByte();
			VACCINE_CAUSED_HEALTH_ISSUES = input.readBoolean();
			HEALTHCARE_VISITS = input.readByte();
			ITCHING = input.readByte();
			JOINT_PAINS = input.readByte();
			MUSCLE_OR_BODY_ACHES = input.readByte();
			NAUSEA = input.readByte();
			PAIN = input.readByte();
			PREGNANT = input.readByte();
			PREGNANCY_TEST = input.readBoolean();
			RASH_OUTSIDE_INJECTION_SITE = input.readByte();
			REDNESS = input.readByte();
			SITE_REACTION = input.readByte();
			SWELLING = input.readByte();
			SYSTEMIC_REACTION = input.readShort();
			TESTED_POSITIVE = input.readBoolean();

			int ct = input.readInt();
			if (ct == 0) {
				TESTED_POSITIVE_DATE = null;
			} else {
				TESTED_POSITIVE_DATE = new GregorianDate(ct);
			}

			VOMITING = input.readByte();
		}

		public void write(DataOutput writer) throws IOException
		{
			writer.writeByte(versionID);

			writer.writeInt(date.getCount());

			writer.writeByte(ABDOMINAL_PAIN);
			writer.writeByte(CHILLS);
			writer.writeByte(DIARRHEA);
			writer.writeByte(FATIGUE);
			writer.writeByte(FEELING_TODAY);
			writer.writeBoolean(FEVER);
			writer.writeBoolean(HAD_SYMPTOMS);
			writer.writeByte(HEADACHE);
			writer.writeByte(HEALTH_IMPACT);
			writer.writeByte(HEALTH_NOW);
			writer.writeByte(HEALTH_NOW_COMPARISON);
			writer.writeBoolean(VACCINE_CAUSED_HEALTH_ISSUES);
			writer.writeByte(HEALTHCARE_VISITS);
			writer.writeByte(ITCHING);
			writer.writeByte(JOINT_PAINS);
			writer.writeByte(MUSCLE_OR_BODY_ACHES);
			writer.writeByte(NAUSEA);
			writer.writeByte(PAIN);
			writer.writeByte(PREGNANT);
			writer.writeBoolean(PREGNANCY_TEST);
			writer.writeByte(RASH_OUTSIDE_INJECTION_SITE);
			writer.writeByte(REDNESS);
			writer.writeByte(SITE_REACTION);
			writer.writeByte(SWELLING);
			writer.writeShort(SYSTEMIC_REACTION);
			writer.writeBoolean(TESTED_POSITIVE);
			writer.writeInt(TESTED_POSITIVE_DATE == null ? 0 : TESTED_POSITIVE_DATE.getCount());
			writer.writeByte(VOMITING);
		}
	}

	public static class Vaccine
	{
		static final byte versionID = 1;
		GregorianDate date;
		byte vaccineNumber;

		public Vaccine(String[] row) throws ParseException
		{
			date = new GregorianDate(row[3]);
			vaccineNumber = (byte)Integer.parseInt(row[2]);
		}

		public void write(DataOutput writer) throws IOException
		{
			writer.writeByte(versionID);
			writer.writeInt(date.getCount());
			writer.writeByte(vaccineNumber);
		}

		public Vaccine(DataInput input) throws IOException
		{
			byte v = input.readByte();
			if (v != versionID) throw new IOException("Sync");
			date = new GregorianDate(input.readInt());
			vaccineNumber = input.readByte();
		}
	}

	public static class Data
	{
		ArrayList<Report> reports = new ArrayList<>();
		ArrayList<Vaccine> vaccines = new ArrayList<>();

		public Data()
		{
		}

		public Data(DataInput dis) throws IOException, ParseException
		{
			if (1 != dis.readByte()) throw new IOException("Sync");
			int nreports = dis.readInt();
			int nvaccines = dis.readInt();
			for (int i = 0; i < nreports; ++i) {
				reports.add(new Report(dis));
			}
			for (int i = 0; i < nvaccines; ++i) {
				vaccines.add(new Vaccine(dis));
			}
		}

		public void write(DataOutput writer) throws IOException
		{
			writer.writeByte(1);		// version
			writer.writeInt(reports.size());
			writer.writeInt(vaccines.size());

			for (Report r: reports) {
				r.write(writer);
			}
			for (Vaccine v: vaccines) {
				v.write(writer);
			}
		}
	}

	public static void run(int totalCount) throws IOException, ParseException, ClassNotFoundException
	{
		HashMap<String, Report1.Visit> visitCount = new HashMap<>();

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
			String[] firstRow = parser.readRow();

			for (; ; ) {
				String[] row = parser.readRow();
				if (row == null) break;

				Report1.Visit v = new Report1.Visit();
				v.er = Integer.parseInt(row[1]);
				v.hospital = Integer.parseInt(row[2]);
				v.outpatient = Integer.parseInt(row[3]);
				v.telehealth = Integer.parseInt(row[4]);

				visitCount.put(row[0], v);
			}

			fr.close();

			System.out.println("Finished reading cache");
		} else {
			throw new RuntimeException("Report2.run cannot run before Report1.run has been run");
		}

		/*
		 *	Now build the set of those who have had any sort of interaction
		 * 	with a health care provider, setting flags for other events that
		 * 	may be of interest here.
		 */

		HashMap<String, Report2.Data> hcData;

		f = new File("cache/healthlogs.ser");
		if (f.exists()) {
			System.out.println("Reading second cache");

			// The file contains a serialized block of data that builds our hash
			// map

			FileInputStream fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis,1024000);
			DataInputStream dis = new DataInputStream(bis);

			int rowCount = 0;
			hcData = new HashMap<>();
			int length = dis.readInt();
			for (int i = 0; i < length; ++i) {
				++rowCount;
				if ((rowCount % 10000) == 0) {
					if ((rowCount % 100000) == 0) {
						System.out.print("+");
					} else {
						System.out.print("-");
					}
					if ((rowCount % 1000000) == 0) {
						System.out.println();
					}
					System.out.flush();
				}

				String r = dis.readUTF();
				Data d = new Data(dis);
				hcData.put(r,d);
			}

			dis.close();

			System.out.println("Finished reading second cache");
		} else {
			/*
			 *	Load from our data
			 */

			hcData = new HashMap<>();
			for (Map.Entry<String, Report1.Visit> entry : visitCount.entrySet()) {
				// Only create record if we have seen a doctor at any time.
				Report1.Visit v = entry.getValue();
				if (v.hasVisit()) {
					hcData.put(entry.getKey(), new Data());
				}
			}

			/*
			 *	Crack open our really big file, copy data records associated with
			 * 	each reporter who made a visit.
			 */

			FileReader fr = new FileReader("data/consolidated_health_checkin.csv");
			CSVParser parser = new CSVParser(fr);
			String[] firstRow = parser.readRow();

			int rindex = Utils.lookup(firstRow, "REGISTRANT_CODE");

			int rowCount = 0;
			for (; ; ) {
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

				if (row.length < rindex) continue;        // Should never happen.

				String registrantCode = row[rindex];
				Data data = hcData.get(registrantCode);
				if (data != null) {
					Report rdata = new Report(row);
					data.reports.add(rdata);
				}
			}
			fr.close();

			/*
			 *	Crack open the vaccine data
			 */

			System.out.println();
			fr = new FileReader("data/consolidated_vaccinations[1].csv");
			parser = new CSVParser(fr);
			parser.readRow();

			rowCount = 0;
			for (; ; ) {
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

				if (row.length < rindex) continue;        // Should never happen.

				String registrantCode = row[0];
				Data data = hcData.get(registrantCode);
				if (data != null) {
					Vaccine vaccine = new Vaccine(row);
					data.vaccines.add(vaccine);
				}
			}
			fr.close();

			/*
			 *	Scan the data and sort
			 */

			for (Map.Entry<String, Data> e : hcData.entrySet()) {
				Data d = e.getValue();
				d.reports.sort(new Comparator<Report>()
				{
					@Override
					public int compare(Report o1, Report o2)
					{
						return o1.date.compareTo(o2.date);
					}
				});
				d.vaccines.sort(new Comparator<Vaccine>()
				{
					@Override
					public int compare(Vaccine o1, Vaccine o2)
					{
						return o1.date.compareTo(o2.date);
					}
				});
			}

			/*
			 *	This was a lot of data. Now spit it out
			 */

			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos,1024000);
			DataOutputStream dos = new DataOutputStream(bos);

			int size = hcData.size();
			dos.writeInt(size);

			rowCount = 0;
			for (Map.Entry<String,Data> d: hcData.entrySet()) {
				++rowCount;
				if ((rowCount % 10000) == 0) {
					if ((rowCount % 100000) == 0) {
						System.out.print("+");
					} else {
						System.out.print("-");
					}
					if ((rowCount % 1000000) == 0) {
						System.out.println();
					}
					System.out.flush();
				}

				dos.writeUTF(d.getKey());
				d.getValue().write(dos);
			}

			dos.flush();
			dos.close();

			System.out.println();
		}

		/*
		 *	This was a lot of data. We now have an ordered list of data records
		 * 	associated with that subset of registrants who saw a doctor, so we
		 * 	can determine things like if the person thought the vaccine caused
		 * 	a problem or not.
		 */

		/*
		 *	Here's a fun thing we can do: Break down the number of people who
		 * 	have had an interaction with a health care provider by those who
		 * 	have the 'VACCINE_CAUSED_HEALTH_ISSUES' set somewhere in their
		 * 	history. If we see they visited a health care provider but it came
		 * 	before they think they had a vaccine-caused problem, then it doesn't
		 * 	count, right?
		 *
		 * 	Yes, no, maybe? But if you never said "I think the vaccine caused
		 * 	my symptoms" and you went to see a doctor--then let's not count
		 * 	those. We'll also not count those who saw a doctor and *later* said
		 * 	"the vaccine caused this" on the assumption their visit was for
		 * 	something unrelated and later they felt problems.
		 */

		int total = 0;
		int er = 0;
		int hosp = 0;
		int out = 0;
		int thealth = 0;

		for (Map.Entry<String,Data> d: hcData.entrySet()) {
			/*
			 *	Try this in SQL! :-P
			 */

			boolean count = false;
			byte visits = 0;

			for (Report r: d.getValue().reports) {
				boolean vflag = false;

				if (r.VACCINE_CAUSED_HEALTH_ISSUES) {
					vflag = true;
				}

				if (vflag && (r.HEALTHCARE_VISITS != 0)) {
					visits |= r.HEALTHCARE_VISITS;
				}
			}

			if (visits != 0) {
				++total;

				if (0 != (visits & Report.HEALTHCARE_VISIT_ER)) ++er;
				if (0 != (visits & Report.HEALTHCARE_VISIT_HOSPITAL)) ++hosp;
				if (0 != (visits & Report.HEALTHCARE_VISIT_OUTPATIENT)) ++out;
				if (0 != (visits & Report.HEALTHCARE_VISIT_TELEHEALTH)) ++thealth;
			}
		}

		System.out.println("Health care interactions by people after vaccine flag set:");
		System.out.println("Any:               " + total + " " + Utils.perc(total,totalCount));
		System.out.println("ER Visits:         " + er + " " + Utils.perc(er,totalCount));
		System.out.println("Hospital Visits:   " + hosp + " " + Utils.perc(hosp,totalCount));
		System.out.println("Outpatient Visits: " + out + " " + Utils.perc(out,totalCount));
		System.out.println("Telehealth Visits: " + thealth + " " + Utils.perc(thealth,totalCount));

	}
}
