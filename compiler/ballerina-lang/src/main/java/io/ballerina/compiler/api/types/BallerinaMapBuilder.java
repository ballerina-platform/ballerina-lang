package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class BallerinaMapBuilder implements TypeBuilder.MapTypeBuilder {

    CompilerContext context;

    public BallerinaMapBuilder(CompilerContext context) {
        this.context = context;
    }

    @Override
    public TypeSymbol withTypeParam(TypeSymbol typeParam) {

        return null;
    }

    @Override
    public MapTypeSymbol build() {

        return null;
    }
}
