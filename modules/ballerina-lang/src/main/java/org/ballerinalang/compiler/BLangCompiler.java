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
package org.ballerinalang.compiler;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.repository.PackageBinary;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.parser.BLangParserListener;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @since 0.94
 */
public class BLangCompiler {
    
    public static PackageBinary compile(PackageRepository repo, PackageSource pkgSource) {
        PackageNode pkgNode = loadPackageNode(pkgSource);
        log("* PackageNode: " + pkgNode);
        return null;
    }
    
    private static PackageNode loadPackageNode(PackageSource pkgSource) {
        PackageNode pkgNode = TreeBuilder.createPackageNode();
        PackageID pkgId = pkgSource.getPackageId();
        pkgNode.setVersion(pkgId.getVersion());
        pkgId.getNameComps().stream().forEach(e -> pkgNode.addNameComponent(e));
        pkgSource.getPackageSourceEntries().forEach(e -> populatePackageModel(pkgNode, e));
        return pkgNode;
    }
    
    private static void populatePackageModel(PackageNode pkgNode, PackageSourceEntry sourceEntry) {
        try {
            ANTLRInputStream ais = new ANTLRInputStream(new ByteArrayInputStream(sourceEntry.getCode()));
            ais.name = sourceEntry.getEntryName();
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
    
    public static void log(Object value) {
        PrintStream writer = System.out;
        writer.println(value);
    }
    
}
