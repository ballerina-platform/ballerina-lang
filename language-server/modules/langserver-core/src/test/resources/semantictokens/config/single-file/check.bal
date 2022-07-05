import ballerina/io;

public function main() {
    json resp = check {LKR_USD: 0.004908};
        decimal rate;
        do {
	        rate = check resp.LKR_USD;
        } on fail var e {
            io:println(e);
        	rate = 0;
        }

        decimal r2 = rate*2;
        io:println(r2);
}

isolated function parseProductJson(json jsonContents) returns Product[]|error {
    Product[] products = [];
    json[] productsJson = <json[]>check jsonContents.products;
    foreach json productJson in productsJson {
        Product product = {

            id: check productJson.id,
            name: check productJson.name,
            description: check productJson.description,
            picture: check productJson.picture,
            price_usd: check parseMoneyJson(check productJson.priceUsd),
            categories: check (check productJson.categories).cloneWithType()
        };
        products.push(product);
    }
    return products;
}

isolated function parseMoneyJson(json moenyJson) returns Money|error {
    return {
        currency_code: check moenyJson.currencyCode,
        units: check moenyJson.units,
        nanos: check moenyJson.nanos
    };
}

public type Product record {|
    string id = "";
    string name = "";
    string description = "";
    string picture = "";
    Money price_usd = {};
    string[] categories = [];
|};
public type Money record {|
    string currency_code = "";
    int units = 0;
    int nanos = 0;
|};
