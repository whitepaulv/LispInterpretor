Lisp Interpretor:

Vince White, 
University of Alabama, 
CS 403: Programming Languages, 
Donald Yessick


This is my implementation of the lisp interpreter project I completed for CS403. 
The code has many comments explaining, and likely over-explaining, my thought process at points.
Some of the comments are notes to myself from when I fixed bugs, while others were comments
I put as I was understanding the process of designing lisp. 

Test cases are stored in test/, while test case output is stored in testOutput/. Random lines
are present in these files, which serve only to help me look at the results in chunks instead of
searching a long file for the exact test case I am looking for. The lines vaguely group the
test cases on what they are testing for.


Use of AI: 
AI was used to generate test cases for each stage of the project. It was also
intended to be used to confirm the test cases, but it was too unreliable and I instead
manually checked test cases. AI was also used at points to assist with finding bugs.

How to run:
if you wish to run the program with your own input:
javac Main.java
Java Main

if you wish to run my test cases:
javac Main.java
Java Main < test/test(x).txt > testOutput/testOutput(x).txt
(where (x) is to be replaced by the numeric value of whichever test cases you wish to run)


