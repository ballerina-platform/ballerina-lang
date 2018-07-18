package org.ballerinalang.bir.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.ballerinalang.bir.model.IrPackage;
import org.ballerinalang.bir.parser.antlr.BallerinaIRLexer;
import org.ballerinalang.bir.parser.antlr.BallerinaIRParser;
import org.ballerinalang.bir.parser.builder.IrModelBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;

abstract class ParserTest {
    private final String className;

    ParserTest() {
        this.className = this.getClass().getSimpleName();
    }

    IrPackage parseBirWithSameName() throws IOException {
        Path birDir = Paths.get(System.getProperty("test.bir.path"));
        Path birFile = birDir.resolve(this.className + ".bir");
        BallerinaIRLexer lexer = new BallerinaIRLexer(new ANTLRFileStream(birFile.toString()));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        BallerinaIRParser parser = new BallerinaIRParser(tokenStream);
        parser.addErrorListener(new AssertingErrorListener(birFile));
        BallerinaIRParser.IrPackageContext irPackageContext = parser.irPackage();
        return IrModelBuilder.buildIrPackage(irPackageContext);
    }

    private static class AssertingErrorListener implements ANTLRErrorListener {
        private final Path birFile;

        AssertingErrorListener(Path birFile) {
            this.birFile = birFile;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object o, int i, int i1, String s, RecognitionException e) {
            throw new AssertionError("Syntax error in " + birFile);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1,
                                    boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i,
                                                int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa,
                                             int i, int i1, int i2, ATNConfigSet atnConfigSet) {

        }
    }
}
