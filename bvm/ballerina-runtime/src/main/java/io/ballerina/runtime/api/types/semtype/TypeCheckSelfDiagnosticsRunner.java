package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.MutableSemType;

interface TypeCheckSelfDiagnosticsRunner {

    void registerTypeResolutionStart(Context cx, MutableSemType type);

    void registerTypeCheckStart(Context cx, SemType t1, SemType t2);

    void registerTypeCheckEnd(Context cx);

    void registerAbruptTypeResolutionEnd(Context cx, Exception ex);

    void registerAbruptTypeCheckEnd(Context cx, Exception ex);

    void registerTypeResolutionExit(Context cx);
}
