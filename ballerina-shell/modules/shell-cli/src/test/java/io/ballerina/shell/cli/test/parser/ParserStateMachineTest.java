/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.test.parser;

import io.ballerina.shell.cli.jline.parser.ParserState;
import io.ballerina.shell.cli.jline.parser.ParserStateMachine;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code ParserStateMachine} behavior.
 *
 * @since 2.0.0
 */
public class ParserStateMachineTest {
    @Test
    public void testString() {
        //                                   0          1           2
        //                                   12345 678 9012 3456789 0
        ParserState[] states = states("Hello\"Hi\"Hee\"(Hello\n");
        Assert.assertEquals(states[0], ParserState.NORMAL);
        Assert.assertEquals(states[5], ParserState.NORMAL);
        Assert.assertEquals(states[8], ParserState.IN_DOUBLE_QUOTES);
        Assert.assertEquals(states[9], ParserState.NORMAL);
        Assert.assertEquals(states[12], ParserState.NORMAL);
        Assert.assertEquals(states[14], ParserState.IN_DOUBLE_QUOTES);
        Assert.assertEquals(states[18], ParserState.IN_DOUBLE_QUOTES);
        Assert.assertEquals(states[20], ParserState.ERROR);
        //                     12345 6 7 89
        states = states("Hello\\\"\\d");
        Assert.assertEquals(states[5], ParserState.NORMAL);
        Assert.assertEquals(states[6], ParserState.AFTER_BACKWARD_SLASH);
        Assert.assertEquals(states[7], ParserState.NORMAL);
        Assert.assertEquals(states[8], ParserState.AFTER_BACKWARD_SLASH);
        Assert.assertEquals(states[9], ParserState.NORMAL);
        states = states("{ {{ { \"} }}");
        Assert.assertEquals(states[states.length - 1], ParserState.IN_DOUBLE_QUOTES);
        //                     123 456 7
        states = states("ff(\"dd\n");
        Assert.assertEquals(states[1], ParserState.NORMAL);
        Assert.assertEquals(states[3], ParserState.NORMAL);
        Assert.assertEquals(states[4], ParserState.IN_DOUBLE_QUOTES);
        Assert.assertEquals(states[6], ParserState.IN_DOUBLE_QUOTES);
        Assert.assertEquals(states[7], ParserState.ERROR);
    }

    @Test
    public void testComments() {
        //                                   0        1           2          3          4
        //                                   123456789012345 6789 0123456 789012 34567890 123
        ParserState[] states = states("// Hello world \n12 \n// Hi \n/ Je \n# Hello\n12");
        Assert.assertEquals(states[0], ParserState.NORMAL);
        Assert.assertEquals(states[1], ParserState.AFTER_FORWARD_SLASH);
        Assert.assertEquals(states[2], ParserState.IN_COMMENT);
        Assert.assertEquals(states[10], ParserState.IN_COMMENT);
        Assert.assertEquals(states[16], ParserState.NORMAL);
        Assert.assertEquals(states[20], ParserState.NORMAL);
        Assert.assertEquals(states[24], ParserState.IN_COMMENT);
        Assert.assertEquals(states[27], ParserState.NORMAL);
        Assert.assertEquals(states[30], ParserState.NORMAL);
        Assert.assertEquals(states[34], ParserState.IN_COMMENT);
        Assert.assertEquals(states[40], ParserState.IN_COMMENT);
        Assert.assertEquals(states[41], ParserState.NORMAL);
    }

    @Test
    public void testTemplate() {
        //                                   0        1         2         3          4         5
        //                                   12345678901234567890123456789012 34567890123456789012
        ParserState[] states = states("Hello ` hello ` Hi ` Hello ${emb\nd { dd } } ` ${ n }");
        Assert.assertEquals(states[0], ParserState.NORMAL);
        Assert.assertEquals(states[7], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[10], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[15], ParserState.NORMAL);
        Assert.assertEquals(states[17], ParserState.NORMAL);
        Assert.assertEquals(states[20], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[25], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[28], ParserState.IN_TEMPLATE_AFTER_DOLLAR);
        Assert.assertEquals(states[29], ParserState.NORMAL);
        Assert.assertEquals(states[32], ParserState.NORMAL);
        Assert.assertEquals(states[38], ParserState.NORMAL);
        Assert.assertEquals(states[41], ParserState.NORMAL);
        Assert.assertEquals(states[43], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[45], ParserState.NORMAL);
        Assert.assertEquals(states[46], ParserState.NORMAL);
        //                     12 34567890
        states = states("`{\n` Hello");
        Assert.assertEquals(states[1], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[2], ParserState.IN_TEMPLATE);
        Assert.assertEquals(states[4], ParserState.NORMAL);
        Assert.assertEquals(states[8], ParserState.NORMAL);
    }

    @Test
    public void testError() {
        //                                   123456789
        ParserState[] states = states("{ ( } ) s");
        Assert.assertEquals(states[0], ParserState.NORMAL);
        Assert.assertEquals(states[4], ParserState.NORMAL);
        Assert.assertEquals(states[5], ParserState.ERROR);
        Assert.assertEquals(states[9], ParserState.ERROR);
    }

    @Test
    public void testOperator() {
        //                                   0        1
        //                                   1234567890123
        ParserState[] states = states("int i = 1 + 3");
        Assert.assertEquals(states[0], ParserState.NORMAL);
        Assert.assertEquals(states[4], ParserState.NORMAL);
        Assert.assertEquals(states[11], ParserState.AFTER_OPERATOR);
        Assert.assertEquals(states[12], ParserState.AFTER_OPERATOR);
        Assert.assertEquals(states[13], ParserState.NORMAL);
    }

    @Test
    public void testBracketIncompleteBug() {
        //                                   0        1
        //                                   1234567890
        ParserState[] states = states("json x = {");
        Assert.assertEquals(states[0], ParserState.NORMAL);
        Assert.assertEquals(states[4], ParserState.NORMAL);
        Assert.assertEquals(states[8], ParserState.AFTER_OPERATOR);
        Assert.assertEquals(states[9], ParserState.AFTER_OPERATOR);
        Assert.assertEquals(states[10], ParserState.NORMAL);
    }

    @Test
    public void testCompletion() {
        Assert.assertFalse(isComplete(" int i = 12 + \n"));
        Assert.assertFalse(isComplete("{ \n"));
        Assert.assertFalse(isComplete("{ \n {} "));
        Assert.assertFalse(isComplete("` Hello \n ${ hi\n"));
        Assert.assertFalse(isComplete("` Hello \n ${ hi\n } \n"));
        Assert.assertFalse(isComplete("var value = json {"));
        Assert.assertFalse(isComplete("json x = {"));
        Assert.assertTrue(isComplete("Hello(\"abc\n"));
        Assert.assertTrue(isComplete("` Hello \n ${ hi\n } \n`"));
        Assert.assertTrue(isComplete("// { \n"));
        Assert.assertTrue(isComplete("{ }"));
        Assert.assertTrue(isComplete("# dsds {"));
        Assert.assertTrue(isComplete("{ ( } "));
        Assert.assertTrue(isComplete(")"));
    }

    private ParserState[] states(String input) {
        ParserStateMachine stateMachine = new ParserStateMachine();
        ParserState[] states = new ParserState[input.length() + 1];
        states[0] = stateMachine.getState();
        for (int i = 0; i < input.length(); i++) {
            stateMachine.feed(input.charAt(i));
            states[i + 1] = stateMachine.getState();
        }
        return states;
    }

    private boolean isComplete(String input) {
        ParserStateMachine stateMachine = new ParserStateMachine();
        for (int i = 0; i < input.length(); i++) {
            stateMachine.feed(input.charAt(i));
        }
        return !stateMachine.isIncomplete();
    }
}
