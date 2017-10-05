package lang.annotations.foo;

import lang.annotations.doc;
import ballerina.net.http;

@doc:Description{value:"Constant holding the name of the current ballerina program"}
const string programName = "TestAnnotations";

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