import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;

@BasePath ("/ecommerceservice")
service Ecommerce {

    @GET
    @Path ("/products")
    resource passthrough (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.get(productsService, "/productsservice", m);
        reply response;
    }

    @POST
    @Path ("/products")
    resource passthrough (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.post(productsService, "/productsservice", m);
        reply response;
    }


    @GET
    @Path ("/orders")
    resource passthrough (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.get(productsService, "/orderservice", m);
        reply response;
    }

    @POST
    @Path ("/orders")
    resource passthrough (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.post(productsService, "/orderservice", m);
        reply response;
    }

    @GET
    @Path ("/customers")
    resource passthrough (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.get(productsService, "/customerservice", m);
        reply response;
    }

    @POST
    @Path ("/customers")
    resource passthrough (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.post(productsService, "/customerservice", m);
        reply response;
    }
}

@BasePath("/productsservice")
service productmgt {

    @GET
    @POST
    resource product (message m) {
        message response;
        json payload;
        string httpMethod;

        httpMethod = http:getMethod(m);
        if ( string:equalsIgnoreCase(httpMethod, "GET") ) {
             payload = `{"Product": {"ID": "123456", "Name": "XYZ","Description": "Sample product."}}`;
        } else {
            payload = `{"Status":"Product is successfully added."}`;
        }
        message:setJsonPayload(response, payload);
        reply response;
    }
}

@BasePath("/orderservice")
service OrderMgtService {

    @GET
    @POST
    resource orders (message m) {
        message response;
        json payload;
        string httpMethod;

        httpMethod = http:getMethod(m);
        if ( string:equalsIgnoreCase(httpMethod, "GET") ) {
             payload = `{"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}}`;
        } else {
            payload = `{"Status":"Order is successfully added."}`;
        }

        message:setJsonPayload(response, payload);
        reply response;
    }
}

@BasePath("/customerservice")
service CustomerMgtService {

    @GET
    @POST
    resource customers (message m) {
        message response;
        json payload;
        string httpMethod;

        httpMethod = http:getMethod(m);
        if ( string:equalsIgnoreCase(httpMethod, "GET") ) {
             payload = `{"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}}`;
        } else {
            payload = `{"Status":"Customer is successfully added."}`;
        }

        message:setJsonPayload(response, payload);
        reply response;
    }
}
