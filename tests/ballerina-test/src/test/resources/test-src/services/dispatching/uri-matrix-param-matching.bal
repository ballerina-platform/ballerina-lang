import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint testEP {
    port:9090
};

@http:ServiceConfig
service<http:Service> testService bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/t1/{person}/bar/{year}/foo"
    }
     test1 (endpoint client, http:Request req, string person, string year) {
        http:Response res = {};
        json outJson = {};
        outJson.pathParams = string `{{person}}, {{year}}`;

        map personMParams = req.getMatrixParams(string `/hello/t1/{{person}}`);
        var age =? <string> personMParams["age"];
        var color =? <string> personMParams["color"];
        outJson.personMatrix = string `age={{age}};color={{color}}`;

        map yearMParams = req.getMatrixParams(string `/hello/t1/{{person}}/bar/{{year}}`);
        var month =? <string> yearMParams["month"];
        var day =? <string> yearMParams["day"];
        outJson.yearMatrix = string `month={{month}};day={{day}}`;

        map fooMParams = req.getMatrixParams(string `/hello/t1/{{person}}/bar/{{year}}/foo`);
        var a =? <string> fooMParams["a"];
        var b =? <string> fooMParams["b"];
        outJson.fooMatrix = string `a={{a}};b={{b}}`;

        map queryParams = req.getQueryParams();
        var x =? <string> queryParams["x"];
        var y =? <string> queryParams["y"];
        outJson.queryParams = string `x={{x}}&y={{y}}`;

        res.setJsonPayload(outJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/t2/{person}/foo;a=5;b=10"
    }
     testEncoded (endpoint client, http:Request req, string person) {
        http:Response res = {};
        json outJson = {};
        outJson.person = person;

        map personMParams = req.getMatrixParams(string `/hello/t2/{{person}}`);
        outJson.personParamSize = lengthof personMParams;

        map fooMParams = req.getMatrixParams(string `/hello/t2/{{person}}/foo`);
        outJson.fooParamSize = lengthof fooMParams;

        res.setJsonPayload(outJson);
        _ = client -> respond(res);
    }
}
