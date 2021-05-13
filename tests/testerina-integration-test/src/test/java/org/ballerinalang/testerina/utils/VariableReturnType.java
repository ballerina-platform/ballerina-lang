package org.ballerinalang.testerina.utils;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.values.BmpStringValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ObjectValue;

import static io.ballerina.runtime.api.TypeTags.BOOLEAN_TAG;
import static io.ballerina.runtime.api.TypeTags.BYTE_TAG;
import static io.ballerina.runtime.api.TypeTags.DECIMAL_TAG;
import static io.ballerina.runtime.api.TypeTags.FLOAT_TAG;
import static io.ballerina.runtime.api.TypeTags.INT_TAG;
import static io.ballerina.runtime.api.TypeTags.STRING_TAG;

/**
 * Class used to test mocking of Dependently typed functions.
 */
public class VariableReturnType {

    public static Object getValue(BTypedesc td) {
        return getValue(td.getDescribingType());
    }

    public static Object getObjectValue(ObjectValue objectValue, BTypedesc td) {
        Type describingType = td.getDescribingType();
        if (describingType.getTag() == STRING_TAG) {
            BString newFname = objectValue.getStringValue(new BmpStringValue("fname"))
                    .concat(new BmpStringValue(" ")).concat(objectValue.getStringValue(new BmpStringValue("lname")));
            objectValue.set(new BmpStringValue("fname"), newFname);
            return newFname;
        }
        return getValue(td.getDescribingType());
    }


    private static Object getValue(Type type) {
        switch (type.getTag()) {
            case INT_TAG:
                return 150L;
            case FLOAT_TAG:
                return 12.34D;
            case DECIMAL_TAG:
                return new DecimalValue("23.45");
            case BOOLEAN_TAG:
                return true;
            case STRING_TAG:
                return new BmpStringValue("Hello World!");
            case BYTE_TAG:
                return 32;
        }
        return null;
    }


}
