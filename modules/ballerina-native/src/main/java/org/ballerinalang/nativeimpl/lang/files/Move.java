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
 * Move Function
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "move",
        args = {@Argument(name = "source", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"),
                @Argument(name = "destination", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function moves a file from a given location to another") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File/Directory that should be moved") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the File/Directory should moved to") })
public class Move extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        BStruct source = (BStruct) getArgument(context, 0);
        BStruct destination = (BStruct) getArgument(context, 1);

        File sourceFile = new File(source.getValue(0).stringValue());
        File destinationFile = new File(destination.getValue(0).stringValue());
        if (!sourceFile.renameTo(destinationFile)) {
            throw new BallerinaException("Error while moving file");
        }
        return VOID_RETURN;
    }
}
