package org.wso2.siddhi.query.test;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.wso2.siddhi.query.compiler.SiddhiQLGrammarLexer;
import org.wso2.siddhi.query.compiler.SiddhiQLGrammarParser;

public class QueryTestCase {

    @Test
    public void Test1() throws RecognitionException {
        SiddhiQLGrammarLexer lexer = new SiddhiQLGrammarLexer();

        lexer.setCharStream(new ANTLRStringStream(
                "define stream `define` ( `string` string, price int, volume float );"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLGrammarParser parser = new SiddhiQLGrammarParser(tokens);

        parser.executionPlan();
//        System.out.println("OK");
//        double float1 = 2d;
//        System.out.println(float1);
        // CommonTree t = (CommonTree) r.getTree();
        //
        // CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        // nodes.setTokenStream(tokens);
        // SiddhiGrammarWalker walker = new SiddhiGrammarWalker(nodes);
        // return walker.siddhiGrammar(existingStreams);

    }


}
