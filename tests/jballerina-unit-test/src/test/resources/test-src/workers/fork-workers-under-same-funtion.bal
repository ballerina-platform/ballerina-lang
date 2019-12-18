function forkWithWorkers() returns int {
    fork {
        @concurrent{}
        worker forkWorker1 returns int {
            int a = 1;
            return a;
        }
        @concurrent{}
        worker forkWorker2 returns int {
            int a = 2;
            return a;
        }
    }
    @concurrent{}
    worker newWorker1 returns int {
        int a = 3;
        return a;
    }
    @concurrent{}
    worker newWorker2 returns int {
        int a = 4;
        return a;
    }

    map<int> result = wait {forkWorker1, forkWorker2, newWorker1, newWorker2};

    return result.get("forkWorker1") + result.get("forkWorker2") + result.get("newWorker1") + result.get("newWorker2");
}