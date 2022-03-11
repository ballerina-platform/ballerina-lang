import ballerina/test;

final BingoData BINGO_DATA_1 = {
    numbers: ["7","4","9","5","11","17","23","2","0","14","21","24","10","16","13","6","15","25","12","22","18","20","8","19","3","26","1"],
    boards: {
        "1": [
            [{ number: "22", marked: false }, { number: "13", marked: false }, { number: "17", marked: false }, { number: "11", marked: false }, { number:  "0", marked: false }],
            [{ number:  "8", marked: false }, { number:  "2", marked: false }, { number: "23", marked: false }, { number:  "4", marked: false }, { number: "24", marked: false }],
            [{ number: "21", marked: false }, { number:  "9", marked: false }, { number: "14", marked: false }, { number: "16", marked: false }, { number:  "7", marked: false }],
            [{ number:  "6", marked: false }, { number: "10", marked: false }, { number:  "3", marked: false }, { number: "18", marked: false }, { number:  "5", marked: false }],
            [{ number:  "1", marked: false }, { number: "12", marked: false }, { number: "20", marked: false }, { number: "15", marked: false }, { number: "19", marked: false }]
        ],
        "2": [
            [{ number:  "3", marked: false }, { number: "15", marked: false }, { number:  "0", marked: false }, { number:  "2", marked: false }, { number: "22", marked: false }],
            [{ number:  "9", marked: false }, { number: "18", marked: false }, { number: "13", marked: false }, { number: "17", marked: false }, { number:  "5", marked: false }],
            [{ number: "19", marked: false }, { number:  "8", marked: false }, { number:  "7", marked: false }, { number: "25", marked: false }, { number: "23", marked: false }],
            [{ number: "20", marked: false }, { number: "11", marked: false }, { number: "10", marked: false }, { number: "24", marked: false }, { number:  "4", marked: false }],
            [{ number: "14", marked: false }, { number: "21", marked: false }, { number: "16", marked: false }, { number: "12", marked: false }, { number:  "6", marked: false }]
        ],
        "3": [
            [{ number: "14", marked: false }, { number: "21", marked: false }, { number: "17", marked: false }, { number: "24", marked: false }, { number:  "4", marked: false }],
            [{ number: "10", marked: false }, { number: "16", marked: false }, { number: "15", marked: false }, { number:  "9", marked: false }, { number: "19", marked: false }],
            [{ number: "18", marked: false }, { number:  "8", marked: false }, { number: "23", marked: false }, { number: "26", marked: false }, { number: "20", marked: false }],
            [{ number: "22", marked: false }, { number: "11", marked: false }, { number: "13", marked: false }, { number:  "6", marked: false }, { number:  "5", marked: false }],
            [{ number:  "2", marked: false }, { number:  "0", marked: false }, { number: "12", marked: false }, { number:  "3", marked: false }, { number:  "7", marked: false }]            
        ]
    }
};

@test:Config {}
function test_part_1() {
    test:assertEquals(part1(BINGO_DATA_1), 4512);
}

@test:Config {}
function test_part_2() {
    test:assertEquals(part2(BINGO_DATA_1), 1924);
}