import ballerina/http;

public function main() {

fork {
        worker w1 returns [int] {
            int j = 23;
            http:Client myClient = new ("");
            return [j];
        }
    }
}

