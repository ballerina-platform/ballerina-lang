Note:
=====
`-e.bal` files in this test folder does not mean what it means in nBallerina repo. In this folder it means the 
tests should run even though jBallerina compiler frontend emit errors.
This was necessary as jBallerina emit errors for some valid type constructs such as empty tuple.
We need to make sure that type negation operator (!) is not used in these `-e.bal` tests.