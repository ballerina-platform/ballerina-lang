function fn() {
    client "http://www.example.com/apis/myapi.yaml" as myapi

    client "http://www.example.com/apis/myapi.yaml" myapi

    client as myapi2;
}
