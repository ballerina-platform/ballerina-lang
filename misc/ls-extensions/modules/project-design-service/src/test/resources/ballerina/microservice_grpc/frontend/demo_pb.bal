// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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

public isolated client class RecommendationServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function ListRecommendations(ListRecommendationsRequest|ContextListRecommendationsRequest req) returns ListRecommendationsResponse|grpc:Error {
        map<string|string[]> headers = {};
        ListRecommendationsRequest message;
        if req is ContextListRecommendationsRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.RecommendationService/ListRecommendations", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <ListRecommendationsResponse>result;
    }

    isolated remote function ListRecommendationsContext(ListRecommendationsRequest|ContextListRecommendationsRequest req) returns ContextListRecommendationsResponse|grpc:Error {
        map<string|string[]> headers = {};
        ListRecommendationsRequest message;
        if req is ContextListRecommendationsRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.RecommendationService/ListRecommendations", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <ListRecommendationsResponse>result, headers: respHeaders};
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

public isolated client class CheckoutServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function PlaceOrder(PlaceOrderRequest|ContextPlaceOrderRequest req) returns PlaceOrderResponse|grpc:Error {
        map<string|string[]> headers = {};
        PlaceOrderRequest message;
        if req is ContextPlaceOrderRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CheckoutService/PlaceOrder", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <PlaceOrderResponse>result;
    }

    isolated remote function PlaceOrderContext(PlaceOrderRequest|ContextPlaceOrderRequest req) returns ContextPlaceOrderResponse|grpc:Error {
        map<string|string[]> headers = {};
        PlaceOrderRequest message;
        if req is ContextPlaceOrderRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.CheckoutService/PlaceOrder", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <PlaceOrderResponse>result, headers: respHeaders};
    }
}

public isolated client class AdServiceClient {
    *grpc:AbstractClientEndpoint;

    private final grpc:Client grpcClient;

    public isolated function init(string url, *grpc:ClientConfiguration config) returns grpc:Error? {
        self.grpcClient = check new (url, config);
        check self.grpcClient.initStub(self, ROOT_DESCRIPTOR_DEMO, getDescriptorMapDemo());
    }

    isolated remote function GetAds(AdRequest|ContextAdRequest req) returns AdResponse|grpc:Error {
        map<string|string[]> headers = {};
        AdRequest message;
        if req is ContextAdRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.AdService/GetAds", message, headers);
        [anydata, map<string|string[]>] [result, _] = payload;
        return <AdResponse>result;
    }

    isolated remote function GetAdsContext(AdRequest|ContextAdRequest req) returns ContextAdResponse|grpc:Error {
        map<string|string[]> headers = {};
        AdRequest message;
        if req is ContextAdRequest {
            message = req.content;
            headers = req.headers;
        } else {
            message = req;
        }
        var payload = check self.grpcClient->executeSimpleRPC("hipstershop.AdService/GetAds", message, headers);
        [anydata, map<string|string[]>] [result, respHeaders] = payload;
        return {content: <AdResponse>result, headers: respHeaders};
    }
}

public client class CartServiceEmptyCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendEmpty(Empty response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextEmpty(ContextEmpty response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class CartServiceCartCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendCart(Cart response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextCart(ContextCart response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class RecommendationServiceListRecommendationsResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendListRecommendationsResponse(ListRecommendationsResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextListRecommendationsResponse(ContextListRecommendationsResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class ProductCatalogServiceListProductsResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendListProductsResponse(ListProductsResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextListProductsResponse(ContextListProductsResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class ProductCatalogServiceSearchProductsResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendSearchProductsResponse(SearchProductsResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextSearchProductsResponse(ContextSearchProductsResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class ProductCatalogServiceProductCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendProduct(Product response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextProduct(ContextProduct response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class ShippingServiceGetQuoteResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendGetQuoteResponse(GetQuoteResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextGetQuoteResponse(ContextGetQuoteResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class ShippingServiceShipOrderResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendShipOrderResponse(ShipOrderResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextShipOrderResponse(ContextShipOrderResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class CurrencyServiceGetSupportedCurrenciesResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendGetSupportedCurrenciesResponse(GetSupportedCurrenciesResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextGetSupportedCurrenciesResponse(ContextGetSupportedCurrenciesResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class CurrencyServiceMoneyCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendMoney(Money response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextMoney(ContextMoney response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class PaymentServiceChargeResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendChargeResponse(ChargeResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextChargeResponse(ContextChargeResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class EmailServiceEmptyCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendEmpty(Empty response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextEmpty(ContextEmpty response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class CheckoutServicePlaceOrderResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendPlaceOrderResponse(PlaceOrderResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextPlaceOrderResponse(ContextPlaceOrderResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
    }
}

public client class AdServiceAdResponseCaller {
    private grpc:Caller caller;

    public isolated function init(grpc:Caller caller) {
        self.caller = caller;
    }

    public isolated function getId() returns int {
        return self.caller.getId();
    }

    isolated remote function sendAdResponse(AdResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendContextAdResponse(ContextAdResponse response) returns grpc:Error? {
        return self.caller->send(response);
    }

    isolated remote function sendError(grpc:Error response) returns grpc:Error? {
        return self.caller->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.caller->complete();
    }

    public isolated function isCancelled() returns boolean {
        return self.caller.isCancelled();
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

const string ROOT_DESCRIPTOR_DEMO = "0A0A64656D6F2E70726F746F120B6869707374657273686F7022450A08436172744974656D121D0A0A70726F647563745F6964180120012809520970726F647563744964121A0A087175616E7469747918022001280552087175616E7469747922540A0E4164644974656D5265717565737412170A07757365725F6964180120012809520675736572496412290A046974656D18022001280B32152E6869707374657273686F702E436172744974656D52046974656D222B0A10456D707479436172745265717565737412170A07757365725F6964180120012809520675736572496422290A0E476574436172745265717565737412170A07757365725F69641801200128095206757365724964224C0A044361727412170A07757365725F69641801200128095206757365724964122B0A056974656D7318022003280B32152E6869707374657273686F702E436172744974656D52056974656D7322070A05456D70747922560A1A4C6973745265636F6D6D656E646174696F6E735265717565737412170A07757365725F69641801200128095206757365724964121F0A0B70726F647563745F696473180220032809520A70726F64756374496473223E0A1B4C6973745265636F6D6D656E646174696F6E73526573706F6E7365121F0A0B70726F647563745F696473180120032809520A70726F6475637449647322BA010A0750726F64756374120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512200A0B6465736372697074696F6E180320012809520B6465736372697074696F6E12180A0770696374757265180420012809520770696374757265122F0A0970726963655F75736418052001280B32122E6869707374657273686F702E4D6F6E657952087072696365557364121E0A0A63617465676F72696573180620032809520A63617465676F7269657322480A144C69737450726F6475637473526573706F6E736512300A0870726F647563747318012003280B32142E6869707374657273686F702E50726F64756374520870726F647563747322230A1147657450726F6475637452657175657374120E0A02696418012001280952026964222D0A1553656172636850726F64756374735265717565737412140A0571756572791801200128095205717565727922480A1653656172636850726F6475637473526573706F6E7365122E0A07726573756C747318012003280B32142E6869707374657273686F702E50726F647563745207726573756C7473226E0A0F47657451756F746552657175657374122E0A076164647265737318012001280B32142E6869707374657273686F702E41646472657373520761646472657373122B0A056974656D7318022003280B32152E6869707374657273686F702E436172744974656D52056974656D7322410A1047657451756F7465526573706F6E7365122D0A08636F73745F75736418012001280B32122E6869707374657273686F702E4D6F6E65795207636F7374557364226F0A10536869704F7264657252657175657374122E0A076164647265737318012001280B32142E6869707374657273686F702E41646472657373520761646472657373122B0A056974656D7318022003280B32152E6869707374657273686F702E436172744974656D52056974656D7322340A11536869704F72646572526573706F6E7365121F0A0B747261636B696E675F6964180120012809520A747261636B696E674964228F010A074164647265737312250A0E7374726565745F61646472657373180120012809520D7374726565744164647265737312120A046369747918022001280952046369747912140A0573746174651803200128095205737461746512180A07636F756E7472791804200128095207636F756E74727912190A087A69705F636F646518052001280552077A6970436F646522580A054D6F6E657912230A0D63757272656E63795F636F6465180120012809520C63757272656E6379436F646512140A05756E6974731802200128035205756E69747312140A056E616E6F7318032001280552056E616E6F7322470A1E476574537570706F7274656443757272656E63696573526573706F6E736512250A0E63757272656E63795F636F646573180120032809520D63757272656E6379436F646573225C0A1943757272656E6379436F6E76657273696F6E5265717565737412260A0466726F6D18012001280B32122E6869707374657273686F702E4D6F6E6579520466726F6D12170A07746F5F636F64651802200128095206746F436F646522E6010A0E43726564697443617264496E666F122C0A126372656469745F636172645F6E756D6265721801200128095210637265646974436172644E756D62657212260A0F6372656469745F636172645F637676180220012805520D63726564697443617264437676123D0A1B6372656469745F636172645F65787069726174696F6E5F7965617218032001280552186372656469744361726445787069726174696F6E59656172123F0A1C6372656469745F636172645F65787069726174696F6E5F6D6F6E746818042001280552196372656469744361726445787069726174696F6E4D6F6E746822790A0D43686172676552657175657374122A0A06616D6F756E7418012001280B32122E6869707374657273686F702E4D6F6E65795206616D6F756E74123C0A0B6372656469745F6361726418022001280B321B2E6869707374657273686F702E43726564697443617264496E666F520A6372656469744361726422370A0E436861726765526573706F6E736512250A0E7472616E73616374696F6E5F6964180120012809520D7472616E73616374696F6E4964225E0A094F726465724974656D12290A046974656D18012001280B32152E6869707374657273686F702E436172744974656D52046974656D12260A04636F737418022001280B32122E6869707374657273686F702E4D6F6E65795204636F73742282020A0B4F72646572526573756C7412190A086F726465725F696418012001280952076F72646572496412300A147368697070696E675F747261636B696E675F696418022001280952127368697070696E67547261636B696E67496412370A0D7368697070696E675F636F737418032001280B32122E6869707374657273686F702E4D6F6E6579520C7368697070696E67436F7374123F0A107368697070696E675F6164647265737318042001280B32142E6869707374657273686F702E41646472657373520F7368697070696E6741646472657373122C0A056974656D7318052003280B32162E6869707374657273686F702E4F726465724974656D52056974656D7322640A1C53656E644F72646572436F6E6669726D6174696F6E5265717565737412140A05656D61696C1801200128095205656D61696C122E0A056F7264657218022001280B32182E6869707374657273686F702E4F72646572526573756C7452056F7264657222D5010A11506C6163654F726465725265717565737412170A07757365725F6964180120012809520675736572496412230A0D757365725F63757272656E6379180220012809520C7573657243757272656E6379122E0A076164647265737318032001280B32142E6869707374657273686F702E4164647265737352076164647265737312140A05656D61696C1805200128095205656D61696C123C0A0B6372656469745F6361726418062001280B321B2E6869707374657273686F702E43726564697443617264496E666F520A6372656469744361726422440A12506C6163654F72646572526573706F6E7365122E0A056F7264657218012001280B32182E6869707374657273686F702E4F72646572526573756C7452056F72646572222E0A0941645265717565737412210A0C636F6E746578745F6B657973180120032809520B636F6E746578744B657973222F0A0A4164526573706F6E736512210A0361647318012003280B320F2E6869707374657273686F702E41645203616473223B0A02416412210A0C72656469726563745F75726C180120012809520B726564697265637455726C12120A047465787418022001280952047465787432CA010A0B4361727453657276696365123C0A074164644974656D121B2E6869707374657273686F702E4164644974656D526571756573741A122E6869707374657273686F702E456D7074792200123B0A0747657443617274121B2E6869707374657273686F702E47657443617274526571756573741A112E6869707374657273686F702E43617274220012400A09456D70747943617274121D2E6869707374657273686F702E456D70747943617274526571756573741A122E6869707374657273686F702E456D70747922003283010A155265636F6D6D656E646174696F6E53657276696365126A0A134C6973745265636F6D6D656E646174696F6E7312272E6869707374657273686F702E4C6973745265636F6D6D656E646174696F6E73526571756573741A282E6869707374657273686F702E4C6973745265636F6D6D656E646174696F6E73526573706F6E736522003283020A1550726F64756374436174616C6F675365727669636512470A0C4C69737450726F647563747312122E6869707374657273686F702E456D7074791A212E6869707374657273686F702E4C69737450726F6475637473526573706F6E7365220012440A0A47657450726F64756374121E2E6869707374657273686F702E47657450726F64756374526571756573741A142E6869707374657273686F702E50726F647563742200125B0A0E53656172636850726F647563747312222E6869707374657273686F702E53656172636850726F6475637473526571756573741A232E6869707374657273686F702E53656172636850726F6475637473526573706F6E7365220032AA010A0F5368697070696E675365727669636512490A0847657451756F7465121C2E6869707374657273686F702E47657451756F7465526571756573741A1D2E6869707374657273686F702E47657451756F7465526573706F6E73652200124C0A09536869704F72646572121D2E6869707374657273686F702E536869704F72646572526571756573741A1E2E6869707374657273686F702E536869704F72646572526573706F6E7365220032B7010A0F43757272656E637953657276696365125B0A16476574537570706F7274656443757272656E6369657312122E6869707374657273686F702E456D7074791A2B2E6869707374657273686F702E476574537570706F7274656443757272656E63696573526573706F6E7365220012470A07436F6E7665727412262E6869707374657273686F702E43757272656E6379436F6E76657273696F6E526571756573741A122E6869707374657273686F702E4D6F6E6579220032550A0E5061796D656E745365727669636512430A06436861726765121A2E6869707374657273686F702E436861726765526571756573741A1B2E6869707374657273686F702E436861726765526573706F6E7365220032680A0C456D61696C5365727669636512580A1553656E644F72646572436F6E6669726D6174696F6E12292E6869707374657273686F702E53656E644F72646572436F6E6669726D6174696F6E526571756573741A122E6869707374657273686F702E456D707479220032620A0F436865636B6F757453657276696365124F0A0A506C6163654F72646572121E2E6869707374657273686F702E506C6163654F72646572526571756573741A1F2E6869707374657273686F702E506C6163654F72646572526573706F6E7365220032480A09416453657276696365123B0A0647657441647312162E6869707374657273686F702E4164526571756573741A172E6869707374657273686F702E4164526573706F6E73652200620670726F746F33";

public isolated function getDescriptorMapDemo() returns map<string> {
    return {"demo.proto": "0A0A64656D6F2E70726F746F120B6869707374657273686F7022450A08436172744974656D121D0A0A70726F647563745F6964180120012809520970726F647563744964121A0A087175616E7469747918022001280552087175616E7469747922540A0E4164644974656D5265717565737412170A07757365725F6964180120012809520675736572496412290A046974656D18022001280B32152E6869707374657273686F702E436172744974656D52046974656D222B0A10456D707479436172745265717565737412170A07757365725F6964180120012809520675736572496422290A0E476574436172745265717565737412170A07757365725F69641801200128095206757365724964224C0A044361727412170A07757365725F69641801200128095206757365724964122B0A056974656D7318022003280B32152E6869707374657273686F702E436172744974656D52056974656D7322070A05456D70747922560A1A4C6973745265636F6D6D656E646174696F6E735265717565737412170A07757365725F69641801200128095206757365724964121F0A0B70726F647563745F696473180220032809520A70726F64756374496473223E0A1B4C6973745265636F6D6D656E646174696F6E73526573706F6E7365121F0A0B70726F647563745F696473180120032809520A70726F6475637449647322BA010A0750726F64756374120E0A0269641801200128095202696412120A046E616D6518022001280952046E616D6512200A0B6465736372697074696F6E180320012809520B6465736372697074696F6E12180A0770696374757265180420012809520770696374757265122F0A0970726963655F75736418052001280B32122E6869707374657273686F702E4D6F6E657952087072696365557364121E0A0A63617465676F72696573180620032809520A63617465676F7269657322480A144C69737450726F6475637473526573706F6E736512300A0870726F647563747318012003280B32142E6869707374657273686F702E50726F64756374520870726F647563747322230A1147657450726F6475637452657175657374120E0A02696418012001280952026964222D0A1553656172636850726F64756374735265717565737412140A0571756572791801200128095205717565727922480A1653656172636850726F6475637473526573706F6E7365122E0A07726573756C747318012003280B32142E6869707374657273686F702E50726F647563745207726573756C7473226E0A0F47657451756F746552657175657374122E0A076164647265737318012001280B32142E6869707374657273686F702E41646472657373520761646472657373122B0A056974656D7318022003280B32152E6869707374657273686F702E436172744974656D52056974656D7322410A1047657451756F7465526573706F6E7365122D0A08636F73745F75736418012001280B32122E6869707374657273686F702E4D6F6E65795207636F7374557364226F0A10536869704F7264657252657175657374122E0A076164647265737318012001280B32142E6869707374657273686F702E41646472657373520761646472657373122B0A056974656D7318022003280B32152E6869707374657273686F702E436172744974656D52056974656D7322340A11536869704F72646572526573706F6E7365121F0A0B747261636B696E675F6964180120012809520A747261636B696E674964228F010A074164647265737312250A0E7374726565745F61646472657373180120012809520D7374726565744164647265737312120A046369747918022001280952046369747912140A0573746174651803200128095205737461746512180A07636F756E7472791804200128095207636F756E74727912190A087A69705F636F646518052001280552077A6970436F646522580A054D6F6E657912230A0D63757272656E63795F636F6465180120012809520C63757272656E6379436F646512140A05756E6974731802200128035205756E69747312140A056E616E6F7318032001280552056E616E6F7322470A1E476574537570706F7274656443757272656E63696573526573706F6E736512250A0E63757272656E63795F636F646573180120032809520D63757272656E6379436F646573225C0A1943757272656E6379436F6E76657273696F6E5265717565737412260A0466726F6D18012001280B32122E6869707374657273686F702E4D6F6E6579520466726F6D12170A07746F5F636F64651802200128095206746F436F646522E6010A0E43726564697443617264496E666F122C0A126372656469745F636172645F6E756D6265721801200128095210637265646974436172644E756D62657212260A0F6372656469745F636172645F637676180220012805520D63726564697443617264437676123D0A1B6372656469745F636172645F65787069726174696F6E5F7965617218032001280552186372656469744361726445787069726174696F6E59656172123F0A1C6372656469745F636172645F65787069726174696F6E5F6D6F6E746818042001280552196372656469744361726445787069726174696F6E4D6F6E746822790A0D43686172676552657175657374122A0A06616D6F756E7418012001280B32122E6869707374657273686F702E4D6F6E65795206616D6F756E74123C0A0B6372656469745F6361726418022001280B321B2E6869707374657273686F702E43726564697443617264496E666F520A6372656469744361726422370A0E436861726765526573706F6E736512250A0E7472616E73616374696F6E5F6964180120012809520D7472616E73616374696F6E4964225E0A094F726465724974656D12290A046974656D18012001280B32152E6869707374657273686F702E436172744974656D52046974656D12260A04636F737418022001280B32122E6869707374657273686F702E4D6F6E65795204636F73742282020A0B4F72646572526573756C7412190A086F726465725F696418012001280952076F72646572496412300A147368697070696E675F747261636B696E675F696418022001280952127368697070696E67547261636B696E67496412370A0D7368697070696E675F636F737418032001280B32122E6869707374657273686F702E4D6F6E6579520C7368697070696E67436F7374123F0A107368697070696E675F6164647265737318042001280B32142E6869707374657273686F702E41646472657373520F7368697070696E6741646472657373122C0A056974656D7318052003280B32162E6869707374657273686F702E4F726465724974656D52056974656D7322640A1C53656E644F72646572436F6E6669726D6174696F6E5265717565737412140A05656D61696C1801200128095205656D61696C122E0A056F7264657218022001280B32182E6869707374657273686F702E4F72646572526573756C7452056F7264657222D5010A11506C6163654F726465725265717565737412170A07757365725F6964180120012809520675736572496412230A0D757365725F63757272656E6379180220012809520C7573657243757272656E6379122E0A076164647265737318032001280B32142E6869707374657273686F702E4164647265737352076164647265737312140A05656D61696C1805200128095205656D61696C123C0A0B6372656469745F6361726418062001280B321B2E6869707374657273686F702E43726564697443617264496E666F520A6372656469744361726422440A12506C6163654F72646572526573706F6E7365122E0A056F7264657218012001280B32182E6869707374657273686F702E4F72646572526573756C7452056F72646572222E0A0941645265717565737412210A0C636F6E746578745F6B657973180120032809520B636F6E746578744B657973222F0A0A4164526573706F6E736512210A0361647318012003280B320F2E6869707374657273686F702E41645203616473223B0A02416412210A0C72656469726563745F75726C180120012809520B726564697265637455726C12120A047465787418022001280952047465787432CA010A0B4361727453657276696365123C0A074164644974656D121B2E6869707374657273686F702E4164644974656D526571756573741A122E6869707374657273686F702E456D7074792200123B0A0747657443617274121B2E6869707374657273686F702E47657443617274526571756573741A112E6869707374657273686F702E43617274220012400A09456D70747943617274121D2E6869707374657273686F702E456D70747943617274526571756573741A122E6869707374657273686F702E456D70747922003283010A155265636F6D6D656E646174696F6E53657276696365126A0A134C6973745265636F6D6D656E646174696F6E7312272E6869707374657273686F702E4C6973745265636F6D6D656E646174696F6E73526571756573741A282E6869707374657273686F702E4C6973745265636F6D6D656E646174696F6E73526573706F6E736522003283020A1550726F64756374436174616C6F675365727669636512470A0C4C69737450726F647563747312122E6869707374657273686F702E456D7074791A212E6869707374657273686F702E4C69737450726F6475637473526573706F6E7365220012440A0A47657450726F64756374121E2E6869707374657273686F702E47657450726F64756374526571756573741A142E6869707374657273686F702E50726F647563742200125B0A0E53656172636850726F647563747312222E6869707374657273686F702E53656172636850726F6475637473526571756573741A232E6869707374657273686F702E53656172636850726F6475637473526573706F6E7365220032AA010A0F5368697070696E675365727669636512490A0847657451756F7465121C2E6869707374657273686F702E47657451756F7465526571756573741A1D2E6869707374657273686F702E47657451756F7465526573706F6E73652200124C0A09536869704F72646572121D2E6869707374657273686F702E536869704F72646572526571756573741A1E2E6869707374657273686F702E536869704F72646572526573706F6E7365220032B7010A0F43757272656E637953657276696365125B0A16476574537570706F7274656443757272656E6369657312122E6869707374657273686F702E456D7074791A2B2E6869707374657273686F702E476574537570706F7274656443757272656E63696573526573706F6E7365220012470A07436F6E7665727412262E6869707374657273686F702E43757272656E6379436F6E76657273696F6E526571756573741A122E6869707374657273686F702E4D6F6E6579220032550A0E5061796D656E745365727669636512430A06436861726765121A2E6869707374657273686F702E436861726765526571756573741A1B2E6869707374657273686F702E436861726765526573706F6E7365220032680A0C456D61696C5365727669636512580A1553656E644F72646572436F6E6669726D6174696F6E12292E6869707374657273686F702E53656E644F72646572436F6E6669726D6174696F6E526571756573741A122E6869707374657273686F702E456D707479220032620A0F436865636B6F757453657276696365124F0A0A506C6163654F72646572121E2E6869707374657273686F702E506C6163654F72646572526571756573741A1F2E6869707374657273686F702E506C6163654F72646572526573706F6E7365220032480A09416453657276696365123B0A0647657441647312162E6869707374657273686F702E4164526571756573741A172E6869707374657273686F702E4164526573706F6E73652200620670726F746F33"};
}

