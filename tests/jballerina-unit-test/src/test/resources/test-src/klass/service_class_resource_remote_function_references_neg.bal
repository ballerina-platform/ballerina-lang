type SType service object {
    remote function onMesage(anydata data);
    resource function get foo/bar();
    resource function get foo/[string j]();
};

service class SClass {
    *SType;
}
