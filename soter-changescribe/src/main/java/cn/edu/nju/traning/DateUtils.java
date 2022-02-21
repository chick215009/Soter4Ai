package cn.edu.nju.traning;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel
 * @school NJU
 * @date 2020.3.17
 * @describe 日期工具类
 */
public class DateUtils {
	public static final SimpleDateFormat TIME_FORMAT =
			new SimpleDateFormat("MM-dd HH:mm:ss");

	/**
     * 格式化时间（yyyy-MM-dd HH:mm:ss）
     * @param date Date对象
     * @return 格式化后的时间
     */
    public static String formatTime(Date date) {
        return TIME_FORMAT.format(date);
    }
}
