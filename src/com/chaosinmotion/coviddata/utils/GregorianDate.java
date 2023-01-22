package com.chaosinmotion.coviddata.utils;

import java.text.ParseException;
import java.util.Objects;

public class GregorianDate implements Comparable<GregorianDate>
{
	/**
	 * This contains the parts of a Gregorian Date: day, month, year
	 */
	public static final class Parts
	{
		private int day;
		private int month;
		private int year;

		public Parts(int day, int month, int year)
		{
			this.day = day;
			this.month = month;
			this.year = year;
		}

		public int getDay()
		{
			return day;
		}

		public int getMonth()
		{
			return month;
		}

		public int getYear()
		{
			return year;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof Parts parts)) return false;
			return day == parts.day && month == parts.month && year == parts.year;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(day, month, year);
		}
	}
	private static final int EPOCH = 1;

	// The day for our date is actually stored as a day count.
	private int count;      // Day count from epoch

	/**
	 * Set the current date object to the specified day, month and year.
	 * Note that this will correct for the input date; thus, if you
	 * enter February 30, 1997, you'll get March 2, 1997.
	 *
	 * @param y Year (i.e., 1900, 2000, etc)
	 * @param m Month (January == 1)
	 * @param d Day of month
	 */
	public GregorianDate(int y, int m, int d)
	{
		count = dayCount(d,m,y);
	}

	/**
	 * Set the current date object from the day count.
	 * @param daycount Create from the day count from epoch
	 */
	public GregorianDate(int daycount)
	{
		count = daycount;
	}

	/**
	 * Parse mm/dd/yyyy
	 * @param date The date as mm/dd/yyyy
	 */
	public GregorianDate(String date) throws ParseException
	{
		String[] split = date.split("/");

		if (split.length != 3) throw new NumberFormatException();

		int m = Integer.parseInt(split[0]);
		int d = Integer.parseInt(split[1]);
		int y = Integer.parseInt(split[2]);

		count = dayCount(d,m,y);
	}

	/**
	 *  toString. This is a hacky thing because it can't easily be
	 *  internationalized.
	 */

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		Parts p = constructFromCount(count);

		builder.append(p.month);
		builder.append('/');
		builder.append(p.day);
		builder.append('/');
		builder.append(p.year);

		return builder.toString();
	}

	/*
	 *  Day utilities
	 */

	/**
	 * Returns the day of the week from 1 == Sunday to 7 == Saturday.
	 * Note that this relies on the EPOCH selected above, which gives
	 * the right value for the day of the week.
	 * @return The day of the week
	 */
	public int getDayOfWeek()
	{
		return (count % 7) + 1;
	}

	/*
	 *  Get the day, month, year
	 */

	/**
	 * Return the day count from EPOCH. This is only really meaningful
	 * if we're adding or subtracting days from a date.
	 * @return
	 */
	public int getCount()
	{
		return count;
	}

	/*
	 *  Compare operations
	 */

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof GregorianDate that)) return false;
		return count == that.count;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(count);
	}

	@Override
	public int compareTo(GregorianDate o)
	{
		if (count > o.count) return 1;
		if (count < o.count) return -1;
		return 0;
	}

	/*
	 *  Internal support routines borrowed from Calendrical Calculations
	 */

	/**
	 * Set our day/month/year fields based on our internal day count
	 */
	private static Parts constructFromCount(int dcount)
	{
		int year = gregorianYear(dcount);
		int priorDays = dcount - dayCount(1,1, year);
		int correction;
		int march = dayCount(1,3, year);	// March 1

		if (dcount < march) {
			correction = 0;
		} else if (isLeapYear(year)) {
			correction = 1;
		} else {
			correction = 2;
		}

		int month = (12 * (priorDays + correction) + 373)/367;
		int day = dcount - dayCount(1, month, year) + 1;

		return new Parts(day,month,year);
	}

	private static int gregorianYear(int dcount)
	{
		int d;

		d = dcount - EPOCH;
		int  n400 = d / 146097;

		d %= 146097;
		int  n100 = d / 36524;

		d %= 36524;
		int  n4   = d / 1461;

		d %= 1461;
		int  n1   = d / 365;

		int year = 400 * n400 + 100 * n100 + 4 * n4 + n1;
		if ((n100 == 4) || (n1 == 4)) return year;
		return year + 1;
	}

	/*
	 *  Date utilities
	 */

	public static boolean isLeapYear(int year)
	{
		if (0 != (year % 4)) return false;		/* like 2017 */
		if (0 != (year % 100)) return true;		/* like 1996 */
		if (0 != (year % 400)) return false;	/* like 1900 */
		return true;						    /* like 2000 */
	}

	/**
	 * Given a date represented by day/month/year, convert to a day count.
	 * Our day count is the number of days from our epoch
	 * @param day
	 * @param month
	 * @param year
	 * @return
	 */
	public static int dayCount(int day, int month, int year)
	{
		int y1 = year - 1;
		int tmp;

		tmp = 365 * y1;
		tmp += y1/4;
		tmp -= y1/100;
		tmp += y1/400;
		tmp += (367 * month - 362)/12;

		if (month > 2) {
			tmp -= isLeapYear(year) ? 1 : 2;
		}

		return tmp + day - EPOCH + 1;
	}
}
