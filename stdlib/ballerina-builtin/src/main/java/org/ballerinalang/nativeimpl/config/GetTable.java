package org.ballerinalang.nativeimpl.config;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

/**
 * Native function ballerina.config:getTable.
 *
 * @since 0.970.0-alpha2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "config",
        functionName = "getTable",
        args = {@Argument(name = "table", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class GetTable extends BlockingNativeCallableUnit {

    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();

    @Override
    public void execute(Context context) {
        String tableHeader = context.getStringArgument(0);
        Map<String, String> table = configRegistry.getConfigTable(tableHeader);
        BMap<String, BString> bTable = new BMap<>();
        // TODO: modify BMap to create one from a Map.
        table.entrySet().forEach(entry -> bTable.put(entry.getKey(), new BString(entry.getValue())));
        context.setReturnValues(bTable);
    }
}
