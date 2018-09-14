import ballerina/io;

function getAgeCategory(int age) returns string|error {
    if (age < 0) {
        error e = { message: "Invalid" };
        return e;
    } else if (age <= 18) {
        return "Child";
    } else {
        return "Adult";
    }
}

function main(string... args) {
    string ageCategory = getAgeCategory(25) but{
   string s=>s,
                  error e=>e.message};

    ageCategory =  getAgeCategory(-5)    but{string s=>s,error e   =>    e.message} ;

    ageCategory = getAgeCategory(25) but {
            error e   =>    e.message ,
            string s=>getAgeCategory(-5) but{string s1 => s1,
                                error e1 => e1.message}};
}

