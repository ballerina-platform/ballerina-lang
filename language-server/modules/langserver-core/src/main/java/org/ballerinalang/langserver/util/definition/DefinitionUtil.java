/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util.definition;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaDefinitionContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for the definition operations.
 *
 * @since 1.2.0
 */
public class DefinitionUtil {
    private static final String SELF_KW = "self";

    /**
     * Get the definition.
     *
     * @param context  Definition context
     * @param position cursor position
     * @return {@link List} List of definition locations
     */
    public static List<Location> getDefinition(BallerinaDefinitionContext context, Position position) {
        fillTokenInfoAtCursor(context);
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return Collections.emptyList();
        }

        LinePosition linePosition = LinePosition.from(position.getLine(), position.getCharacter());
        Optional<Symbol> symbol = semanticModel.get().symbol(srcFile.get(), linePosition);

        if (symbol.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Location> location;
        if (isSelfClassSymbol(symbol.get(), context)) {
            // Within the #isSelfClassSymbol we do the instance check against the symbol. Hence casting is safe 
            // If the self variable is referring to the class instance, navigate to class definition
            TypeSymbol rawType = CommonUtil.getRawType(((VariableSymbol) symbol.get()).typeDescriptor());
            location = getLocation(rawType, context);
        } else {
            location = getLocation(symbol.get(), context);
        }
        return location.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private static Optional<Location> getLocation(Symbol symbol, BallerinaDefinitionContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty() || symbol.getModule().isEmpty()) {
            return Optional.empty();
        }
        ModuleID moduleID = symbol.getModule().get().id();
        String orgName = moduleID.orgName();
        String moduleName = moduleID.moduleName();

        Optional<Path> filepath;
        if (CommonUtil.isLangLib(orgName, moduleName)) {
            filepath = getFilePathForLanglib(orgName, moduleName, project.get(), symbol);
        } else {
            filepath = getFilePathForDependency(orgName, moduleName, project.get(), symbol);
        }

        if (filepath.isEmpty() || symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }

        String uri = filepath.get().toUri().toString();
        io.ballerina.tools.diagnostics.Location symbolLocation = symbol.getLocation().get();
        LinePosition startLine = symbolLocation.lineRange().startLine();
        LinePosition endLine = symbolLocation.lineRange().endLine();
        Position start = new Position(startLine.line(), startLine.offset());
        Position end = new Position(endLine.line(), endLine.offset());
        Range range = new Range(start, end);

        return Optional.of(new Location(uri, range));
    }

    private static Optional<Path> getFilePathForDependency(String orgName, String moduleName,
                                                           Project project, Symbol symbol) {
        if (symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }
        Collection<ResolvedPackageDependency> dependencies =
                project.currentPackage().getResolution().dependencyGraph().getNodes();
        Optional<Path> filepath = Optional.empty();
        String sourceFile = symbol.getLocation().get().lineRange().filePath();
        for (ResolvedPackageDependency depNode : dependencies) {
            Package depPackage = depNode.packageInstance();
            for (ModuleId moduleId : depPackage.moduleIds()) {
                if (depPackage.packageOrg().value().equals(orgName) &&
                        depPackage.module(moduleId).moduleName().toString().equals(moduleName)) {
                    Module module = depPackage.module(moduleId);
                    for (DocumentId docId : module.documentIds()) {
                        if (module.document(docId).name().equals(sourceFile)) {
                            filepath =
                                    module.project().documentPath(docId);
                            break;
                        }
                    }
                }
            }
        }

        return filepath;
    }

    private static Optional<Path> getFilePathForLanglib(String orgName, String moduleName,
                                                        Project project, Symbol symbol) {
        Package langLibPackage = project.projectEnvironmentContext().environment().getService(PackageCache.class)
                .getPackages(PackageOrg.from(orgName), PackageName.from(moduleName)).get(0);
        String sourceFile = symbol.getLocation().get().lineRange().filePath();

        Optional<Path> filepath = Optional.empty();
        for (ModuleId moduleId : langLibPackage.moduleIds()) {
            Module module = langLibPackage.module(moduleId);
            for (DocumentId docId : module.documentIds()) {
                if (module.document(docId).name().equals(sourceFile)) {
                    filepath =
                            module.project().documentPath(docId);
                    break;
                }
            }
        }

        return filepath;
    }

    private static boolean isSelfClassSymbol(Symbol symbol, BallerinaDefinitionContext context) {
        Optional<ModuleMemberDeclarationNode> enclosedModuleMember = context.enclosedModuleMember();
        Optional<String> name = symbol.getName();
        if (enclosedModuleMember.isEmpty() || enclosedModuleMember.get().kind() != SyntaxKind.CLASS_DEFINITION
                || symbol.kind() != SymbolKind.VARIABLE || name.isEmpty() || !name.get().equals(SELF_KW)) {
            return false;
        }
        Optional<Symbol> memberSymbol = context.workspace().semanticModel(context.filePath())
                .get().symbol(enclosedModuleMember.get());

        if (memberSymbol.isEmpty() || memberSymbol.get().kind() != SymbolKind.CLASS) {
            return false;
        }
        ClassSymbol classSymbol = (ClassSymbol) memberSymbol.get();
        VariableSymbol selfSymbol = (VariableSymbol) symbol;
        TypeSymbol varTypeSymbol = CommonUtil.getRawType(selfSymbol.typeDescriptor());

        return classSymbol.equals(varTypeSymbol);
    }

    private static void fillTokenInfoAtCursor(BallerinaDefinitionContext context) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
    }
}
