@256
D=A
@SP
M=D
@Sys.init
0;JMP
(Sys.init)
@Sys.init.locals
M=0
(Sys.init.locals_loop)
@0
D=A
@Sys.init.locals
D=D-M
@Sys.init.end_locals_loop
D;JEQ
@SP
M=M+1
A=M-1
M=0
@Sys.init.locals
M=M+1
@Sys.init.locals_loop
0;JMP
(Sys.init.end_locals_loop)
@4
D=A
@SP
A=M
M=D
@SP
M=M+1
@Main.fibonacci.return_0
D=A
@SP
M=M+1
A=M-1
M=D
@LCL
D=M
@SP
M=M+1
A=M-1
M=D
@ARG
D=M
@SP
M=M+1
A=M-1
M=D
@THIS
D=M
@SP
M=M+1
A=M-1
M=D
@THAT
D=M
@SP
M=M+1
A=M-1
M=D
D=A+1
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Main.fibonacci
0;JMP
@Main.fibonacci.return_0
(Sys.init$WHILE)
@Sys.init$WHILE
0;JMP
(Main.fibonacci)
@Main.fibonacci.locals
M=0
(Main.fibonacci.locals_loop)
@0
D=A
@Main.fibonacci.locals
D=D-M
@Main.fibonacci.end_locals_loop
D;JEQ
@SP
M=M+1
A=M-1
M=0
@Main.fibonacci.locals
M=M+1
@Main.fibonacci.locals_loop
0;JMP
(Main.fibonacci.end_locals_loop)
@ARG
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
@2
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
@SP
AM=M-1
D=M
@Main.fibonacci$IF_TRUE
D;JNE
@Main.fibonacci$IF_FALSE
0;JMP
(Main.fibonacci$IF_TRUE)
@ARG
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@R14
M=D
@5
A=D-A
D=M
@R15
M=D
@SP
M=M-1
A=M
D=M
@ARG
A=M
M=D
D=A+1
@SP
M=D
@R14
AM=M-1
D=M
@THAT
M=D
@R14
AM=M-1
D=M
@THIS
M=D
@R14
AM=M-1
D=M
@ARG
M=D
@R14
A=M-1
D=M
@LCL
M=D
@R15
A=M
0;JMP
(Main.fibonacci$IF_FALSE)
@ARG
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
@2
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
M=M-D
@Main.fibonacci.return_1
D=A
@SP
M=M+1
A=M-1
M=D
@LCL
D=M
@SP
M=M+1
A=M-1
M=D
@ARG
D=M
@SP
M=M+1
A=M-1
M=D
@THIS
D=M
@SP
M=M+1
A=M-1
M=D
@THAT
D=M
@SP
M=M+1
A=M-1
M=D
D=A+1
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Main.fibonacci
0;JMP
@Main.fibonacci.return_1
@ARG
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
M=M-D
@Main.fibonacci.return_2
D=A
@SP
M=M+1
A=M-1
M=D
@LCL
D=M
@SP
M=M+1
A=M-1
M=D
@ARG
D=M
@SP
M=M+1
A=M-1
M=D
@THIS
D=M
@SP
M=M+1
A=M-1
M=D
@THAT
D=M
@SP
M=M+1
A=M-1
M=D
D=A+1
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Main.fibonacci
0;JMP
@Main.fibonacci.return_2
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
@LCL
D=M
@R14
M=D
@5
A=D-A
D=M
@R15
M=D
@SP
M=M-1
A=M
D=M
@ARG
A=M
M=D
D=A+1
@SP
M=D
@R14
AM=M-1
D=M
@THAT
M=D
@R14
AM=M-1
D=M
@THIS
M=D
@R14
AM=M-1
D=M
@ARG
M=D
@R14
A=M-1
D=M
@LCL
M=D
@R15
A=M
0;JMP
