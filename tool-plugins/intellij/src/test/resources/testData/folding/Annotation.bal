@http:ServiceConfig <fold text='{...}'>{
    basePath: "/hello"
}</fold>
service helloWorld on helloWorldEP <fold text='{...}'>{

    @http:ResourceConfig <fold text='{...}'>{
        methods: ["GET"],
        path: "/"
    }</fold>
    resource function sayHello(http:Caller caller, http:Request req) <fold text='{...}'>{

    }</fold>
}</fold>
