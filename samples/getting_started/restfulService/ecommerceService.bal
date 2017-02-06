import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.json;

@BasePath ("/ecommerceservice")
service Ecommerce {

    http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");


    @GET
    @Path ("/products/{productId}")
    resource productsInfo (message m, @PathParam("productId") string prodId)  {
        message response;
        string reqPath;

        reqPath = "/productsservice/" + prodId;
        response = http:HTTPConnector.get(productsService, reqPath, m);
        reply response;
    }

    @POST
    @Path ("/products")
    resource productMgt (message m) {
        message response;
        response = http:HTTPConnector.post(productsService, "/productsservice", m);
        reply response;
    }


    @GET
    @Path ("/orders")
    resource ordersInfo (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.get(productsService, "/orderservice", m);
        reply response;
    }

    @POST
    @Path ("/orders")
    resource ordersMgt (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.post(productsService, "/orderservice", m);
        reply response;
    }

    @GET
    @Path ("/customers")
    resource customersInfo (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.get(productsService, "/customerservice", m);
        reply response;
    }

    @POST
    @Path ("/customers")
    resource customerMgt (message m) {
        http:HTTPConnector productsService = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.post(productsService, "/customerservice", m);
        reply response;
    }
}

@BasePath("/productsservice")
service productmgt {

    map productsMap;

    boolean isInit;

    @GET
    @Path ("/{id}")
    resource product (message m, @PathParam("id") string prodId) {
        message response;
        json payload;
        string httpMethod;

        if (!isInit) {
            isInit = true;
            populateSampleProducts(productsMap);
        }

        payload = productsMap[prodId];
        // ToDo : Fix for non-existing products

        message:setJsonPayload(response, payload);
        reply response;
    }

    @POST
    @Path ("/")
    resource product (message m) {
        message response;
        json jsonReq;
        json payload;
        string httpMethod;
        string productId;

        jsonReq = message:getJsonPayload(m);

        productId = json:getString(jsonReq, "$.Product.ID");
        productsMap[productId]= jsonReq;

        payload = `{"Status":"Product is successfully added."}`;

        message:setJsonPayload(response, payload);
        reply response;
    }

}


function populateSampleProducts(map productsMap) {
    json prod_1;
    json prod_2;
    json prod_3;

    prod_1 = `{"Product": {"ID": "123000", "Name": "ABC_1","Description": "Sample product."}}`;
    prod_2 = `{"Product": {"ID": "123001", "Name": "ABC_2","Description": "Sample product."}}`;
    prod_3 = `{"Product": {"ID": "123002", "Name": "ABC_3","Description": "Sample product."}}`;

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
