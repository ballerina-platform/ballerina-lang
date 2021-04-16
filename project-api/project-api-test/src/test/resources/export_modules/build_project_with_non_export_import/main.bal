import foo/winery.services;
import foo/winery.storage;

public function main() {
   services:runServices();
   storage:initDatabase();
}
