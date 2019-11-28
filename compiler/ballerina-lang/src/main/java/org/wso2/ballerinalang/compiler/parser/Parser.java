/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.packaging.converters.FileSystemSourceInput;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorListener;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * This class is responsible for parsing Ballerina source files.
 *
 * @since 0.94
 */
public class Parser {

    private static final CompilerContext.Key<Parser> PARSER_KEY = new CompilerContext.Key<>();
    private final boolean preserveWhitespace;

    private CompilerContext context;
    private PackageCache pkgCache;
    private ParserCache parserCache;

    public static Parser getInstance(CompilerContext context) {
        Parser parser = context.get(PARSER_KEY);
        if (parser == null) {
            parser = new Parser(context);
        }

        return parser;
    }

    public Parser(CompilerContext context) {
        this.context = context;
        this.context.put(PARSER_KEY, this);

        CompilerOptions options = CompilerOptions.getInstance(context);
        this.preserveWhitespace = Boolean.parseBoolean(options.get(CompilerOptionName.PRESERVE_WHITESPACE));
        this.pkgCache = PackageCache.getInstance(context);
        this.parserCache = ParserCache.getInstance(context);
    }

    public BLangPackage parse(PackageSource pkgSource, Path sourceRootPath) {
        PackageID pkgId = pkgSource.getPackageId();
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.pkgCache.put(pkgId, pkgNode);
        for (CompilerInput sourceInput: pkgSource.getPackageSourceEntries()) {
            if (ProjectDirs.isTestSource(((FileSystemSourceInput) sourceInput).getPath(),
                    sourceRootPath , pkgId.getName().value)) {
                // This check is added to ensure that there is exactly one testable package per bLangPackage
                if (!pkgNode.containsTestablePkg()) {
                    BLangTestablePackage testablePkg = TreeBuilder.createTestablePackageNode();
                    testablePkg.flagSet.add(Flag.TESTABLE);
                    testablePkg.pos = new DiagnosticPos(new BDiagnosticSource(pkgId, pkgSource.getName()), 1, 1, 1, 1);
                    pkgNode.addTestablePkg(testablePkg);
                }
                pkgNode.getTestablePkg().addCompilationUnit(generateCompilationUnit(sourceInput, pkgId));
            } else {
                pkgNode.addCompilationUnit(generateCompilationUnit(sourceInput, pkgId));
            }
        }
        pkgNode.pos = new DiagnosticPos(new BDiagnosticSource(pkgId,
                pkgSource.getName()), 1, 1, 1, 1);
        pkgNode.repos = pkgSource.getRepoHierarchy();
        return pkgNode;
    }

    private CompilationUnitNode generateCompilationUnit(CompilerInput sourceEntry, PackageID packageID) {
        try {
            BLangCompilationUnit compilationUnit = parserCache.get(sourceEntry, packageID);
            if (compilationUnit == null) {
                compilationUnit = getCompilationUnit(sourceEntry, packageID);
                parserCache.put(sourceEntry, packageID, compilationUnit);
            }
            return compilationUnit;
        } catch (IOException e) {
            throw new RuntimeException("error reading module: " + e.getMessage(), e);
        }
    }

    private BLangCompilationUnit getCompilationUnit(CompilerInput sourceEntry, PackageID packageID)
            throws IOException {

        BDiagnosticSource diagnosticSrc = getDiagnosticSource(sourceEntry, packageID);
        BLangCompilationUnit compUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        compUnit.setName(sourceEntry.getEntryName());
        compUnit.pos = new DiagnosticPos(diagnosticSrc, 1, 1, 1, 1);

        CommonTokenStream tokenStream = createTokenStream(sourceEntry, diagnosticSrc);
        BallerinaParser parser = new BallerinaParser(tokenStream);
        parser.setErrorHandler(getErrorStrategy(diagnosticSrc));
        parser.addParseListener(newListener(tokenStream, compUnit, diagnosticSrc));
        parser.compilationUnit();
        return compUnit;
    }

    private CommonTokenStream createTokenStream(CompilerInput sourceEntry, BDiagnosticSource diagnosticSrc)
            throws IOException {

        ANTLRInputStream ais = new ANTLRInputStream(
                new InputStreamReader(new ByteArrayInputStream(sourceEntry.getCode()), StandardCharsets.UTF_8));
        ais.name = sourceEntry.getEntryName();
        BallerinaLexer lexer = new BallerinaLexer(ais);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BallerinaParserErrorListener(context, diagnosticSrc));
        return new CommonTokenStream(lexer);
    }

    private BLangParserListener newListener(CommonTokenStream tokenStream,
                                            CompilationUnitNode compUnit,
                                            BDiagnosticSource diagnosticSrc) {
        if (this.preserveWhitespace) {
            return new BLangWSPreservingParserListener(this.context, tokenStream, compUnit, diagnosticSrc);
        } else {
            return new BLangParserListener(this.context, compUnit, diagnosticSrc);
        }
    }

    private BDiagnosticSource getDiagnosticSource(CompilerInput sourceEntry, PackageID packageID) {
        String entryName = sourceEntry.getEntryName();
        return new BDiagnosticSource(packageID, entryName);
    }

    private DefaultErrorStrategy getErrorStrategy(BDiagnosticSource diagnosticSrc) {
        DefaultErrorStrategy customErrorStrategy = context.get(DefaultErrorStrategy.class);
        if (customErrorStrategy == null) {
            customErrorStrategy = new BallerinaParserErrorStrategy(context, diagnosticSrc);
        } else {
            ((BallerinaParserErrorStrategy) customErrorStrategy).setDiagnosticSrc(diagnosticSrc);
        }
        return customErrorStrategy;
    }
}
