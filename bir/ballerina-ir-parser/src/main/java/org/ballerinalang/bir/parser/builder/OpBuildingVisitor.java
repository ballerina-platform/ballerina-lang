package org.ballerinalang.bir.parser.builder;

import org.ballerinalang.bir.model.op.Op;
import org.ballerinalang.bir.model.op.OpGoTo;
import org.ballerinalang.bir.model.op.OpReturn;
import org.ballerinalang.bir.model.placeholders.PlaceholderBasicBlock;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParserBaseVisitor;

class OpBuildingVisitor extends BallerinaIRParserBaseVisitor<Op> {
    private static final OpBuildingVisitor INSTANCE = new OpBuildingVisitor();

    public static OpBuildingVisitor getInstance() {
        return INSTANCE;
    }

    private OpBuildingVisitor() {
    }

    @Override
    public Op visitOpGoTo(BallerinaIRParser.OpGoToContext ctx) {
        int index = Integer.parseInt(ctx.BB().getText().substring(2));
        return new OpGoTo(new PlaceholderBasicBlock(index));
    }

    @Override
    public Op visitOpReturn(BallerinaIRParser.OpReturnContext ctx) {
        return new OpReturn();
    }

    @Override
    protected Op aggregateResult(Op aggregate, Op nextResult) {
        if (nextResult == null) {
            return aggregate;
        }
        return nextResult;
    }
}
