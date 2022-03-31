package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaXMLTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class BallerinaXMLBuilder implements TypeBuilder.XMLBuilder {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol typeParam;

    public BallerinaXMLBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public BallerinaXMLBuilder withTypeParam(TypeSymbol typeParam) {
        this.typeParam = typeParam;
        return this;
    }

    @Override
    public XMLTypeSymbol build() {
        BXMLType xmlType = new BXMLType(getBType(typeParam), symTable.xmlType.tsymbol);
        return (XMLTypeSymbol) typesFactory.getTypeDescriptor(xmlType);
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol) {
                return ((AbstractTypeSymbol) typeSymbol).getBType();
            }
        }

        return symTable.noType;
    }
}
