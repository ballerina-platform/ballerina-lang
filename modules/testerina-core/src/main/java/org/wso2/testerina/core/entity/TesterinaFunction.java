package org.wso2.testerina.core.entity;

import org.wso2.ballerina.core.exception.AssertionException;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Created by nirodha on 1/26/17.
 */
public class TesterinaFunction {

    private String name;
    private Type type;
    private Function bFunction;
    private TesterinaFile tFile;

    public static final String PREFIX_TEST = "test";
    public static final String PREFIX_BEFORETEST = "beforeTest";
    public static final String PREFIX_AFTERTEST = "afterTest";

    public enum Type{
        TEST(PREFIX_TEST), BEFORETEST(PREFIX_BEFORETEST), AFTERTEST(PREFIX_AFTERTEST);
        private String prefix;

        private Type(String prefix){
            this.prefix = prefix;
        }
    };

    TesterinaFunction(String name, Type type, Function bFunction, TesterinaFile tFile){
        this.name = name;
        this.type = type;
        this.bFunction = bFunction;
        this.tFile = tFile;
    }

    public BValue[] invoke() throws AssertionException{
        return Functions.invoke(this.tFile.getBFile(), this.name);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Function getbFunction() {
        return this.bFunction;
    }

    public void setbFunction(Function bFunction) {
        this.bFunction = bFunction;
    }






}
