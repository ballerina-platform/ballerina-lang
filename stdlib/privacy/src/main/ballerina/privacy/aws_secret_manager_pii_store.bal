public type AwsSecretManagerPiiStore object {
    public function pseudonymize (string pii) returns string|error {
        return "";
    }

    public function depseudonymize (string id) returns string|error {
        return "";
    }

    public function delete (string id) returns error? {
        return ();
    }
};