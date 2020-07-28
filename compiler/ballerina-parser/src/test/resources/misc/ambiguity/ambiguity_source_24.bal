function foo() {
    x = a/<b>c;
    x = a/<b>;
    x = a/<b|d>c;
    x = a/<b|d>;

    x = a/ <b>c;
    x = a/ <b>;
    x = a/ <b|d>c;
    x = a/ <b|d>;

    x = a/<b>[c];
    x = a/<b>+5;
}
