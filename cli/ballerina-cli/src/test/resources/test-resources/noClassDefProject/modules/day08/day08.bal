import aoc21.util;
import ballerina/io;
import ballerina/lang.array;
public function run(int[] parts) returns record { int? part1; int? part2; } {
    // read puzzle data -------------------------------------------------------
    string[] stringData = checkpanic io:fileReadLines("../../inputs/2021-08");
    PuzzleData[] puzzleData = stringData.map(function (string line) returns PuzzleData {
        final string[] splitted = util:split(line, " | ");
        return {
            signalPatterns: util:split(splitted[0], " "),
            outputValues: util:split(splitted[1], " ")
        };
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

type PuzzleData record {
    string[] signalPatterns;
    string[] outputValues;
};

function part1(PuzzleData[] data) returns int {
    return data.reduce(function (int total1, PuzzleData data1) returns int {
        return data1.outputValues.reduce(function (int total2, string outputValue) returns int {
            final int len = outputValue.length();
            final int adder = (len == 2 || len == 3 || len == 4 || len == 7) ? 1 : 0;
            return total2 + adder;
        }, total1);
    }, 0);
}

function stringToSet(string str) returns util:StringSet {
    util:StringSet set = new;
    final var chars = util:splitToChar(str);
    foreach var char in chars {
        set.add(char);
    }
    return set;
}

function part2(PuzzleData[] data) returns int {
    int total = 0;
    foreach var item in data {
        map<util:StringSet> signals = {
            "STEP1": new,
            "STEP2": new,
            "STEP3": new,
            "STEP4": new,
            "STEP5": new
        };

        {
            string[] step4RawStrings = [];
            string[] step5RawStrings = [];

            foreach var signalPattern in item.signalPatterns {
                final var signalPatternChars = util:splitToChar(signalPattern); 
                match signalPattern.length() {
                2 => {
                        foreach var char in signalPatternChars {
                            signals.get("STEP1").add(char);
                        }
                }
                3 => {
                        foreach var char in signalPatternChars {
                            signals.get("STEP2").add(char);
                        }
                }
                4 => {
                        foreach var char in signalPatternChars {
                            signals.get("STEP3").add(char);
                        }
                }
                5 => {
                        step5RawStrings.push(signalPattern);
                }
                6 => {
                        step4RawStrings.push(signalPattern);
                    }
                }
            }

            var decodeRawStrings = function (string[] rawStrings, string key) {
                map<int> charCounts = {"a": 0, "b": 0, "c": 0, "d": 0, "e": 0, "f": 0, "g": 0 };
                foreach var rawString in rawStrings {
                    foreach var char in rawString {
                        charCounts[char] = (charCounts[char] ?: 0) + 1;
                    }
                }
                foreach var char in charCounts.keys() {
                    if charCounts.get(char) < 3 {
                        signals.get(key).add(char);
                    }
                }
            };
            decodeRawStrings(step4RawStrings, "STEP4");
            decodeRawStrings(step5RawStrings, "STEP5");
        }
        // io:println("---");
        // io:print(" signals: ");
        // io:println(signals);

        map<util:StringSet> segments = {
            "SEG1": new,
            "SEG2": new,
            "SEG3": new,
            "SEG4": new,
            "SEG5": new,
            "SEG6": new,
            "SEG7": new
        };

        // STEP 1 -------------------------------------------------------------

        segments["SEG3"] = signals.get("STEP1");
        segments["SEG6"] = signals.get("STEP1");

        // io:print("segments: ");
        // io:println(segments);

        // STEP 2 -------------------------------------------------------------

        segments["SEG3"] = util:intersection(segments.get("SEG3"), signals.get("STEP2"));
        segments["SEG6"] = util:intersection(segments.get("SEG6"), signals.get("STEP2"));

        segments["SEG1"] = util:difference(signals.get("STEP2"), segments.get("SEG3"));

        // io:print("segments: ");
        // io:println(segments);

        // STEP 3 -------------------------------------------------------------

        segments["SEG3"] = util:intersection(segments.get("SEG3"), signals.get("STEP3"));
        segments["SEG6"] = util:intersection(segments.get("SEG6"), signals.get("STEP3"));

        segments["SEG2"] = util:difference(signals.get("STEP3"), segments.get("SEG3"));
        segments["SEG4"] = util:difference(signals.get("STEP3"), segments.get("SEG3"));

        // io:print("segments: ");
        // io:println(segments);

        // STEP 4 -------------------------------------------------------------
        {
            segments["SEG3"] = util:intersection(segments.get("SEG3"), signals.get("STEP4"));
            segments["SEG4"] = util:intersection(segments.get("SEG4"), signals.get("STEP4"));

            util:StringSet temp = util:difference(signals.get("STEP4"), segments.get("SEG3"));
            segments["SEG5"] = util:difference(temp, segments.get("SEG4"));

            // io:print("segments: ");
            // io:println(segments);
        }

        // STEP 5 -------------------------------------------------------------

        segments["SEG2"] = util:intersection(segments.get("SEG2"), signals.get("STEP5"));
        segments["SEG3"] = util:intersection(segments.get("SEG3"), signals.get("STEP5"));
        segments["SEG5"] = util:intersection(segments.get("SEG5"), signals.get("STEP5"));
        segments["SEG6"] = util:intersection(segments.get("SEG6"), signals.get("STEP5"));

        // io:print("segments: ");
        // io:println(segments);

        // STEP 6 -------------------------------------------------------------
        {
            util:StringSet[] uniqueSignals = [];
            foreach var key in segments.keys() {
                if segments.get(key).values().length() == 1 {
                    uniqueSignals.push(segments.get(key));
                }
            }
            foreach var segmentKey in segments.keys() {
                if segments.get(segmentKey).values().length() > 1 {
                    foreach var uniqueSignal in uniqueSignals {
                        segments[segmentKey] = util:difference(segments.get(segmentKey), uniqueSignal);
                    }

                }
            }
            // io:print("segments: ");
            // io:println(segments);
        }
        // STEP 7 -------------------------------------------------------------

        segments["SEG7"] = new;
        segments.get("SEG7").add("a");
        segments.get("SEG7").add("b");
        segments.get("SEG7").add("c");
        segments.get("SEG7").add("d");
        segments.get("SEG7").add("e");
        segments.get("SEG7").add("f");
        segments.get("SEG7").add("g");

        segments["SEG7"] = util:difference(segments.get("SEG7"), segments.get("SEG1"));
        segments["SEG7"] = util:difference(segments.get("SEG7"), segments.get("SEG2"));
        segments["SEG7"] = util:difference(segments.get("SEG7"), segments.get("SEG3"));
        segments["SEG7"] = util:difference(segments.get("SEG7"), segments.get("SEG4"));
        segments["SEG7"] = util:difference(segments.get("SEG7"), segments.get("SEG5"));
        segments["SEG7"] = util:difference(segments.get("SEG7"), segments.get("SEG6"));

        // io:print("segments: ");
        // io:println(segments);

        // resolve output values ----------------------------------------------
        {
            var resolver = function(string input) returns string {
                var outputValue = stringToSet(input);
                if input.length() == 6 { // 0, 6, 9
                    if !outputValue.has(segments.get("SEG4").values()[0]) {
                        return "0";
                    } else if !outputValue.has(segments.get("SEG3").values()[0]) {
                        return "6";
                    } else {
                        return "9";
                    }
                } else { // 2, 3, 5
                    if !outputValue.has(segments.get("SEG2").values()[0]) && !outputValue.has(segments.get("SEG6").values()[0]) {
                        return "2";
                    } else if !outputValue.has(segments.get("SEG2").values()[0]) && !outputValue.has(segments.get("SEG5").values()[0]) {
                        return "3";
                    } else {
                        return "5";
                    }
                }
            };
            string outputNumberStr = "";
            foreach var outputValue in item.outputValues {
                match outputValue.length() {
                    2 => { outputNumberStr += "1"; }
                    3 => { outputNumberStr += "7"; }
                    4 => { outputNumberStr += "4"; }
                    7 => { outputNumberStr += "8"; }
                    _ => { outputNumberStr += resolver(outputValue); }
                }
            }
            // io:print("output: ");
            // io:println(outputNumberStr);

            // calculate total
            total += util:str2int(outputNumberStr);
        }
    }
    return total;
}

function printSortedData(PuzzleData[] data) {
    // sort for "readability"
    foreach var item in data {
        string[] sorted = [];
        foreach var s in item.signalPatterns {
            string x = util:joinToString(array:sort(util:splitToChar(s)));
            sorted.push(x);
        }
        io:println(sorted);
    }
}