/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bindgen;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.BindingsGenerator;
import org.ballerinalang.bindgen.model.JClass;
import org.ballerinalang.bindgen.utils.BindgenEnv;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.PrintStream;

/**
 * Test the ballerina bindgen syntax tree generated.
 *
 * @since 2.0.0
 */
public class SyntaxTreeGeneratorTest {

    @Test()
    public void test() throws FormatterException, ClassNotFoundException, BindgenException {
        BindgenEnv bindgenEnv = new BindgenEnv();
        bindgenEnv.setDirectJavaClass(true);
        bindgenEnv.setPublicFlag(true);
        bindgenEnv.setModulesFlag(true);
        bindgenEnv.setPackageName("packagex");
        BindingsGenerator bindingsGenerator = new BindingsGenerator(bindgenEnv);
        SyntaxTree syntaxTree = bindingsGenerator.generate(new JClass(this.getClass().getClassLoader()
                .loadClass("org.ballerinalang.bindgen.BindgenTestResource"), bindgenEnv));
        new PrintStream(System.out).println(Formatter.format(syntaxTree.toSourceCode()));

    }
}
