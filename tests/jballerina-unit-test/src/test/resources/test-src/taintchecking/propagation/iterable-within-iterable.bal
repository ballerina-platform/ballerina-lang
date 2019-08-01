record{} globalVar = {};

public function main (string... args) {
    data.get("address_components")
    .filter(function (record {| anydata...; |} comp) returns boolean {
            secureFunction("untainted", "untainted");
            return true;
        })
    .forEach(function (record{} k) { globalVar = k;});
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}

string[] types0 = [ "street_number" ];
string[] types1 = [ "postal_code_suffix" ];

map<record{}[]> data = {
    "address_components": [
        {
            "long_name": "1823",
            "short_name": "1823",
            "types": types0
        },
        {
            "long_name": "CMW",
            "short_name": "CMW",
            "types": types1
        }
    ]
};
