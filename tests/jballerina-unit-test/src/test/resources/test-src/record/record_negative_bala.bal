import testorg/foo.records;

readonly service class PersonInterceptor {
}

readonly class Bar {
}

function v() {
    records:Config k = {interceptors: [new PersonInterceptor()], foo: [new Bar()]};
}
