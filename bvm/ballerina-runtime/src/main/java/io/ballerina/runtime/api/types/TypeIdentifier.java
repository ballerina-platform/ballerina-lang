package io.ballerina.runtime.api.types;

import io.ballerina.runtime.api.Module;

public record TypeIdentifier(Module pkg, String typeName) {

    public TypeIdentifier {
        assert typeName != null;
        assert pkg != null;
    }
}
