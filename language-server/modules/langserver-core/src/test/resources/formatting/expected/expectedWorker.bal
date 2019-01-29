import ballerina/http;

function main(string... args) {
    worker default {
        int a = 0;
    }

    worker worker1 {
        int b = 0;
    }
}
