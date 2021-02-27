function foo() {
    match bar {
        _ => {
            int a = 5;
            int b = 7;
            return a + b;
        }
        "south"|"north" => {
            return "This is a direction";
        }
        "west" if getX() | getY() => {
            return "This is a direction with condition";
        }
    }
}
