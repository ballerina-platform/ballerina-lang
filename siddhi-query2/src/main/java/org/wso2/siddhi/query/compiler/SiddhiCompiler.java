/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.query.compiler;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;
import org.wso2.siddhi.query.compiler.internal.SiddhiQLGrammarBasedVisitorImpl;

import java.util.List;

public class SiddhiCompiler {

//    private static SiddhiCompiler compiler = null;
//
//    private SiddhiCompiler() {
//    }
//
//    /**
//     * get the SiddhiCompiler instance
//     *
//     * @return the instance of query factory
//     */
//    public static SiddhiCompiler getInstance() {
//        if (null == compiler) {
//            compiler = new SiddhiCompiler();
//        }
//        return compiler;
//    }

    public static void main(String[] args) {
//       SiddhiCompiler.parse("define table foo (price string) from ('a'='av','b'='bv')");
       SiddhiCompiler.parse("from abc[price>7] insert into efg ;");
    }
    public static List<ExecutionPlan> parse(String source) throws SiddhiParserException {
        try {

            ANTLRInputStream input = new ANTLRInputStream(source);
            SiddhiQLGrammarLexer lexer = new SiddhiQLGrammarLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SiddhiQLGrammarParser parser = new SiddhiQLGrammarParser(tokens);
            ParseTree tree = parser.query(); // parse

            SiddhiQLGrammarVisitor eval = new SiddhiQLGrammarBasedVisitorImpl();
            System.out.println(eval.visit(tree));
//          return (List<ExecutionPlan>) eval.visit(tree);
          return null;

        } catch (Throwable e) {
            throw new SiddhiParserException(e.getMessage(), e);
        }
    }

//    public static StreamDefinition parseStreamDefinition(String source) throws SiddhiParserException {
//        try {
//            SiddhiQLGrammarLexer lexer = new SiddhiQLGrammarLexer();
//            lexer.setCharStream(new ANTLRStringStream(source));
//            CommonTokenStream tokens = new CommonTokenStream(lexer);
//            SiddhiQLGrammarParser parser = new SiddhiQLGrammarParser(tokens);
//
//            SiddhiQLGrammarParser.definitionStreamFinal_return r = parser.definitionStreamFinal();
//            CommonTree t = (CommonTree) r.getTree();
//
//            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
//            nodes.setTokenStream(tokens);
//            SiddhiQLGrammarWalker walker = new SiddhiQLGrammarWalker(nodes);
//            return walker.definitionStreamFinal();
//        } catch (Throwable e) {
//            throw new SiddhiParserException(e.getMessage(), e);
//        }
//    }
//
//    public static TableDefinition parseTableDefinition(String source) throws SiddhiParserException {
//        try {
//            SiddhiQLGrammarLexer lexer = new SiddhiQLGrammarLexer();
//            lexer.setCharStream(new ANTLRStringStream(source));
//            CommonTokenStream tokens = new CommonTokenStream(lexer);
//            SiddhiQLGrammarParser parser = new SiddhiQLGrammarParser(tokens);
//
//            SiddhiQLGrammarParser.definitionTableFinal_return r = parser.definitionTableFinal();
//            CommonTree t = (CommonTree) r.getTree();
//
//            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
//            nodes.setTokenStream(tokens);
//            SiddhiQLGrammarWalker walker = new SiddhiQLGrammarWalker(nodes);
//            return walker.definitionTableFinal();
//        } catch (Throwable e) {
//            throw new SiddhiParserException(e.getMessage(), e);
//        }
//    }
//
//    public static PartitionDefinition parsePartitionDefinition(String source) throws SiddhiParserException {
//        try {
//            SiddhiQLGrammarLexer lexer = new SiddhiQLGrammarLexer();
//            lexer.setCharStream(new ANTLRStringStream(source));
//            CommonTokenStream tokens = new CommonTokenStream(lexer);
//            SiddhiQLGrammarParser parser = new SiddhiQLGrammarParser(tokens);
//
//            SiddhiQLGrammarParser.definitionPartitionFinal_return r = parser.definitionPartitionFinal();
//            CommonTree t = (CommonTree) r.getTree();
//
//            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
//            nodes.setTokenStream(tokens);
//            SiddhiQLGrammarWalker walker = new SiddhiQLGrammarWalker(nodes);
//            return walker.definitionPartitionFinal();
//        } catch (Throwable e) {
//            throw new SiddhiParserException(e.getMessage(), e);
//        }
//    }
//
//    public static Query parseQuery(String source) throws SiddhiParserException {
//        try {
//            SiddhiQLGrammarLexer lexer = new SiddhiQLGrammarLexer();
//            lexer.setCharStream(new ANTLRStringStream(source));
//            CommonTokenStream tokens = new CommonTokenStream(lexer);
//            SiddhiQLGrammarParser parser = new SiddhiQLGrammarParser(tokens);
//
//            SiddhiQLGrammarParser.queryFinal_return r = parser.queryFinal();
//            CommonTree t = (CommonTree) r.getTree();
//
//            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
//            nodes.setTokenStream(tokens);
//            SiddhiQLGrammarWalker walker = new SiddhiQLGrammarWalker(nodes);
//            return walker.queryFinal();
//        } catch (Throwable e) {
//            throw new SiddhiParserException(e.getMessage(), e);
//        }
//    }


}
