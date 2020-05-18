package org.ballerinalang.langlib.testutils;


import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.XMLValue;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitializeTestSuite {

    public static void FillGroup(XMLValue xmlValue, CTestGroup cTestGroup) {
        MapValue<String, ?> name = xmlValue.getAttributesMap();
        cTestGroup.setName(name.getStringValue("name"));
        XMLValue elements = xmlValue.children();
        for (int i = 0; i < elements.size(); i++) {
            XMLValue subElement = elements.getItem(i);
            if (subElement.getItemType() == "element") {
                if (subElement.getElementName() == "description") {
                    cTestGroup.setDescription(subElement.children().toString());
                } else if (subElement.getElementName() == "test") {
                    CTests cTests = new CTests();
                    fillTest(subElement, cTests);
                    cTestGroup.addTests(cTests);
                }
            }
        }
    }

    public static void fillTest(XMLValue xmlvalue, CTests cTests) {
        XMLValue testElements = xmlvalue.children();
        for (int i = 0; i < testElements.size(); i++) {
            XMLValue testArgs = testElements.getItem(i);
            if (testArgs.getItemType() == "element") {
                if (testArgs.getElementName() == "description") {
                    cTests.setDescription(testArgs.children().toString());
                } else if (testArgs.getElementName() == "file") {
                    cTests.setPathName(testArgs.children().toString());
                } else if (testArgs.getElementName() == "callFunction") {
                    cTests.addTestFunctions(callerfunction(testArgs));
                } else {
                    fillTest(testArgs, cTests);
                }
            }
        }
    }

    // create testSteps object
    public static TestSteps callerfunction(XMLValue xmlValue) {
        TestSteps testSteps = new TestSteps();
        MapValue<String, ?> attrMap = xmlValue.getAttributesMap();
        testSteps.setFunctionName(attrMap.getStringValue("name"));
        XMLValue functionArgs = xmlValue.children();
        for (int i = 0; i < functionArgs.size(); i++) {
            XMLValue xmlElemnets = functionArgs.getItem(i);
            List<Map<String, Object>> paramList;
            if (xmlElemnets.getItemType() == "element") {
                if (xmlElemnets.getElementName() == "parameters") {
                    paramList = getFunctionParams(xmlElemnets);
                    testSteps.addParameterList(paramList);
                } else if (xmlElemnets.getElementName() == "assert") {
                    XMLValue xmlElment = xmlElemnets.children().getItem(1);
                    if (xmlElment.getElementName() == "error") {
                        paramList = getFunctionParams(xmlElemnets);
                        testSteps.addAssertVal(paramList);
                    } else if (xmlElment.getElementName() == "return") {
                        paramList = getFunctionParams(xmlElment.children());
                        testSteps.addAssertVal(paramList);
                    }
                }
            }
        }
        return testSteps;
    }

    // pass parameters, return and error xmlvalues
    public static List<Map<String, Object>> getFunctionParams(XMLValue xmlValue) {
        XMLValue parameters = xmlValue.children();
        List<Map<String, Object>> paramList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            XMLValue itemxml = parameters.getItem(i);
            if (itemxml.getItemType() == "element") {
                Map<String, Object> params = new HashMap<>();
                String typeOfElem = itemxml.getElementName();
                Object elementVal = itemxml.children();
                params.put(typeOfElem, elementVal);
                paramList.add(params);
            }
        }
        return paramList;
    }

    public static void extractPath(XMLValue xmlvalue, List<String> paths,String preffix){
        XMLValue elements = xmlvalue.children();
        for(int i=0;i<elements.size();i++){
            XMLValue itemxml = elements.getItem(i);
            if (itemxml.getItemType() == "element") {
                if (itemxml.getElementName() == "name") {
                    System.out.println(itemxml.children());
                } else if (itemxml.getElementName() == "description") {
                    System.out.println(itemxml.children());
                } else if (itemxml.getElementName() == "test-group") {
                    paths.add(preffix + itemxml.children().toString());
                } else {
                    extractPath(itemxml, paths, preffix);
                }
            }
        }
    }
}
