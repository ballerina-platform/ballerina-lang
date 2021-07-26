import testproject.module2;

public function getCountry() returns module2:Country {
    return {name: "Sri Lanka"};
}

public function getCountryWithError() returns module2:Country|error {
    return {name: "Sri Lanka"};
}

function callGetCountry() {
    getCountry();
}
