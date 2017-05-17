function testSimpleWorker(message msg)(message) {
    message result;
    //message msg = {};
    int x = 100;
    float y;
    msg, x ->sampleWorker;
    system:println("Worker calling function test started");
    y, result <-sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    system:println("Value received from worker is " + y);
    return result;

    worker sampleWorker {
        message result;
        message m;
        int a;
        float b = 12.34;
        m, a <-default;
        system:println("passed in value is " + a);
        json j;
        j = `{"name":"chanaka"}`;
        messages:setJsonPayload(m, j);
        b, m ->default;
    }
}