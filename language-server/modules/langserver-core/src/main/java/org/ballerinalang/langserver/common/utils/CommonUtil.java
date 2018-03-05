/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.utils;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.BLangPackageContext;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Common utils to be reuse in language server implementation.
 * */
public class CommonUtil {
    /**
     * Get the package URI to the given package name.
     *
     * @param pkgName        Name of the package that need the URI for
     * @param currentPkgPath String URI of the current package
     * @param currentPkgName Name of the current package
     * @return String URI for the given path.
     */
    public static String getPackageURI(List<Name> pkgName, String currentPkgPath, List<Name> currentPkgName) {
        String newPackagePath;
        // If current package path is not null and current package is not default package continue,
        // else new package path is same as the current package path.
        if (currentPkgPath != null && !currentPkgName.get(0).getValue().equals(".")) {
            int indexOfCurrentPkgName = currentPkgPath.indexOf(currentPkgName.get(0).getValue());
            newPackagePath = currentPkgPath.substring(0, indexOfCurrentPkgName);
            for (Name pkgDir : pkgName) {
                newPackagePath = Paths.get(newPackagePath, pkgDir.getValue()).toString();
            }
        } else {
            newPackagePath = currentPkgPath;
        }
        return newPackagePath;
    }

    /**
     * Get the command instances for a given diagnostic.
     * @param diagnostic        Diagnostic to get the command against
     * @param params            Code Action parameters
     * @param documentManager   Document Manager instance
     * @param pkgContext        BLang Package Context
     * @return  {@link List}    List of commands related to the given diagnostic
     */
    public static List<Command> getCommandsByDiagnostic(Diagnostic diagnostic, CodeActionParams params,
                                                        WorkspaceDocumentManager documentManager,
                                                        BLangPackageContext pkgContext) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Command> commands = new ArrayList<>();
        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                    diagnosticMessage.lastIndexOf("'"));

            Path openedPath = getPath(params.getTextDocument().getUri());
            String pkgName = TextDocumentServiceUtil.getPackageFromContent(documentManager.getFileContent(openedPath));
            String sourceRoot = TextDocumentServiceUtil.getSourceRoot(openedPath, pkgName);
            PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
            CompilerContext context = TextDocumentServiceUtil.prepareCompilerContext(packageRepository, sourceRoot);

            ArrayList<PackageID> sdkPackages = pkgContext.getSDKPackages(context);
            sdkPackages.stream()
                    .filter(packageID -> packageID.getName().toString().endsWith("." + packageAlias))
                    .forEach(packageID -> {
                        String commandTitle = CommandConstants.IMPORT_PKG_TITLE + " " + packageID.getName().toString();
                        CommandArgument pkgArgument =
                                new CommandArgument(CommandConstants.ARG_KEY_PKG_NAME, packageID.getName().toString());
                        CommandArgument docUriArgument = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI,
                                params.getTextDocument().getUri());
                        commands.add(new Command(commandTitle, CommandConstants.CMD_IMPORT_PACKAGE,
                                new ArrayList<>(Arrays.asList(pkgArgument, docUriArgument))));
            });
        }

        return commands;
    }

    private static boolean isUndefinedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_PACKAGE);
    }

    /**
     * Inner class for the command argument holding argument key and argument value.
     */
    private static class CommandArgument {
        private String argumentK;

        private String argumentV;

        CommandArgument(String argumentK, String argumentV) {
            this.argumentK = argumentK;
            this.argumentV = argumentV;
        }

        public String getArgumentK() {
            return argumentK;
        }

        public String getArgumentV() {
            return argumentV;
        }
    }

    /**
     * Common utility to get a Path from the given uri string.
     * @param uri               URI of the file to get as a  Path
     * @return {@link Path}     Path of the uri
     */
    public static Path getPath(String uri) {
        Path path = null;
        try {
            path = Paths.get(new URL(uri).toURI());
        } catch (URISyntaxException | MalformedURLException e) {
            // Do Nothing
        }

        return path;
    }

    /**
     * Calculate the user defined type position.
     *
     * @param position position of the node
     * @param name     name of the user defined type
     * @param pkgAlias package alias name of the user defined type
     */
    public static void calculateEndColumnOfGivenName(DiagnosticPos position, String name, String pkgAlias) {
        position.eCol = position.sCol + name.length() + (!pkgAlias.isEmpty() ? (pkgAlias + ":").length() : 0);
    }

    /**
     * Convert the diagnostic position to a zero based positioning diagnostic position.
     * @param diagnosticPos - diagnostic position to be cloned
     * @return {@link DiagnosticPos} converted diagnostic position
     */
    public static DiagnosticPos toZeroBasedPosition(DiagnosticPos diagnosticPos) {
        int startLine = diagnosticPos.getStartLine() - 1;
        int endLine = diagnosticPos.getEndLine() - 1;
        int startColumn = diagnosticPos.getStartColumn() - 1;
        int endColumn = diagnosticPos.getEndColumn() - 1;
        return new DiagnosticPos(diagnosticPos.getSource(), startLine, endLine, startColumn, endColumn);
    }

    /**
     * Get the previous default token from the given start index.
     * @param tokenStream       Token Stream
     * @param startIndex        Start token index
     * @return {@link Token}    Previous default token
     */
    public static Token getPreviousDefaultToken(TokenStream tokenStream, int startIndex) {
        return getDefaultTokenToLeftOrRight(tokenStream, startIndex, -1);
    }

    /**
     * Get the next default token from the given start index.
     * @param tokenStream       Token Stream
     * @param startIndex        Start token index
     * @return {@link Token}    Previous default token
     */
    public static Token getNextDefaultToken(TokenStream tokenStream, int startIndex) {
        return getDefaultTokenToLeftOrRight(tokenStream, startIndex, 1);
    }

    /**
     * Get the Nth Default token to the left of current token index.
     * @param tokenStream       Token Stream to traverse
     * @param startIndex        Start position of the token stream
     * @param offset            Number of tokens to traverse left
     * @return {@link Token}    Nth Token
     */
    public static Token getNthDefaultTokensToLeft(TokenStream tokenStream, int startIndex, int offset) {
        Token token = null;
        int indexCounter = startIndex;
        for (int i = 0; i < offset; i++) {
            token = getPreviousDefaultToken(tokenStream, indexCounter);
            indexCounter = token.getTokenIndex();
        }
        
        return token;
    }

    /**
     * Get the Nth Default token to the right of current token index.
     * @param tokenStream       Token Stream to traverse
     * @param startIndex        Start position of the token stream
     * @param offset            Number of tokens to traverse right
     * @return {@link Token}    Nth Token
     */
    public static Token getNthDefaultTokensToRight(TokenStream tokenStream, int startIndex, int offset) {
        Token token = null;
        int indexCounter = startIndex;
        for (int i = 0; i < offset; i++) {
            token = getNextDefaultToken(tokenStream, indexCounter);
            indexCounter = token.getTokenIndex();
        }
        
        return token;
    }
    
    private static Token getDefaultTokenToLeftOrRight(TokenStream tokenStream, int startIndex, int direction) {
        Token token;
        while (true) {
            startIndex += direction;
            token = tokenStream.get(startIndex);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                break;
            }
        }
        return token;
    }
}
