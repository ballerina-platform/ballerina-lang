/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.rest.datamodel;

import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BLangFragmentParserTest {

    private Map<String, String> expressions = new HashMap<>();
    private Map<String, String> expressionsExpected = new HashMap<>();
    private Map<String, String> statements = new HashMap<>();
    private Map<String, String> statementsExpected = new HashMap<>();

    @BeforeClass
    public void setup() throws Exception {
        File expressionSamples = new File(getClass().getClassLoader()
                .getResource("samples/fragments/expressions").getFile());
        if (expressionSamples.isDirectory()) {
            for (File expressionSample: expressionSamples.listFiles()) {
                if (expressionSample.isFile()) {
                    expressions.put(FilenameUtils.removeExtension(expressionSample.getName()),
                            new String(Files.readAllBytes(expressionSample.toPath())));
                }
            }
        }
        File expressionSamplesExpected = new File(getClass().getClassLoader()
                .getResource("samples/fragments/expressions/expected").getFile());
        if (expressionSamplesExpected.isDirectory()) {
            for (File expressionSampleExpected: expressionSamplesExpected.listFiles()) {
                if (expressionSampleExpected.isFile()) {
                    expressionsExpected.put(FilenameUtils.removeExtension(expressionSampleExpected.getName()),
                            new String(Files.readAllBytes(expressionSampleExpected.toPath())));
                }
            }
        }
        File statementSamples = new File(getClass().getClassLoader()
                .getResource("samples/fragments/statements").getFile());
        if (statementSamples.isDirectory()) {
            for (File statementSample: statementSamples.listFiles()) {
                if (statementSample.isFile()) {
                    statements.put(FilenameUtils.removeExtension(statementSample.getName()),
                            new String(Files.readAllBytes(statementSample.toPath())));
                }
            }
        }
        File statementSamplesExpected = new File(getClass().getClassLoader()
                .getResource("samples/fragments/statements/expected").getFile());
        if (statementSamplesExpected.isDirectory()) {
            for (File statementSampleExpected: statementSamplesExpected.listFiles()) {
                if (statementSampleExpected.isFile()) {
                    statementsExpected.put(FilenameUtils.removeExtension(statementSampleExpected.getName()),
                            new String(Files.readAllBytes(statementSampleExpected.toPath())));
                }
            }
        }

    }

    @Test
    public void testExpressionFragments() {
        expressions.entrySet().stream().forEach(entry -> {
            String caseName = entry.getKey();
            String sourceFragment = entry.getValue();
            String expectedJson = expressionsExpected.get(caseName);

            BLangSourceFragment fragment = new BLangSourceFragment();
            fragment.setSource(sourceFragment);
            fragment.setExpectedNodeType(BLangFragmentParserConstants.EXPRESSION);

            String result = BLangFragmentParser.parseFragment(fragment);
            Assert.assertEquals(result, expectedJson.trim(),
                    "Expected json not found while parsing expression fragment " + caseName);

        });
    }

    @Test
    public void testStatementFragments() {
        statements.entrySet().stream().forEach(entry -> {
            String caseName = entry.getKey();
            String sourceFragment = entry.getValue();
            String expectedJson = statementsExpected.get(caseName);

            BLangSourceFragment fragment = new BLangSourceFragment();
            fragment.setSource(sourceFragment);
            fragment.setExpectedNodeType(BLangFragmentParserConstants.STATEMENT);

            String result = BLangFragmentParser.parseFragment(fragment);
            Assert.assertEquals(result, expectedJson.trim(),
                    "Expected json not found while parsing statement fragment " + caseName);

        });
    }
}
