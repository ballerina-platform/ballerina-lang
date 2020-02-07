import ballerina/http;

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {
    basePath:"/hello"
}
service testService on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/t1/{person}/bar/{yearParam}/foo"
    }
    resource function test1(http:Caller caller, http:Request req, string person, string yearParam) {
        http:Response res = new;
        map<json> outJson = {};
        outJson["pathParams"] = string `${person}, ${yearParam}`;

        map<any> personMParams = req.getMatrixParams(string `/hello/t1/${person}`);
        string age = <string> personMParams["age"];
        string color = <string> personMParams["color"];
        outJson["personMatrix"] = string `age=${age};color=${color}`;

        map<any> yearMParams = req.getMatrixParams(string `/hello/t1/${person}/bar/${yearParam}`);
        string monthValue = <string> yearMParams["month"];
        string dayValue = <string> yearMParams["day"];
        outJson["yearMatrix"] = string `month=${monthValue};day=${dayValue}`;

        map<any> fooMParams = req.getMatrixParams(string `/hello/t1/${person}/bar/${yearParam}/foo`);
        string a = <string> fooMParams["a"];
        string b = <string> fooMParams["b"];
        outJson["fooMatrix"] = string `a=${a};b=${b}`;

        map<string[]> queryParams = req.getQueryParams();
        string[]? x = queryParams["x"];
        string[]? y = queryParams["y"];
        string xVal = x is string[] ? x[0] : "";
        string yVal = y is string[] ? y[0] : "";
        outJson["queryParams"] = string `x=${xVal}&y=${yVal}`;

        res.setJsonPayload(<@untainted json> outJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/t2/{person}/foo%3Ba%3D5%3Bb%3D10"
    }
    resource function testEncoded(http:Caller caller, http:Request req, string person) {
        http:Response res = new;
        map<json> outJson = {};
        outJson["person"] = person;

        map<any> personMParams = req.getMatrixParams(string `/hello/t2/${person}`);
        outJson["personParamSize"] = personMParams.length();

        map<any> fooMParams = req.getMatrixParams(string `/hello/t2/${person}/foo`);
        outJson["fooParamSize"] = fooMParams.length();

        res.setJsonPayload(<@untainted json> outJson);
        checkpanic caller->respond(res);
    }
}
