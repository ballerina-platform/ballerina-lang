package org.ballerinalang.langlib.regexp;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BTupleType;
import org.wso2.ballerinalang.util.Lists;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    static BTupleType SPAN_AS_TUPLE_TYPE = new BTupleType(Lists.of(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT,
            PredefinedTypes.TYPE_STRING));

    static BArrayType GROUPS_AS_SPAN_ARRAY_TYPE = new BArrayType(SPAN_AS_TUPLE_TYPE);

    static BArrayType GROUPS_ARRAY_TYPE = new BArrayType(GROUPS_AS_SPAN_ARRAY_TYPE);
    static Matcher getMatcher(BRegexpValue regexpVal, BString inputStr) {
        String patternStr = StringUtils.getStringValue(regexpVal, null);
        Pattern pattern = Pattern.compile(patternStr);
        return pattern.matcher(inputStr.getValue());
    }
}
