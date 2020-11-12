public function foo() {
    match bar {
        _ => {}
        a => {}
        +1 => {}
        -5 => {}
        () => {}
        "south"|"north" => {}
        "east" => {}
        "west" if getX() | getY() => {}
    } on fail error e {
        io:println("Exception thrown...");
    }
}
