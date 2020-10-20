function foo() {
    match bar {
        var _ => {}
        var a => {}
        var [a, b, ...c] => {}
        var [] => {}
        () => {}
        var { a:b } => {}
        "east" => {}
        "west" if getX() | getY() => {}
    }
}
