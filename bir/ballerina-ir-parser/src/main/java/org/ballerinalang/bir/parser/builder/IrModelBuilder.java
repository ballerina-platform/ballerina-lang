package org.ballerinalang.bir.parser.builder;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ballerinalang.bir.model.IrBasicBlock;
import org.ballerinalang.bir.model.IrFunction;
import org.ballerinalang.bir.model.IrPackage;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.BasicBlockContext;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.FunctionContext;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser.IrPackageContext;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParserBaseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates ir object model from bir source.
 * <p>
 * Uses recursive descent like tree transform instead visitor.
 */
public class IrModelBuilder extends BallerinaIRParserBaseListener {

    public static IrPackage buildIrPackage(IrPackageContext context) {
        List<IrFunction> functions = mapChildrenToList(context, FunctionContext.class, IrModelBuilder::buildFunction);
        return new IrPackage(unquote(context.Identifier()), functions);
    }

    private static IrFunction buildFunction(FunctionContext context) {
        List<IrBasicBlock> bbs = mapChildrenToList(context, BasicBlockContext.class, IrModelBuilder::buildBasicBlock);
        return new IrFunction(unquote(context.Identifier()), bbs);
    }

    private static IrBasicBlock buildBasicBlock(BasicBlockContext context) {
        int index = Integer.parseInt(context.BB().getText().substring(2));
        return new IrBasicBlock(index);
    }

    private static String unquote(TerminalNode identifier) {
        if (identifier == null) {
            return "";
        }
        String quoted = identifier.getText();
        return quoted.substring(1, quoted.length() - 1);
    }

    /**
     * Filter children of parent context and map them to model objects.
     *
     * @param parent  context to scan for children
     * @param ctxType type of the children to filter
     * @param mapper  antlr context to model object mapper
     * @param <T>     type of antlr child contexts to filter
     * @param <R>     type of ir model type
     * @return list of children mapped to model objects
     */
    private static <T extends ParserRuleContext, R> List<R> mapChildrenToList(
            ParserRuleContext parent,
            Class<? extends T> ctxType,
            java.util.function.Function<T, R> mapper) {

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
