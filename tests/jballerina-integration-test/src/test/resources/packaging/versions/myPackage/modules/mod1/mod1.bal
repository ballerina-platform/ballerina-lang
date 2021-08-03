import waruna/http.'client as httpClient;
import waruna/websub.server as websubServer;

public function getWebSubHtppVersion() returns string {
     return websubServer:getHttpVersion();
}

public function getHtppVersion() returns string {
     return httpClient:getVersion();
}
