type Lib_Book record {
    @xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
    string[] title;
};

type Au_Author record {
    @xmldata:Namespace {prefix: "au", uri: "http://example.com/author"}
    string[] name;
};

type Pr_Price record {
    @xmldata:Namespace {prefix: "pr", uri: "http://example.com/price"}
    (decimal|int)[] price;
};

@xmldata:Name {value: "library"}
type Library record {
    @xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
    Lib_Book book;
    @xmldata:Namespace {prefix: "au", uri: "http://example.com/author"}
    Au_Author author;
    @xmldata:Namespace {prefix: "pr", uri: "http://example.com/price"}
    Pr_Price price;
};
