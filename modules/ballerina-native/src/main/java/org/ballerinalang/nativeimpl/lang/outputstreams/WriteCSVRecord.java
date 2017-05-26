package org.ballerinalang.nativeimpl.lang.outputstreams;

import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BOutputStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Write an array to a CSV file
 */
@BallerinaFunction(
        packageName = "ballerina.lang.outputstreams",
        functionName = "writeCSVRecord",
        args = {@Argument(name = "arr", type = TypeEnum.ARRAY, elementType = TypeEnum.STRING),
                @Argument(name = "outputStream", type = TypeEnum.OUTPUTSTREAM),
                @Argument(name = "charset", type = TypeEnum.STRING),
                @Argument(name = "applyQuotes", type = TypeEnum.BOOLEAN)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function writes an array to a given location as a CSV file") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "array",
        value = "String") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "file",
        value = "Path of the file") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "charset",
        value = "Charset to be used in writing CSV") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "applyQuotes",
        value = "Apply quotes to all data") })
public class WriteCSVRecord extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        CSVWriter csvWriter = null;
        BArray<BString> arr = (BArray) getArgument(context, 0);
        BOutputStream outputStream = (BOutputStream) getArgument(context, 1);
        BString charset = (BString) getArgument(context, 2);
        BBoolean applyQuotes = (BBoolean) getArgument(context, 3);
        try {
            csvWriter = new CSVWriter(new OutputStreamWriter(outputStream.value(), charset.stringValue()));
            csvWriter.writeNext(toStringArray(arr), applyQuotes.booleanValue());

        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Unsupported Encoding", e);
        } finally {
            IOUtils.closeQuietly(csvWriter);
        }
        return VOID_RETURN;
    }

    private String[] toStringArray(BArray<BString> arr) {
        String[] stringArray = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            stringArray[i] = arr.get(i).stringValue();
        }
        return stringArray;
    }
}
