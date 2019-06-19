record{} globalVar = {};

public function main (string... args) {
    taintedJsonReturn().address_components
    .filter(function (any comp) returns boolean {
            if comp is record{} {
                secureFunction(taintedReturn(), taintedReturn());
                return true;
            }
            return false;
        })
    .foreach(function (record{} k) { globalVar = k;});
}

function secureFunction (@sensitive string secureIn, string insecureIn) {

}

function taintedReturn () returns @tainted string {
    return "tainted";
}

function taintedJsonReturn() returns @tainted map<record{}[]> {
    return data;
}

map<record{}[]> data = {
    "address_components": [
        {
            long_name: "1823",
            short_name: "1823",
            types: <string[]> [
                "street_number"
            ]
        },
        {
            long_name: "CMW",
            short_name: "CMW",
            types: <string[]> [
                "postal_code_suffix"
            ]
        }
    ]
};