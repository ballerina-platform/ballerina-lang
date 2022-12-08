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

import ballerina/http;

//Request Records
type AddToCartRequest record {|
    string productId;
    int quantity;
|};

type CheckoutRequest record {|
    string email;
    string street_address;
    int zip_code;
    string city;
    string state;
    string country;
    string credit_card_number;
    int credit_card_expiration_month;
    int credit_card_expiration_year;
    int credit_card_cvv;
|};

//Response Records

type CartItemView record {
    Product product;
    int quantity;
    string price;
};

type MetadataResponse record {|
    *http:Ok;
    MetadataBody body;
|};

type MetadataBody record {|
    string user_currency;
    string[] currencies;
    int cart_size;
    boolean is_cymbal_brand;
|};

type ProductLocalized record {|
    *Product;
    string price;
|};

type HomeResponse record {|
    *http:Ok;
    HomeBody body;
|};

type HomeBody record {|
    ProductLocalized[] products;
    Ad ad;
|};

type ProductResponse record {|
    *http:Ok;
    ProductBody body;
|};

type ProductBody record {|
    ProductLocalized product;
    Product[] recommendations;
    Ad ad;
|};

type CartResponse record {|
    *http:Ok;
    CartBody body;
|};

type CartBody record {|
    Product[] recommendations;
    string shipping_cost;
    string total_cost;
    CartItemView[] items;
    int[] expiration_years;
|};

type CheckoutResponse record {|
    *http:Ok;
    CheckoutBody body;
|};

type CheckoutBody record {|
    OrderResult 'order;
    string total_paid;
    Product[] recommendations;
|};
