package io.ballerina.compiler.api.types;

import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class BallerinaTypeBuilder extends TypeBuilder {

    private static final CompilerContext.Key<BallerinaTypeBuilder> TYPE_BUILDER_KEY = new CompilerContext.Key<>();
    public final CompilerContext context;

    public BallerinaTypeBuilder(CompilerContext context) {
        context.put(TYPE_BUILDER_KEY, this);
        this.context = context;
        this.XML = new BallerinaXMLBuilder(this.context);
        this.MAP = new BallerinaMapBuilder(this.context);
    }

    public static TypeBuilder getInstance(CompilerContext context) {
        TypeBuilder typeBuilder = context.get(TYPE_BUILDER_KEY);
        if (typeBuilder == null) {
            typeBuilder = new BallerinaTypeBuilder(context);
        }

        return typeBuilder;
    }
}
