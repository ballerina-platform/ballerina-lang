package org.ballerinalang.nativeimpl.lang.outputstreams;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BOutputStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Write String to an Output Stream
 */
@BallerinaFunction(
        packageName = "ballerina.lang.outputstreams",
        functionName = "writeString",
        args = {@Argument(name = "content", type = TypeEnum.STRING),
                @Argument(name = "os", type = TypeEnum.OUTPUTSTREAM),
                @Argument(name = "charset", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function writes a string to an Output Stream") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "content",
        value = "String to be written") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "os",
        value = "Output Stream") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "charset",
        value = "Charset to be used in writing CSV") })
public class WriteString extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        InputStream inputStream = null;
        BString content = (BString) getArgument(context, 0);
        BOutputStream outputStream = (BOutputStream) getArgument(context, 1);
        BString charset = (BString) getArgument(context, 2);
        try {
            inputStream = new ByteArrayInputStream(content.stringValue().getBytes(charset.stringValue()));
            IOUtils.copy(inputStream, outputStream.value());
            outputStream.value().flush();

        } catch (FileSystemException e) {
            throw new BallerinaException("Error while resolving file", e);
        } catch (IOException e) {
            throw new BallerinaException("Error while writing file", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return VOID_RETURN;
    }
}
