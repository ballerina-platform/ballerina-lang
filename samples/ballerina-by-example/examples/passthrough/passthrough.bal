import ballerina.net.http;
import ballerina.doc;

@doc:Description {value:"Since the basepath is not specified, it's value will be '/passthrough' which is the name of the service."}
service<http> passthrough {

    @http:Path {value:"/"}
    resource passthrough (message m) {
        http:ClientConnector endPoint = create http:ClientConnector(
                                        "http://samples.openweathermap.org");
        string method = http:getMethod(m);
        //Action execute() returns the response from backend service. It includes endPoint, HTTP method, resource path and message as parameters.
        message response = http:ClientConnector.execute(endPoint, method
				, "/data/2.5/weather?lat=35&lon=139&appid=b1b1", m);
        reply response;

    }

}

