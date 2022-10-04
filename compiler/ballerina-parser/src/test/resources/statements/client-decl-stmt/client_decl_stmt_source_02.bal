function fn() {
    @AnnotationOne {
        x: 1
    }
    client "http://www.example.com/apis/myapi.yaml" as myapi;

    @AnnotationTwo
    client "http://www.example.com/apis/myapi.yaml" as myapi;
}
