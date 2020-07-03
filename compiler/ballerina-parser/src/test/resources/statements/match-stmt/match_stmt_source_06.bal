function foo () {
    match bar {
        var _ => {}
        a => {}
        [a, b, ...c]=> {}
        var [] => {}
        () => {}
        {a:b} => {}
        "east" => {}
        "west"  if getX() | getY() => {}
    }
}
