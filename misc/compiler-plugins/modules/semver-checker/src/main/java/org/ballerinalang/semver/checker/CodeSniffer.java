package org.ballerinalang.semver.checker;

import com.google.gson.JsonArray;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

import java.util.ArrayList;


public class CodeSniffer extends CodeAnalyzer {
    JsonArray array = FunctionNodeAnalyser.getArray();
   ArrayList<SyntaxTree> syn = FunctionNodeAnalyser.getList();
    @Override
    public void init(CodeAnalysisContext analysisContext) {
        analysisContext.addSyntaxNodeAnalysisTask( new FunctionNodeAnalyser(syn) , SyntaxKind.MODULE_PART);

    }
}

