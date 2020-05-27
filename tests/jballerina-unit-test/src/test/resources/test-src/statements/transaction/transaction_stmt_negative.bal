
function commitExpMissingInTransactionStmt(int i) returns (string) {
    string a = "start";

    transaction {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " rollback";
            rollback;
        }
        rollback;
        a = a + " endTrx";
    }
    return a;
}

transactional function txStmtWithinTransactionalScope(int i) returns (string) {
    string a = "start";
    var o = testTransactionalInvo(a);
    transaction {
        a = a + " inTrx";
        if (i == -1) {
            a = a + " rollback";
            rollback;
        }
        var res = commit;
        a = a + " endTrx";
    }
    var c = start testInvo(a);
    return a;
}

function invocationsWithinTx(int i) returns (string) {
   string a = "start";

   transaction {
      a = a + " inTrx";
      var b = start testInvo(a);
      var c = commit;
      var d = testTransactionalInvo(a);
   }
   return a;
}

function txWithMultiplePaths(int i)  {
    string a = "start";

    transaction {
        a = a + " inTrx";
        if(i == 3) {
            a = a + " inIf3";
            var b = testTransactionalInvo(a);
            var o = commit;
        } else {
            a = a + " inElse";
            var b = testTransactionalInvo(a);
            rollback;
        }
        var o = commit;
    }

    transaction {
        a = a + " inTrx";
        if(i == 3) {
            a = a + " inIf3";
            var b = testTransactionalInvo(a);
            var o = commit;
        } else if (i == 5) {
            a = a + " inIf5";
            var b = testTransactionalInvo(a);
            //var o = commit;
        }
        var o = commit;
    }
}

function testInvo(string str) returns string {
 return str + " non-transactional call";
}

transactional function testTransactionalInvo(string str) returns string {
    return str + " transactional call";
}

//function txStmtWithInvalidRollbackExp(int i) returns (string) {
//     string a = "start";
//     transaction {
//         a = a + " inTrx";
//         if (i == 0) {
//             a = a + " rollback";
//             rollback "rollback str";
//         }
//         var res = commit;
//         a = a + " endTrx";
//     }
//     return a;
//}

//function testTransactionAbort3() {
//    int i = 10;
//    abort;
//    i = i + 11;
//}
//
//function testTransactionAbort4() {
//    int i = 10;
//    transaction {
//        i = i + 1;
//        abort;
//        i = i + 2;
//    }
//}
//
//function testTransactionAbort5() {
//    int i = 10;
//    transaction {
//        i = i + 1;
//        if (i > 10) {
//            abort;
//        }
//        while (i < 40) {
//            i = i + 2;
//            if (i == 44) {
//                break;
//                int k = 9;
//            }
//        }
//        abort;
//        i = i + 2;
//    }
//}
//
//function testBreakWithinTransaction() returns (string) {
//    int i = 0;
//    while (i < 5) {
//        i = i + 1;
//        transaction {
//            if (i == 2) {
//                break;
//            }
//        }
//    }
//    return "done";
//}
//
//function testNextWithinTransaction() returns (string) {
//    int i = 0;
//    while (i < 5) {
//        i = i + 1;
//        transaction {
//            if (i == 2) {
//                continue;
//            }
//        }
//    }
//    return "done";
//}
//
//function testReturnWithinTransaction() returns (string) {
//    int i = 0;
//    while (i < 5) {
//        i = i + 1;
//        transaction {
//            if (i == 2) {
//                return "ff";
//            }
//        }
//    }
//    return "done";
//}
//
//function testInvalidDoneWithinTransaction() {
//    string workerTest = "";
//
//    int i = 0;
//    transaction {
//        workerTest = workerTest + " withinTx";
//        if (i == 0) {
//            workerTest = workerTest + " beforeDone";
//            return;
//        }
//    }
//    workerTest = workerTest + " beforeReturn";
//    return;
//}
//
//function testReturnWithinMatchWithinTransaction() returns (string) {
//    int i = 0;
//    string|int unionVar = "test";
//    while (i < 5) {
//        i = i + 1;
//        transaction {
//            if (unionVar is string) {
//                if (i == 2) {
//                    return "ff";
//                }
//            } else {
//                if (i == 2) {
//                    return "ff";
//                }
//            }
//
//        }
//    }
//    return "done";
//}
