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
import ballerina/log;
import ballerina/random;

configurable string currencyHost = "localhost";
@display {
    label: "",
    id: "currency"
}
final CurrencyServiceClient currencyClient = check new ("http://" + currencyHost + ":9093");
configurable string catalogHost = "localhost";
@display {
    label: "",
    id: "catalog"
}
final ProductCatalogServiceClient catalogClient = check new ("http://" + catalogHost + ":9091");
configurable string cartHost = "localhost";
@display {
    label: "",
    id: "cart"
}
final CartServiceClient cartClient = check new ("http://" + cartHost + ":9092");
configurable string shippingHost = "localhost";
@display {
    label: "",
    id: "shipping"
}
final ShippingServiceClient shippingClient = check new ("http://" + shippingHost + ":9095");
configurable string recommandHost = "localhost";
@display {
    label: "",
    id: "recommendation"
}
final RecommendationServiceClient recommandClient = check new ("http://" + recommandHost + ":9090");
configurable string adHost = "localhost";
@display {
    label: "",
    id: "ads"
}
final AdServiceClient adClient = check new ("http://" + adHost + ":9099");
configurable string checkoutHost = "localhost";
@display {
    label: "",
    id: "checkout"
}
final CheckoutServiceClient checkoutClient = check new ("http://" + checkoutHost + ":9094");

isolated function getSupportedCurrencies() returns string[]|error {
    GetSupportedCurrenciesResponse|grpc:Error supportedCurrencies = currencyClient->GetSupportedCurrencies({});
    if supportedCurrencies is grpc:Error {
        log:printError("failed to call getSupportedCurrencies from currency service", 'error = supportedCurrencies);
        return supportedCurrencies;
    }
    return supportedCurrencies.currency_codes;
}

isolated function getProducts() returns Product[]|error {
    ListProductsResponse|grpc:Error products = catalogClient->ListProducts({});

    if products is grpc:Error {
        log:printError("failed to call listProducts from catalog service", 'error = products);
        return products;
    }
    return products.products;
}

isolated function getProduct(string prodId) returns Product|error {
    GetProductRequest req = {
        id: prodId
    };
    Product|grpc:Error product = catalogClient->GetProduct(req);

    if product is grpc:Error {
        log:printError("failed to call getProduct from catalog service", 'error = product);
    }
    return product;
}

isolated function getCart(string userId) returns Cart|error {
    GetCartRequest req = {
        user_id: userId
    };
    Cart|grpc:Error cart = cartClient->GetCart(req);

    if cart is grpc:Error {
        log:printError("failed to call getCart from cart service", 'error = cart);
    }
    return cart;
}

isolated function emptyCart(string userId) returns error? {
    EmptyCartRequest req = {
        user_id: userId
    };
    Empty|grpc:Error cart = cartClient->EmptyCart(req);

    if cart is grpc:Error {
        log:printError("failed to call emptyCart from cart service", 'error = cart);
        return cart;
    }
}

isolated function insertCart(string userId, string productId, int quantity) returns error? {
    AddItemRequest req = {
        user_id: userId,
        item: {
            product_id: productId,
            quantity: quantity
        }
    };
    Empty|grpc:Error cart = cartClient->AddItem(req);

    if cart is grpc:Error {
        log:printError("failed to call addItem from cart service", 'error = cart);
        return cart;
    }
}

isolated function convertCurrency(Money usd, string userCurrency) returns Money|error {
    CurrencyConversionRequest req1 = {
        'from: usd,
        to_code: userCurrency
    };
    Money|grpc:Error convert = currencyClient->Convert(req1);
    if convert is grpc:Error {
        log:printError("failed to call convert from currency service", 'error = convert);
        return convert;
    }
    return currencyClient->Convert(req1);
}

isolated function getShippingQuote(CartItem[] items, string currency) returns Money|error {
    GetQuoteRequest req1 = {
        items,
        address: {}
    };
    GetQuoteResponse|grpc:Error quote = shippingClient->GetQuote(req1);
    if quote is grpc:Error {
        log:printError("failed to call getQuote from shipping service", 'error = quote);
        return quote;
    }
    return check convertCurrency(quote.cost_usd, currency);
}

isolated function getRecommendations(string userId, string[] productIds) returns Product[]|error {
    ListRecommendationsRequest req = {
        user_id: userId,
        product_ids: ["2ZYFJ3GM2N", "LS4PSXUNUM"]
    };
    ListRecommendationsResponse|grpc:Error recommendations = recommandClient->ListRecommendations(req);
    if recommendations is grpc:Error {
        log:printError("failed to call listRecommnadation from recommandation service", 'error = recommendations);
        return recommendations;
    }

    return from string productId in recommendations.product_ids
        limit 4
        select check getProduct(productId);
}

isolated function getAd(string[] ctxKeys) returns Ad[]|error {
    AdRequest request = {
        context_keys: ctxKeys
    };
    AdResponse|grpc:Error ads = adClient->GetAds(request);
    if ads is grpc:Error {
        log:printError("failed to call getAds from ads service", 'error = ads);
        return ads;
    }
    return ads.ads;
}

isolated function chooseAd(string[] ctxKeys) returns Ad|error {
    Ad[] ads = check getAd(ctxKeys);
    return ads[check random:createIntInRange(0, ads.length())];
}

isolated function checkoutCart(PlaceOrderRequest req) returns OrderResult|error {
    PlaceOrderResponse|error placeOrderResponse = checkoutClient->PlaceOrder(req);
    if placeOrderResponse is error {
        log:printError("failed to call placeOrder from checkout service", 'error = placeOrderResponse);
        return placeOrderResponse;
    }
    return placeOrderResponse.'order;
}
