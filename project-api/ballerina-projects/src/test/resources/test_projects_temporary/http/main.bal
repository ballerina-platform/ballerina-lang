import sameera/http.utils;
import sameera/http.auth;

public function main() {
   auth:runServices();
   utils:initDatabase();
}
