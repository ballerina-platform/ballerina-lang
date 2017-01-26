package org.wso2.testerina.core.model;

/**
 * Created by Chanuka on 1/26/17.
 */


//import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.exception.AssertionException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
//import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.lang.assertion.AssertFalse;
import org.wso2.ballerina.core.nativeimpl.lang.assertion.AssertTrue;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnInt;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;


public class Executor {
    private static final String funcToTest = "getString";
    private BallerinaFile bFile;

    public void executeTest(){
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new PrintlnInt());
        FunctionUtils.addNativeFunction(symScope, new PrintlnString());
        FunctionUtils.addNativeFunction(symScope, new AssertTrue());
        FunctionUtils.addNativeFunction(symScope,  new AssertFalse());
        //BuiltInNativeConstructLoader.loadConstructs();
        // SymScope globalSymScope = GlobalScopeHolder.getInstance().getScope();
        //bFile = ParserUtils.parseBalFile(File.separator+"home"+File.separator+"nirodha"+File.separator+"wso2"+File.separator+"bal"+File.separator+"BallerinaEcho"+File.separator+"Echo.bal", globalSymScope);
        bFile = ParserUtils.parseBalFile("testerina.resources/Echo.bal", symScope);
        Function[] functions = bFile.getFunctions();
        for(int i=0; i < functions.length; i++){
            String name = functions[i].getFunctionName();
            if(name.startsWith("test")){
                System.out.println(name);
            }

        }
        //bFile = ParserUtils.parseBalFile("/home/nirodha/wso2/bal/BallerinaEcho/Echo.bal", symScope, true);

        BValue[] args = {};
        BValue[] returns = {};
        try {
            returns = Functions.invoke(bFile, funcToTest, args);
        } catch (AssertionException e){
            System.out.println(e.getMessage());
        }

        //Functions.getFunction()
        //String response = returns[0].stringValue();
        //System.out.println(response);
    }
}
