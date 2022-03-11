import aoc21.util;
import ballerina/io;

public function run(int[] parts) returns record { int? part1; int? part2; } {
    // read puzzle data -------------------------------------------------------
    final string[] stringData = checkpanic io:fileReadLines("../../inputs/2021-04");
    final string[] numbers = util:split(stringData.remove(0));
    final string[][] boardRawData = stringData.map(function (string line) returns string[] {
        return util:split(util:unpad(line), " ");
    }).filter(function (string[] line) returns boolean {
        return line.length() == BOARD_SIZE;
    });
    final Number[][] boardData = boardRawData.map(function (string[] line) returns Number[]{
        return line.map(function (string value) returns Number {
            return {
                number: value,
                marked: false
            };
        });
    });

    // io:println(boardData);

    map<Number[][]> boards = {};
    {
        int boardId = 1;
        Number[][] board = [];
        int i = 0;
        foreach var line in boardData {
            if i < BOARD_SIZE {
                board.push(line);
                i += 1;
            } else {
                boards[boardId.toString()] = board.clone();
                board.removeAll();
                i = 1;
                boardId += 1;
                board.push(line);
            }
        }
        boards[boardId.toString()] = board.clone();
    }

    // io:println(boards);

    final BingoData puzzleData = {
        numbers: numbers,
        boards: boards
    };

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

type BingoData record {
    string[] numbers;
    map<Number[][]> boards;
};

type Number record {
    string number;
    boolean marked;
};

type NumberMetaData record {
    string number;
    string boardId;
    int row;
    int col;    
};

type BoardMetaData record {
    string boardId;
    int[] markedOnRows;
    int[] markedOnCols;
};

function part1(BingoData bingoData) returns int {
    final map<BoardMetaData> boardMetaData = initBoardMetaData(bingoData);
    final map<NumberMetaData[]> numberMetaData = initNumberMetaData(bingoData);

    final var runBingoFn = function () returns NumberMetaData? {
        foreach var number in bingoData.numbers {
            NumberMetaData[] x = numberMetaData.get(number);
            foreach var nmd in x {
                var board = <Number[][]>bingoData.boards[nmd.boardId];
                board[nmd.row][nmd.col].marked = true;

                var metaBoard = <BoardMetaData>boardMetaData[nmd.boardId];
                metaBoard.markedOnCols[nmd.col] += 1;
                metaBoard.markedOnRows[nmd.row] += 1;

                if metaBoard.markedOnCols[nmd.col] == BOARD_SIZE ||
                   metaBoard.markedOnRows[nmd.row] == BOARD_SIZE {
                    return nmd;
                }
            }
        }
        return ();
    };

    final NumberMetaData bingoNumber = <NumberMetaData>runBingoFn();

    final Number[][] board = <Number[][]>bingoData.boards[bingoNumber.boardId];
    final int sumOfUnmarked = board.reduce(sumOfUnmarkedFn, 0);
    
    return checkpanic int:fromString(bingoNumber.number) * sumOfUnmarked;
}

function part2(BingoData bingoData) returns int {
    final map<BoardMetaData> boardMetaData = initBoardMetaData(bingoData);
    final map<NumberMetaData[]> numberMetaData = initNumberMetaData(bingoData);

    final var runBingoFn = function () returns NumberMetaData? {
        foreach var number in bingoData.numbers {
            NumberMetaData[] x = numberMetaData.get(number);
            foreach var nmd in x {
                var board = bingoData.boards[nmd.boardId];
                if board is () { continue; }
                board[nmd.row][nmd.col].marked = true;

                var metaBoard = <BoardMetaData>boardMetaData[nmd.boardId];
                metaBoard.markedOnCols[nmd.col] += 1;
                metaBoard.markedOnRows[nmd.row] += 1;

                if metaBoard.markedOnCols[nmd.col] == BOARD_SIZE ||
                   metaBoard.markedOnRows[nmd.row] == BOARD_SIZE {
                    if bingoData.boards.length() > 1 {
                        var _ = bingoData.boards.remove(nmd.boardId);
                    } else {
                        return nmd;
                    }
                }
            }
        }
        return ();
    };

    final NumberMetaData bingoNumber = <NumberMetaData>runBingoFn();

    final Number[][] board = <Number[][]>bingoData.boards[bingoNumber.boardId];
    final int sumOfUnmarked = board.reduce(sumOfUnmarkedFn, 0);
    
    return checkpanic int:fromString(bingoNumber.number) * sumOfUnmarked;
}

function sumOfUnmarkedFn(int accu, Number[] numbers) returns int {
    return numbers.reduce(function (int total, Number number) returns int {
        if !number.marked {
            return total + checkpanic int:fromString(number.number);
        } else {
            return total;
        }           
    }, accu);
}

function initBoardMetaData(BingoData bingoData) returns map<BoardMetaData>{
    map<BoardMetaData> boardMetaData = {};
    foreach var boardId in bingoData.boards.keys() {
        boardMetaData[boardId] = {
            boardId: boardId,
            markedOnRows: [0, 0, 0, 0, 0],
            markedOnCols: [0, 0, 0, 0, 0]
        };
    }
    return boardMetaData;    
}

function initNumberMetaData(BingoData bingoData) returns map<NumberMetaData[]> {
    map<NumberMetaData[]> numberMetaData = {};
    bingoData.numbers.forEach(function (string number){
        numberMetaData[number] = [];
    });
    foreach var item in bingoData.boards.entries() {
        final string boardId = item[0];
        final Number[][] board = item[1];
        int x = 0; // column
        int y = 0; // row
        foreach var row in board { // row = y
            foreach var number in row { // column = x
                (<NumberMetaData[]>numberMetaData[number.number]).push({ number: number.number, boardId: boardId, row: y, col: x });
                x += 1;
            }
            x = 0;
            y += 1;
        }
    }

    return numberMetaData;
}

const BOARD_SIZE = 5;