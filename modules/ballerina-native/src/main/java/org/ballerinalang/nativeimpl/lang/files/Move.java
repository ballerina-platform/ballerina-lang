package org.ballerinalang.nativeimpl.lang.files;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BFile;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Copy Function
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "move",
        args = {@Argument(name = "source", type = TypeEnum.FILE),
                @Argument(name = "destination", type = TypeEnum.FILE)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function moves a file from a given location to another") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File that should be moved") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the File should be moved to") })
public class Move extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {
        BFile source = (BFile) getArgument(context, 0);
        BFile destination = (BFile) getArgument(context, 1);

        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject targetObj = fsManager.resolveFile(source.stringValue());
            if (targetObj.exists()) {
                FileObject destinationObj = fsManager.resolveFile(destination.stringValue());
                targetObj.moveTo(destinationObj);
            }

        } catch (FileSystemException e) {
            throw new BallerinaException("Error while resolving file", e);
        }
        return VOID_RETURN;
    }
}
