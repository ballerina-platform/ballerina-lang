import ballerina.doc;

@doc:Description{value:"Adds two given values and returns answer"}
@doc:Param{value:"value1: double input value"}
@doc:Param{value:"value2: double input value"}
@doc:Return{value:"result: Result of the execution"}
function addValue(float value1, float value2)(float result){
    result = value1+ value2;
    return;
}

@doc:Description{value:"Performs tasks on given values"}
@doc:Param{value:"value1: double input value"}
@doc:Param{value:"value2: double input value"}
@doc:Param{value:"originalList: map value before"}
@doc:Param{value:"changeList: map value after"}
@doc:Return{value:"changedList: result of execution"}
@doc:Return{value:"status: status of execution"}
function addValue(float value1, float value2, map originalList, map changeList)(map changedList, boolean status){
    return;
}