/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.test.unit;

import io.ballerina.shell.Evaluator;
import io.ballerina.shell.EvaluatorBuilder;
import io.ballerina.shell.invoker.classload.ClassLoadInvoker;
import io.ballerina.shell.parser.SerialTreeParser;
import io.ballerina.shell.preprocessor.SeparatorPreprocessor;
import io.ballerina.shell.snippet.factory.BasicSnippetFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests if evaluator builds are successful.
 *
 * @since 2.0.0
 */
public class EvaluatorBuildTest {
    @Test
    public void testBuilderDefaults() {
        Evaluator evaluator = new EvaluatorBuilder().build();
        Assert.assertEquals(evaluator.getPreprocessor().getClass(), SeparatorPreprocessor.class);
        Assert.assertEquals(evaluator.getTreeParser().getClass(), SerialTreeParser.class);
        Assert.assertEquals(evaluator.getSnippetFactory().getClass(), BasicSnippetFactory.class);
        Assert.assertEquals(evaluator.getInvoker().getClass(), ClassLoadInvoker.class);
    }

    @Test
    public void testCustomBuilder() {
        Evaluator evaluator = new EvaluatorBuilder()
                .preprocessor(new CustomPreprocessor())
                .treeParser(new CustomTreeParser())
                .snippetFactory(new CustomSnippetFactory())
                .invoker(new CustomInvoker())
                .build();
        Assert.assertEquals(evaluator.getPreprocessor().getClass(), CustomPreprocessor.class);
        Assert.assertEquals(evaluator.getTreeParser().getClass(), CustomTreeParser.class);
        Assert.assertEquals(evaluator.getSnippetFactory().getClass(), CustomSnippetFactory.class);
        Assert.assertEquals(evaluator.getInvoker().getClass(), CustomInvoker.class);
    }

    private static class CustomPreprocessor extends SeparatorPreprocessor {
    }

    private static class CustomTreeParser extends SerialTreeParser {
        public CustomTreeParser() {
            super(0);
        }
    }

    private static class CustomSnippetFactory extends BasicSnippetFactory {
    }

    private static class CustomInvoker extends ClassLoadInvoker {
    }
}
