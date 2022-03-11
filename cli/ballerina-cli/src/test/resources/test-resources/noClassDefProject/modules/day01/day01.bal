import ballerina/io;
public function run(int[] parts) returns record { int? part1; int? part2; } {
    // read puzzle data -------------------------------------------------------
    string[] stringData = checkpanic io:fileReadLines("resources/2021-01");
    int[] puzzleData = stringData.map(function (string line) returns int {
        return checkpanic 'int:fromString(line);
    });

    // run puzzle solvers -----------------------------------------------------
    final record { int? part1; int? part2; } answers = { part1: (), part2: () };
    foreach int part in parts {
        match part {
            1 => { answers.part1 = part1(puzzleData); }
            2 => { answers.part2 = part2(puzzleData); }
        }
    }
    return answers;
}

function part1(int[] depthMeasurements) returns int {
    int increments = 0;
    int oldValue = depthMeasurements[0];

    foreach int i in 1 ..< depthMeasurements.length() {
        int newValue = depthMeasurements[i];
        if newValue > oldValue {
            increments += 1;
        }
        oldValue = newValue;
    }

    return increments;
}

function part2(int[] depthMeasurements) returns int {
    final var sum = function(int total, int n) returns int { return total + n; };
    final int startIndexOfLastWindow = ((depthMeasurements.length() / WINDOW_SIZE) * WINDOW_SIZE) - 1;

    int increments = 0;

    int[] oldWindow = depthMeasurements.slice(0, WINDOW_SIZE);
    int oldValue = oldWindow.reduce(sum, 0);
    
    foreach int i in 1 ... startIndexOfLastWindow {
        int[] newWindow = depthMeasurements.slice(i, i + WINDOW_SIZE);
        int newValue = newWindow.reduce(sum, 0);

        if newValue > oldValue {
            increments += 1;
        }

        oldWindow = newWindow;
        oldValue = newValue;
    }
    
    return increments;
}

const WINDOW_SIZE = 3;