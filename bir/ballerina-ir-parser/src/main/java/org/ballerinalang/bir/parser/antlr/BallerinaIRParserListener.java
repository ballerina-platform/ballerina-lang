// Generated from BallerinaIRParser.g4 by ANTLR 4.5.3
package org.ballerinalang.bir.parser.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BallerinaIRParser}.
 */
public interface BallerinaIRParserListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link BallerinaIRParser#irPackage}.
     *
     * @param ctx the parse tree
     */
    void enterIrPackage(BallerinaIRParser.IrPackageContext ctx);

    /**
     * Exit a parse tree produced by {@link BallerinaIRParser#irPackage}.
     *
     * @param ctx the parse tree
     */
    void exitIrPackage(BallerinaIRParser.IrPackageContext ctx);

    /**
     * Enter a parse tree produced by {@link BallerinaIRParser#function}.
     *
     * @param ctx the parse tree
     */
    void enterFunction(BallerinaIRParser.FunctionContext ctx);

    /**
     * Exit a parse tree produced by {@link BallerinaIRParser#function}.
     *
     * @param ctx the parse tree
     */
    void exitFunction(BallerinaIRParser.FunctionContext ctx);

    /**
     * Enter a parse tree produced by {@link BallerinaIRParser#basicBlock}.
     *
     * @param ctx the parse tree
     */
    void enterBasicBlock(BallerinaIRParser.BasicBlockContext ctx);

    /**
     * Exit a parse tree produced by {@link BallerinaIRParser#basicBlock}.
     *
     * @param ctx the parse tree
     */
    void exitBasicBlock(BallerinaIRParser.BasicBlockContext ctx);
}