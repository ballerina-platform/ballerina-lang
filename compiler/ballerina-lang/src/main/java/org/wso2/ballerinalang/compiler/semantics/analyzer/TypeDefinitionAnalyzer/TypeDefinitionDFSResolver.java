package org.wso2.ballerinalang.compiler.semantics.analyzer.TypeDefinitionAnalyzer;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.model.tree.NodeKind.CLASS_DEFN;
import static org.ballerinalang.model.tree.NodeKind.ERROR_TYPE;
import static org.ballerinalang.model.tree.NodeKind.RECORD_TYPE;
import static org.ballerinalang.model.tree.NodeKind.TYPE_DEFINITION;
import static org.ballerinalang.model.tree.NodeKind.UNION_TYPE_NODE;

/**
 * This class will try to use a depth first search approach to resolved type definitions and their dependencies.
 * This class will help symbol resolver and will be invoked from symbol enter.
 */
public class TypeDefinitionDFSResolver {

    private static final CompilerContext.Key<TypeDefinitionDFSResolver> TYPE_DEFINITION_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final Names names;

    private SymbolEnv pkgEnv;
    private Map<TypeDefinitionNode.TypeDefinitionNodeKey, TypeDefinitionNode> typeDefinitionNodes;
    private List<BLangNode> finalTypeDefinitions;

    private Stack<TypeDefinitionNode> typeDefinitionNodeStack;
    public boolean processDone;

    public static TypeDefinitionDFSResolver getInstance(CompilerContext context) {

        TypeDefinitionDFSResolver refAnalyzer = context.get(TYPE_DEFINITION_RESOLVER_KEY);
        if (refAnalyzer == null) {
            refAnalyzer = new TypeDefinitionDFSResolver(context);
        }
        return refAnalyzer;
    }

    public void cleanup() {
        initialize();
    }

    private TypeDefinitionDFSResolver(CompilerContext context) {

        context.put(TYPE_DEFINITION_RESOLVER_KEY, this);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        initialize();
    }

    private void initialize() {
        this.typeDefinitionNodes = new HashMap<>();
        this.typeDefinitionNodeStack = new Stack<>();
    }

    public void populateTypeDefinitions(List<BLangNode> typeDefinitions, SymbolEnv pkgEnv) {

        processDone = false;
        setPackageEnv(pkgEnv);
        finalTypeDefinitions = new ArrayList<>(typeDefinitions.size());

        for (BLangNode node : typeDefinitions) {
            if (node.getKind() == TYPE_DEFINITION) {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) node;
                Name pkgAlias = pkgEnv.enclPkg.symbol.pkgID.name;
                Name typeName = names.fromIdNode(typeDefinition.name);

                TypeDefinitionNode typeDefinitionNode = new TypeDefinitionNode(typeDefinition, pkgAlias, typeName);
                if (typeDefinitionNodes.containsKey(typeDefinitionNode.getKey())) {
                    System.out.printf("######################### " +
                            "Duplicate key " + typeDefinition.name.originalValue);
                }
                typeDefinitionNodes.put(typeDefinitionNode.getKey(), typeDefinitionNode);
            } else {
                finalTypeDefinitions.add(node);
            }
        }
    }

    private TypeDefinitionNode findTypeDefinitionNode(BLangTypeDefinition typeDefinition) {
        Name typename = names.fromIdNode(typeDefinition.name);
        TypeDefinitionNode node = new TypeDefinitionNode(typeDefinition, typeDefinition.symbol.pkgID.name, typename);
        return typeDefinitionNodes.get(node.getKey());
    }

    public void startDefiningTypeDefinition(BLangTypeDefinition typeDefinition) {
        TypeDefinitionNode foundNode = findTypeDefinitionNode(typeDefinition);
        if (foundNode != null) {
            foundNode.resolvedStatus = TypeDefinitionNode.ResolvedStatus.DEFINING_STARTED;
            typeDefinitionNodeStack.push(foundNode);
        }
    }

    public void finishDefiningFields(BLangTypeDefinition typeDefinition) {
        TypeDefinitionNode foundNode = findTypeDefinitionNode(typeDefinition);
        if (foundNode != null) {
            TypeDefinitionNode stackNode = typeDefinitionNodeStack.pop();
            if (stackNode.resolvedStatus != TypeDefinitionNode.ResolvedStatus.START_FIELD_DEFINING) {
//                throw new IllegalStateException("Fields are defined out of field defining timeline");
            }
            foundNode.resolvedStatus = TypeDefinitionNode.ResolvedStatus.FINISHED_FIELD_DEFINING;
        }
    }

    public void startDefiningFields(BLangTypeDefinition typeDefinition) {
        TypeDefinitionNode foundNode = findTypeDefinitionNode(typeDefinition);
        if (foundNode != null) {
            if (foundNode.resolvedStatus != TypeDefinitionNode.ResolvedStatus.DEFINING_FINISHED) {
//                throw new IllegalStateException("Fields are defined out of field defining timeline");
            }
            foundNode.setState(TypeDefinitionNode.ResolvedStatus.START_FIELD_DEFINING);
            typeDefinitionNodeStack.push(foundNode);
        }
    }


    public void finishDefiningTypedefinition() {
        if (!typeDefinitionNodeStack.isEmpty()) {
            TypeDefinitionNode popped = typeDefinitionNodeStack.pop();
            if (popped.resolvedStatus != TypeDefinitionNode.ResolvedStatus.DEFINING_STARTED) {
//                throw new IllegalStateException("Type def maybe already defined");
            }
            popped.resolvedStatus = TypeDefinitionNode.ResolvedStatus.DEFINING_FINISHED;
        }
    }

    private TypeDefinitionNode findTypeDefinitionNode(Name pkgAlias, Name typeName) {
        Name pkg = pkgAlias == Names.EMPTY ? Names.DOT : pkgAlias;
        TypeDefinitionNode typeDefinitionNode = new TypeDefinitionNode(pkg, typeName);
        return typeDefinitionNodes.get(typeDefinitionNode.getKey());
    }

    private boolean isObjectOrRecordTypeNode(TypeDefinitionNode foundTypeDefNode) {
        BLangType node = foundTypeDefNode.typeDefinition.typeNode;
        NodeKind nodeKind = node.getKind();
        if (nodeKind != NodeKind.OBJECT_TYPE && nodeKind != NodeKind.RECORD_TYPE) {
            return false;
        }
        return true;
    }

    public boolean defineTypeIfAvailable(Name pkgAlias, Name typeName) {
        if (processDone) {
            return false;
        }
        TypeDefinitionNode foundTypeDefNode = findTypeDefinitionNode(pkgAlias, typeName);
        boolean possibleSuccessfulDefining = false;
        if (foundTypeDefNode != null) {
            if (!isObjectOrRecordTypeNode(foundTypeDefNode)) {
                return false;
            }

            typeDefinitionNodeStack.push(foundTypeDefNode);
            foundTypeDefNode.resolvedStatus = TypeDefinitionNode.ResolvedStatus.DEFINING_STARTED;
            symEnter.defineNode(foundTypeDefNode.typeDefinition, pkgEnv);
            foundTypeDefNode.resolvedStatus = TypeDefinitionNode.ResolvedStatus.DEFINING_FINISHED;
            typeDefinitionNodeStack.pop();

            possibleSuccessfulDefining = true;
        }
        return possibleSuccessfulDefining;
    }

    public boolean defineFieldsIfAvailable(Name pkgAlias, Name typeName) {
        if (processDone) { // This is later stages like DataFlowAnalyzer
            return false;
        }
        TypeDefinitionNode foundTypeDefNode = findTypeDefinitionNode(pkgAlias, typeName);
        boolean possibleSuccessfulFieldsDefining = false;
        if (foundTypeDefNode != null) {
            if (foundTypeDefNode.typeDefinition.hasCyclicReference) {
                return false;
            }
            if (!isObjectOrRecordTypeNode(foundTypeDefNode)) {
                return false;
            }
            if (foundTypeDefNode.resolvedStatus == TypeDefinitionNode.ResolvedStatus.FINISHED_FIELD_DEFINING) {
                return false;
            }
            if (foundTypeDefNode.resolvedStatus == TypeDefinitionNode.ResolvedStatus.START_FIELD_DEFINING) {
                return false;
            }
            typeDefinitionNodeStack.push(foundTypeDefNode);
            foundTypeDefNode.resolvedStatus = TypeDefinitionNode.ResolvedStatus.START_FIELD_DEFINING;
            symEnter.defineFields(foundTypeDefNode.typeDefinition, pkgEnv);
            foundTypeDefNode.resolvedStatus = TypeDefinitionNode.ResolvedStatus.FINISHED_FIELD_DEFINING;
            typeDefinitionNodeStack.pop();

            possibleSuccessfulFieldsDefining = true;
        }
        return possibleSuccessfulFieldsDefining;
    }

    public List<BLangNode> getUnresolvedTypes() {
        Iterator it = typeDefinitionNodes.values().iterator();

        while (it.hasNext()) {
            TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) it.next();
            finalTypeDefinitions.add(typeDefinitionNode.typeDefinition);
        }

        return finalTypeDefinitions;
    }

    public void setPackageEnv(SymbolEnv pkgEnv) {

        this.pkgEnv = pkgEnv;
    }
}
