function func1() {
    typedesc<object {}> b = object {
        public string name = "";
    };
}

type RecordName1 record {
    int id;
    int name;

    object {
        string country;
        string street;
        string state;

        function init(int id, string country = "USA", string street = "",
            string state = "") {
            self.country = country;
            self.street = street;
            self.state = state;
        }

        function getAddress() returns string {
            return self.street + "," + self.state + "," + self.country;
        }
    } address;
};

function func2() {
    typedesc<object {}> b = object {public string name = "";};
}

type RecordName2 record {
    int id;
    int name;

    object {string country; string street; string state;} address;
};

type RecordName3 record {
    int id;
    int name;

    object {
        string country = "";
        string street = "";
        string state = "";

        function getAddress() returns string {
            return self.street + "," + self.state + "," + self.country;
        }
    } address;
};
