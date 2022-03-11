import aoc21.day01;
import aoc21.day04;
import aoc21.day08;
import ballerina/io;
import ballerina/test;

public type InputPuzzleId record {
    int day;
    int part?;
};
public function main() returns error? {
    InputPuzzleId inputPuzzle = {
        day: 1
    };
    check validate(inputPuzzle);
    final PuzzleId puzzleId = toPuzzleId(inputPuzzle);

    match puzzleId.day {
         1 => { puzzleRun(puzzleId, expected = [1583, 1627],            runner = day01:run); }
         4 => { puzzleRun(puzzleId, expected = [44088, 23670],          runner = day04:run); }
         8 => { puzzleRun(puzzleId, expected = [239, 946346],           runner = day08:run); }
         _ => {
            return error(string`not implemented: ${toDayString(inputPuzzle)}`);
        }
    }
}

type PuzzleId record { int day; int[] parts; };

function toDayString(InputPuzzleId puzzle) returns string {
    return string`(day ${puzzle.day})`;
}

function toPartString(InputPuzzleId puzzle) returns string {
    if puzzle.part is int {
        return string`(part ${<int>puzzle.part})`;
    } else {
        return string`(part nil)`;
    }
}

function isValid(InputPuzzleId puzzle) returns boolean {
    if puzzle.day < 1 || puzzle.day > 25 { return false; }
    if puzzle.part is int && puzzle.part != 1 && puzzle.part != 2 { return false; }
    return true;
}

function validate(InputPuzzleId puzzle) returns error? {
    final string dayStr = toDayString(puzzle);
    final string partStr = toPartString(puzzle);
    final string puzzleStr = string`${dayStr}${partStr}`;

    if !isValid(puzzle) {
        return error(string`no such puzzle: ${puzzleStr}`);
    }
}

function toPuzzleId(InputPuzzleId input) returns PuzzleId {
    int[] parts = [];

    if input.part is int {
        parts.push(<int>input.part);
    } else {
        parts.push(1);
        parts.push(2);
    }

    return {
        day: input.day,
        parts: parts
    };
}

function puzzleRun(
     PuzzleId puzzleId
    ,int[] expected
    ,function(int[]) returns record { int? part1; int? part2; } runner
) {
    var answers = runner(puzzleId.parts);

    if answers.part1 is int {
        io:println(string`(day ${puzzleId.day})(part 1)(answer ${<int>answers.part1})`);
        test:assertEquals(answers.part1, expected[0], string`Wrong answer to part 1`);
    }

    if answers.part2 is int {
        io:println(string`(day ${puzzleId.day})(part 2)(answer ${<int>answers.part2})`);
        test:assertEquals(answers.part2, expected[1], string`Wrong answer to part 2`);
    }
}
