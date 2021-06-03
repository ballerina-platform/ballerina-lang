
public function main() returns error? {
     MyMainClient clientEndpoint = new ("http://postman-echo.com");
     var response = check clientEndpoint->getAll("");
     var df = check myClient->getAll("");
}
