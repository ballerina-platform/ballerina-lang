# To run the program, put the code in a "hello-world.bal" file
# and use the "ballerina run main" command.
$ ballerina run main hello-world.bal
Hello, World!

# To build a program archive, you can use the
# "ballerina build main" command followed by
# one or more packages to be included into the archive.
$ ballerina build main hello-world.bal
$ ls
hello-world.bmz	hello-world.go

# This is how you specify the name of the archive file. In this case it is "hello.bmz".
$ ballerina build main hello-world.bal -o hello.bmz

# You can now run the archive directly using the "ballerina run main" command.
$ ballerina run main hello.bmz
Hello, World!

# Now that you are familiar with the hello world program,
# have a look at some other examples.
