
public function main(string... args) {
    FilePIIStore pii = new("./test.file", CSV);
    var pseudoResult = pseudonymize(check <PIIStore>pii, "ayoma");
}