package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BallerinaFunctionTypeBuilder implements TypeBuilder.FUNCTION {

    private final CompilerContext context;
    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private List<ParameterSymbol> parameterSymbols = new ArrayList<>();
    private ParameterSymbol restParam;
    private TypeSymbol returnTypeSymbol;
    private Annotatable annots;

    public PARAMETER_BUILDER PARAM_BUILDER;

    public BallerinaFunctionTypeBuilder(CompilerContext context) {
        this.context = context;
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
        PARAM_BUILDER = new ParameterBuilder(context);
    }

    @Override
    public TypeBuilder.FUNCTION withParams(ParameterSymbol... params) {
        if (!parameterSymbols.isEmpty()) {
            parameterSymbols = new ArrayList<>();
        }

        parameterSymbols.addAll(Arrays.asList(params));

        return this;
    }

    @Override
    public TypeBuilder.FUNCTION withRestParam(ParameterSymbol restParam) {
        this.restParam = restParam;

        return this;
    }

    @Override
    public TypeBuilder.FUNCTION withReturnType(TypeSymbol returnType) {
        this.returnTypeSymbol = returnType;

        return this;
    }

    @Override
    public TypeBuilder.FUNCTION withReturnTypeAnnots(Annotatable annots) {
        this.annots = annots;

        return this;
    }

    @Override
    public PARAMETER_BUILDER parameterBuilder() {
        return new ParameterBuilder(context);
    }

    @Override
    public FunctionTypeSymbol build() {
        List<BType> paramTypes = getParamTypes(parameterSymbols);
        BType restType = getBType(restParam.typeDescriptor());
        BType returnType = getReturnBType(returnTypeSymbol);
        BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) symTable.invokableType.tsymbol;
        tsymbol.returnType = returnType;
        tsymbol.params = getBParamSymbols(parameterSymbols);
        tsymbol.restParam = (BVarSymbol) ((BallerinaSymbol) restParam).getInternalSymbol();
        BInvokableType bInvokableType = new BInvokableType(paramTypes, restType, returnType, tsymbol);

        return (FunctionTypeSymbol) typesFactory.getTypeDescriptor(bInvokableType);
    }

    private List<BVarSymbol> getBParamSymbols(List<ParameterSymbol> parameterSymbols) {
        List<BVarSymbol> params = new ArrayList<>();
        for (ParameterSymbol parameterSymbol : parameterSymbols) {
            params.add((BVarSymbol) ((BallerinaSymbol) parameterSymbol).getInternalSymbol());
        }

        return params;
    }

    private List<BType> getParamTypes(List<ParameterSymbol> parameterSymbols) {
        List<BType> parameterTypes = new ArrayList<>();
        for (ParameterSymbol parameterSymbol : parameterSymbols) {
            parameterTypes.add(getBType(parameterSymbol.typeDescriptor()));
        }

        return parameterTypes;
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol) {
                return ((AbstractTypeSymbol) typeSymbol).getBType();
            }
        }

        throw new IllegalArgumentException("Invalid type provided");
    }

    private BType getReturnBType(TypeSymbol returnTypeSymbol) {
        if (returnTypeSymbol != null) {
            if (returnTypeSymbol instanceof AbstractTypeSymbol
                    && (returnTypeSymbol.subtypeOf(typesFactory.getTypeDescriptor(symTable.anyType))
                    || returnTypeSymbol.subtypeOf(typesFactory.getTypeDescriptor(symTable.nilType)))) {
                return ((AbstractTypeSymbol) returnTypeSymbol).getBType();
            }
        }

        throw new IllegalArgumentException("Invalid return type provided");
    }

    public class ParameterBuilder implements PARAMETER_BUILDER {

        private SymbolFactory symbolFactory;
        private SymbolEnter symbolEnter;
        private String name;
        private TypeSymbol type;
        private ParameterKind kind;

        private ParameterBuilder(CompilerContext context) {
            symbolFactory = SymbolFactory.getInstance(context);
            symbolEnter = SymbolEnter.getInstance(context);
        }

        @Override
        public PARAMETER_BUILDER withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public PARAMETER_BUILDER withType(TypeSymbol type) {
            this.type = type;
            return this;
        }

        @Override
        public PARAMETER_BUILDER ofKind(ParameterKind kind) {
            this.kind = kind;
            return this;
        }

        @Override
        public ParameterSymbol build() {
            if (name == null) {
                throw new IllegalArgumentException("Parameter name can not be null");
            }

            long flags = Flags.REQUIRED_PARAM;
            if (kind == ParameterKind.DEFAULTABLE) {
                flags = Flags.DEFAULTABLE_PARAM;
            } else if (kind == ParameterKind.REST) {
                flags = Flags.REST_PARAM;
            } else if (kind == ParameterKind.INCLUDED_RECORD) {
                flags = Flags.INCLUDED;
            }

            BVarSymbol bVarSymbol = new BVarSymbol(flags, Names.fromString(name),
                    Names.fromString(name), symTable.rootPkgSymbol.pkgID, getBType(type),
                    symTable.rootPkgSymbol.owner, symTable.builtinPos, symTable.rootPkgSymbol.origin);

            return (ParameterSymbol) symbolFactory.getBCompiledSymbol(bVarSymbol, name);
        }

        private BType getBType(TypeSymbol typeSymbol) {
            if (typeSymbol != null) {
                if (typeSymbol instanceof AbstractTypeSymbol) {
                    return ((AbstractTypeSymbol) typeSymbol).getBType();
                }
            }

            throw new IllegalArgumentException("Invalid type provided");
        }


    }
}
