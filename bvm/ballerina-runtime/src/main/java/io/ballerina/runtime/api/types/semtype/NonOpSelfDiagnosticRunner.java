package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.MutableSemType;

public class NonOpSelfDiagnosticRunner implements TypeCheckSelfDiagnosticsRunner {

    @Override
    public void registerTypeResolutionStart(Context cx, MutableSemType type) {

    }

    @Override
    public void registerTypeCheckStart(Context cx, SemType t1, SemType t2) {

    }

    @Override
    public void registerTypeCheckEnd(Context cx) {

    }

    @Override
    public void registerAbruptTypeResolutionEnd(Context cx, Exception ex) {

    }

    @Override
    public void registerAbruptTypeCheckEnd(Context cx, Exception ex) {

    }
}
