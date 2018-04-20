import ballerina/http;
import ballerina/http;

endpoint http:NonListener testEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> testService bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/t1/{person}/bar/{yearParam}/foo"
    }
     test1 (endpoint caller, http:Request req, string person, string yearParam) {
        http:Response res = new;
        json outJson = {};
        outJson.pathParams = string `{{person}}, {{yearParam}}`;

        map personMParams = req.getMatrixParams(string `/hello/t1/{{person}}`);
        string age = <string> personMParams["age"];
        string color = <string> personMParams["color"];
        outJson.personMatrix = string `age={{age}};color={{color}}`;

        map yearMParams = req.getMatrixParams(string `/hello/t1/{{person}}/bar/{{yearParam}}`);
        string monthValue = <string> yearMParams["month"];
        string dayValue = <string> yearMParams["day"];
        outJson.yearMatrix = string `month={{monthValue}};day={{dayValue}}`;

        map fooMParams = req.getMatrixParams(string `/hello/t1/{{person}}/bar/{{yearParam}}/foo`);
        string a = <string> fooMParams["a"];
        string b = <string> fooMParams["b"];
        outJson.fooMatrix = string `a={{a}};b={{b}}`;

        map<string> queryParams = req.getQueryParams();
        string x = queryParams["x"];
        string y = queryParams["y"];
        outJson.queryParams = string `x={{x}}&y={{y}}`;

        res.setJsonPayload(outJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/t2/{person}/foo;a=5;b=10"
    }
     testEncoded (endpoint caller, http:Request req, string person) {
        http:Response res = new;
        json outJson = {};
        outJson.person = person;

        map personMParams = req.getMatrixParams(string `/hello/t2/{{person}}`);
        outJson.personParamSize = lengthof personMParams;

        map fooMParams = req.getMatrixParams(string `/hello/t2/{{person}}/foo`);
        outJson.fooParamSize = lengthof fooMParams;

        res.setJsonPayload(outJson);
        _ = caller -> respond(res);
    }
}
