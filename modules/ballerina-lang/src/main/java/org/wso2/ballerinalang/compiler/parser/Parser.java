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
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This represents the Ballerina source parser.
 *
 * @since 0.94
 */
public class Parser {

    private static final CompilerContext.Key<Parser> PARSER_KEY = new CompilerContext.Key<>();

    private CompilerContext context;

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
    }

    public BLangPackage parse(PackageSource pkgSource) {
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        pkgSource.getPackageSourceEntries()
                .forEach(e -> pkgNode.addCompilationUnit(generateCompilationUnit(e)));
        return pkgNode;
    }

    private CompilationUnitNode generateCompilationUnit(PackageSourceEntry sourceEntry) {
        try {
            ANTLRInputStream ais = new ANTLRInputStream(new ByteArrayInputStream(sourceEntry.getCode()));
            ais.name = sourceEntry.getEntryName();
            BallerinaLexer lexer = new BallerinaLexer(ais);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            BallerinaParser parser = new BallerinaParser(tokenStream);
            CompilationUnitNode compUnit = TreeBuilder.createCompilationUnit();
            compUnit.setName(sourceEntry.getEntryName());
            BLangParserListener listener = new BLangParserListener(compUnit);
            parser.addParseListener(listener);
            parser.compilationUnit();
            return compUnit;
        } catch (IOException e) {
            throw new RuntimeException("Error in populating package model: " + e.getMessage(), e);
        }
    }

}
