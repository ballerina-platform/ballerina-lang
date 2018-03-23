package org.ballerinalang.nativeimpl.config;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.config:contains.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "config",
        functionName = "contains",
        args = {@Argument(name = "configKey", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class Contains extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String configKey = context.getStringArgument(0);
        boolean containsKey = ConfigRegistry.getInstance().contains(configKey);
        context.setReturnValues(new BBoolean(containsKey));
    }
}
