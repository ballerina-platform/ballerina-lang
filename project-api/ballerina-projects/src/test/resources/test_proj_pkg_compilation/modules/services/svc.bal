import myproject/myproject.storage;
import http/http.utils;


public function runServices() {
    storage:initDatabase();
    utils:initDatabase();
    // Semantic error
    int a = "d";
}