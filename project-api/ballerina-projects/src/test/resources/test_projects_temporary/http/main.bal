import http/http.utils;
import http/http.auth;

public function main() {
   auth:runServices();
   utils:initDatabase();
}
