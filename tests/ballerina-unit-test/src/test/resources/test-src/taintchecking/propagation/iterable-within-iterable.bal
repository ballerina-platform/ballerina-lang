json globalVar;

public function main (string... args) {
    jsonData.address_components
    .filter((json comp) => boolean {
            return comp.types.filter((json compType) => boolean {
                        secureFunction("untainted", "untainted");
                        return compType.toString() == "street_number";
                    }).count() > 0; })
    .foreach((json k) => { globalVar = k;});
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
