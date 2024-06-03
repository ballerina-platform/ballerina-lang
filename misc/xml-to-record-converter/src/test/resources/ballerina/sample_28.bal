@xmldata:Name {value: "book"}
@xmldata:Namespace {prefix: "bk", uri: "http://example.com/book"}
type Bk_Book record {
    @xmldata:Name {value: "title"}
    @xmldata:Namespace {prefix: "bk", uri: "http://example.com/book"}
    string bkTitle;
    @xmldata:Name {value: "title"}
    @xmldata:Namespace {prefix: "au", uri: "http://example.com/author"}
    string auTitle;
    @xmldata:Name {value: "title"}
    @xmldata:Namespace {prefix: "da", uri: "http://example.com/date"}
    string daTitle;
    @xmldata:Namespace {prefix: "bk", uri: "http://example.com/book"}
    int chapter;
    @xmldata:Namespace {prefix: "au", uri: "http://example.com/author"}
    string country;
};
