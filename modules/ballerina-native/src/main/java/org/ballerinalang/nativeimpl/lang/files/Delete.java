package org.ballerinalang.nativeimpl.lang.files;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;

/**
 * Delete Function
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "delete",
        args = {@Argument(name = "target", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function deletes a file from a given location") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "target",
        value = "File/Directory that should be copied") })
public class Delete extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        BStruct target = (BStruct) getArgument(context, 0);
        File targetFile = new File(target.getValue(0).stringValue());
        if (!targetFile.exists()) {
            throw new BallerinaException("File intended to delete does not exist");
        }
        if (!targetFile.delete()) {
            throw new BallerinaException("Error while deleting file");
        }
        return VOID_RETURN;
    }
}
