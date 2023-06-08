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
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Test class to test report using a ballerina project.
 *
 * @since 2.0.0
 */
public class CodeCoverageReportTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;
    private Path coverageXMLPath;
    private String multiModuleTestRoot;
    private String singleModuleTestRoot;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        singleModuleTestRoot = "single-module-codecov";
        multiModuleTestRoot = "test-report-tests";
    }

    @Test
    public void singleModulePkgCoverageTest() throws BallerinaTestException {
        projectPath = projectBasedTestsPath.resolve(singleModuleTestRoot).toString();
        coverageXMLPath = projectBasedTestsPath.resolve(singleModuleTestRoot).resolve("target").resolve("report")
                .resolve("codecov").resolve("coverage-report.xml");
        balClient.runMain("test", new String[]{"--code-coverage", "--coverage-format=xml"}, null,
                new String[]{}, new LogLeecher[]{}, projectPath);
        Path reportRoot = projectBasedTestsPath.resolve(singleModuleTestRoot).resolve("target").
                resolve("report").resolve("codecov");
        if (!reportRoot.toFile().exists()) {
            Assert.fail("Error occurred while generating the coverage XML for package.");
        }
        copyReportDTDFile(reportRoot);
        //Validate Package names in XML File
        List<String> expectedPackageNames = Arrays.asList(
                "report/codecov$test/0/types",
                "single-module-codecov",
                "report/codecov/0/creators",
                "report/codecov$test/0",
                "report/codecov/0/types",
                "report/codecov$test/0/constants",
                "report/codecov$test/0/creators",
                "report/codecov/0",
                "report/codecov$test/0/annotations",
                "report/codecov/0/annotations",
                "report/codecov/0/constants"
        );
        if (validatePackageNames(expectedPackageNames)) {
            Assert.assertTrue(true);
        } else {
            Assert.fail("Package Name Validation for coverage XML failed for single module project");
        }
    }

    @Test
    public void multipleModulePkgCoverageTest() throws BallerinaTestException {
        projectPath = projectBasedTestsPath.resolve(multiModuleTestRoot).toString();
        coverageXMLPath = projectBasedTestsPath.resolve(multiModuleTestRoot).resolve("target").resolve("report")
                .resolve("foo").resolve("coverage-report.xml");
        balClient.runMain("test", new String[]{"--code-coverage", "--coverage-format=xml"}, null,
                new String[]{}, new LogLeecher[]{}, projectPath);
        Path reportRoot = projectBasedTestsPath.resolve(multiModuleTestRoot).resolve("target").
                resolve("report").resolve("foo");
        if (!reportRoot.toFile().exists()) {
            Assert.fail("Error occurred while generating the coverage XML for package.");
        }
        copyReportDTDFile(reportRoot);
        ArrayList<String> expectedPackageNames = new ArrayList<>();
        Collections.addAll(expectedPackageNames,
                "testerina_report/foo$0046math$test/0/constants",
                "testerina_report/foo$test/0/types",
                "testerina_report/foo$0046bar/0/creators",
                "testerina_report/foo$0046bar$0046tests/0",
                "testerina_report/foo$0046math$test/0",
                "testerina_report/foo/0/creators",
                "testerina_report/foo$0046bar$0046tests/0/creators",
                "testerina_report/foo$0046bar$0046tests/0/types",
                "testerina_report/foo$0046math/0/constants",
                "testerina_report/foo$0046bar$0046tests$test/0/constants",
                "testerina_report/foo$0046math/0/types",
                "testerina_report/foo$0046bar/0/annotations",
                "test-report-tests/modules/math",
                "testerina_report/foo$0046math$test/0/creators",
                "testerina_report/foo$0046math/0/creators",
                "testerina_report/foo$0046bar$0046tests$test/0/types",
                "test-report-tests",
                "testerina_report/foo/0/types",
                "testerina_report/foo$0046bar/0/types",
                "testerina_report/foo$0046bar$0046tests/0/constants",
                "testerina_report/foo$test/0",
                "testerina_report/foo/0",
                "testerina_report/foo$0046bar/0",
                "testerina_report/foo/0/annotations",
                "testerina_report/foo/0/constants",
                "testerina_report/foo$0046math$test/0/types",
                "testerina_report/foo$0046math/0/annotations",
                "testerina_report/foo$0046bar$0046tests$test/0/creators",
                "testerina_report/foo$0046bar$0046tests$test/0",
                "testerina_report/foo$0046bar$0046tests/0/annotations",
                "testerina_report/foo$0046bar$0046tests$test/0/annotations",
                "testerina_report/foo$0046math$test/0/annotations",
                "testerina_report/foo$test/0/annotations",
                "testerina_report/foo$test/0/constants",
                "testerina_report/foo$0046math/0",
                "testerina_report/foo$0046bar/0/constants",
                "test-report-tests/modules/bar",
                "testerina_report/foo$test/0/creators"
        );
        // Validate Package names in XML File
        if (validatePackageNames(expectedPackageNames)) {
            Assert.assertTrue(true);
        } else {
            Assert.fail("Package Name Validation for coverage XML falied for multi module project");
        }
    }

    @Test
    public void normalizedCoverageClassTest() throws BallerinaTestException {
        projectPath = projectBasedTestsPath.resolve(multiModuleTestRoot).toString();
        balClient.runMain("test", new String[]{"--code-coverage", "--coverage-format=xml"}, null,
                new String[]{}, new LogLeecher[]{}, projectPath);
        Path reportRoot = projectBasedTestsPath.resolve(multiModuleTestRoot).resolve("target").
                resolve("report").resolve("foo");
        if (!reportRoot.toFile().exists()) {
            Assert.fail("Error occurred while generating the coverage XML for package.");
        }
        copyReportDTDFile(reportRoot);
        //Validate class names in XML File per module
        validateClassNames(getExpectedCoverageClasses());
    }

    /**
     * Get the expected class elements per each package element in coverage XML.
     *
     * @return HashMap<String, List<String>>
     */
    private HashMap<String, List<String>> getExpectedCoverageClasses() {
        HashMap<String, List<String>> coverageClassMap = new HashMap<>();
        coverageClassMap.put(multiModuleTestRoot,
                Arrays.asList(multiModuleTestRoot + "/main", multiModuleTestRoot + "/foo",
                        multiModuleTestRoot + "/$value$Record", multiModuleTestRoot + "/$typedesc$Record",
                        multiModuleTestRoot + "/$value$ABC"));
        coverageClassMap.put(multiModuleTestRoot + "/modules/bar",
                List.of(multiModuleTestRoot + "/modules/bar/main"));
        coverageClassMap.put(multiModuleTestRoot + "/modules/math",
                Arrays.asList(multiModuleTestRoot + "/modules/math/add", multiModuleTestRoot +
                        "/modules/math/divide", multiModuleTestRoot + "/modules/math/foo$$$math"));
        coverageClassMap.put(multiModuleTestRoot + "/modules/bar.tests",
                List.of(multiModuleTestRoot + "/modules/bar.tests/foo$$$bar$$$tests"));
        return coverageClassMap;
    }

    /**
     * Copy the report.dtd file to the coverage xml path for validation.
     *
     * @param reportRoot Path
     * @throws BallerinaTestException
     */
    private void copyReportDTDFile(Path reportRoot) throws BallerinaTestException {
        File reportDTDFile = new File(projectBasedTestsPath.resolve(singleModuleTestRoot).resolve("resources").
                resolve("report.dtd").toString());
        File reportDTDFileCopy = new File(reportRoot.resolve("report.dtd").toString());
        try (FileOutputStream outputStream = new FileOutputStream(reportDTDFileCopy);
             FileInputStream inputStream = new FileInputStream(reportDTDFile);) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

        } catch (IOException exception) {
            throw new BallerinaTestException("Error while copying the report.dtd file for Jacoco coverageXML report " +
                    "validation.", exception);
        }
    }

    /**
     * Validates actual package names against the expected package names.
     *
     * @param expectedPackageNames List<String>
     * @return
     * @throws BallerinaTestException
     */
    private boolean validatePackageNames(List<String> expectedPackageNames) throws BallerinaTestException {
        //Validate XML File
        boolean isValidPackageName = false;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(coverageXMLPath.toString()));
            NodeList actualPackages = doc.getElementsByTagName("package");
            for (int i = 0; i < actualPackages.getLength(); i++) {
                String packageName = actualPackages.item(i).getAttributes().getNamedItem("name").getNodeValue();
                if (expectedPackageNames.contains(packageName)) {
                    isValidPackageName = true;
                } else {
                    isValidPackageName = false;
                    break;
                }
            }
            if (expectedPackageNames.size() != actualPackages.getLength()) {
                isValidPackageName = false;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BallerinaTestException("Error while parsing coverage xml file for test. ", e);
        }
        return isValidPackageName;
    }

    /**
     * Validate actual class names against the expected class names.
     *
     * @param expectedClassMap HashMap<String, List<String>>
     * @throws BallerinaTestException
     */
    private void validateClassNames(HashMap<String, List<String>> expectedClassMap) throws BallerinaTestException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(coverageXMLPath.toString()));
            NodeList actualPackages = doc.getElementsByTagName("package");
            for (int i = 0; i < actualPackages.getLength(); i++) {
                String packageName = actualPackages.item(i).getAttributes().getNamedItem("name").getNodeValue();
                if (expectedClassMap.containsKey(packageName)) {
                    NodeList actualClassList = actualPackages.item(i).getChildNodes();
                    for (int j = 0; j < actualClassList.getLength(); j++) {
                        if ("class".equals(actualClassList.item(j).getNodeName())) {
                            String className = actualClassList.item(j).getAttributes().
                                    getNamedItem("name").getNodeValue();
                            if (expectedClassMap.get(packageName).contains(className)) {
                                Assert.assertTrue(true);
                            } else {
                                Assert.fail("The class '" + className + "' is not in expected class list for " +
                                        packageName + " in " + coverageXMLPath.toString());
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BallerinaTestException("Error while parsing coverage xml file for test. ", e);
        }
    }
}

