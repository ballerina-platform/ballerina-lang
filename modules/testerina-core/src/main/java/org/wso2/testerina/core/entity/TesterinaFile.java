package org.wso2.testerina.core.entity;

import org.wso2.ballerina.core.model.*;


import java.util.ArrayList;

/**
 * Created by nirodha on 1/25/17.
 */
public class TesterinaFile {

    private String name ;
    private ArrayList<TesterinaFunction> testFunctions;
    private String resourcePath;
    private BallerinaFile bFile;

    TesterinaFile(String name, String resourcePath, BallerinaFile bFile) {
        this.name = name;
        this.resourcePath = resourcePath;
        this.bFile = bFile;
        setTestFunctions(this.bFile);
    }

    private void setTestFunctions(BallerinaFile bFile){
        /**
         * Private method to set only the 'test' functions, parsed from the *.bal file
         *
         * @param bFile Path to Bal file.
         * @return void
         */
        this.testFunctions = new ArrayList<TesterinaFunction>();
        Function[] allFunctions = bFile.getFunctions();
        for(int i=0; i < allFunctions.length; i++){
            String name = allFunctions[i].getFunctionName();
            if(name.startsWith(TesterinaFunction.PREFIX_TEST)){
                Function bFunc = allFunctions[i];
                TesterinaFunction tFunction = new TesterinaFunction(bFunc.getFunctionName(), TesterinaFunction.Type.TEST, bFunc, this);
                this.testFunctions.add(tFunction);
            }
        }
    }

    public ArrayList<TesterinaFunction> getTestFunctions(){
        /**
         * Getter method for testFunctions. Returns an ArrayList of functions starting with prefix 'test'.
         * @return ArrayList
         */
        return this.testFunctions;
    }

    public String getName(){
        /**
         * Getter method for 'name'. Returns the file name.
         * @return String
         */
        return this.name;
    }

    public BallerinaFile getBFile(){
        /**
         * Getter method for 'bFile'. Returns the BallerinaFile object.
         * @return BallerinaFile
         */
        return this.bFile;
    }


}
