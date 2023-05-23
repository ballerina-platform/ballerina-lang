# Represents an organization
#
# + id - Organization ID 
# + name - Organization name  
# + domain - Organization domain
type Organization record {
    string id;
    string name;
    string domain;
};

public function main() {
    Organization org = {
        domain: "myorg.com",
        name: "My Org",
        id: "abc123"
    };
}
