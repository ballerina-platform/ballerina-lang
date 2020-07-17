/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.testutils;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Read the xml file and initialize the test Suite.
 *
 * @since 2.0.0
 */

public class InitializeTestSuite {

    // Function For parsing Test-Group info
    public static void fillGroup(XMLValue xmlValue, CTestGroup cTestGroup, String absPath) {
        cTestGroup.setName(getAttributeValue(xmlValue.getAttributesMap()));
        XMLValue elements = xmlValue.children();
        for (int i = 0; i < elements.size(); i++) {
            XMLValue subElement = elements.getItem(i);
            if (subElement.getItemType().equals(CTestConstants.TEST_ELEMENT_TAG)) {
                if (subElement.getElementName().equals(CTestConstants.TEST_DESCRIPTION_TAG)) {
                    cTestGroup.setDescription(subElement.children().toString());
                } else if (subElement.getElementName().equals(CTestConstants.TEST_TAG)) {
                    CTests cTests = new CTests();
                    cTests.setTestName(getAttributeValue(subElement.getAttributesMap()));
                    fillTest(subElement, cTests, absPath);
                    cTestGroup.addTests(cTests);
                }
            }
        }
    }

    public static String getAttributeValue(MapValue<BString, ?> attrMap) {
        String attrValue = null;
        for (Map.Entry<BString, ?> entry : attrMap.entrySet()) {
            if (entry.getKey().getValue().equals(CTestConstants.TEST_NAME_TAG)) {
                attrValue = entry.getValue().toString();
            }
        }
        return attrValue;
    }

    // Function For parsing Test info
    public static void fillTest(XMLValue xmlvalue, CTests cTests, String absPath) {
        XMLValue testElements = xmlvalue.children();
        for (int i = 0; i < testElements.size(); i++) {
            XMLValue testArgs = testElements.getItem(i);
            if (testArgs.getItemType().equals(CTestConstants.TEST_ELEMENT_TAG)) {
                switch (testArgs.getElementName()) {
                    case CTestConstants.TEST_DESCRIPTION_TAG:
                        cTests.setDescription(testArgs.children().toString());
                        break;
                    case CTestConstants.TEST_FILE_TAG:
                        cTests.setPathName(testArgs.children().toString());
                        break;
                    case CTestConstants.TEST_NEGATIVE_TAG:
                        cTests.setNegativeTestFlag();
                        String path = absPath + testArgs.children().toString();
                        cTests.setPathName(path);
                        break;
                    case CTestConstants.TEST_CALLFUNCTION_TAG:
                        cTests.addTestFunctions(callerfunction(testArgs));
                        break;
                    default:
                        fillTest(testArgs, cTests, absPath);
                        break;
                }
            }
        }
    }

    // Function For parsing Test-Steps info
    public static TestFunction callerfunction(XMLValue xmlValue) {
        TestFunction testFunction = new TestFunction();
        testFunction.setFunctionName(getAttributeValue(xmlValue.getAttributesMap()));
        XMLValue functionArgs = xmlValue.children();
        for (int i = 0; i < functionArgs.size(); i++) {
            XMLValue xmlElemnets = functionArgs.getItem(i);
            List<ParamTypes> paramList;
            if (xmlElemnets.getItemType().equals(CTestConstants.TEST_ELEMENT_TAG)) {
                if (xmlElemnets.getElementName().equals(CTestConstants.TEST_PARAMETERS_TAG)) {
                    paramList = getFunctionParams(xmlElemnets);
                    testFunction.addDataProvider(paramList);
                    testFunction.setParamFlag();
                } else if (xmlElemnets.getElementName().equals(CTestConstants.TEST_ASSERT_TAG)) {
                    XMLValue xmlElment = xmlElemnets.children().getItem(1);
                    if (xmlElment.getElementName().equals(CTestConstants.TEST_ERROR_TAG)) {
                        paramList = getFunctionParams(xmlElemnets);
                        testFunction.addAssertDataProvider(paramList);
                        testFunction.unSetPanicFlag();
                    } else if (xmlElment.getElementName().equals(CTestConstants.TEST_RETURN_TAG)) {
                        paramList = getFunctionParams(xmlElment.children());
                        testFunction.addAssertDataProvider(paramList);
                    } else {
                        paramList = getFunctionParams(xmlElemnets);
                        testFunction.addAssertDataProvider(paramList);
                    }
                    testFunction.setAssertParamFlag();
                }
            }
        }
        return testFunction;
    }

    // Function for parsing Return Values, Function Parameters
    public static List<ParamTypes> getFunctionParams(XMLValue xmlValue) {
        XMLValue parameters = xmlValue.children();
        List<ParamTypes> paramList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            XMLValue param = parameters.getItem(i);
            if (param.getItemType().equals(CTestConstants.TEST_ELEMENT_TAG)) {
                if (param.getElementName().equals(CTestConstants.ARRAY_VALUE_TAG)) {
                    XMLValue xmlElement = param.children().getItem(1);
                    String typeOfElem  = xmlElement.getElementName();
                    String elementVal = xmlElement.getTextValue();
                    paramList.add(new ParamTypes(CTestConstants.ARRAY_VALUE_TAG, typeOfElem, elementVal));
                } else {
                    String typeOfElem = param.getElementName();
                    //replace \n with the newline character
                    String elementVal = param.children().toString().replace("\\n", "\n");
                    paramList.add(new ParamTypes(typeOfElem, typeOfElem, elementVal));
                }
            }
        }
        return paramList;
    }

    //function for parsing Test-Group xml file paths
    public static void extractPath(XMLValue xmlvalue, CTestSuite cTestSuite, String preffix) {
        XMLValue elements = xmlvalue.children();
        for (int i = 0; i < elements.size(); i++) {
            XMLValue element = elements.getItem(i);
            if (element.getItemType().equals(CTestConstants.TEST_ELEMENT_TAG)) {
                switch (element.getElementName()) {
                    case CTestConstants.TEST_NAME_TAG:
                        cTestSuite.setSuiteName(element.children().toString());
                        break;
                    case CTestConstants.TEST_DESCRIPTION_TAG:
                        cTestSuite.setSuiteDescription(element.children().toString());
                        break;
                    case CTestConstants.TEST_GROUPS_TAG:
                        cTestSuite.setTestGroupName(getAttributeValue(element.getAttributesMap()));
                        extractPath(element, cTestSuite, preffix);
                        break;
                    case CTestConstants.TEST_GROUP_TAG:
                        cTestSuite.setPaths(preffix + element.children().toString());
                        break;
                    default:
                        extractPath(element, cTestSuite, preffix);
                        break;
                }
            }
        }
    }
}
