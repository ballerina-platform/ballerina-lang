package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.types.Env;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.types.*;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.*;

/**
 * @since 2201.3.0
 */

public class TypeResolver {

    private static final CompilerContext.Key<TypeResolver> TYPE_RESOLVER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final SourceDirectory sourceDirectory;
    private final ConstantValueResolver constResolver;
    private List<BLangNode> unresolvedTypes;
    private Set<BLangNode> unresolvedRecordDueToFields;
    private boolean resolveRecordsUnresolvedDueToFields;
    private List<BLangClassDefinition> unresolvedClasses;
    private HashSet<SymbolEnter.LocationData> unknownTypeRefs;
    private List<PackageID> importedPackages;
    private int typePrecedence;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;
    private PackageCache packageCache;
    private List<BLangNode> intersectionTypes;
    private Map<BType, BLangTypeDefinition> typeToTypeDef;

    private SymbolEnv env;

    public TypeResolver(CompilerContext context) {
        context.put(TYPE_RESOLVER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.sourceDirectory = context.get(SourceDirectory.class);
        this.importedPackages = new ArrayList<>();
        this.unknownTypeRefs = new HashSet<>();
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.constResolver = ConstantValueResolver.getInstance(context);
        this.intersectionTypes = new ArrayList<>();
    }

    public static TypeResolver getInstance(CompilerContext context) {
        TypeResolver typeResolver = context.get(TYPE_RESOLVER_KEY);
        if (typeResolver == null) {
            typeResolver = new TypeResolver(context);
        }

        return typeResolver;
    }

    private void defineBTypes(List<BLangNode> moduleDefs, SymbolEnv pkgEnv) {
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(SymbolEnter.getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);

        constResolver.resolve(pkgEnv.enclPkg.constants, pkgEnv.enclPkg.packageID, pkgEnv);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.CLASS_DEFN) {
                throw new IllegalStateException("Semtype are not supported for class definitions yet");
            } else if (def.getKind() == NodeKind.CONSTANT) {
                resolveConstant(pkgEnv.enclPkg.semtypeEnv, modTable, (BLangConstant) def);
            } else {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                resolveTypeDefn(pkgEnv.enclPkg.semtypeEnv, modTable, typeDefinition, 0);
            }
        }
    }

    private void resolveConstant(Env semtypeEnv, Map<String, BLangNode> modTable, BLangConstant constant) {
        SemType semtype;
        if (constant.associatedTypeDefinition != null) {
            semtype = resolveTypeDefn(semtypeEnv, modTable, constant.associatedTypeDefinition, 0);
        } else {
            semtype = evaluateConst(constant);
        }
        addSemtypeBType(constant.getTypeNode(), semtype);
        semtypeEnv.addTypeDef(constant.name.value, semtype);
    }

    private SemType evaluateConst(BLangConstant constant) {
        switch (constant.symbol.value.type.getKind()) {
            case INT:
                return SemTypes.intConst((long) constant.symbol.value.value);
            case BOOLEAN:
                return SemTypes.booleanConst((boolean) constant.symbol.value.value);
            case STRING:
                return  SemTypes.stringConst((String) constant.symbol.value.value);
            case FLOAT:
                return SemTypes.floatConst((double) constant.symbol.value.value);
            default:
                throw new AssertionError("Expression type not implemented for const semtype");
        }
    }

    private SemType resolveTypeDefn(Env semtypeEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth) {
        if (defn.semType != null) {
            return defn.semType;
        }

        if (depth == defn.cycleDepth) {
            dlog.error(defn.pos, DiagnosticErrorCode.INVALID_TYPE_CYCLE, defn.name);
            return null;
        }
        defn.cycleDepth = depth;
        SemType s = resolveTypeDesc(semtypeEnv, mod, defn, depth, defn.typeNode);
        addSemtypeBType(defn.getTypeNode(), s);
        if (defn.semType == null) {
            defn.semType = s;
            defn.cycleDepth = -1;
            semtypeEnv.addTypeDef(defn.name.value, s);
            return s;
        } else {
            return s;
        }
    }

    private SemType resolveTypeDesc(Env semtypeEnv, Map<String, BLangNode> mod, BLangTypeDefinition defn, int depth,
                                    BLangType td) {
        if (td == null) {
            return null;
        }
        switch (td.getKind()) {
            case VALUE_TYPE:
                return resolveTypeDesc((BLangValueType) td, semtypeEnv);
            case CONSTRAINED_TYPE: // map<?> and typedesc<?>
                return resolveTypeDesc((BLangConstrainedType) td, semtypeEnv, mod, depth, defn);
            case ARRAY_TYPE:
                return resolveTypeDesc(((BLangArrayType) td), semtypeEnv, mod, depth, defn);
            case TUPLE_TYPE_NODE:
                return resolveTypeDesc((BLangTupleTypeNode) td, semtypeEnv, mod, depth, defn);
            case RECORD_TYPE:
                return resolveTypeDesc((BLangRecordTypeNode) td, semtypeEnv, mod, depth, defn);
            case FUNCTION_TYPE:
                return resolveTypeDesc((BLangFunctionTypeNode) td, semtypeEnv, mod, depth, defn);
            case ERROR_TYPE:
                return resolveTypeDesc((BLangErrorType) td, semtypeEnv, mod, depth, defn);
            case UNION_TYPE_NODE:
                return resolveTypeDesc((BLangUnionTypeNode) td, semtypeEnv, mod, depth, defn);
            case INTERSECTION_TYPE_NODE:
                return resolveTypeDesc((BLangIntersectionTypeNode) td, semtypeEnv, mod, depth, defn);
            case USER_DEFINED_TYPE:
                return resolveTypeDesc((BLangUserDefinedType) td, semtypeEnv, mod, depth);
            case BUILT_IN_REF_TYPE:
                return resolveTypeDesc((BLangBuiltInRefTypeNode) td, semtypeEnv);
            case FINITE_TYPE_NODE:
                return resolveSingletonType((BLangFiniteTypeNode) td, semtypeEnv);
            case TABLE_TYPE:
                return resolveTypeDesc((BLangTableTypeNode) td, semtypeEnv, mod, depth);
            default:
                throw new AssertionError("not implemented");
        }

    }

    private SemType resolveSingletonType(BLangFiniteTypeNode td, Env semtypeEnv) {
        if (td.valueSpace.size() > 1) {
            return resolveFiniteTypeUnion(td, semtypeEnv);
        }
        return resolveSingletonType(td, 0);
    }

}
