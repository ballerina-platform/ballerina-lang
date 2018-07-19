// Generated from BallerinaIRParser.g4 by ANTLR 4.5.3
package org.ballerinalang.bir.parser.antlr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BallerinaIRParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface BallerinaIRParserVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link BallerinaIRParser#irPackage}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIrPackage(BallerinaIRParser.IrPackageContext ctx);

    /**
     * Visit a parse tree produced by {@link BallerinaIRParser#function}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFunction(BallerinaIRParser.FunctionContext ctx);

    /**
     * Visit a parse tree produced by {@link BallerinaIRParser#basicBlock}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBasicBlock(BallerinaIRParser.BasicBlockContext ctx);

    /**
     * Visit a parse tree produced by {@link BallerinaIRParser#op}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOp(BallerinaIRParser.OpContext ctx);

    /**
     * Visit a parse tree produced by {@link BallerinaIRParser#opGoTo}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOpGoTo(BallerinaIRParser.OpGoToContext ctx);

    /**
     * Visit a parse tree produced by {@link BallerinaIRParser#opReturn}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOpReturn(BallerinaIRParser.OpReturnContext ctx);
}