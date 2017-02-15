package a.b;

@doc:Description("Adds two given values and returns answer")
@doc:Param("value1: double input value")
@doc:Param("value2: double input value")
@doc:Return("result: Result of the execution")
function addValue(double value1, double value2)(double result){
result = value1+ value2;
return;
}

@doc:Description("Performs tasks on given values")
@doc:Param("value1: double input value")
@doc:Param("value2: double input value")
@doc:Param("originalList: map value before")
@doc:Param("changeList: map value after")
@doc:Return("changedList: result of execution")
@doc:Return("status: status of execution")
function addValue(double value1, double value2, map originalList, map changeList)(map changedList, boolean status){
return;
}