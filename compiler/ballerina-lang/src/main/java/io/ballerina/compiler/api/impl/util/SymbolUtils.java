/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.util;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.identifier.Utils;
import io.ballerina.projects.Document;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Common util methods related to symbols.
 */
public final class SymbolUtils {

    private SymbolUtils() {
    }

    public static String unescapeUnicode(String value) {
        if (value.startsWith("'")) {
            return Utils.unescapeUnicodeCodepoints(value.substring(1));
        }
        return Utils.unescapeUnicodeCodepoints(value);
    }

    public static Optional<TypeSymbol> getTypeDescriptor(Symbol symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return switch (symbol.kind()) {
            case TYPE_DEFINITION -> Optional.ofNullable(((TypeDefinitionSymbol) symbol).typeDescriptor());
            case VARIABLE -> Optional.ofNullable(((VariableSymbol) symbol).typeDescriptor());
            case PARAMETER -> Optional.ofNullable(((ParameterSymbol) symbol).typeDescriptor());
            case ANNOTATION -> ((AnnotationSymbol) symbol).typeDescriptor();
            case FUNCTION,
                 METHOD -> Optional.ofNullable(((FunctionSymbol) symbol).typeDescriptor());
            case CONSTANT,
                 ENUM_MEMBER -> Optional.ofNullable(((ConstantSymbol) symbol).typeDescriptor());
            case CLASS -> Optional.of((ClassSymbol) symbol);
            case RECORD_FIELD -> Optional.ofNullable(((RecordFieldSymbol) symbol).typeDescriptor());
            case OBJECT_FIELD -> Optional.of(((ObjectFieldSymbol) symbol).typeDescriptor());
            case CLASS_FIELD -> Optional.of(((ClassFieldSymbol) symbol).typeDescriptor());
            case TYPE -> Optional.of((TypeSymbol) symbol);
            default -> Optional.empty();
        };
    }

    /**
     * Retrieves the type parameter's bound type based on the kind of the input BType.
     *
     * @param type The input BType for which the bound type is to be retrieved.
     * @return The bound type based on the kind of the input BType. Returns null if the kind is not supported.
     */
     public static BType getTypeParamBoundType(BType type) {
         return switch (type.getKind()) {
             case MAP -> ((BMapType) type).constraint;
             case ARRAY -> ((BArrayType) type).eType;
             case TABLE -> ((BTableType) type).constraint;
             case STREAM -> ((BStreamType) type).constraint;
             case XML -> {
                 if (type.tag == TypeTags.XML) {
                     yield ((BXMLType) type).constraint;
                 }
                 yield type;
             }
             // The following explicitly mentioned type kinds should be supported, but they are not for the moment.
             case ERROR -> null;
             default -> null;
         };
    }

    /**
     * Gets the compilation unit for a given source file document within a package.
     * @see #getCompilationUnit(BLangPackage, String)
     *
     * @param bLangPackage The package to search within.
     * @param srcFile      The source file document.
     * @return Optional containing the compilation unit if found, empty otherwise.
     */
    public static Optional<BLangCompilationUnit> getCompilationUnit(BLangPackage bLangPackage, Document srcFile) {
        return getCompilationUnit(bLangPackage, srcFile.name());
    }

    /**
     * Finds a compilation unit by its name within a package, including its testable packages.
     *
     * @param bLangPackage The package to search within.
     * @param srcFile      The name of the source file to find.
     * @return Optional containing the compilation unit if found, empty otherwise.
     */
    public static Optional<BLangCompilationUnit> getCompilationUnit(BLangPackage bLangPackage, String srcFile) {
        List<BLangCompilationUnit> testSrcs = new ArrayList<>();
        for (BLangTestablePackage pkg : bLangPackage.testablePkgs) {
            testSrcs.addAll(pkg.compUnits);
        }

        Stream<BLangCompilationUnit> units = Stream.concat(bLangPackage.compUnits.stream(), testSrcs.stream());
        return units.filter(unit -> unit.name.equals(srcFile)).findFirst();
    }
}
