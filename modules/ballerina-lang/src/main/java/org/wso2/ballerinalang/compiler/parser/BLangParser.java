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
import org.ballerinalang.model.tree.PackageNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.util.BLangSourcePackageFile;
import org.wso2.ballerinalang.compiler.util.BLangSourcePackageFile.BLangSourceFile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;

/**
 * @since 0.94
 */
public class BLangParser {
    private static final CompilerContext.Key<BLangParser> parserKey = new CompilerContext.Key<>();

    private CompilerContext context;

    public static BLangParser getInstance(CompilerContext context) {
        BLangParser parser = context.get(parserKey);
        if (parser == null) {
            parser = new BLangParser(context);
        }

        return parser;
    }

    public BLangParser(CompilerContext context) {
        this.context = context;
        this.context.put(parserKey, this);
    }

    public PackageNode parse(BLangSourcePackageFile sourcePackageFile) {
        PackageNode pkgNode = TreeBuilder.createPackageNode();
        for (BLangSourceFile sourceFile : sourcePackageFile.getSourceFiles()) {
            parseSourceFile(pkgNode, sourceFile);
        }

        return pkgNode;
    }

    private void parseSourceFile(PackageNode pkgNode, BLangSourceFile sourceFile) {
        try {
            ANTLRInputStream ais = new ANTLRInputStream(sourceFile.getInputStream());
            ais.name = sourceFile.getName();
            BallerinaLexer lexer = new BallerinaLexer(ais);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            BallerinaParser parser = new BallerinaParser(tokenStream);
            BLangParserListener listener = new BLangParserListener(pkgNode);
            parser.addParseListener(listener);
            parser.compilationUnit();
        } catch (IOException e) {
            throw new RuntimeException("Error in populating package model: " + e.getMessage(), e);
        }
    }
}
