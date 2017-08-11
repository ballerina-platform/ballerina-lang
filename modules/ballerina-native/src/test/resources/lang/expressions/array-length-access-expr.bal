
function arrayLengthAccessNullArrayCase() {

    fork {
        worker forkWorker1 {
        }
        worker forkWorker2 {
        }
    } join (all) (map m) {
    }


    worker newWorker1 {
       int a = 5;
    }
    worker newWorker2 {
       int b = 5;
    }
}