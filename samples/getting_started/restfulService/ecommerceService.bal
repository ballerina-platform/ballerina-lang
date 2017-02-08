import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.json;

@BasePath ("/ecommerceservice")
service Ecommerce {

    http:HTTPConnector productsService = create http:HTTPConnector("http://localhost:9090");


    @GET
    @Path ("/products/{productId}")
    resource productsInfo (message m, @PathParam("productId") string prodId)  {
        string reqPath = "/productsservice/" + prodId;
        message response = http:HTTPConnector.get(productsService, reqPath, m);
        reply response;
    }

    @POST
    @Path ("/products")
    resource productMgt (message m) {
        message response = http:HTTPConnector.post(productsService, "/productsservice", m);
        reply response;
    }


    @GET
    @Path ("/orders")
    resource ordersInfo (message m) {
        http:HTTPConnector productsService = create http:HTTPConnector("http://localhost:9090");
        message response = http:HTTPConnector.get(productsService, "/orderservice", m);
        reply response;
    }

    @POST
    @Path ("/orders")
    resource ordersMgt (message m) {
        http:HTTPConnector productsService = create http:HTTPConnector("http://localhost:9090");
        message response = http:HTTPConnector.post(productsService, "/orderservice", m);
        reply response;
    }

    @GET
    @Path ("/customers")
    resource customersInfo (message m) {
        http:HTTPConnector productsService = create http:HTTPConnector("http://localhost:9090");
        message response = http:HTTPConnector.get(productsService, "/customerservice", m);
        reply response;
    }

    @POST
    @Path ("/customers")
    resource customerMgt (message m) {
        http:HTTPConnector productsService = create http:HTTPConnector("http://localhost:9090");
        message response = http:HTTPConnector.post(productsService, "/customerservice", m);
        reply response;
    }
}

@BasePath("/productsservice")
service productmgt {

    map productsMap = {};

    boolean isInit;

    @GET
    @Path ("/{id}")
    resource product (message m, @PathParam("id") string prodId) {
        if (!isInit) {
            isInit = true;
            populateSampleProducts(productsMap);
        }

        json payload = productsMap[prodId];
        // ToDo : Fix for non-existing products

        message response = {};
        message:setJsonPayload(response, payload);
        reply response;
    }

    @POST
    @Path ("/")
    resource product (message m) {
        json jsonReq = message:getJsonPayload(m);

        string productId = json:getString(jsonReq, "$.Product.ID");
        productsMap[productId] = jsonReq;

        json payload = `{"Status":"Product is successfully added."}`;
        message response = {};
        message:setJsonPayload(response, payload);
        reply response;
    }

}


function populateSampleProducts(map productsMap) {
    json prod_1 = `{"Product": {"ID": "123000", "Name": "ABC_1","Description": "Sample product."}}`;
    json prod_2 = `{"Product": {"ID": "123001", "Name": "ABC_2","Description": "Sample product."}}`;
    json prod_3 = `{"Product": {"ID": "123002", "Name": "ABC_3","Description": "Sample product."}}`;

    productsMap["123000"]= prod_1;
    productsMap["123001"]= prod_2;
    productsMap["123002"]= prod_3;
    system:println("Sample products are added.");

}

@BasePath("/orderservice")
service OrderMgtService {

    @GET
    @POST
    resource orders (message m) {
        json payload = {};

        string httpMethod = http:getMethod(m);

        if ( string:equalsIgnoreCase(httpMethod, "GET") ) {
            payload = `{"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}}`;
        } else {
            payload = `{"Status":"Order is successfully added."}`;
        }

        message response = {};
        message:setJsonPayload(response, payload);
        reply response;
    }
}

@BasePath("/customerservice")
service CustomerMgtService {

    @GET
    @POST
    resource customers (message m) {
        json payload = {};

        string httpMethod = http:getMethod(m);

        if ( string:equalsIgnoreCase(httpMethod, "GET") ) {
             payload = `{"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}}`;
        } else {
            payload = `{"Status":"Customer is successfully added."}`;
        }

        message response = {};
        message:setJsonPayload(response, payload);
        reply response;
    }
}
