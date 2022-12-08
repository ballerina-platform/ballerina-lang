import ballerina/grpc;

public isolated client class CartServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function AddItem(AddItemRequest|ContextAddItemRequest req) returns Empty|grpc:Error {
        map<string|string[]> headers = {};
        AddItemRequest message;
        if req is ContextAddItemRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CartService/AddItem", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Empty>result;
    }

    isolated remote function AddItemContext(AddItemRequest|ContextAddItemRequest req) returns ContextEmpty|grpc:Error {
        map<string|string[]> headers = {};
        AddItemRequest message;
        if req is ContextAddItemRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CartService/AddItem", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Empty>result, headers: respHeaders};
    }

    isolated remote function GetCart(GetCartRequest|ContextGetCartRequest req) returns Cart|grpc:Error {
        map<string|string[]> headers = {};
        GetCartRequest message;
        if req is ContextGetCartRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CartService/GetCart", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Cart>result;
    }

    isolated remote function GetCartContext(GetCartRequest|ContextGetCartRequest req) returns ContextCart|grpc:Error {
        map<string|string[]> headers = {};
        GetCartRequest message;
        if req is ContextGetCartRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CartService/GetCart", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Cart>result, headers: respHeaders};
    }

    isolated remote function EmptyCart(EmptyCartRequest|ContextEmptyCartRequest req) returns Empty|grpc:Error {
        map<string|string[]> headers = {};
        EmptyCartRequest message;
        if req is ContextEmptyCartRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CartService/EmptyCart", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Empty>result;
    }

    isolated remote function EmptyCartContext(EmptyCartRequest|ContextEmptyCartRequest req) returns ContextEmpty|grpc:Error {
        map<string|string[]> headers = {};
        EmptyCartRequest message;
        if req is ContextEmptyCartRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CartService/EmptyCart", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Empty>result, headers: respHeaders};
    }
}

public isolated client class ShippingServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function GetQuote(GetQuoteRequest|ContextGetQuoteRequest req) returns GetQuoteResponse|grpc:Error {
        map<string|string[]> headers = {};
        GetQuoteRequest message;
        if req is ContextGetQuoteRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ShippingService/GetQuote", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <GetQuoteResponse>result;
    }

    isolated remote function GetQuoteContext(GetQuoteRequest|ContextGetQuoteRequest req) returns ContextGetQuoteResponse|grpc:Error {
        map<string|string[]> headers = {};
        GetQuoteRequest message;
        if req is ContextGetQuoteRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ShippingService/GetQuote", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <GetQuoteResponse>result, headers: respHeaders};
    }

    isolated remote function ShipOrder(ShipOrderRequest|ContextShipOrderRequest req) returns ShipOrderResponse|grpc:Error {
        map<string|string[]> headers = {};
        ShipOrderRequest message;
        if req is ContextShipOrderRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ShippingService/ShipOrder", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <ShipOrderResponse>result;
    }

    isolated remote function ShipOrderContext(ShipOrderRequest|ContextShipOrderRequest req) returns ContextShipOrderResponse|grpc:Error {
        map<string|string[]> headers = {};
        ShipOrderRequest message;
        if req is ContextShipOrderRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ShippingService/ShipOrder", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <ShipOrderResponse>result, headers: respHeaders};
    }
}

public isolated client class ProductCatalogServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function ListProducts(Empty|ContextEmpty req) returns ListProductsResponse|grpc:Error {
        map<string|string[]> headers = {};
        Empty message;
        if req is ContextEmpty {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ProductCatalogService/ListProducts", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <ListProductsResponse>result;
    }

    isolated remote function ListProductsContext(Empty|ContextEmpty req) returns ContextListProductsResponse|grpc:Error {
        map<string|string[]> headers = {};
        Empty message;
        if req is ContextEmpty {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ProductCatalogService/ListProducts", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <ListProductsResponse>result, headers: respHeaders};
    }

    isolated remote function GetProduct(GetProductRequest|ContextGetProductRequest req) returns Product|grpc:Error {
        map<string|string[]> headers = {};
        GetProductRequest message;
        if req is ContextGetProductRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ProductCatalogService/GetProduct", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Product>result;
    }

    isolated remote function GetProductContext(GetProductRequest|ContextGetProductRequest req) returns ContextProduct|grpc:Error {
        map<string|string[]> headers = {};
        GetProductRequest message;
        if req is ContextGetProductRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ProductCatalogService/GetProduct", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Product>result, headers: respHeaders};
    }

    isolated remote function SearchProducts(SearchProductsRequest|ContextSearchProductsRequest req) returns SearchProductsResponse|grpc:Error {
        map<string|string[]> headers = {};
        SearchProductsRequest message;
        if req is ContextSearchProductsRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ProductCatalogService/SearchProducts", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <SearchProductsResponse>result;
    }

    isolated remote function SearchProductsContext(SearchProductsRequest|ContextSearchProductsRequest req) returns ContextSearchProductsResponse|grpc:Error {
        map<string|string[]> headers = {};
        SearchProductsRequest message;
        if req is ContextSearchProductsRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.ProductCatalogService/SearchProducts", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <SearchProductsResponse>result, headers: respHeaders};
    }
}

public isolated client class CurrencyServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function GetSupportedCurrencies(Empty|ContextEmpty req) returns GetSupportedCurrenciesResponse|grpc:Error {
        map<string|string[]> headers = {};
        Empty message;
        if req is ContextEmpty {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CurrencyService/GetSupportedCurrencies", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <GetSupportedCurrenciesResponse>result;
    }

    isolated remote function GetSupportedCurrenciesContext(Empty|ContextEmpty req) returns ContextGetSupportedCurrenciesResponse|grpc:Error {
        map<string|string[]> headers = {};
        Empty message;
        if req is ContextEmpty {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CurrencyService/GetSupportedCurrencies", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <GetSupportedCurrenciesResponse>result, headers: respHeaders};
    }

    isolated remote function Convert(CurrencyConversionRequest|ContextCurrencyConversionRequest req) returns Money|grpc:Error {
        map<string|string[]> headers = {};
        CurrencyConversionRequest message;
        if req is ContextCurrencyConversionRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CurrencyService/Convert", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Money>result;
    }

    isolated remote function ConvertContext(CurrencyConversionRequest|ContextCurrencyConversionRequest req) returns ContextMoney|grpc:Error {
        map<string|string[]> headers = {};
        CurrencyConversionRequest message;
        if req is ContextCurrencyConversionRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CurrencyService/Convert", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Money>result, headers: respHeaders};
    }
}

public isolated client class PaymentServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function Charge(ChargeRequest|ContextChargeRequest req) returns ChargeResponse|grpc:Error {
        map<string|string[]> headers = {};
        ChargeRequest message;
        if req is ContextChargeRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.PaymentService/Charge", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <ChargeResponse>result;
    }

    isolated remote function ChargeContext(ChargeRequest|ContextChargeRequest req) returns ContextChargeResponse|grpc:Error {
        map<string|string[]> headers = {};
        ChargeRequest message;
        if req is ContextChargeRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.PaymentService/Charge", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <ChargeResponse>result, headers: respHeaders};
    }
}

public isolated client class EmailServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function SendOrderConfirmation(SendOrderConfirmationRequest|ContextSendOrderConfirmationRequest req) returns Empty|grpc:Error {
        map<string|string[]> headers = {};
        SendOrderConfirmationRequest message;
        if req is ContextSendOrderConfirmationRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.EmailService/SendOrderConfirmation", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <Empty>result;
    }

    isolated remote function SendOrderConfirmationContext(SendOrderConfirmationRequest|ContextSendOrderConfirmationRequest req) returns ContextEmpty|grpc:Error {
        map<string|string[]> headers = {};
        SendOrderConfirmationRequest message;
        if req is ContextSendOrderConfirmationRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.EmailService/SendOrderConfirmation", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <Empty>result, headers: respHeaders};
    }
}

public type ContextEmptyCartRequest record {|
    EmptyCartRequest content;
    map<string|string[]> headers;
|};

public type ContextAddItemRequest record {|
    AddItemRequest content;
    map<string|string[]> headers;
|};

public type ContextEmpty record {|
    Empty content;
    map<string|string[]> headers;
|};

public type ContextGetCartRequest record {|
    GetCartRequest content;
    map<string|string[]> headers;
|};

public type ContextCart record {|
    Cart content;
    map<string|string[]> headers;
|};

public type ContextListRecommendationsResponse record {|
    ListRecommendationsResponse content;
    map<string|string[]> headers;
|};

public type ContextListRecommendationsRequest record {|
    ListRecommendationsRequest content;
    map<string|string[]> headers;
|};

public type ContextSearchProductsRequest record {|
    SearchProductsRequest content;
    map<string|string[]> headers;
|};

public type ContextSearchProductsResponse record {|
    SearchProductsResponse content;
    map<string|string[]> headers;
|};

public type ContextGetProductRequest record {|
    GetProductRequest content;
    map<string|string[]> headers;
|};

public type ContextListProductsResponse record {|
    ListProductsResponse content;
    map<string|string[]> headers;
|};

public type ContextProduct record {|
    Product content;
    map<string|string[]> headers;
|};

public type ContextShipOrderRequest record {|
    ShipOrderRequest content;
    map<string|string[]> headers;
|};

public type ContextShipOrderResponse record {|
    ShipOrderResponse content;
    map<string|string[]> headers;
|};

public type ContextGetQuoteRequest record {|
    GetQuoteRequest content;
    map<string|string[]> headers;
|};

public type ContextGetQuoteResponse record {|
    GetQuoteResponse content;
    map<string|string[]> headers;
|};

public type ContextMoney record {|
    Money content;
    map<string|string[]> headers;
|};

public type ContextCurrencyConversionRequest record {|
    CurrencyConversionRequest content;
    map<string|string[]> headers;
|};

public type ContextGetSupportedCurrenciesResponse record {|
    GetSupportedCurrenciesResponse content;
    map<string|string[]> headers;
|};

public type ContextChargeRequest record {|
    ChargeRequest content;
    map<string|string[]> headers;
|};

public type ContextChargeResponse record {|
    ChargeResponse content;
    map<string|string[]> headers;
|};

public type ContextSendOrderConfirmationRequest record {|
    SendOrderConfirmationRequest content;
    map<string|string[]> headers;
|};

public type ContextPlaceOrderRequest record {|
    PlaceOrderRequest content;
    map<string|string[]> headers;
|};

public type ContextPlaceOrderResponse record {|
    PlaceOrderResponse content;
    map<string|string[]> headers;
|};

public type ContextAdRequest record {|
    AdRequest content;
    map<string|string[]> headers;
|};

public type ContextAdResponse record {|
    AdResponse content;
    map<string|string[]> headers;
|};

public type ShipOrderRequest record {|
    Address address = {};
    CartItem[] items = [];
|};

public type SearchProductsResponse record {|
    Product[] results = [];
|};

public type Ad record {|
    string redirect_url = "";
    string text = "";
|};

public type Address record {|
    string street_address = "";
    string city = "";
    string state = "";
    string country = "";
    int zip_code = 0;
|};

public type GetProductRequest record {|
    string id = "";
|};

public type Product record {|
    string id = "";
    string name = "";
    string description = "";
    string picture = "";
    Money price_usd = {};
    string[] categories = [];
|};

public type CartItem record {|
    string product_id = "";
    int quantity = 0;
|};

public type SendOrderConfirmationRequest record {|
    string email = "";
    OrderResult 'order = {};
|};

public type PlaceOrderResponse record {|
    OrderResult 'order = {};
|};

public type Money record {|
    string currency_code = "";
    int units = 0;
    int nanos = 0;
|};

public type AdRequest record {|
    string[] context_keys = [];
|};

public type GetCartRequest record {|
    string user_id = "";
|};

public type AdResponse record {|
    Ad[] ads = [];
|};

public type CreditCardInfo record {|
    string credit_card_number = "";
    int credit_card_cvv = 0;
    int credit_card_expiration_year = 0;
    int credit_card_expiration_month = 0;
|};

public type ListRecommendationsRequest record {|
    string user_id = "";
    string[] product_ids = [];
|};

public type Cart record {|
    string user_id = "";
    CartItem[] items = [];
|};

public type ChargeRequest record {|
    Money amount = {};
    CreditCardInfo credit_card = {};
|};

public type GetQuoteRequest record {|
    Address address = {};
    CartItem[] items = [];
|};

public type CurrencyConversionRequest record {|
    Money 'from = {};
    string to_code = "";
|};

public type ListProductsResponse record {|
    Product[] products = [];
|};

public type GetSupportedCurrenciesResponse record {|
    string[] currency_codes = [];
|};

public type GetQuoteResponse record {|
    Money cost_usd = {};
|};

public type ListRecommendationsResponse record {|
    string[] product_ids = [];
|};

public type EmptyCartRequest record {|
    string user_id = "";
|};

public type AddItemRequest record {|
    string user_id = "";
    CartItem item = {};
|};

public type SearchProductsRequest record {|
    string query = "";
|};

public type ShipOrderResponse record {|
    string tracking_id = "";
|};

public type OrderItem record {|
    CartItem item = {};
    Money cost = {};
|};

public type PlaceOrderRequest record {|
    string user_id = "";
    string user_currency = "";
    Address address = {};
    string email = "";
    CreditCardInfo credit_card = {};
|};

public type OrderResult record {|
    string order_id = "";
    string shipping_tracking_id = "";
    Money shipping_cost = {};
    Address shipping_address = {};
    OrderItem[] items = [];
|};

public type ChargeResponse record {|
    string transaction_id = "";
|};

public type Empty record {|
|};

const string ROOT_DESCRIPTOR_DEMO = "<demo>";

public isolated function getDescriptorMapDemo() returns map<string> {
    return {"demo.proto": "<demo.proto>"};
}

