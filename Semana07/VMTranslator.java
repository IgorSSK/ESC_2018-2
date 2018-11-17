import java.awt.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

import javax.print.DocFlavor.STRING;


class VMTranslator{

    public static void main(String[] args) {
        if(args.length != 0){
        LerArquivo(args[0]);    
        }else{
            System.out.println("Err[0] = Caminho para o arquivo inválido ou inexistente!");
        }
    }

    public static void LerArquivo(String path){
        
        try {
            FileInputStream input = new FileInputStream(path);
            File output = new File(path);

            Parser parse = new Parser(input);
            CodeWritter cWritter = new CodeWritter(output);
            cWritter.setFileName();
            
            while(parse.hasMoreCommands()){
                parse.advance();

                if(parse.commandType(parse.cur_line) == ("C_ARITHMETIC")){
                    cWritter.writeArithmetic(parse.arg1());
                }else if(parse.commandType(parse.cur_line) == ("C_PUSH")){
                    cWritter.writePushPop("C_PUSH", parse.arg1(), parse.arg2());
                }else if(parse.commandType(parse.cur_line) == ("C_POP")){
                    cWritter.writePushPop("C_POP", parse.arg1(), parse.arg2());
                }
                
                System.out.println(parse.cur_line);
                
            }

            parse.close();
            cWritter.close();

            // Scanner scan = new Scanner(file);
            // List list = new List();

            // if(file.available() != -1){
            
            //     while(scan.hasNext()){
            //         String line = scan.nextLine();
            //         System.out.print(line);
            //         list.add(line);

            //     }
            // }
            // file.close();
            // scan.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally{
            
        }
    }
    
}

class Parser{

    Scanner file;
    String cur_line;

    public Parser(FileInputStream file){
        this.file = new Scanner(file);
        this.cur_line = "";    
    }

    public boolean hasMoreCommands(){
        return file.hasNext();
    }

    public String advance(){
        return cur_line = file.nextLine();
    }

    public String commandType(String command){
        return commands().get(command.split(" ")[0].trim().toLowerCase());
    }

    public String arg1(){
        String[] istr = cur_line.split(" ");
        String arg = "";
        if(!(istr[0].isEmpty())){
            if(istr.length == 1){
                arg = istr[0];
            }else if(istr.length >= 2){
                arg = istr[1];
            }
        }

        return arg;
    }

    public int arg2(){
        String[] istr = cur_line.split(" ");
        int arg = 0;
        if(!(istr[0].isEmpty())){
            if(istr.length == 3){
                arg = Integer.parseInt(istr[2]);
            }
        }

        return arg;
    }

    public void close(){
        file.close();
    }

    private HashMap<String,String> commands(){
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("add", "C_ARITHMETIC");
        map.put("sub", "C_ARITHMETIC");
        map.put("neg", "C_ARITHMETIC");
        map.put("eq", "C_ARITHMETIC");
        map.put("gt", "C_ARITHMETIC");
        map.put("lt", "C_ARITHMETIC");
        map.put("and", "C_ARITHMETIC");
        map.put("or", "C_ARITHMETIC");
        map.put("not", "C_ARITHMETIC");
        map.put("push", "C_PUSH");
        map.put("pop", "C_POP");
        map.put("label", "C_LABEL");
        map.put("goto", "C_GOTO");
        map.put("if-goto", "C_IF");
        map.put("function", "C_FUNCTION");
        map.put("return", "C_RETURN");
        map.put("call", "C_CALL");

        return map;
    }

}

class CodeWritter{

    PrintStream file;
    File in;
    String fileName;
    int bool_count;

    public CodeWritter(File file){
        this.in = file;
        this.fileName = file.getName();
        this.bool_count = 0;
    }

    public void setFileName(){
        try{
        String nFilePath = in.getAbsolutePath().replace(".vm", ".asm");
        File nFile = new File(nFilePath);
        
        //Files.createFile(Paths.get("/home/igors/Documentos/UFU/ESC/Semana07/texto.txt"));
        
        nFile.getParentFile().mkdirs();
        nFile.createNewFile();
        if(nFile.exists()){
            this.file = new PrintStream(nFile);
        }
    }catch(Exception e){
        System.out.print(e.getMessage());
    }
    }

    public void writeArithmetic(String command){

    if(command != "neg" && command != "not"){
        this.popD();
    }
    this.decrementStack();
    this.putAonStack();

    switch(command){
        case "add":
            this.file.println("M=M+D");
            break;
        case "sub":
            this.file.println("M=M-D");
            break;
        case "and":
            this.file.println("M=M&D");
            break;
        case "or":
            this.file.println("M=M|D");
            break;
        case "neg":
            this.file.println("M=-M");
            break;  
        case "not":
            this.file.println("M=!M");
            break;
        case "eq":
            this.writeBoolean("eq");
            break; 
        case "gt":
            this.writeBoolean("gt");
            break; 
        case "lt":
            this.writeBoolean("lt");
            break; 

    };
}
    public void writeBoolean(String oper){
        this.file.println("D=M-D");
        this.file.println(String.format("@BOOL%d", bool_count));

         if(oper.equals("eq")){
             this.file.println("D;JEQ");
         }
         if(oper.equals("gt")){
             this.file.println("D;JGT");
         }
         if(oper.equals("lt")){
             this.file.println("D;JLT");
         }
         
         this.putAonStack();
         this.file.println("M=0");
         this.file.println(String.format("@ENDBOOL%d", bool_count));
         this.file.println("0;JMP");

         this.file.println(String.format("(BOOL%d)", bool_count));
         this.putAonStack();
         this.file.println("M=-1");

         this.file.println(String.format("(ENDBOOL%d)", bool_count));
         bool_count++;
    }

    public void pushD(){

        this.file.println("@SP"); 
        this.file.println("A=M"); 
        this.file.println("M=D"); 
        this.file.println("@SP");
        this.file.println("M=M+1");
    }

    public void popD(){

        this.file.println("@SP"); 
        this.file.println("M=M-1"); 
        this.file.println("A=M"); 
        this.file.println("D=M");

    }

    public void decrementStack(){
        this.file.println("@SP");
        this.file.println("M=M-1");
    }

    public void incrementStack(){
        this.file.println("@SP");
        this.file.println("M=M+1");
    }

    public void putAonStack(){
        this.file.println("@SP");
        this.file.println("A=M");
    }

    public void writePushPop(String command, String segment, int index){
       //Setting Adress 
        String address = this.address().get(segment);
        if(segment.equals("constant")){
            this.file.println("@" + index);
        }
        if(segment.equals("static")){
            this.file.println("@" + this.fileName + "." + index);
        }
        if(segment.equals("pointer") || segment.equals("temp")){
            this.file.println("@R" + (Integer.parseInt(address) + index));
        }
        if(segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")){
            this.file.println("@" + address);
            this.file.println("D=M");
            this.file.println("@" + index);
            this.file.println("A=D+A");
        }
        
        if(command.equals("C_PUSH")){
            if(segment.equals("constant")){
                this.file.println("D=A");
            }else{
            this.file.println("D=M");
            }
            this.pushD();
        }
        if(command.equals("C_POP")){ 
            this.file.println("D=A");
            this.file.println("@R13");
            this.file.println("M=D");
            this.popD();
            this.file.println("@R13");
            this.file.println("A=M");
            this.file.println("M=D");
        }
    }

    public void close(){
        file.close();
    }

    private HashMap<String,String> address(){
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("local", "LCL");    //R1
        map.put("argument", "ARG"); //R2
        map.put("this", "THIS");    // R3
        map.put("that", "THAT");    // R4
        map.put("pointer", "3");    // R3); R4
        map.put("temp", "5");       // R5-12
                                    //R13-15 free space
        map.put("static", "16");    //R16-255


        return map;
    }


}