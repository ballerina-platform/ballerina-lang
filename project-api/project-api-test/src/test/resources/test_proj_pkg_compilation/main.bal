import myproject.services;
import sameera/myproject.storage;

public function main() {
    services:runServices();
    // Syntactic error
    storage:initDatabase()
}