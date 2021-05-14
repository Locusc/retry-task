package cn.locusc.retry.task.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.*;

/**
 * 时间处理工具类
 */
@Slf4j
public class DateUtil {
    /*** 1毫秒 */
    public static final long ONE_MILLI_SECOND = 1L;
    /*** 1秒 */
    public static final long ONE_SECOND = ONE_MILLI_SECOND * 1000;
    /*** 1分钟*/
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    /*** 1小时 */
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    /*** 1天 */
    public static final long ONE_DAY = ONE_HOUR * 24;
    /*** 1月 */
    public static final long ONE_MONTH = ONE_DAY * 30;
    /*** 1年 */
    public static final long ONE_YEAR = ONE_MONTH * 12;
    /*** 1世纪 */
    public static final long ONE_CENTURY = ONE_YEAR * 100;

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS EEE Z";

    public static final String ymdhmsS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";

    public static final String ymdhms_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String ymdhm_DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public static final String ymd_E_DATE_FORMAT = "yyyy-MM-dd EEE";

    public static final String ymd_DATE_FORMAT = "yyyy-MM-dd";

    public static final String ym_DATE_FORMAT = "yyyy-MM";

    public static final String hms_DATE_FORMAT = "HH:mm:ss";

    public static final String hm_DATE_FORMAT = "HH:mm";

    public static final String dd_DATE_FORMAT = "dd";

    public static final String mdy_E_DATE_FORMAT_US = "MM/dd/yyyy EEE";

    public static final String mdy_DATE_FORMAT_US = "MM/dd/yyyy";

    public static final String my_DATE_FORMAT_US = "MM/yyyy";

    public static final String ymdhms_TIME_STAMP_FORMAT = "yyyyMMddHHmmss";

    public static final String ymdhm_TIME_STAMP_FORMAT = "yyyyMMddHHmm";

    public static final String ymd_TIME_STAMP_FORMAT = "yyyyMMdd";

    public static final String ym_TIME_STAMP_FORMAT = "yyyyMM";

    public static final String hms_TIME_STAMP_FORMAT = "HHmmss";

    public static final String chinese_ymdhms_E_DATE_FORMAT = "yyyy年MM月dd日 HH:mm:ss EEE";

    public static final String chinese_ymdhms_DATE_FORMAT = "yyyy年MM月dd日 HH:mm:ss";

    public static final String chinese_ymd_DATE_FORMAT = "yyyy年MM月dd日";

    public static final String ymd_regex_FORMAT = "^\\d{4}\\D+\\d{1,2}\\D+\\d{1,2}$"; //yyyy-MM-dd，yyyy/MM/dd，yyyy年MM月dd日

    public static final String symbol_ymd_DATE_FORMAT = "yyyy.MM.dd";

    public static final String DEFAULT_TERM_TO_DATE = "9999.12.30";

    /**
     * 取系统当前日期的开始时间（yyyy-MM-dd 00:00:00:000）
     */
    public static Date getCurrentDateBegin() {
        Date date = new Date();
        return zeroConvertTime(date);
    }

    /**
     * 将日期后的时间填满 变成(yyyy-MM-dd 23:59:59:999)
     */
    public static Date getCurrentDateEnd() {
        Date date = new Date();
        return totalConvertTime(date);
    }

    /**
     * 把日期后的时间归0 变成(yyyy-MM-dd 00:00:00:000)
     */
    public static Date zeroConvertTime(Date fullDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fullDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 将日期后的时间填满 变成(yyyy-MM-dd 23:59:59:999)
     */
    public static Date totalConvertTime(Date fullDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fullDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取当前日期对用的月份的第一天
     */
    public static Date getFirstDayOfOneMonth(Date fullDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fullDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 获取当前日期对应的月份的最后一天
     */
    public static Date getLastDayOfOneMonth(Date fullDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fullDate);
        cal.add(Calendar.MONTH, 1); //当前月加1 就是下个月
        cal.set(Calendar.DATE, 1); //下个月的第一天
        cal.add(Calendar.DATE, -1); //下个月的前一天就是上个月的最后一天
        return cal.getTime();
    }

    /**
     * 获取当前时间 如 2010-08-24 18:45:45
     */
    public static String getCurrentDateStr() {
        return ymdhmsFormat(new Date());
    }

    /**
     * 将指定时间转换为ymdhms格式 如：20100824185707
     */
    public static String ymdhmsTimeStampFormat(Date date) {

        return DateFormatUtils.format(date, ymdhms_TIME_STAMP_FORMAT);
    }

    /**
     * 将指定时间转换为ymd格式 如：20100824185707
     */
    public static String ymdTimeStampFormat(Date date) {

        return DateFormatUtils.format(date, ymd_TIME_STAMP_FORMAT);
    }

    /**
     * 将指定时间转换为mm/yyyy格式 如：08/2017
     */
    public static String myFormat(Date date) {

        return DateFormatUtils.format(date, my_DATE_FORMAT_US);
    }

    /**
     * 将指定时间转换为ym格式 如：20100824185707
     */
    public static String ymTimeStampFormat(Date date) {

        return DateFormatUtils.format(date, ym_TIME_STAMP_FORMAT);
    }

    /**
     * 将指定时间转换为ymdhms格式 如：2010-08-24 18:57:07
     */
    public static String ymdhmsFormat(Date date) {

        return DateFormatUtils.format(date, ymdhms_DATE_FORMAT);
    }

    /**
     * 将指定时间转换为ym格式 如：2010-08-24 18:57:07
     */
    public static String ymFormat(Date date) {
        return DateFormatUtils.format(date, ym_DATE_FORMAT);
    }

    /**
     * 将指定时间转换为ymdhm格式 如：2010-08-24 18:57
     */
    public static String ymdhmFormat(Date date) {
        return DateFormatUtils.format(date, ymdhm_DATE_FORMAT);
    }

    /**
     * 获取中文显示的指定时间
     */
    public static String chineseFormat(Date date) {
        return DateFormatUtils.format(date, chinese_ymdhms_DATE_FORMAT);
    }

    /**
     * 获取中文显示的当前时间
     */
    public static String getCurrentChineseDate() {
        return chineseFormat(new Date());
    }

    /**
     * 将日期类型为 2010-08-25 08:53:56 格式的字符数据转换为日期类型
     */
    public static Date praseDate(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 将日期类型为 2010-08-25 08:53 格式的字符数据转换为日期类型
     */
    public static Date praseYMDHMDate(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyy-MM-dd HH:mm");
    }

    /**
     * 将日期类型为 2010-08-25 08:53:56 格式的字符数据转换为日期类型
     */
    public static Date praseDateYYYY_MM_DD(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyy-MM-dd");
    }

    /**
     * 将日期类型为 2010-08-25 08:53:56 格式的字符数据转换为日期类型
     */
    public static Date praseDateYYYYMMDD(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyyMMdd");
    }

    /**
     * 将日期类型为 2010-08-25 08:53:56 格式的字符数据转换为日期类型
     */
    public static Date praseDateYYYY_MM(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyy-MM");
    }

    /**
     * 将日期类型为 2010-08-25 08:53:56 格式的字符数据转换为日期类型
     */
    public static Date praseDateYYYYMM(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyyMM");
    }

    /**
     * 将中文类型的字符数据转换为日期类型 如 2010年8月25日 08:56:05
     */
    public static Date praseChineseDate(String dateStr) throws ParseException {
        return DateUtils.parseDate(dateStr, "yyyy年MM月dd日 HH:mm:ss");
    }

}
