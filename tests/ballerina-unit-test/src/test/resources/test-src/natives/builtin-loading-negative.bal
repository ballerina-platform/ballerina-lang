import ballerina/builtin;
import ballerina/math;

function main(string... args){
    var v = math:pow(2, 2);
    builtin:print(v);
}
