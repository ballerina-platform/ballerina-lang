import ballerina/test;

final PuzzleData[] PUZZLE_DATA = [
    { signalPatterns: ["be", "cfbegad", "cbdgef", "fgaecd", "cgeb", "fdcge", "agebfd", "fecdb", "fabcd", "edb"], outputValues: ["fdgacbe", "cefdb", "cefbgd", "gcbe"] },
    { signalPatterns: ["edbfga", "begcd", "cbg", "gc", "gcadebf", "fbgde", "acbgfd", "abcde", "gfcbed", "gfec"], outputValues: ["fcgedb", "cgb", "dgebacf", "gc"] },
    { signalPatterns: ["fgaebd", "cg", "bdaec", "gdafb", "agbcfd", "gdcbef", "bgcad", "gfac", "gcb", "cdgabef"], outputValues: ["cg", "cg", "fdcagb", "cbg"] },
    { signalPatterns: ["fbegcd", "cbd", "adcefb", "dageb", "afcb", "bc", "aefdc", "ecdab", "fgdeca", "fcdbega"], outputValues: ["efabcd", "cedba", "gadfec", "cb"] },
    { signalPatterns: ["aecbfdg", "fbg", "gf", "bafeg", "dbefa", "fcge", "gcbea", "fcaegb", "dgceab", "fcbdga"], outputValues: ["gecf", "egdcabf", "bgf", "bfgea"] },
    { signalPatterns: ["fgeab", "ca", "afcebg", "bdacfeg", "cfaedg", "gcfdb", "baec", "bfadeg", "bafgc", "acf"], outputValues: ["gebdcfa", "ecba", "ca", "fadegcb"] },
    { signalPatterns: ["dbcfg", "fgd", "bdegcaf", "fgec", "aegbdf", "ecdfab", "fbedc", "dacgb", "gdcebf", "gf"], outputValues: ["cefg", "dcbef", "fcge", "gbcadfe"] },
    { signalPatterns: ["bdfegc", "cbegaf", "gecbf", "dfcage", "bdacg", "ed", "bedf", "ced", "adcbefg", "gebcd"], outputValues: ["ed", "bcgafe", "cdgba", "cbgef"] },
    { signalPatterns: ["egadfb", "cdbfeg", "cegd", "fecab", "cgb", "gbdefca", "cg", "fgcdab", "egfdb", "bfceg"], outputValues: ["gbdfcae", "bgc", "cg", "cgb"] },
    { signalPatterns: ["gcafb", "gcf", "dcaebfg", "ecagb", "gf", "abcdeg", "gaef", "cafbge", "fdbac", "fegbdc"], outputValues: ["fgae", "cfgab", "fg", "bagce"] }
];

@test:Config {}
function test_part_1() {
    test:assertEquals(part1(PUZZLE_DATA), 26);
}

@test:Config {}
function test_part_2() {
    test:assertEquals(part2(PUZZLE_DATA), 61229);
}
