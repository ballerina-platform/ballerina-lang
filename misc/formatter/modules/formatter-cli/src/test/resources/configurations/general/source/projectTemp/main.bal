const int MAX_STUDENTS = 100;
const string WELCOME_MESSAGE = "Welcome";
const int MAX_AGE = 20;

type Student record {
    string name;
    int age;
};

function registerStudent(Student[] students, string name, int age, int numStudents) returns boolean {
    if numStudents  <  MAX_STUDENTS {
        students.push({name, age});
        return true;
    } else   {
    return false;
    }
}

function queryTest() {
    int[] nums  =  [1 , 2,3,  4];
    int[] numsTimes10 = from var i in nums select i * 10;
    int[] evenNums = from int i in nums
        where i  % 2  == 0
        select i;
    int[] numsReversed = from int  i in nums
        order  by i   descending
    select i;
}
