package com.messaging.rcs.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Rahul
 *
 */
public class DateUtils {

	private DateUtils() {
		super();
	}

	/**
	 * Convert double value upto two decimal.
	 * 
	 * @param value
	 * @return formatted double value as #.##
	 */

	public static Double getFormattedDecimalVale(Double value) {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(value));
	}

	public static boolean validateStringIsEmpty(String string) {
		return StringUtils.isBlank(string);
	}

	/**
	 * Convert date.
	 *
	 * @param timeZone the time zone
	 * @return the date
	 */
	public static Date convertDate(TimeZone timeZone) {
		return new DateTime().toMutableDateTime(DateTimeZone.forTimeZone(timeZone)).toDate();
	}

	public static String randomPasswordGenerator() {
		int length = 10;
		boolean useLetters = true;
		boolean useNumbers = true;
		return RandomStringUtils.random(length, useLetters, useNumbers);
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public static List<Date> getMinMaxDate(Date date) {
		return Arrays.asList(
				new DateTime(date).hourOfDay().withMinimumValue().minuteOfHour().withMinimumValue().secondOfMinute()
						.withMinimumValue().millisOfSecond().withMinimumValue().toDate(),
				new DateTime(date).hourOfDay().withMaximumValue().minuteOfHour().withMaximumValue().secondOfMinute()
						.withMaximumValue().millisOfSecond().withMaximumValue().toDate());
	}

	public static DateTime getMinDate(Date date) {
		return new DateTime(date).hourOfDay().withMinimumValue().minuteOfHour().withMinimumValue().secondOfMinute()
				.withMinimumValue().millisOfSecond().withMinimumValue();
	}

	private static boolean isWorkingDay(Calendar cal) {
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)
			return false;
		// tests for other holidays here
		// ...
		return true;
	}

	public static Date getWorkingDays(Integer daysToAdd) {
		Calendar cal = new GregorianCalendar();
		for (int i = 0; i < daysToAdd; i++)
			do {
				cal.add(Calendar.DAY_OF_MONTH, 1);
			} while (!isWorkingDay(cal));

		return cal.getTime();
	}

	public static void printJsonString(Object... objects) {
		String jsonString = toJsonString(objects);
		// Logger.getLogger(Utils.class).info(jsonString);
	}

	public static String toJsonString(Object... objects) {
		ObjectMapper mapperObj = new ObjectMapper();
		try {
			return mapperObj.writeValueAsString(objects);
		} catch (Exception e) {
			// Logger.getLogger(Utils.class).error(e.getLocalizedMessage(), e);
		}
		return "";
	}

	public static String generateBatchId() {
		return UUID.randomUUID().toString();
	}

	public static String matchDate(String dateForUpdate, String currentDate) {
		String returnDate = null;
		try {
			SimpleDateFormat shipmentJourneyFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			SimpleDateFormat shipmentFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date updateDate = shipmentJourneyFormat.parse(dateForUpdate);
			Date cc = shipmentFormat.parse(currentDate);
			if (cc.getTime() < updateDate.getTime()) {
				returnDate = shipmentFormat.format(updateDate);
				return returnDate;
			}
		} catch (ParseException e) {
			return returnDate;
		}

		return null;
	}

	public static String getCurrentTime(String timeZone) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			if (timeZone.equalsIgnoreCase("EST")) {
				calendar.add(Calendar.HOUR, 1);
			}
			sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
			System.out.println("Time Zone : " + timeZone);
			System.out.println("Current Server Date : " + new Date());
			System.out.println("EST Time of Current Server Date : " + sdf.format(new Date()));
			return sdf.format(calendar.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getStringDateInTimeZone(Date date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			/*
			 * if (timeZone.equalsIgnoreCase("EST")) { calendar.add(Calendar.HOUR, 1); }
			 */
			/// sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
			// System.out.println("Time Zone : " + timeZone);
			System.out.println("Current Server Date : " + new Date());
			System.out.println("EST Time of Current Server Date : " + sdf.format(date));
			return sdf.format(calendar.getTime()); // sdf.format(date) //sdf.format(calendar.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * public static void main(String[] args) {
	 *//*
		 * TimeZone utcTZ= TimeZone.getTimeZone("UTC"); TimeZone estTZ=
		 * TimeZone.getTimeZone("EST");
		 * 
		 * //System.out.println(""+getCurrentTime("EST")); Calendar sourceCalendar =
		 * Calendar.getInstance(); sourceCalendar.setTime(new Date());
		 * sourceCalendar.setTimeZone(utcTZ);
		 * 
		 * Calendar targetCalendar = Calendar.getInstance(); for (int field : new int[]
		 * {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR,
		 * Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND}) {
		 * targetCalendar.set(field, sourceCalendar.get(field)); }
		 * targetCalendar.setTimeZone(estTZ);
		 * System.out.println(targetCalendar.getTime());
		 * 
		 * System.out.println(convertDate(targetCalendar.getTime()));
		 * 
		 * Date estTime = new Date(new Date().getTime() +
		 * TimeZone.getTimeZone("EST").getRawOffset()); System.out.println(estTime);
		 *//*
			
			*//*
				 * SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); // timeZone =
				 * Calendar.getInstance().getTimeZone().getID(); // set timezone to
				 * SimpleDateFormat sdf.setTimeZone(TimeZone.getTimeZone("EST")); // return Date
				 * in required format with timezone as String
				 * System.out.println("--  "+sdf.format(new Date()));
				 *//*
					 * Calendar calendar = Calendar.getInstance(); calendar.setTime(new Date());
					 * calendar.add(Calendar.HOUR,1); SimpleDateFormat sdf = new
					 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					 * sdf.setTimeZone(TimeZone.getTimeZone("EST"));
					 * System.out.println(sdf.format(calendar.getTime()));
					 * 
					 * 
					 * }
					 */

	public static Date getCurrentDateTimeBasedOnZone(String timeZone) {
		/*
		 * try{ Calendar calendar = Calendar.getInstance(); calendar.setTime(new
		 * Date()); SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 * sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		 * System.out.println(sdf.format(calendar.getTime())); return
		 * sdf.parse(sdf.format(calendar.getTime())); }catch (Exception e){
		 * e.printStackTrace(); }
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());

			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(new Date());
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf1.setTimeZone(TimeZone.getTimeZone(timeZone));
			System.out.println("--vin--" + sdf1.parse(sdf.format(calendar.getTime())));

			String d = sdf.format(calendar.getTime());
			System.out.println("current time zone ==> " + new Date());
			System.out.println("EST in string " + d);
			System.out.println("EST in date object ==> " + sdf.parse(sdf.format(calendar.getTime())));
			return sdfLocal.parse(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date getScheduleDateBasedOnZone(Date scheduleDt, String timeZone) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(scheduleDt);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
			return sdf.parse(sdf.format(calendar.getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Double getDuration(Date startDate) {
		DateTime dateTimeEnd = new DateTime(new Date());
		DateTime dateTimeStart = new DateTime(startDate);
		Period p = new Period(dateTimeStart, dateTimeEnd);
		int durationMins = p.getMinutes() + ((p.getHours() + (p.getDays() * 24) + (p.getWeeks() * 7 * 24)) * 60);
		System.out.println("startDate : " + startDate + " -- EndDate : " + new Date() + " -- durationMins"
				+ durationMins + " -- in double : " + Double.parseDouble(durationMins + ""));
		return Double.parseDouble(durationMins + "");
	}

	public static String getDurationBetweenDates(Date startDate, Date endDate) {
		DateTime dateTimeEnd = new DateTime(endDate);
		DateTime dateTimeStart = new DateTime(startDate);
		Period p = new Period(dateTimeStart, dateTimeEnd);
		int durationMins = p.getMinutes() + (p.getHours() + (p.getDays() * 24) + (p.getWeeks() * 7 * 24)) * 60;
		System.out.println("startDate : " + startDate + " -- EndDate : " + new Date() + " -- durationMins"
				+ durationMins + " -- in double : " + Double.parseDouble(durationMins + ""));
		return durationMins + "";
	}

	/*
	 * public static void main(String[] args) throws Exception{ Calendar calendar1 =
	 * Calendar.getInstance(); calendar1.setTime(new Date()); SimpleDateFormat sdf1
	 * = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 * sdf1.setTimeZone(TimeZone.getTimeZone("EST"));
	 * System.out.println("Time in EST "+sdf1.format(calendar1.getTime()));
	 * System.out.println("--vin-- "+sdf1.parse(sdf1.format(calendar1.getTime())));
	 * 
	 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 * sdf.setTimeZone(TimeZone.getTimeZone("EST")); SimpleDateFormat sdfLocal = new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); try { Calendar calendar =
	 * Calendar.getInstance(); calendar.setTime(new Date()); String d =
	 * sdf.format(calendar.getTime());
	 * System.out.println("current time zone ==> "+new Date());
	 * System.out.println("EST in string "+d);
	 * System.out.println("EST in date object ==> "+sdf.parse(sdf.format(calendar.
	 * getTime()))); }catch (Exception e){ e.printStackTrace(); } }
	 */

	public static String formateDate(Date date, String formate) {
		if (Objects.isNull(date) || date == null) {
			return null;
		}
		SimpleDateFormat formater = new SimpleDateFormat(formate);
		return formater.format(date);
	}

	// return 1 if first date is greater.
	// return -1 if second date is greater.
	public static int compareDate(Date firstDate, Date secondDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Long firstDateLong = Long.valueOf(formatter.format(firstDate));
		Long secondDateLong = Long.valueOf(formatter.format(secondDate));
		if (firstDateLong > secondDateLong) {
			return 1;
		} else if (firstDateLong < secondDateLong) {
			return -1;
		} else {
			return 0;
		}
	}

	public static Date calculateDate(Integer day, Date date) {
		if (Objects.isNull(day) || day == 0) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}

	public static Date parseDateFileUpload(String date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date parseDateFormat(String date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date calculategivenDate(Integer day, Date date) {
		if (Objects.isNull(day) || day == 0) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}

	public static Date minusFiveHoursAndThirtyMinsReturnStringFormat(Date date) {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date myDateTime = null;

		try {
			myDateTime = simpleDateFormat.parse(simpleDateFormat.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(myDateTime);

		// minus 5 Hours 30 mins to your Date
		cal.add(Calendar.MINUTE, -330);
		return cal.getTime();

	}

	public static String convertDateToStringGivenFormat(Date date, String dateFormat) {
		DateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(date);
	}

	public static String convertDateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

	public static String getDateInString() {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		// System.out.println("***** Current Date IN String Format::" +
		// dateFormat.format(date));
		return dateFormat.format(date);
	}

	public static String getDateInStringYYYYMMddd() {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// System.out.println("***** Current Date IN String Format::" +
		// dateFormat.format(date));
		return dateFormat.format(date);
	}

	public static String getLastWeekDateByCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		Date todate1 = cal.getTime();
		String fromdate = dateFormat.format(todate1);
		System.out.println("FromDate::" + fromdate);
		return fromdate;
	}

	public static String convertStringDateToAnotherStringDate(String stringdate) {
		DateTimeFormatter formatter = null;
		LocalDate date = null;
		try {
			formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
			date = LocalDate.parse(stringdate, formatter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date.toString();
	}

	/**
	 * By Using This Method We Can Get the last Month Date After Given Date
	 * 
	 * @param localDate
	 * @return
	 * @throws ParseException
	 */
	public static String getLastMonthDateByGivenDate(String localDate) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date myDate = dateFormat.parse(localDate);
		Calendar now = Calendar.getInstance();
		now.setTime(myDate);
		System.out.println("Current Date By Given Date: " + dateFormat.format(myDate));
		now.add(Calendar.MONTH, -1);
		Date previousDate = now.getTime();
		System.out.println("Get Last Month By Given Date:" + dateFormat.format(previousDate));
		return dateFormat.format(previousDate);
	}

	public static Date yesterday() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}

	public static String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return dateFormat.format(cal.getTime());
	}
}
