package org.ballerinalang.bir.parser.builder;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ballerinalang.bir.model.IrBasicBlock;
import org.ballerinalang.bir.model.IrFunction;
import org.ballerinalang.bir.model.IrPackage;
import org.ballerinalang.bir.model.op.Op;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.BasicBlockContext;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.FunctionContext;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.IrPackageContext;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.OpContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates ir object model from bir source.
 * <p>
 * Uses recursive descent like tree transform instead visitor.
 */
public class IrModelBuilder {

    public static IrPackage buildIrPackage(IrPackageContext ctx) {
        List<IrFunction> functions = mapChildrenToList(ctx, FunctionContext.class, IrModelBuilder::buildFunction);
        return new IrPackage(unquote(ctx.Identifier()), functions);
    }

    private static IrFunction buildFunction(FunctionContext ctx) {
        List<IrBasicBlock> bbs = mapChildrenToList(ctx, BasicBlockContext.class, IrModelBuilder::buildBasicBlock);
        return new IrFunction(unquote(ctx.Identifier()), bbs);
    }

    private static IrBasicBlock buildBasicBlock(BasicBlockContext ctx) {
        int index = Integer.parseInt(ctx.BB().getText().substring(2));
        List<Op> ops = mapChildrenToList(ctx, OpContext.class, IrModelBuilder::buildOp);
        return new IrBasicBlock(index, ops);
    }

    private static Op buildOp(OpContext ctx) {
        return ctx.accept(OpBuildingVisitor.getInstance());
    }

    private static String unquote(TerminalNode identifier) {
        if (identifier == null) {
            return "";
        }
        String quoted = identifier.getText();
        return quoted.substring(1, quoted.length() - 1);
    }

    /**
     * Filter children of parent ctx and map them to model objects.
     *
     * @param parent  ctx to scan for children
     * @param ctxType type of the children to filter
     * @param mapper  antlr ctx to model object mapper
     * @param <T>     type of antlr child contexts to filter
     * @param <R>     type of ir model type
     * @return list of children mapped to model objects
     */
    private static <T extends ParserRuleContext, R, M extends R> List<R> mapChildrenToList(
            ParserRuleContext parent,
            Class<? extends T> ctxType,
            java.util.function.Function<T, M> mapper) {

        if (parent.children == null) {
            return Collections.emptyList();
        }

        List<R> contexts = null;
        for (ParseTree o : parent.children) {
            if (ctxType.isInstance(o)) {
                if (contexts == null) {
                    contexts = new ArrayList<R>();
                }

                contexts.add(mapper.apply(ctxType.cast(o)));
            }
        }

        if (contexts == null) {
            return Collections.emptyList();
        }

        return contexts;
    }

}
