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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This represents the Ballerina source parser.
 *
 * @since 0.94
 */
public class Parser {

    private static final CompilerContext.Key<Parser> PARSER_KEY = new CompilerContext.Key<>();
    private final boolean preserveWhitespace;

    private CompilerContext context;
    private DiagnosticLog dlog;

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
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public BLangPackage parse(PackageSource pkgSource) {
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        pkgSource.getPackageSourceEntries()
                .forEach(e -> pkgNode.addCompilationUnit(generateCompilationUnit(e)));
        pkgNode.pos = new DiagnosticPos(new BDiagnosticSource(pkgSource.getPackageId(),
                pkgSource.getPackageSourceEntries().get(0).getEntryName()), 1, 1, 1, 1);
        return pkgNode;
    }

    private CompilationUnitNode generateCompilationUnit(PackageSourceEntry sourceEntry) {
        try {
            String entryName = sourceEntry.getEntryName();
            CompilationUnitNode compUnit = TreeBuilder.createCompilationUnit();
            compUnit.setName(sourceEntry.getEntryName());

            BDiagnosticSource diagnosticSrc = getDiagnosticSource(sourceEntry);

            ANTLRInputStream ais = new ANTLRInputStream(new ByteArrayInputStream(sourceEntry.getCode()));
            ais.name = entryName;
            BallerinaLexer lexer = new BallerinaLexer(ais);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            BallerinaParser parser = new BallerinaParser(tokenStream);
            parser.setErrorHandler(new BallerinaParserErrorStrategy(context, diagnosticSrc));
            parser.addParseListener(newListener(tokenStream, compUnit, diagnosticSrc));
            parser.compilationUnit();

            if (dlog.errorCount > 0) {
                throw new BLangParserException("syntax errors in: " + entryName);
            }

            return compUnit;
        } catch (IOException e) {
            throw new RuntimeException("Error in populating package model: " + e.getMessage(), e);
        }
    }

    private BLangParserListener newListener(CommonTokenStream tokenStream,
                                            CompilationUnitNode compUnit,
                                            BDiagnosticSource diagnosticSrc) {
        if (this.preserveWhitespace) {
            return new BLangWSPreservingParserListener(tokenStream, compUnit, diagnosticSrc);
        } else {
            return new BLangParserListener(compUnit, diagnosticSrc);
        }
    }

    private BDiagnosticSource getDiagnosticSource(PackageSourceEntry sourceEntry) {
        String entryName = sourceEntry.getEntryName();
        return new BDiagnosticSource(sourceEntry.getPackageID(), entryName);
    }

}
