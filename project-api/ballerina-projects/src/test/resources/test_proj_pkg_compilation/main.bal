import myproject.services;
import myproject/myproject.storage;

public function main() {
    services:runServices();
    // Syntactic error
    storage:initDatabase()
}