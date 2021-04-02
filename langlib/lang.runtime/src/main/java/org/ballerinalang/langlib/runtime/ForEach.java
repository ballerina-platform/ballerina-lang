package org.ballerinalang.langlib.runtime;

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.RUNTIME_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.util.BLangCompilerConstants.RUNTIME_VERSION;

public class ForEach {
    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, RUNTIME_LANG_LIB,
            RUNTIME_VERSION, "forEach");

    public static void forEach(BArray arr, BFunctionPointer<Object, Object> func) {
        int size = arr.size();
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                () -> new Object[]{parentStrand,
                        arr.get(index.incrementAndGet()), true},
                result -> {
                }, () -> null, Scheduler.getStrand().scheduler);
    }
}
