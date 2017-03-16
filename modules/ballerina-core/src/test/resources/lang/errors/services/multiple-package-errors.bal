package lang.errors.services;

import lang.errors.runtime;

@http:BasePath ("/test")
service echoService {
    @http:GET
    @http:Path ("/crossPkg")
    resource echoResource (message m) {
        runtime:testStackTrace();
        reply m;
    }
}
