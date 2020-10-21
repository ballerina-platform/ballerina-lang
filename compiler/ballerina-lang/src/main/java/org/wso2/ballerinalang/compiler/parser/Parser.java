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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.packaging.converters.FileSystemSourceInput;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * This class is responsible for parsing Ballerina source files.
 *
 * @since 0.94
 */
public class Parser {

    private static final CompilerContext.Key<Parser> PARSER_KEY = new CompilerContext.Key<>();
    private CompilerContext context;
    private PackageCache pkgCache;
    private ParserCache parserCache;
    private NodeCloner nodeCloner;
    private BLangDiagnosticLog dlog;

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
        this.pkgCache = PackageCache.getInstance(context);
        this.parserCache = ParserCache.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public BLangPackage parse(PackageSource pkgSource, Path sourceRootPath) {
        PackageID pkgId = pkgSource.getPackageId();
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.pkgCache.put(pkgId, pkgNode);

        for (CompilerInput sourceInput : pkgSource.getPackageSourceEntries()) {
            BDiagnosticSource diagnosticSource = getDiagnosticSource(sourceInput, pkgId);
            if (ProjectDirs.isTestSource(((FileSystemSourceInput) sourceInput).getPath(),
                    sourceRootPath, pkgId.getName().value)) {
                // This check is added to ensure that there is exactly one testable package per bLangPackage
                if (!pkgNode.containsTestablePkg()) {
                    BLangTestablePackage testablePkg = TreeBuilder.createTestablePackageNode();
                    testablePkg.flagSet.add(Flag.TESTABLE);
                    testablePkg.pos = new DiagnosticPos(new BDiagnosticSource(pkgId, pkgSource.getName()), 1, 1, 1, 1);
                    pkgNode.addTestablePkg(testablePkg);
                }
                pkgNode.getTestablePkg().addCompilationUnit(
                        generateCompilationUnitNew(sourceInput, pkgId, diagnosticSource)
                );
            } else {
                pkgNode.addCompilationUnit(generateCompilationUnitNew(sourceInput, pkgId, diagnosticSource));
            }
        }

        pkgNode.pos = new DiagnosticPos(new BDiagnosticSource(pkgId, pkgSource.getName()), 0, 0, 0, 0);
        pkgNode.repos = pkgSource.getRepoHierarchy();
        return pkgNode;
    }

    private CompilationUnitNode generateCompilationUnitNew(CompilerInput sourceEntry, PackageID packageID,
                                                           BDiagnosticSource diagnosticSource) {
        String entryName = sourceEntry.getEntryName();
        BLangCompilationUnit compilationUnit;
        SyntaxTree tree = sourceEntry.getTree();
        reportSyntaxDiagnostics(diagnosticSource, tree);

        //TODO: Get hash and length from tree
        byte[] code = sourceEntry.getCode();
        int hash = getHash(code);
        int length = code.length;

        compilationUnit = parserCache.get(packageID, entryName, hash, length);
        if (compilationUnit != null) {
            return compilationUnit;
        }

        BLangNodeTransformer bLangNodeTransformer = new BLangNodeTransformer(this.context, diagnosticSource);
        compilationUnit = (BLangCompilationUnit) bLangNodeTransformer.accept(tree.rootNode()).get(0);
        parserCache.put(packageID, entryName, hash, length, compilationUnit);
        // Node cloner will run for valid ASTs.
        // This will verify, any modification done to the AST will get handled properly.
        compilationUnit = nodeCloner.cloneCUnit(compilationUnit);
        return compilationUnit;
    }

    private BDiagnosticSource getDiagnosticSource(CompilerInput sourceEntry, PackageID packageID) {
        String entryName = sourceEntry.getEntryName();
        return new BDiagnosticSource(packageID, entryName);
    }


    private static int getHash(byte[] code) {
        // Assuming hash collision is unlikely in a modified source.
        // Additionally code.Length is considered to avoid hash collision.
        return Arrays.hashCode(code);
    }

    private void reportSyntaxDiagnostics(BDiagnosticSource diagnosticSource, SyntaxTree tree) {
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            dlog.logDiagnostic(diagnosticSource.pkgID, syntaxDiagnostic);
        }
    }
}
