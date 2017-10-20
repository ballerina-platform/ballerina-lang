# To run the program, put the code in "hello-world.bal"
# and use "ballerina run" command.
$ ballerina run hello-world.bal
Hello, World!

# To build a compiled program file, we can use the
# "ballerina build" command followed by
# the ballerina source file or the package
# which contains the main function.
$ ballerina build hello-world.bal
$ ls
hello-world.balx	hello-world.bal

# The output file can be specified with the -o flag.
$ ballerina build hello-world.bal -o hello.balx

# Here is how you can run the program file.
$ ballerina run hello.balx
Hello, World!

# Now that we are familiar with the hello world program,
# let's dig a little deeper.