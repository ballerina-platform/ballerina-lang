function getError() returns error? {
    return error("error");
}

function testNonTransactionalScope1() returns error? {
    transaction {
     do {
        check commit;
        check getError();
     } on fail {
        check commit; // Error: commit is not allowed here
     }
    }
}

function testNonTransactionalScope2() {
    transaction {
     do {
        check commit;
        check getError();
     } on fail {
     }
    }
}

function testPreCommitExit1() {
    transaction { // Error: transaction exit prior to commit/rollback identified]
     do {
        check getError();
        check commit;
     } on fail {
     }
    }
}

function testPreCommitExitWithRollback() {
    transaction {
     do {
        check getError();
        check commit;
     } on fail {
        rollback;
     }
    }
}

function testPreCommitExit2() {
    transaction { // invalid transaction commit count
                  // transaction exit prior to commit/rollback identified
     do {
        check getError();
     } on fail {
     }
    }
}

function testCommittedExit1() returns error? {
    transaction {
     do {
        check getError();
     } on fail {
     }
     check commit;
    }
}

function testPreCommitExitWithRollback2() returns error? {
    transaction { // invalid transaction commit count
     do {
        check getError();
     } on fail {
     }
     rollback;
    }
}

function testPreCommitExit3() returns error? {
    transaction {
       transaction { // error: exit from transaction prior to commit/rollback identified
          do {
             check getError();
             check commit;
          } on fail {
          }
       }
       check commit;
    }
}

function testCommittedExit2() returns error? {
    transaction {
       transaction {
          do {
             check commit;
             check getError();
          } on fail {
          }
       }
       check commit;
    }
}

function testCommittedExit3() returns error? {
    transaction {
       do {
             check getError();
             check commit;
          } on fail {
             rollback;
          }
       transaction {
          check commit;
       }
    }
}

function testCommittedExit4() {
    transaction {
        do {
            check commit;
        } on fail {
            rollback; // Error: rollback is not allowed here
        }
    }
}

function testFailingCheckExpression() {
    transaction {
        do {
            check getError();
            check commit;
        } on fail {
            rollback;
        }
    }
}

function testNestedTransactions1() returns error? {
    transaction { // Error: exit from transaction prior to commit/rollback identified
                  // Error: invalid commit count
        do {
            transaction {
                check getError();
                check commit;
            }
        } on fail {
            rollback;
        }
    }
}

function testNestedTransactions2() returns error? {
    transaction {
        do {
            transaction {
                check getError();
                check commit;
            }
        } on fail {
            rollback;
        }
        check commit; // Error: commit not allowed here
    }
}

function testMultipleCheckExpressions() returns error? {
    transaction {
        do {
            check getError();
            check getError();
            check commit;
        } on fail {
            rollback;
        }
    }
}

function testPreCommitExitWithNestedTransaction() returns error? {
    transaction {
        transaction { // Error: exit from transaction prior to commit/rollback identified
            do {
                check getError();
                check commit;
            } on fail {
            }
        }
        check commit;
    }
}

function testNestedTransactionRollback() returns error? {
    transaction {
        transaction {
            do {
                check getError();
                check commit;
            } on fail {
                rollback;
            }
        }
        check commit;
    }
}

function testPreCommitExitNoRollback() returns error? {
    transaction {
        transaction { // Error: exit from transaction prior to commit/rollback identified
            do {
                check getError();
                check commit;
            } on fail {
                // No rollback here
            }
        }
        check commit;
    }
}

function testMultiplePreCommitExits1() returns error? {
    transaction { // Error: exit from transaction prior to commit/rollback identified
        do {
            check getError();
            check commit;
        } on fail {
            // No rollback here
        }
        do {
            check getError();
            check commit; // Error: commit not allowed
        } on fail {
            // No rollback here
        }
    }
}

function testCommittedExit5() returns error? {
    transaction {
        do {
            check getError();
            check commit;
        } on fail {
            // No rollback here
        }
        check commit; // Error: commit not allowed
    }
}

function testMultiplePreCommitExits2() returns error? {
    transaction {
        do {
            check getError();
            check commit;
        } on fail {
            // No rollback here
        }
        do {
            check getError();
            check commit; // Error: commit not allowed
        } on fail {
            rollback;
        }
    }
}

function testPreCommitExit4(boolean foo) returns error? {
    transaction { // Error: exit from transaction prior to commit/rollback identified
      if foo {
        check commit;
        return;
      }
    }
}

function testCommittedExit6(boolean foo) returns error? {
    transaction {
      if foo {
        check commit;
        return;
      } else {
        rollback;
      }
    }
}
