package lang.annotations.foo;

import lang.annotations.doc;

@doc:Description{value:"This is a test function",
                 paramValue:@doc:Param{},
                 queryParamValue:[@doc:QueryParam{
                    name:"paramName", 
                    value:"paramValue"}],
                 queryParamValue2:[@doc:QueryParam{}],
                 code:[1,2,5],
                 args: @doc:Args{}}
@Args{value:"args: input parameter"}
function foo (@Args{} string args) {
    // do nothing
}

annotation Args {
    string value;
}