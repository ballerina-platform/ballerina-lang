/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload.visitors;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.classload.ImportProcessor;
import io.ballerina.shell.utils.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Converts a type into its string format.
 * Any imports that need to be done are also calculated.
 * Eg: if the type was abc/z:TypeA then it will be converted as
 * 'z:TypeA' and 'import abc/z' will be added as an import.
 * We need to traverse all the sub-typed because any sub-type
 * may need to be imported.
 * Eg: object {abc/c:P p;}. These situations are rare since most of the time,
 * the object or record is exported outright instead of specifying sub-types.
 *
 * @since 2.0.0
 */
public class TypeSignatureTransformer extends TypeSymbolTransformer<String> {
    private static final String ANON_MODULE = "$anon";

    private final ImportProcessor importProcessor;
    private final Set<String> implicitImportPrefixes;

    public TypeSignatureTransformer(ImportProcessor importProcessor) {
        this.implicitImportPrefixes = new HashSet<>();
        this.importProcessor = importProcessor;
    }

    @Override
    protected void resetState() {
        this.setState("");
    }

    @Override
    protected void visitParameter(ParameterSymbol parameterSymbol) {
        StringJoiner joiner = new StringJoiner(" ");
        parameterSymbol.qualifiers().forEach(accessModifier -> joiner.add(accessModifier.getValue()));
        String signature;
        if (parameterSymbol.kind() == ParameterKind.REST) {
            signature = transformType(parameterSymbol.typeDescriptor());
            signature = signature.substring(0, signature.length() - 2) + "...";
        } else {
            signature = transformType(parameterSymbol.typeDescriptor());
        }

        joiner.add(signature);
        if (parameterSymbol.name().isPresent()) {
            joiner.add(parameterSymbol.name().get());
        }

        this.setState(joiner.toString());
    }

    @Override
    protected void visitField(ObjectFieldSymbol fieldSymbol) {
        StringJoiner joiner = new StringJoiner(" ");
        fieldSymbol.qualifiers().forEach(qualifier -> joiner.add(qualifier.getValue()));
        String signature = joiner.add(transformType(fieldSymbol.typeDescriptor()))
                .add(fieldSymbol.name()).toString();
        this.setState(signature);
    }

    @Override
    protected void visitField(RecordFieldSymbol fieldSymbol) {
        StringJoiner joiner = new StringJoiner(" ");
        fieldSymbol.qualifiers().forEach(qualifier -> joiner.add(qualifier.getValue()));

        String signature = joiner.add(transformType(fieldSymbol.typeDescriptor()))
                .add(fieldSymbol.name()).toString();
        if (fieldSymbol.isOptional()) {
            signature += "?";
        }
        this.setState(signature);
    }

    @Override
    protected void visitMethod(MethodSymbol methodSymbol) {
        StringJoiner qualifierJoiner = new StringJoiner(" ");
        methodSymbol.qualifiers().stream().map(Qualifier::getValue).forEach(qualifierJoiner::add);
        qualifierJoiner.add("function ");

        StringBuilder signature = new StringBuilder(qualifierJoiner.toString());
        StringJoiner joiner = new StringJoiner(", ");
        signature.append(methodSymbol.name()).append("(");

        methodSymbol.typeDescriptor().parameters().stream().map(this::transformParameter).forEach(joiner::add);
        methodSymbol.typeDescriptor().restParam().map(this::transformParameter).ifPresent(joiner::add);
        signature.append(joiner.toString()).append(")");
        methodSymbol.typeDescriptor().returnTypeDescriptor().map(this::transformType)
                .ifPresent(retType -> signature.append(" returns ").append(retType));

        this.setState(signature.toString());
    }

    @Override
    protected void visit(ArrayTypeSymbol symbol) {
        // TODO: size is always empty. (Returns always Optional.empty)
        // BArrayType string is always in format TYPE[.....]
        String lengthRepr = symbol.size().map(String::valueOf).orElse("");
        String stringRepr = transformType(symbol.memberTypeDescriptor()) + "[" + lengthRepr + "]";
        this.setState(stringRepr);
    }

    @Override
    protected void visit(FunctionTypeSymbol symbol) {
        // Functions are of function (PARAMETERS) RETURN format
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(", ");
        symbol.parameters().stream().map(this::transformParameter).forEach(joiner::add);
        symbol.restParam().ifPresent(param -> joiner.add(transformParameter(param)));
        signature.append(joiner.toString()).append(")");
        symbol.returnTypeDescriptor().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(transformType(typeDescriptor)));
        this.setState(signature.toString());
    }

    @Override
    protected void visit(FutureTypeSymbol symbol) {
        // Future type of format future<TYPE>. TYPE is at least ().
        String memberSignature = symbol.typeParameter().map(this::transformType).orElse("()");
        String stringRepr = "future<" + memberSignature + ">";
        this.setState(stringRepr);
    }

    @Override
    protected void visit(IntersectionTypeSymbol symbol) {
        // Intersection types. TYPE1 & TYPE2
        List<TypeSymbol> memberTypes = symbol.memberTypeDescriptors();
        StringJoiner joiner = new StringJoiner(" & ");
        memberTypes.forEach(typeDescriptor -> joiner.add(transformType(typeDescriptor)));
        this.setState(joiner.toString());
    }

    @Override
    protected void visit(MapTypeSymbol symbol) {
        // String representation is map<CONSTRAINT>.
        String typeRepr = symbol.typeParameter().map(this::transformType).orElse("");
        String stringRepr = "map<" + typeRepr + ">";
        this.setState(stringRepr);
    }

    @Override
    protected void visit(ObjectTypeSymbol symbol) {
        // Object.
        StringBuilder signature = new StringBuilder();
        StringJoiner qualifierJoiner = new StringJoiner(" ");
        StringJoiner fieldJoiner = new StringJoiner(";");
        StringJoiner methodJoiner = new StringJoiner(" ");

        symbol.qualifiers().stream().map(Qualifier::getValue).forEach(qualifierJoiner::add);
        qualifierJoiner.add("object {");
        signature.append(qualifierJoiner.toString());

        symbol.fieldDescriptors().values().stream().map(this::transformField).forEach(fieldJoiner::add);
        symbol.methods().values().stream().map(this::transformMethod)
                .forEach(method -> methodJoiner.add(method).add(";"));

        String stringRepr = signature.append(fieldJoiner.toString())
                .append(methodJoiner.toString()).append("}").toString();
        this.setState(stringRepr);
    }

    @Override
    protected void visit(RecordTypeSymbol symbol) {
        // Record type symbol.

        StringJoiner joiner;
        // TODO: Find a better way to decide if a symbol is sealed or not.
        boolean isRecordInclusive = !symbol.signature().endsWith("|}");
        if (isRecordInclusive) {
            joiner = new StringJoiner(" ", "{ ", " }");
            symbol.fieldDescriptors().values().stream().map(fd -> transformField(fd) + ";").forEach(joiner::add);
        } else {
            joiner = new StringJoiner(" ", "{| ", " |}");
            symbol.fieldDescriptors().values().stream().map(fd -> transformField(fd) + ";").forEach(joiner::add);
            symbol.restTypeDescriptor().ifPresent(typeDescriptor -> joiner.add(transformType(typeDescriptor) + "...;"));
        }
        this.setState("record " + joiner.toString());
    }

    @Override
    protected void visit(StreamTypeSymbol symbol) {
        // Stream is of format stream<TYPE>
        StringBuilder sigBuilder = new StringBuilder("stream<");
        sigBuilder.append(transformType(symbol.typeParameter()));
        if (symbol.completionValueTypeParameter().typeKind() != TypeDescKind.NEVER) {
            sigBuilder.append(", ").append(transformType(symbol.completionValueTypeParameter()));
        }
        sigBuilder.append('>');
        this.setState(sigBuilder.toString());
    }

    @Override
    protected void visit(TableTypeSymbol symbol) {
        // Table type may have key constraint and key specifiers.
        StringBuilder sigBuilder = new StringBuilder("table");
        Optional<TypeSymbol> keyConstraint = symbol.keyConstraintTypeParameter();
        List<String> keySpecifiers = symbol.keySpecifiers();

        sigBuilder.append('<').append(transformType(symbol.rowTypeParameter())).append('>');
        keyConstraint.ifPresent(t -> sigBuilder.append(" key<").append(transformType(t)).append(">"));

        if (!keySpecifiers.isEmpty()) {
            StringJoiner specifiersBuilder = new StringJoiner(",", "(", ")");
            keySpecifiers.forEach(specifiersBuilder::add);
            sigBuilder.append(" key").append(specifiersBuilder.toString());
        }

        this.setState(sigBuilder.toString());
    }

    @Override
    protected void visit(TupleTypeSymbol symbol) {
        // Tuple type. [TYPE1, TYPE2, ...]
        StringJoiner joiner = new StringJoiner(", ");
        symbol.memberTypeDescriptors().stream().map(this::transformType).forEach(joiner::add);
        if (symbol.restTypeDescriptor().isPresent()) {
            joiner.add("..." + transformType(symbol.restTypeDescriptor().get()));
        }
        this.setState("[" + joiner.toString() + "]");
    }

    @Override
    protected void visit(TypeDescTypeSymbol symbol) {
        // A general type desc with/without parameters. TYPE<PARAMS>
        String paramRepr = symbol.typeParameter().map(param -> "<" + transformType(param) + ">").orElse("");
        this.setState("typedesc" + paramRepr);
    }

    @Override
    protected void visit(UnionTypeSymbol symbol) {
        // If optional type, it will be TYPE?
        // If union type, it will be TYPE1|TYPE2.
        String stringRepr;
        List<TypeSymbol> memberTypes = symbol.memberTypeDescriptors();
        if (memberTypes.size() == 2 && memberTypes.get(1).typeKind() == TypeDescKind.NIL) {
            stringRepr = transformType(memberTypes.get(0)) + "?";
        } else {
            StringJoiner joiner = new StringJoiner("|");
            memberTypes.forEach(typeDescriptor -> joiner.add(transformType(typeDescriptor)));
            stringRepr = joiner.toString();
        }
        this.setState(stringRepr);
    }

    @Override
    protected void visit(XMLTypeSymbol symbol) {
        // XML. If signature is `xml` it is kept as is.
        // Otherwise, signature is recalculated. This signature is at least 'never'.
        String stringRepr = "xml";
        // TODO: Find better way to check if xml type is empty. (BType is xmlType)
        if (!symbol.signature().equals("xml")) {
            String typeRepr = symbol.typeParameter().map(this::transformType).orElse("never");
            stringRepr = "xml<" + typeRepr + ">";
        }
        this.setState(stringRepr);
    }

    @Override
    protected void visit(ErrorTypeSymbol symbol) {
        this.setState(transformExternalRefType(symbol));
    }

    @Override
    protected void visit(TypeReferenceTypeSymbol symbol) {
        this.setState(transformExternalRefType(symbol));
    }

    @Override
    protected void visit(TypeSymbol symbol) {
        setState(symbol.signature());
    }

    /**
     * Formats the type signature so that it can be used as a typedef. For example, int will be formatted to int.
     * ballerina/abc:1.0:pqr will be converted to 'imp1:pqr and an import added as import ballerina/abc.pqr as 'imp1.
     */
    private String transformExternalRefType(TypeSymbol typeSymbol) {
        // If the module is not anon, imports module.

        String typeName = typeSymbol.name();
        if (typeName.isBlank()) {
            String typeSignature = typeSymbol.signature();
            typeName = typeSignature.substring(typeSignature.lastIndexOf(':') + 1);
        }

        if (typeSymbol.moduleID().orgName().equals(ANON_MODULE)
                || (typeSymbol.moduleID().moduleName().equals("lang.annotations")
                && typeSymbol.moduleID().orgName().equals("ballerina"))) {
            // No import required. If the name is not found,
            // signature can be used without module parts.
            return typeName;

        } else {
            String moduleName = Arrays.stream(typeSymbol.moduleID().moduleName().split("\\."))
                    .map(StringUtils::quoted).collect(Collectors.joining("."));
            String fullModuleName = typeSymbol.moduleID().orgName() + "/" + moduleName;
            String defaultPrefix = StringUtils.quoted(typeSymbol.moduleID().modulePrefix());
            try {
                String importPrefix = importProcessor.processImplicitImport(fullModuleName, defaultPrefix);
                implicitImportPrefixes.add(importPrefix);
                return String.format("%s:%s", importPrefix, typeName);
            } catch (InvokerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get all the implicitly imported import prefixes.
     *
     * @return Set of implicit prefixes.
     */
    public Collection<String> getImplicitImportPrefixes() {
        return implicitImportPrefixes;
    }
}
