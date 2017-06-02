package org.ballerinalang.nativeimpl.lang.files;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Write Blob to a file
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "write",
        args = {@Argument(name = "blob", type = TypeEnum.BLOB),
                @Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function writes a file using the given input stream") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "content",
        value = "Blob content to be written") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "file",
        value = "The file which the blob should be written to") })
public class Write extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        OutputStream outputStream = null;
        BBlob content = (BBlob) getArgument(context, 0);
        BStruct destination = (BStruct) getArgument(context, 1);
        try {
            File destinationFile = new File(destination.getValue(0).stringValue());
            File parent = destinationFile.getParentFile();
            if (parent != null) {
                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new BallerinaException("Error in writing file");
                    }
                }
            }
            if (!destinationFile.exists()) {
                if (!destinationFile.createNewFile()) {
                    throw new BallerinaException("Error in writing file");
                }
            }
            outputStream = new FileOutputStream(destinationFile, true);
            outputStream.write(content.blobValue());
            outputStream.flush();

        } catch (IOException e) {
            throw new BallerinaException("Error while writing file", e);
        } finally {
            closeQuietly(outputStream);
        }
        return VOID_RETURN;
    }

    private void closeQuietly(Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (Exception e) {
            throw new BallerinaException("Exception during Resource.close()", e);
        }
    }
}
