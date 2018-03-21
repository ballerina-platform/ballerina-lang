import ballerina/test.echo;

function invokeNativeFunction(string s1) (string){
    return echo:echoString(s1);
}