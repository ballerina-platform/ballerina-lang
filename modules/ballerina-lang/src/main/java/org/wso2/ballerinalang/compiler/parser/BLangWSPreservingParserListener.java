package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;

import java.util.Set;
import java.util.TreeSet;

public class BLangWSPreservingParserListener extends BLangParserListener {
    public BLangWSPreservingParserListener(CommonTokenStream tokenStream,
                                           CompilationUnitNode compUnit,
                                           BDiagnosticSource diagnosticSource) {
        super(compUnit, diagnosticSource);
    }

    @Override
    protected Set<WSToken> getWS(ParserRuleContext ctx) {
        return new TreeSet<>();
    }
}
