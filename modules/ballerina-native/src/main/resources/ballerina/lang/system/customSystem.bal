package ballerina.lang.system;

import ballerina.lang.json;
import ballerina.lang.type;

function customFunction1() {
	return;
}

function customFunction2() {
	type:testFunctionInTypePkg();
	return;
}

function customFunction3(json j) (json){
  json:set(j, "$/wrong/path", "Menu1");
  return j;
} 