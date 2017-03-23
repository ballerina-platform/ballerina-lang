package lang.annotations;

import lang.annotations.doc;

@doc:Description{value:"This is a test function",
                 queryParamValue:@doc:QueryParam{
                    name:"paramName", 
                    value:"paramValue"},
                 queryParamValue2:@doc:QueryParam{},
                 code:[1,2,5]}
@doc:Param{value:"args: input parameter"}
function foo (@Args{} string args) {
    // do nothing
}

annotation Args attach parameter {
    string value;
}