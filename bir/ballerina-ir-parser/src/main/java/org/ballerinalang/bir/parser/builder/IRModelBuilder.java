package org.ballerinalang.bir.parser.builder;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.ballerinalang.bir.model.IrPackage;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParserBaseListener;

import java.util.ArrayList;

/**
 * Creates ir object model from bir source.
 * <p>
 * Uses recursive descent like tree transform instead visitor.
 */
public class IRModelBuilder extends BallerinaIRParserBaseListener {

//
//    public static void main(String[] args) throws IOException {
//        BallerinaIRLexer lexer = new BallerinaIRLexer(new ANTLRFileStream("/home/manu/sc/2018-07-16/empty.bir"));
//        TokenStream tokenStream = new CommonTokenStream(lexer);
//        BallerinaIRParser parser = new BallerinaIRParser(tokenStream);
//        BallerinaIRParser.IrPackageContext irPackageContext = parser.irPackage();
//        IrPackage irPackage = buildIrPackage(irPackageContext);
//        PrintStream out = System.out;
//        out.println(irPackage);
//    }

    public static IrPackage buildIrPackage(BallerinaIRParser.IrPackageContext irPackageContext) {
        TerminalNode identifier = irPackageContext.Identifier();
        String pkgName = "";
        if (identifier != null) {
            pkgName = identifier.getText();
        }
        return new IrPackage(unquot(pkgName), new ArrayList<>());
    }

    private static String unquot(String quoted) {
        if (quoted.startsWith("\"")) {
            return quoted.substring(1, quoted.length() - 1);
        } else {
            return quoted;
        }
    }

}
