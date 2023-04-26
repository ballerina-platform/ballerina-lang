package org.ballerinalang.testerina.natives;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

public class CommonUtils {
    public static Object sleep(BDecimal seconds)  {
        try {
            Thread.sleep(seconds.intValue() * 1000);
        } catch (InterruptedException e) {
            return BLangExceptionHelper.getRuntimeException(
                    RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR, "Invalid duration: " + e.getMessage());
        }
        return null;
    }

    public static BDecimal currentTimeInMillis() {
        long currentTime = System.currentTimeMillis();
        return BDecimal.valueOf(currentTime);
    }
}
