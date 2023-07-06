import sameera/myproject.storage;
import sameera/http.utils;

public function runServices() {
    storage:initDatabase();
    utils:initDatabase();
}
