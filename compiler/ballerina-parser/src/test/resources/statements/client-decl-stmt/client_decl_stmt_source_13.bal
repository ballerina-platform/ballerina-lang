function fn() {
    isolated client "http://www.example.com/apis/myapi2.yaml" as myapi2;

    transactional client "http://www.example.com/apis/myapi1.yaml" as myapi1;
}
