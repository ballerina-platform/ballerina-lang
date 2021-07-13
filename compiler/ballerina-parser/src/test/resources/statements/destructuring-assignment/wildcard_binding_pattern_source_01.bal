function foo() {
    _ = x;

    [_, _, _] = "hello";
    [_, error(_, _)] = "hello";
    [_, error T(_, _), {x: error(_, _)}, _] = "hello";

    {x: _, y: _, z: _} = "hello";
    {x: error(_, _)} = "hello";
    {x: error T(_, _), y: [_, error(_, _)], z: _} = "hello";

    error(_, _, d = _) = "hello";
    error(_, error T(_, _), d = error(_, _)) = "hello";
    error T(_, error(_, _), d = error T(_, _)) = "hello";
}
