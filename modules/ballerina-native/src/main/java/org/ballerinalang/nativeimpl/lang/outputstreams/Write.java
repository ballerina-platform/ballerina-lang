package org.ballerinalang.nativeimpl.lang.outputstreams;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInputStream;
import org.ballerinalang.model.values.BOutputStream;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;

/**
 * Write Output Stream
 */
@BallerinaFunction(
        packageName = "ballerina.lang.outputstreams",
        functionName = "write",
        args = {@Argument(name = "inputStream", type = TypeEnum.INPUTSTREAM),
                @Argument(name = "outputStream", type = TypeEnum.OUTPUTSTREAM)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function writes a file using the given input stream") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "inputStream",
        value = "Input Stream") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "outputStream",
        value = "Output Stream") })
public class Write extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        BInputStream inputStream = (BInputStream) getArgument(context, 0);
        BOutputStream outputStream = (BOutputStream) getArgument(context, 1);
        try {
            IOUtils.copy(inputStream.value(), outputStream.value());
            outputStream.value().flush();
        } catch (FileSystemException e) {
            throw new BallerinaException("Error while resolving file", e);
        } catch (IOException e) {
            throw new BallerinaException("Error while writing file", e);
        }
        return VOID_RETURN;
    }
}
