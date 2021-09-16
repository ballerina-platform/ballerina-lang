package org.wso2.ballerinalang.compiler.semantics.analyzer.TypeDefinitionAnalyzer;

import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

public class TypeDefinitionNode {

    int precedence;
    ResolvedStatus resolvedStatus;
    BLangTypeDefinition typeDefinition;

    private final TypeDefinitionNodeKey key;
    List<TypeDefinitionNode> dependencyList;

    public TypeDefinitionNode(Name pkgAlias, Name typeName) {
        this.precedence = 0;
        this.resolvedStatus = ResolvedStatus.NOT_STARTED;
        this.dependencyList = new ArrayList<>();
        key = new TypeDefinitionNodeKey(pkgAlias, typeName);
    }

    public TypeDefinitionNode(BLangTypeDefinition typeDefinition) {
        this(typeDefinition.symbol.pkgID.name, typeDefinition.symbol.name);
        this.typeDefinition = typeDefinition;
    }

    public TypeDefinitionNode(BLangTypeDefinition typeDefinition, Name pkgAlias, Name typeName) {
        this(pkgAlias, typeName);
        this.typeDefinition = typeDefinition;
    }

    public enum ResolvedStatus {
        UNKNOWN,
        NOT_STARTED,
        DEFINING_STARTED,
        DEFINING_FINISHED,
        START_FIELD_DEFINING,
        FINISHED_FIELD_DEFINING,
        FAILED
    }

    public void nextState() {
        switch (resolvedStatus) {
            case UNKNOWN:
                resolvedStatus = ResolvedStatus.NOT_STARTED;
                break;
            case NOT_STARTED:
                resolvedStatus = ResolvedStatus.DEFINING_STARTED;
                break;
            case DEFINING_STARTED:
                resolvedStatus = ResolvedStatus.DEFINING_FINISHED;
                break;
            case DEFINING_FINISHED:
                resolvedStatus = ResolvedStatus.START_FIELD_DEFINING;
                break;
            case START_FIELD_DEFINING:
                resolvedStatus = ResolvedStatus.FINISHED_FIELD_DEFINING;
                break;
            default:
        }
    }

    public boolean setState(ResolvedStatus state) {
        if (state.ordinal() > resolvedStatus.ordinal()) {
            resolvedStatus = state;
            return true;
        }
        return false;
    }

    public TypeDefinitionNodeKey getKey() {

        return key;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeDefinitionNode that = (TypeDefinitionNode) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {

        return key.hashCode();
    }

    @Override
    public String toString() {

        return "TypeDefinitionNode{" +
                "precedence=" + precedence +
                ", resolvedStatus=" + resolvedStatus +
                ", typeDefinition=" + typeDefinition +
                ", key='" + key + '\'' +
                ", dependencyList=" + dependencyList +
                '}';
    }

    public class TypeDefinitionNodeKey {
        Name pkgAlias;
        Name typeName;

        public TypeDefinitionNodeKey(Name pkgAlias, Name typeName) {

            this.pkgAlias = pkgAlias;
            this.typeName = typeName;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypeDefinitionNodeKey that = (TypeDefinitionNodeKey) o;

            if (!pkgAlias.equals(that.pkgAlias)) return false;
            return typeName.equals(that.typeName);
        }

        @Override
        public int hashCode() {

            int result = pkgAlias.hashCode();
            result = 31 * result + typeName.hashCode();
            return result;
        }

        @Override
        public String toString() {

            return "TypeDefinitionNodeKey{" +
                    "pkgAlias=" + pkgAlias +
                    ", typeName=" + typeName +
                    '}';
        }
    }
}
