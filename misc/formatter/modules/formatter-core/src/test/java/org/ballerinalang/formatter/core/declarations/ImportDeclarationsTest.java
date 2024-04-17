/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core.declarations;

import org.ballerinalang.formatter.core.FormatterTest;
import org.ballerinalang.formatter.core.options.FormattingOptions;
import org.ballerinalang.formatter.core.options.ImportFormattingOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

/**
 * Test the formatting of import declarations.
 *
 * @since 2.0.0
 */
public class ImportDeclarationsTest extends FormatterTest {

    @Test(dataProvider = "test-file-provider-custom")
    public void test(String source, String sourcePath) throws IOException {
        super.test(source, sourcePath);
    }

    @Test(dataProvider = "test-file-provider-custom")
    public void testWithCustomOptions(String source, String sourcePath, FormattingOptions formattingOptions)
            throws IOException {
        super.testWithCustomOptions(source, sourcePath, formattingOptions);
    }

    @DataProvider(name = "test-file-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[0][];
    }

    @DataProvider(name = "test-file-provider-custom")
    @Override
    public Object[][] dataProviderWithCustomTests(Method testName) {
        String testResourceDir = this.getTestResourceDir();
        switch (testName.getName()) {
            case "test":
                return new Object[][] {
                        {"import_declaration_1.bal", testResourceDir},
                        {"import_declaration_2.bal", testResourceDir},
                        {"import_declaration_6.bal", testResourceDir},
                        {"import_declaration_7.bal", testResourceDir},
                        {"import_declaration_8.bal", testResourceDir},
                        {"import_declaration_9.bal", testResourceDir},
                        {"import_declaration_10.bal", testResourceDir},
                        {"import_declaration_11.bal", testResourceDir}
                };
            case "testWithCustomOptions":
                FormattingOptions optionWithNoGrouping = FormattingOptions.builder()
                        .setImportFormattingOptions(ImportFormattingOptions.builder().setGroupImports(false).build())
                        .build();
                FormattingOptions optionWithNoSorting = FormattingOptions.builder()
                        .setImportFormattingOptions(ImportFormattingOptions.builder().setSortImports(false).build())
                        .build();
                FormattingOptions optionWithNoGroupingAndSorting = FormattingOptions.builder()
                        .setImportFormattingOptions(
                                ImportFormattingOptions.builder().setGroupImports(false).setSortImports(false).build())
                        .build();
                return new Object[][] {
                        {"import_declaration_3.bal", testResourceDir, optionWithNoGrouping},
                        {"import_declaration_4.bal", testResourceDir, optionWithNoSorting},
                        {"import_declaration_5.bal", testResourceDir, optionWithNoGroupingAndSorting}
                };
        }
        return null;
    }

    @Override
    public String getTestResourceDir() {
        return Paths.get("declarations", "import").toString();
    }
}
