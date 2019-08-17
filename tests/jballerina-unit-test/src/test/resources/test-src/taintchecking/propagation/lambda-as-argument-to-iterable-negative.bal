record{} globalVar = {};

public function main (string... args) {
    taintedJsonReturn().get("address_components")
    .filter(function (record {| anydata...; |} comp) returns boolean {
            secureFunction(taintedReturn(), taintedReturn());
            return true;
        })
    .forEach(function (record{} k) { globalVar = k;});
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}

function taintedReturn () returns @tainted string {
    return "tainted";
}

function taintedJsonReturn() returns @tainted map<record{}[]> {
    return data;
}

string[] types0 = [ "street_number" ];
string[] types1 = [ "postal_code_suffix" ];

type AddressLine record {
    string long_name;
    string short_name;
    string[] types;
};
map<AddressLine[]> data = {
    "address_components": [
        {
            long_name: "1823",
            short_name: "1823",
            types: types0
        },
        {
            long_name: "CMW",
            short_name: "CMW",
            types: types1
        }
    ]
};
