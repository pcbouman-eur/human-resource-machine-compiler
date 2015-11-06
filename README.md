# Human Resource Machine Compiler
A JParsec based compiler for a very simple language that generates code for the game Human Resource Machine as an output.

This project is mostly for fun; it allows you to write loops and things in a more structured way than the game allows
you to, but ultimately it is probably not useful if you want optimized solutions.

It requires the JParsec library to function and makes use of Java 8. The documentation is mostly lacking,
although the source code of the parser class has some explanation of the grammar of the language.

JParsec is imported via maven. I don't use maven for anything else at the moment.

# Example
The following example is an input file which solves one of the later levels in the game:
```
#MEMSIZE 8
#CONSTANT div 15
#CONSTANT zero 14

// Recoordinator

routine main
{
  repeat
  {
	 rem = sub in div;
	 div();
	 out add rem four;
	 out i;
  }  
}

routine div
{
  i = zero;
  while not isneg(rem)
  {
     rem = sub rem div;
     i++;
  }
}
```

In this level, the divisor value (4) is stored at tile location 15. The constant 'zero' is stored at tile location 14.
The routine *div()* performs a tail division on the variable *rem* storing the answer in variable *i*.
Variables are global and shared between routines.

The generated output is:
```
-- HUMAN RESOURCE MACHINE PROGRAM --

a:
    COPYFROM 15
    COPYTO   8
    INBOX   
    SUB      8
    COPYTO   8
    COPYFROM 14
    COPYTO   7
b:
    COPYFROM 8
    JUMPN    c
    COPYFROM 15
    COPYTO   6
    COPYFROM 8
    SUB      6
    COPYTO   8
    BUMPUP   7
    JUMP     b
c:
    COPYFROM 6
    COPYTO   5
    COPYFROM 8
    ADD      5
    OUTBOX  
    COPYFROM 7
    OUTBOX  
    JUMP     a
```

On Windows, you can just code the code to the clipboard and use the paste feature of the game to get it into the game.

# Some Remarks
- Adding and subtracting is kind of weird now, as proper parsing of arithmetic expressions requires an OperatorTable.
- Code generation is directly to string output. In case someone is crazy enough to write a backend optimization module,
  this should probably have its own data structure as well. 
