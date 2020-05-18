package org.ballerinalang.langlib.testutils;

import java.util.List;
import java.util.Map;

public class TestSteps {

     String functionName;
     String description;
     List<Map<String, Object>> assertval;
     List<Map<String, Object>> parameters;


    public void addParameterList(List<Map<String, Object>> parameters){
        this.parameters = parameters;
    }

    public void addAssertVal(List<Map<String, Object>> assertval){
        this.assertval =assertval;
    }

    public List<Map<String, Object>> getAssertVal(){
        return this.assertval;
    }

    public List<Map<String, Object>> getParameters(){
        return this.parameters;
    }

    public void setFunctionName(String functionName) { this.functionName = functionName; };
    public String getFunctionName() { return  this.functionName; };
}
