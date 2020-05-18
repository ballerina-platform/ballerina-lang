package org.ballerinalang.langlib.testutils;


import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.XMLValue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        String testSuitePath = args[0] + args[1];
        List<String> testGroups = new ArrayList<>();
        InitializeTestSuite.extractPath(readXml(testSuitePath),testGroups , args[0]);
        for (String groupPath : testGroups) {
            CTestGroup cTestGroup = new CTestGroup();
            InitializeTestSuite.FillGroup(readXml(groupPath), cTestGroup);
            CTestRunner.testRunner(cTestGroup);
        }
    }

    public static XMLValue readXml(String path){
        XMLValue xmlvalue = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
             xmlvalue = XMLFactory.parse(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return xmlvalue;
    }
}
