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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BLangFragmentParserTest {

    public static final ClassLoader CLASS_LOADER = BLangFragmentParserTest.class.getClassLoader();
    public static final String SAMPLES_DIR = "samples/fragments/";

    private static void loadSamples(List<String[]> fragments, String type) throws IOException {
        File samples = new File(CLASS_LOADER
                .getResource(SAMPLES_DIR + type).getFile());
        if (samples.isDirectory()) {
            for (File sample : samples.listFiles()) {
                if (sample.isFile()) {
                    String nameWithoutExt = FilenameUtils.removeExtension(sample.getName());
                    File expected = new File(CLASS_LOADER.getResource(
                            SAMPLES_DIR + type + "/expected/" + nameWithoutExt + ".json").getFile());
                    if (expected.isFile()) {
                        fragments.add(new String[]{type, nameWithoutExt, sample.getPath(), expected.getPath()});
                    }
                }
            }
        }
    }

    @DataProvider(name = "fragments")
    public static Object[][] primeNumbers() throws IOException {
        ArrayList<String[]> fragments = new ArrayList<>();
        loadSamples(fragments, BLangFragmentParserConstants.EXPRESSION);
        loadSamples(fragments, BLangFragmentParserConstants.STATEMENT);
        loadSamples(fragments, BLangFragmentParserConstants.JOIN_CONDITION);
        return fragments.toArray(new String[fragments.size()][]);
    }

    @Test(dataProvider = "fragments")
    public void testFragmentParse(String type, String caseName, String sourcePath, String expectedPath)
            throws IOException {
        BLangSourceFragment fragment = new BLangSourceFragment();
        fragment.setSource(readFromFile(sourcePath));
        fragment.setExpectedNodeType(type);

        String result = BLangFragmentParser.parseFragment(fragment);
        Assert.assertEquals(result, readFromFile(expectedPath),
                "Expected json not found while parsing " + type + " fragment " + caseName);
    }

    private String readFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path))).trim();
    }

}
