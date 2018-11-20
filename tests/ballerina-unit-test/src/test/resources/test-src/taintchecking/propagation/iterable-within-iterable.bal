json globalVar = {};

public function main (string... args) {
    jsonData.address_components
    .filter(function (json comp) returns boolean {
            return comp.types.filter(function (json compType) returns boolean {
                        secureFunction("untainted", "untainted");
                        return compType.toString() == "street_number";
                    }).count() > 0; })
    .foreach(function (json k) { globalVar = k;});
}

function secureFunction (@sensitive string secureIn, string insecureIn) {

}

json jsonData = {
    "address_components": [
        {
            "long_name": "1823",
            "short_name": "1823",
            "types": [
                "street_number"
            ]
        },
        {
            "long_name": "CMW",
            "short_name": "CMW",
            "types": [
                "postal_code_suffix"
            ]
        }
    ]
};
