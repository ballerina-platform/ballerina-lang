package org.ballerinalang.nativeimpl.config;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.config.cipher.AESCipherToolException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.config:getEncrypted.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        packageName = "ballerina.config",
        functionName = "getEncrypted",
        args = {@Argument(name = "configKey", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetEncrypted extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(GetEncrypted.class);

    @Override
    public void execute(Context context) {
        String configKey = null;
        try {
            configKey = context.getStringArgument(0);
            String globalValue = ConfigRegistry.getInstance().getEncryptedValue(configKey);
            context.setReturnValues(new BString(globalValue));
        } catch (AESCipherToolException e) {
            log.error("Failed to get decrypted value for '" + configKey + "'", e);
            throw new BLangRuntimeException(
                    "failed to retrieve decrypted value for '" + configKey + "': " + e.getMessage());
        }
    }
}
