package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class BallerinaXMLBuilder implements TypeBuilder.XMLBuilder {

    CompilerContext context;
    TypeSymbol typeParam;

    public BallerinaXMLBuilder(CompilerContext context) {
        this.context = context;
    }

    @Override
    public BallerinaXMLBuilder withTypeParam(TypeSymbol typeParam) {
        this.typeParam = typeParam;
        return this;
    }

    @Override
    public XMLTypeSymbol build() {
        return null;
    }
}
