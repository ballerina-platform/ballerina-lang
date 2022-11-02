package org.ballerinalang.langlib.regexp;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.values.RegExpValue;

/**
 * Native implementation of lang.regexp:split(string).
 *
 * @since 2201.3.0
 */
public class Split {

    public static BArray split(BRegexpValue regExp, BString str) {
        String originalString = str.getValue();
        RegExpValue translatedRegExpVal = RegExpFactory.translateRegExpConstructs((RegExpValue) regExp);
        String regex = StringUtils.getStringValue(translatedRegExpVal, null);
        String[] splitStrArr = originalString.split(regex);
        if (splitStrArr.length == 0) {
            splitStrArr = new String[]{originalString};
        }
        return StringUtils.fromStringArray(splitStrArr);
    }
}
