
import lang.annotations.doc1 as doc;
import ballerina/http;

@doc:Description{value:"Constant holding the name of the current ballerina program"}
@final string programName = "TestAnnotations";

@doc:Description{value:"This is a test function",
                 paramValue:@doc:Param{},
                 queryParamValue:[
                    @doc:QueryParam{name:"paramName", value:"paramValue"}, 
                    @doc:QueryParam{name:"paramName2", value:"paramValue2"},
                    @doc:QueryParam{name:"paramName3", value:"paramValue3"}],
                 queryParamValue2:[@doc:QueryParam{}],
                 code:[1,2,5],
                 args: @doc:Args{}}
@Args{value:"test @Args annotation"}
function foo (@Args{value:"args: input parameter : type string"} string args) {
    // do nothing
}

@doc:Description{value:"This is a test annotation"}
annotation Args {
    string value;
}

@doc:Description{value:"Pizza service"}
service<http> PizzaService {

    @doc:Description{value:"Order pizza"}
    resource orderPizza(@Args{value:"input parameter for oderPizza resource"} string s) {
    }

    @doc:Description{value:"Check order status"}
    resource checkStatus(string s) {
    }

}

@doc:Description{value:"Test connector"}
connector TestConnector (string url) {
    
    @doc:Description{value:"Test action of test connector"}
    action testAction() (http:Response) {
        http:Response m = {};
        return m;
    }
}

@doc:Description{value:"User defined struct : Person"}
struct Person {
    string name;
    int age;
}

@doc:Description{value:"User defined transformer: from string to Person"}
transformer <string s, Person p> Bar(int i) {
    p.name = s;
}

@Args{value:"test @Args annotation with named return"}
function namedReturnAnnotationTest (string args) (@Args{value:"named return : type string"} string returnValue) {
    returnValue = args;
    return;
}

@Args{value:"test @Args annotation with return"}
function returnAnnotationTest (string args) (@Args{value:"return : type string"} string) {
    return args;
}

@Args{value:"test @Args annotation with multiple returns"}
function multiReturnAnnotationTest (int args1, string args2) (@Args{value:"return : type string"} string,
                                                         @Args{value:"return : type int"} int) {
    return args2, args1;
}