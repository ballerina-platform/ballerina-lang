import ballerina/http;
import ballerina/doc;

@doc:Description {value:"Since the basepath is not specified, it's value will be '/passthrough' which is the name of the service."}
service<http> passthrough {
    @http:GET {}
    @http:Path {value:"/"}
    resource passthrough (message m) {
        http:ClientConnector endPoint = create http:ClientConnector(
                                        "http://samples.openweathermap.org");

        //Action get() returns the response from backend service. It includes endPoint, resource path and message as parameters.
        message response = http:ClientConnector.get(endPoint, "/data/2.5/weather?lat=35&lon=139&appid=b1b1", m);
        reply response;

    }

}

