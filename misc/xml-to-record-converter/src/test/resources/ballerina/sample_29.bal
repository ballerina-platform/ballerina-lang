type Lib_Book record {
    @xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
    int id;
    @xmldata:Name {value: "title"}
    @xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
    string libTitle;
    @xmldata:Name {value: "title"}
    @xmldata:Namespace {prefix: "au", uri: "http://example.com/author"}
    string auTitle;
    @xmldata:Name {value: "author"}
    @xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
    string libAuthor;
    @xmldata:Name {value: "author"}
    @xmldata:Namespace {prefix: "au", uri: "http://example.com/author"}
    string auAuthor;
};

@xmldata:Name {value: "library"}
@xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
type Lib_Library record {
    @xmldata:Namespace {prefix: "lib", uri: "http://example.com/library"}
    Lib_Book book;
};
