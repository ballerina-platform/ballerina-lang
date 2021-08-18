function foo () {
    match bar {
        var &_ => {}
        var %a => {}
        var @ [a b, &% ...c] gfg %$ => {}
        var , [^] => {}
        () => {}
        var {a:b, c d e f g} => {}
        var {a:b, c d e f g}  => {}
        var {v:b, c d e f g}   {}
        "east" => {}
        "west"  if getX() | getY() => {}
    }
}
