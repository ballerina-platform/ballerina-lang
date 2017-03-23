@Description {value:"Overridden description of the service/function",
              queryParamValue:@QueryParam{
                    name:"paramName", 
                    value:"paramValue"},
              queryParamValue2:@QueryParam2{
                    name:"paramName2", 
                    value:"paramValue2"},
              code:[1,2,5]}
@Doc {docValue:"2nd Overridden description of the service/function",
              docParamValue:@DocParam{
                    name:"paramName3", 
                    value:"paramValue3"}}
function foo (@Args{} string args) {
    // do nothing
}

annotation Description attach service, function {
    string value = "Description of the service/function";
    int[] code;
    Param paramValue;
    QueryParam[] queryParamValue;
    QueryParam[] queryParamValue2;
    string[] paramValue2;
}

annotation Param attach service, function, connector {
    string j = "Description of the input argument";
}

annotation QueryParam attach service {
    string name;
    string value;
}

annotation Doc attach service, function, connector {
    Description des;
}
