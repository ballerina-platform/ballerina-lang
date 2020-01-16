// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

CheckoutServiceBlockingClient blockingEp = new("http://localhost:9108");

public function main (string... args) {
    log:printInfo(testOptionalFieldMessage());
}

public function testOptionalFieldMessage() returns string {
    PlaceOrderRequest orderRequest = {
        user_id: "2e8f27b9-b966-45b0-b51f-dcccea697d01",
        user_currency: "USD",
        address: {
            zip_code: 94043
        },
        email: "someone@example.com",
        credit_card: {
            credit_card_number: "1"
        }
    };
    var result = blockingEp->PlaceOrder(orderRequest);
    if (result is error) {
        log:printError("Error response.", result);
        return "Error occurred while calling remote method, placeorder";
    } else {
        PlaceOrderResponse response;
        [response, _] = result;
        return response.'order;
    }
}

public type CheckoutServiceBlockingClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client? grpcClient = ();

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config);
        checkpanic c.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        self.grpcClient = c;
    }

    public remote function PlaceOrder(PlaceOrderRequest req, grpc:Headers? headers = ()) returns
                                                                    ([PlaceOrderResponse, grpc:Headers]|grpc:Error) {
        if (self.grpcClient is ()) {
            error err = error("UninitializedClientError", message = "Field(s) are not initialized");
            return grpc:prepareError(grpc:INTERNAL_ERROR, "gRPC client endpoint is not initialized", err);
        }
        grpc:Client 'client = <grpc:Client> self.grpcClient;
        var payload = check 'client->blockingExecute("grpcservices.CheckoutService/PlaceOrder", req, headers);
        grpc:Headers resHeaders = new;
        anydata result = ();
        [result, resHeaders] = payload;
        return [<PlaceOrderResponse>result, resHeaders];
    }

};

public type CheckoutServiceClient client object {

    *grpc:AbstractClientEndpoint;

    private grpc:Client? grpcClient = ();

    public function __init(string url, grpc:ClientConfiguration? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config);
        checkpanic c.initStub(self, "non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        self.grpcClient = c;
    }

    public remote function PlaceOrder(PlaceOrderRequest req, service msgListener, grpc:Headers? headers = ()) returns (grpc:Error?) {
        if (self.grpcClient is ()) {
            error err = error("UninitializedClientError", message = "Field(s) are not initialized");
            return grpc:prepareError(grpc:INTERNAL_ERROR, "gRPC client endpoint is not initialized", err);
        }
        grpc:Client 'client = <grpc:Client> self.grpcClient;
        return 'client->nonBlockingExecute("grpcservices.CheckoutService/PlaceOrder", req, msgListener, headers);
    }

};

public type PlaceOrderRequest record {|
    string user_id = "";
    string user_currency = "";
    Address2? address = ();
    string email = "";
    CreditCardInfo? credit_card = ();

|};

public type PlaceOrderResponse record {|
    string 'order = "";

|};

public type Address2 record {|
    string street_address = "";
    string city = "";
    string state = "";
    string country = "";
    int zip_code = 0;

|};

public type CreditCardInfo record {|
    string credit_card_number = "";
    int credit_card_cvv = 0;
    int credit_card_expiration_year = 0;
    int credit_card_expiration_month = 0;

|};

const string ROOT_DESCRIPTOR = "0A15636865636B6F7574736572766963652E70726F746F120C67727063736572766963657322D8010A11506C6163654F726465725265717565737412170A07757365725F6964180120012809520675736572496412230A0D757365725F63757272656E6379180220012809520C7573657243757272656E637912300A076164647265737318032001280B32162E6772706373657276696365732E416464726573733252076164647265737312140A05656D61696C1805200128095205656D61696C123D0A0B6372656469745F6361726418062001280B321C2E6772706373657276696365732E43726564697443617264496E666F520A63726564697443617264222A0A12506C6163654F72646572526573706F6E736512140A056F7264657218012001280952056F726465722290010A08416464726573733212250A0E7374726565745F61646472657373180120012809520D7374726565744164647265737312120A046369747918022001280952046369747912140A0573746174651803200128095205737461746512180A07636F756E7472791804200128095207636F756E74727912190A087A69705F636F646518052001280552077A6970436F646522E6010A0E43726564697443617264496E666F122C0A126372656469745F636172645F6E756D6265721801200128095210637265646974436172644E756D62657212260A0F6372656469745F636172645F637676180220012805520D63726564697443617264437676123D0A1B6372656469745F636172645F65787069726174696F6E5F7965617218032001280552186372656469744361726445787069726174696F6E59656172123F0A1C6372656469745F636172645F65787069726174696F6E5F6D6F6E746818042001280552196372656469744361726445787069726174696F6E4D6F6E746832640A0F436865636B6F75745365727669636512510A0A506C6163654F72646572121F2E6772706373657276696365732E506C6163654F72646572526571756573741A202E6772706373657276696365732E506C6163654F72646572526573706F6E73652200620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "checkoutservice.proto":"0A15636865636B6F7574736572766963652E70726F746F120C67727063736572766963657322D8010A11506C6163654F726465725265717565737412170A07757365725F6964180120012809520675736572496412230A0D757365725F63757272656E6379180220012809520C7573657243757272656E637912300A076164647265737318032001280B32162E6772706373657276696365732E416464726573733252076164647265737312140A05656D61696C1805200128095205656D61696C123D0A0B6372656469745F6361726418062001280B321C2E6772706373657276696365732E43726564697443617264496E666F520A63726564697443617264222A0A12506C6163654F72646572526573706F6E736512140A056F7264657218012001280952056F726465722290010A08416464726573733212250A0E7374726565745F61646472657373180120012809520D7374726565744164647265737312120A046369747918022001280952046369747912140A0573746174651803200128095205737461746512180A07636F756E7472791804200128095207636F756E74727912190A087A69705F636F646518052001280552077A6970436F646522E6010A0E43726564697443617264496E666F122C0A126372656469745F636172645F6E756D6265721801200128095210637265646974436172644E756D62657212260A0F6372656469745F636172645F637676180220012805520D63726564697443617264437676123D0A1B6372656469745F636172645F65787069726174696F6E5F7965617218032001280552186372656469744361726445787069726174696F6E59656172123F0A1C6372656469745F636172645F65787069726174696F6E5F6D6F6E746818042001280552196372656469744361726445787069726174696F6E4D6F6E746832640A0F436865636B6F75745365727669636512510A0A506C6163654F72646572121F2E6772706373657276696365732E506C6163654F72646572526571756573741A202E6772706373657276696365732E506C6163654F72646572526573706F6E73652200620670726F746F33"

    };
}
