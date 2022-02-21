/**
 *
 */
package cn.edu.nju.traning;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author Daniel
 * @school NJU
 * @date 2020.3.10
 * @describe 处理字符串
 */
public class StringUtils {

	/**
	 * 将字符串原样输出
	 * @param source
	 * @return
	 */
	public static String stringEscape(String source) {
        String unresolve = StringEscapeUtils.escapeJava(source);

        // 若没有转义字符，则直接输出原字符串
        if(source.equals(unresolve))
            return source;

        return unresolve;
    }
}
