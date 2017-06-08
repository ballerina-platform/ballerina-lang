package org.ballerinalang.nativeimpl.lang.files;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
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
 * Copy File
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "copy",
        args = {@Argument(name = "source", type = TypeEnum.FILE),
                @Argument(name = "destination", type = TypeEnum.FILE)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function copies a file from a given location to another") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File/Directory that should be copied") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the File/Directory should be pasted") })
public class Copy extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        BFile target = (BFile) getArgument(context, 0);
        BFile destination = (BFile) getArgument(context, 1);

        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject sourceObj = fsManager.resolveFile(target.stringValue());
            if (sourceObj.exists()) {
                FileObject destinationObj = fsManager.resolveFile(destination.stringValue());
                destinationObj.copyFrom(sourceObj, Selectors.SELECT_ALL);
            }

        } catch (FileSystemException e) {
            throw new BallerinaException("Error while resolving file", e);
        }
        return VOID_RETURN;
    }
}
