import java.awt.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.print.DocFlavor.STRING;

import com.sun.java.accessibility.util.Translator;



class JackAnalyser{
    private static String cur_fileName;
    private static String new_fileName;

    public static void main(String[] args) {
        if(args.length != 0){
            String path = args[0];//"/home/igors/Documentos/UFU/ESC/Semana10/ExpressionLessSquare/Main.jack";//
            try{
                File files = new File(path);
                if(files.isDirectory()){
                    for (File file : files.listFiles()) {
                        cur_fileName = file.getName();
                        if(cur_fileName.endsWith(".jack")){
                            
                            LerArquivo(file.getAbsolutePath());
                        }
                    }
                }else if(files.isFile()){
                    cur_fileName = files.getName();
                        if(cur_fileName.endsWith(".jack")){
                            LerArquivo(files.getAbsolutePath());
                        }else{
                            System.out.println("Operação Interrompida! Arquivo de entrada não é do tipo .JACK");          
                        }    
                }
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            System.out.println("Err[0] = Caminho para o arquivo inválido ou inexistente!");
        }
    }

    public static void LerArquivo(String path){
        
        try {
            FileInputStream input = new FileInputStream(path);
            new_fileName = cur_fileName.replace(".jack", "_Tokens.xml");

            File output = new File(path.replace(cur_fileName, new_fileName));
            output.getParentFile().mkdirs();
            output.createNewFile();

            JackTokenizer jTokenizer = new JackTokenizer(input, output);
            
            while(jTokenizer.hasMoreTokens()){
                String token = jTokenizer.advance();

                if(!token.equals("")){
                    jTokenizer.writeFile();
                }
            }
            jTokenizer.close();

            File outputCompiler = new File(path.replace(".jack", ".xml"));
            outputCompiler.getParentFile().mkdirs();
            outputCompiler.createNewFile();

            JackCompiler jCompiler = new JackCompiler(new FileInputStream(path.replace(cur_fileName, new_fileName)), outputCompiler);
            jCompiler.Compile();
            jCompiler.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally{
            
        }
    }
    
}

class JackTokenizer{

    Scanner inputfile;
    String cur_line;
    String cur_token;
    boolean isReading = false;
    ArrayList<String> tokens;
    PrintStream outputfile;
    private HashMap<String,String> tokensTypes(){
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("KEYWORD", "keyword");
        map.put("SYMBOL", "symbol");
        map.put("IDENTIFIER", "identifier");
        map.put("INT_CONST", "integerConstant");
        map.put("STRING_CONST", "stringConstant");

        return map;
    } 
    private ArrayList<String> keywords = new ArrayList<String>();
    
    private ArrayList<String> symbols = new ArrayList<String>(); 

    public JackTokenizer(FileInputStream file, File outFile){
        this.inputfile = new Scanner(file);
        this.cur_line = "";
        this.cur_token = "";
        try {
            this.outputfile = new PrintStream(outFile);    
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.keywords.addAll(Arrays.asList(
        "class", "constructor", "function", "method",
        "field", "static", "var", "int", "char", "boolean",
        "void", "true", "false", "null", "this", "let", "do",
        "if", "else", "while", "return"
        ));
        this.symbols.addAll(Arrays.asList(
            "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-",
            "*", "/", "&", "|", "<", ">", "=", "~"
        ));
        tokens = new ArrayList<String>();
        this.outputfile.println("<tokens>");
    }

    public boolean hasMoreTokens(){
         if(this.isReading || inputfile.hasNext()){
            return true;
         }else{
             return false;
         }
    }

    public String advance(){
            if(!isReading){
            this.cur_line = inputfile.nextLine().replace("\n", "").replace("\t","").replace("\r","").trim();
            this.cur_token = "";
            System.out.println(this.cur_line);

            if((this.cur_line.contains("//") || this.cur_line.contains("/*")) && !this.cur_line.startsWith("/")){
                this.cur_line = this.cur_line.split("/")[0];
            }

            }
            if((!cur_line.contains("//")) && (!cur_line.contains("/*")) && (!cur_line.contains("*/")) && (!cur_line.equals(""))){
                char[] characters= cur_line.trim().toCharArray();
                int i = 0;
                
                if(tokens.isEmpty()){
                while(i <= characters.length -1){
                    
                    if(characters[i] == ' '){
                        tokens.add(cur_token);
                        cur_token = "";
                        i++;
                    }else if(symbols.contains(Character.toString(characters[i]))){
                        tokens.add(cur_token);
                        tokens.add(Character.toString(characters[i]));
                        cur_token = "";
                        i++;
                    }else if(characters[i] == '\"'){
                        i++;
                        while(characters[i] != '\"'){
                            cur_token = cur_token.concat(Character.toString(characters[i]));
                            i++;
                        }
                        tokens.add("\"" + cur_token + "\"");
                        cur_token = "";
                        i++;
                    }else{
                        cur_token = cur_token.concat(Character.toString(characters[i]));
                        i++;
                    }
                }
                cur_token = tokens.get(0);
                tokens.remove(0);
                this.isReading = true;
                return cur_token;
            }else{
                cur_token = tokens.get(0);
                tokens.remove(0);
                if(tokens.isEmpty()){
                    this.isReading = false;
                }
                return cur_token;                 
            }
        }else{
            return "";
        }
    }

    public String TokenType(String token){
        if(keywords.contains(token)){
            return "KEYWORD";            
        }else if(symbols.contains(token)){
            return "SYMBOL";
        }else if(token.startsWith("\"") && token.endsWith("\"")){
            return "STRING_CONST";
        }else {
            try {
            Integer.parseInt(token);
            return "INT_CONST";
        }catch(Exception e){
                return "IDENTIFIER";
            }
        }
    }

    public String Keyword(String token){
        return keywords.get(keywords.indexOf(token));
    }

    public String Symbol(String token){
        return symbols.get(symbols.indexOf(token));
    }

    public int IntValue(){
        return Integer.parseInt(cur_token);
    }

    public String StringValue(){
        return cur_token;
    }

    public String Identifier(){
        return cur_token;
    }

    public void writeFile(){

        String tkType = TokenType(this.cur_token);
        String tkValue = "";

        if(tkType == "KEYWORD"){
            tkValue = this.Keyword(this.cur_token).toLowerCase();
        }

        if(tkType == "SYMBOL"){
            tkValue = this.Symbol(this.cur_token);
            tkValue = tkValue.replace("&", "&amp;").replace("\"", "&quot;");
            tkValue = tkValue.replace("<", "&lt;").replace(">", "&gt;");
        }

        if(tkType == "IDENTIFIER"){
            tkValue = this.Identifier();
        }

        if(tkType == "INT_CONST"){
            tkValue = String.valueOf(this.IntValue());
        }

        if(tkType == "STRING_CONST"){
            tkValue = this.StringValue().replace("\"", "");

        }

        this.outputfile.println(String.format("<%s> %s </%s>" , this.tokensTypes().get(tkType), 
                                                                tkValue, this.tokensTypes().get(tkType)));
    }
    
    public void close(){
        this.outputfile.println("</tokens>");
        inputfile.close();
        outputfile.close();
    }
}

class JackCompiler{

    PrintStream outputFile;
    Scanner inputFile;
    ArrayList<String> otherFiles;
    int identValeu = 0;
    String line;

    public JackCompiler(FileInputStream input, File file){
        this.inputFile = new Scanner(input);
        try {
            this.outputFile = new PrintStream(file);    
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void Compile(){
        this.line = inputFile.nextLine();
        if(this.line.contains("<tokens>")){
            this.line = inputFile.nextLine();
            while(!this.line.contains("</tokens>")){
                String lineType = line.split(" ")[1].trim();
                
                if(lineType.equals("class")){
                    CompileClass();
                }else if(lineType.equals("var")){
                    CompileVarDec();
                }else if(lineType.equals("function")){
                    CompileSubroutineDec();
                }else if(lineType.equals("let") || lineType.equals("while") || lineType.equals("do") || lineType.equals("if") || lineType.equals("return")){
                    CompileStatements();
                }
                this.line = inputFile.nextLine();
            }
        }
    }

    private void writeInFile(String l){
        this.outputFile.println(String.format("%"+ identValeu*2 +"s", " ") + l);
    }

    public void CompileClass(){

        this.outputFile.println("<class>");
        this.identValeu++;
        writeInFile(this.line);
        while(!this.line.contains("{")){
            this.line = inputFile.nextLine();
            writeInFile(this.line);
        }
        this.line = inputFile.nextLine();

        this.CompileClassVarDec();
        this.CompileSubroutineDec();

        if(this.line.contains("}")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            identValeu--;
            this.outputFile.println("</class>");
        }
    }

    public void CompileClassVarDec(){

        while(this.line.contains("var") || this.line.contains("static") || this.line.contains("field")){
            writeInFile("<classVarDec>");
            identValeu++;

            while(!this.line.contains(";")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            }
            writeInFile(this.line);
            this.line = inputFile.nextLine();

            identValeu--;
            writeInFile("</classVarDec>");
        }
    }

    public void CompileSubroutineDec(){

        
        while(this.line.contains("method") || this.line.contains("function") || this.line.contains("constructor")){
            writeInFile("<subroutineDec>");
            identValeu++;
            while(!this.line.contains("{")){
                if(this.line.contains("(")){
                    CompileParameterList();
                    this.line = inputFile.nextLine();
                }else{
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                }
            }
            CompileSubroutineBody();
            identValeu--;
            writeInFile("</subroutineDec>");
        }
    }

    public void CompileParameterList(){
        
        writeInFile(this.line);

        writeInFile("<parameterList>");
        identValeu++;
        this.line = inputFile.nextLine();
        while(!this.line.contains(")")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
        }
        
        identValeu--;
        writeInFile("</parameterList>");
        writeInFile(this.line);
    }

    public void CompileSubroutineBody(){

        writeInFile("<subroutineBody>");
        identValeu++;
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        CompileVarDec();
        CompileStatements();
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        identValeu--;
        writeInFile("</subroutineBody>");
      
    }

    public void CompileVarDec(){
        while(this.line.contains("var")){
            writeInFile("<varDec>");
            identValeu++;
            while(!this.line.contains(";")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
            writeInFile(this.line);
            identValeu--;
            writeInFile("</varDec>");
            this.line = inputFile.nextLine();
        }
    }

    public void CompileStatements(){
       ArrayList statementType = new ArrayList<>();
       statementType.addAll(Arrays.asList("let", "do",
        "if", "while", "return"));
        String lineType = line.split(" ")[1].trim();
        writeInFile("<statements>");
        identValeu++;

        while(statementType.contains(lineType)){

            switch(lineType){
                case "let":
                    CompileLet();
                    break;
                case "do":
                    CompileDo();
                    break;
                case "if":
                    CompileIf();
                    break;
                case "while":
                    CompileWhile();
                    break;
                case "return":
                    CompileReturn();
            }

            lineType = line.split(" ")[1].trim();
        }

        identValeu--;
        writeInFile("</statements>");
    }

    public void CompileLet(){

        writeInFile("<letStatement>");
        identValeu++;
        while(!this.line.contains(";")){
            if(this.line.contains("[")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                CompileExpression();
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else if(this.line.contains("=")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                CompileExpression();
            }else{
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        identValeu--;
        writeInFile("</letStatement>");

    }

    public void CompileIf(){
        writeInFile("<ifStatement>");
        identValeu++;
        
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        CompileExpression();

        writeInFile(this.line);
        this.line = inputFile.nextLine();

        while(!this.line.contains("}")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileStatements();
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        
        if(this.line.contains("else")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            writeInFile(this.line);
            this.line = inputFile.nextLine();

            while(!this.line.contains("}")){
                CompileStatements();
            }
            writeInFile(this.line);
            this.line = inputFile.nextLine();
        }


        identValeu--;
        writeInFile("</ifStatement>");
    }

    public void CompileWhile(){
        writeInFile("<whileStatement>");
        identValeu++;

        while(!this.line.contains("{")){
            if(this.line.contains("(")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                CompileExpression();
                
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else{
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
        }
        while(!this.line.contains("}")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileStatements();
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        
        identValeu--;
        writeInFile("</whileStatement>");
    }

    public void CompileDo(){
        writeInFile("<doStatement>");
        identValeu++;
        
        while(!this.line.contains(";")){
            if(this.line.contains("(")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                CompileExpressionList();
                
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else{
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        identValeu--;
        writeInFile("</doStatement>");
    }

    public void CompileReturn(){
        writeInFile("<returnStatement>");
        identValeu++;
        
        writeInFile(this.line);
        this.line = inputFile.nextLine();
        if(!this.line.contains(";")){
            CompileExpression();
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        identValeu--;
        writeInFile("</returnStatement>");
    }

    public void CompileExpression(){
        ArrayList expressionOperators = new ArrayList<>();
        expressionOperators.addAll(Arrays.asList("+", "-", "*", "/", "&amp;", "|", "&lt;", "&gt;", "="));
        writeInFile("<expression>");
        identValeu++;
        
        CompileTerm();

        if(expressionOperators.contains(this.line.split(" ")[1].trim())){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileTerm();
        }

        identValeu--;
        writeInFile("</expression>");
    }

    public void CompileTerm(){
        writeInFile("<term>");
        identValeu++;
        if(this.line.contains("identifier")){
            
            writeInFile(this.line);
            this.line = inputFile.nextLine();

            if(this.line.contains(".")){
                while(!this.line.contains(")")){
                    if(this.line.contains("(")){
                        writeInFile(this.line);
                        this.line = inputFile.nextLine();
                        CompileExpressionList();
                    }else{
                    writeInFile(this.line);
                    this.line = inputFile.nextLine();
                    }
                }
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else if(this.line.contains("[")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                CompileExpression();
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }

        }else if(this.line.contains("(")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileExpression();
            writeInFile(this.line);
            this.line = inputFile.nextLine();
        }else if(this.line.contains("identifier") || this.line.contains("keyword") || this.line.contains("stringConstant") || this.line.contains("integerConstant")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
        }else if(this.line.contains("-") || this.line.contains("~")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileTerm();
        }

        identValeu--;
        writeInFile("</term>");
        
    }

    public void CompileExpressionList(){
        writeInFile("<expressionList>");
        identValeu++;
        
        while(!this.line.contains(")")){
            if(this.line.contains(",")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else{
                CompileExpression();
            }
        }

        identValeu--;
        writeInFile("</expressionList>");
    }   

    public void close(){
        outputFile.close();
    }

}