function forkJoinWithWorkers () {
    worker default {
        fork {
            worker forkWorker1 {
                int a = 5;
            }

            worker forkWorker2 {
                int a = 5;
            }
        } join (all) (map m) {
        }
    }

    worker newWorker1 {
        int a = 5;
    }

    worker newWorker2 {
        int a = 5;
    }
}