
function testSimpleWorker() {
    message result;
    //message msg = {};;
    int x = 100;
    float y;
    undefined,undefined -> sampleWorker;
    system:println("Worker calling function test started");
    undefined,undefined <- sampleWorker;
    string s = messages:getStringPayload(undefined);
    system:println(undefined);
    system:println("Value received from worker is " + undefined);


    return undefined;
    worker sampleWorker {
        message result;
        message m;
        int a;
        float b = 12.34;
        undefined,undefined <- default;
        system:println("passed in value is " + undefined);
        json j;
        undefined = `{"name":"chanaka"}`;
        messages:setJsonPayload(undefined, undefined);
        undefined,undefined -> default;
}
}
