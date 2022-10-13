client "https://postman-echo.com/get?name=projectapiclientplugin" as myapi1; 

function name() {
    myapi1:ClientConfiguration config = {specVersion : "3.0.0"};
    myapi1:client cl = new (config);
    string specVersion = cl->
}
