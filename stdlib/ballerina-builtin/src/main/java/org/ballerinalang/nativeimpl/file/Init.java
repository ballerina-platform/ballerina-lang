package org.ballerinalang.nativeimpl.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.file.utils.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates whether the given input is a directory.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "Path.init",
        args = {
                @Argument(name = "path", type = TypeKind.STRUCT, structType = "Path", structPackage = "ballerina.file"),
                @Argument(name = "link", type = TypeKind.STRING)
        },
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {
    /**
     * Retrieves the path from the given location.
     *
     * @param path the values of the path.
     * @return reference to the path location.
     */
    private Path getPath(String path) {
        return Paths.get(path);
    }

    @Override
    public void execute(Context context) {
        String basePath = context.getStringArgument(0);
        BStruct path = (BStruct) context.getRefArgument(0);
        path.addNativeData(Constants.PATH_DEFINITION_NAME, getPath(basePath));
    }
}
