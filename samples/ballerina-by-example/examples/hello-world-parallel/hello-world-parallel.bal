@Description {value:"Workers don’t need to be explicitly started. They start at the same time as the default worker."}
function main (string[] args) {

    worker w1 {
        println("Hello, World! #m");
    }

    worker w2 {
        println("Hello, World! #n");
    }

    worker w3 {
        println("Hello, World! #k");
    }
}
