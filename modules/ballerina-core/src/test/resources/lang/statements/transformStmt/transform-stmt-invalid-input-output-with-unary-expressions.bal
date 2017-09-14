function binaryExpInTransform() (boolean){
    boolean flag = false;
    boolean newFlag;
    transform {
        newFlag = !flag;
        flag = false;
    }
    return newFlag;
}
