// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

	@8192
	D=A
	@top
	M=D

   (START)
	@0
	D=A
	@i
	M=D-1
   (LOOP)
	@i
	M=M+1
	
	D=M
	@top
	D=M-D
	@START
	D;JEQ

	@KBD
	D=M

	@BLACK
	D;JNE

	@i
	D=M
	@SCREEN
	A=A+D
	M=0
	@LOOP
	0;
   (BLACK)
	@i
	D=M
	@SCREEN
	A=A+D
	M=-1
	@LOOP
	0;JMP


