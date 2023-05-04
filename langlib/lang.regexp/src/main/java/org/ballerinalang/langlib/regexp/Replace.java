package org.ballerinalang.langlib.regexp;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;

import java.util.regex.Matcher;

import static org.ballerinalang.langlib.regexp.RegexUtil.checkIndexWithinRange;

/**
 * Native implementation of lang.regexp:replace(string).
 *
 * @since 2201.6.0
 */
public class Replace {
    public static BString replace(BRegexpValue regExp, long startIndex, BString str, BString replacementStr) {
        checkIndexWithinRange(str, startIndex);
        int index = (int) startIndex;
        String inputStr = str.getValue();
        String prefixStr = inputStr.substring(0, index);
        String suffixStr = inputStr.substring(index);
        Matcher matcher = RegexUtil.getMatcher(regExp, suffixStr);
        String updatedStr = prefixStr + matcher.replaceFirst(replacementStr.getValue());
        return StringUtils.fromString(updatedStr);
    }
}
