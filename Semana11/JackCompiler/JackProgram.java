import java.awt.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.print.DocFlavor.STRING;

import com.sun.java.accessibility.util.Translator;



class JackProgram{
    private static String cur_fileName;
    private static String new_fileName;

    public static void main(String[] args) {

        if(args.length != 0){
            String path = args[0];
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

    private PrintStream outputFile;
    private Scanner inputFile;
    private ArrayList<String> otherFiles;
    private int identValeu = 0;
    private String line;
    private SymbolTable symbolTable;
    private int labels;
    private VMWritter writter;


    public JackCompiler(FileInputStream input, File file){
        this.inputFile = new Scanner(input);
        try {
            this.outputFile = new PrintStream(file);    
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.symbolTable = null;
        this.labels = 0;
        InicializaWritter(file);
       
    }

    public void InicializaWritter(File file){
        
        try{
            String nameFile = file.getName().replace(".xml", ".vm");
            String pathFile = file.getAbsolutePath().replace(file.getName(), nameFile);
            File nFile = new File(pathFile);
            nFile.getParentFile().mkdirs();
            nFile.createNewFile();
            this.writter = new VMWritter(nFile);
        }catch(Exception e){
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
                    //CompileSubroutineDec();
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

        this.symbolTable = new SymbolTable();

        this.outputFile.println("<class>");
        this.identValeu++;
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        String name = this.line.split(" ")[1].trim();

        while(!this.line.contains("{")){
            this.line = inputFile.nextLine();
            writeInFile(this.line);
        }
        this.line = inputFile.nextLine();

        this.CompileClassVarDec();
        this.CompileSubroutineDec(name);

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

            String kind = this.line.split(" ")[1].trim();
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            String type = this.line.split(" ")[1].trim();

            while(!this.line.contains(";")){
                if(!this.line.contains("symbol")){
                    String name = this.line.split(" ")[1].trim();

                    this.symbolTable.define(name, type, kind);
                }
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
            writeInFile(this.line);
            this.line = inputFile.nextLine();

            identValeu--;
            writeInFile("</classVarDec>");
        }
    }

    public void CompileSubroutineDec(String nameClass){

        
        while(this.line.contains("method") || this.line.contains("function") || this.line.contains("constructor")){
            int nArgs  = 0;
            
            writeInFile("<subroutineDec>");
            identValeu++;

            this.symbolTable.startSubroutine();
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            String type = this.line.split(" ")[1].trim();
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            String name = this.line.split(" ")[1].trim();
            while(!this.line.contains("{")){
                if(this.line.contains("(")){
                    nArgs = CompileParameterList();
                    this.line = inputFile.nextLine();
                }else{
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                }
            }

            this.writter.writeFunction(String.format("%s.%s", nameClass, name), nArgs);

            CompileSubroutineBody();
            identValeu--;
            writeInFile("</subroutineDec>");
        }
    }

    public int CompileParameterList(){
        int i = 0;

        writeInFile(this.line);
        writeInFile("<parameterList>");
        identValeu++;
        this.line = inputFile.nextLine();
        while(!this.line.contains(")")){
            if(!this.line.contains("symbol")){
                String type = this.line.split(" ")[1].trim();
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                String name = this.line.split(" ")[1].trim();
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                symbolTable.define(name, type, "argument");
                i++;
            }else{
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
        }
        
        identValeu--;
        writeInFile("</parameterList>");
        writeInFile(this.line);
        
        return i;
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
            String kind = this.line.split(" ")[1].trim();

            writeInFile(this.line);
            this.line = inputFile.nextLine();
            String type = this.line.split(" ")[1].trim();

            while(!this.line.contains(";")){
                if(!this.line.contains("symbol")){
                    String name = this.line.split(" ")[1].trim();
                    this.symbolTable.define(name, type, kind);

                    writeInFile(this.line);
                    this.line = inputFile.nextLine();
                }else{
                    writeInFile(this.line);
                    this.line = inputFile.nextLine();
                }
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

        writeInFile(this.line);
        this.line = inputFile.nextLine();
        String name = this.line.split(" ")[1].trim();

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

        String kind = this.symbolTable.kindOf(name);
        int index = this.symbolTable.indexOf(name);
        this.writter.writePop(kind, index);

        identValeu--;
        writeInFile("</letStatement>");

    }

    public void CompileIf(){
        writeInFile("<ifStatement>");
        identValeu++;
        String elseLabel = String.format("L%s",this.labels);
        this.labels++;
        String endLabel = String.format("L%s",this.labels);
        this.labels++;

        writeInFile(this.line);
        this.line = inputFile.nextLine();
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        CompileExpression();

        writeInFile(this.line);
        this.line = inputFile.nextLine();

        this.writter.writeArithmetic("~");
        this.writter.writeIf(elseLabel);

        while(!this.line.contains("}")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileStatements();
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        this.writter.writeArithmetic("~");
        this.writter.writeIf(endLabel);
        
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

        this.writter.writeLabel(endLabel);
        identValeu--;
        writeInFile("</ifStatement>");
    }

    public void CompileWhile(){
        writeInFile("<whileStatement>");
        identValeu++;

        String startLabel = String.format("L%s",this.labels);
        this.labels++;
        String endLabel = String.format("L%s",this.labels);
        this.labels++;

        this.writter.writeLabel(startLabel);

        while(!this.line.contains("{")){
            if(this.line.contains("(")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                CompileExpression();

                this.writter.writeArithmetic("~");
                this.writter.writeIf(endLabel);
                
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
        
        this.writter.writeGoto(startLabel);
        this.writter.writeLabel(endLabel);
        identValeu--;
        writeInFile("</whileStatement>");
    }

    public void CompileDo(){
        writeInFile("<doStatement>");
        identValeu++;
        String function = "";
        int nArgs = 0;
        
        while(!this.line.contains(";")){
            if(this.line.contains("(")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                nArgs =  CompileExpressionList();
                
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else{
                function = function + this.line.split(" ")[1].trim();
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        this.writter.writeCall(function, nArgs);
        this.writter.writePop("temp", 0);

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
        }else{
            this.writter.writePush("constant", 0);
        }
        writeInFile(this.line);
        this.line = inputFile.nextLine();

        this.writter.writeReturn();
        identValeu--;
        writeInFile("</returnStatement>");
    }

    public void CompileExpression(){
        ArrayList expressionOperators = new ArrayList<>();
        expressionOperators.addAll(Arrays.asList("+", "-", "*", "/", "&amp;", "|", "&lt;", "&gt;", "="));
        writeInFile("<expression>");
        identValeu++;
        
        CompileTerm();

        String command = this.line.split(" ")[1].trim();
        if(expressionOperators.contains(this.line.split(" ")[1].trim())){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileTerm();
        }

        this.writter.writeArithmetic(command);
        identValeu--;
        writeInFile("</expression>");
    }

    public void CompileTerm(){
        String name, type, kind;
        int index, nArgs = 0;

        writeInFile("<term>");
        identValeu++;
        if(this.line.contains("identifier")){
            name = line.split(" ")[1].trim();
            writeInFile(this.line);
            this.line = inputFile.nextLine();

            if(this.line.contains(".")){
                name = name + ".";
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                name = name + line.split(" ")[1].trim();

                while(!this.line.contains(")")){
                    if(this.line.contains("(")){
                        writeInFile(this.line);
                        this.line = inputFile.nextLine();
                        nArgs = CompileExpressionList();
                    }else{
                    writeInFile(this.line);
                    this.line = inputFile.nextLine();
                    }
                }
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                this.writter.writeCall(name, nArgs);

            }else if(this.line.contains("[")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
                CompileExpression();
                writeInFile(this.line);
                this.line = inputFile.nextLine();

                kind = this.symbolTable.kindOf(name);
                index = this.symbolTable.indexOf(name);
                this.writter.writePush(kind, index);
                this.writter.writeArithmetic("+");
                this.writter.writePop("pointer", 1);
                this.writter.writePush("that", 0);

            }else{
                kind = this.symbolTable.kindOf(name);
                index = this.symbolTable.indexOf(name);
                this.writter.writePush(kind, index);
            }

        }else if(this.line.contains("(")){
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileExpression();
            writeInFile(this.line);
            this.line = inputFile.nextLine();
        }else if(this.line.contains("keyword") || this.line.contains("stringConstant") || this.line.contains("integerConstant")){
            
            if(this.line.contains("keyword")){
                
                if(this.line.split(" ")[1].trim().equals("true")){
                    this.writter.writePush("constant", 0);
                    this.writter.writeArithmetic("~");
                }else if(this.line.split(" ")[1].trim().equals("false")){
                    this.writter.writePush("constant", 0);
                }

            }else if(this.line.contains("stringConstant")){

                String str = "";
                for (String var : this.line.split(" ")) {
                    if(!var.isEmpty() && !(var.contains("<") || var.contains("/>"))){
                        str = str + " " + var;
                    }
                }
                str = str.trim();
                this.writter.writePush("constant", str.length());
                this.writter.writeCall("String.appendChar", 1);

                for (char c : str.toCharArray()) {
                    int i = (int) c;
                    this.writter.writePush("constant", i);
                    this.writter.writeCall("String.appendChar", 2);       
                }

            }else if(this.line.contains("integerConstant")){
                int n = Integer.parseInt(this.line.split(" ")[1].trim());
                this.writter.writePush("constant", n);
            }

            writeInFile(this.line);
            this.line = inputFile.nextLine();
        }else if(this.line.contains("-") || this.line.contains("~")){
            String operation = this.line.split(" ")[1].trim();
            if(!operation.equals("~")){
                operation = "neg";
            }
            
            writeInFile(this.line);
            this.line = inputFile.nextLine();
            CompileTerm();
            this.writter.writeArithmetic(operation);
        }

        identValeu--;
        writeInFile("</term>");
        
    }

    public int CompileExpressionList(){
        writeInFile("<expressionList>");
        identValeu++;
        int nArgs = 0;

        while(!this.line.contains(")")){
            if(this.line.contains(",")){
                writeInFile(this.line);
                this.line = inputFile.nextLine();
            }else{
                CompileExpression();
                nArgs++;
            }
        }

        identValeu--;
        writeInFile("</expressionList>");

        return nArgs;
    }   

    public void close(){
        outputFile.close();
        this.writter.close();
    }

}

class SymbolTable{
    private ArrayList<SymbolRow> _class;
    private ArrayList<SymbolRow> _subroutine;
    

    public SymbolTable(){
        this._class = new ArrayList<SymbolRow>();
        this._subroutine = new ArrayList<SymbolRow>();
    }

    public void startSubroutine(){
        this._subroutine = new ArrayList<SymbolRow>();
    }

    public void define(String name, String type, String kind){
        if(kind.toLowerCase().equals("static") || kind.toLowerCase().equals("field")){
            this._class.add(new SymbolRow(name, type, kinds(kind), varCount(kinds(kind))));
        }else if(kind.toLowerCase().equals("argument") || kind.toLowerCase().equals("var")){
            this._subroutine.add(new SymbolRow(name, type, kinds(kind), varCount(kinds(kind))));
        }
    }

    public int varCount(String kind){
        int i = 0;

        ArrayList<SymbolRow> totalTable = new ArrayList<SymbolRow>();
        totalTable.addAll(this._class);
        totalTable.addAll(this._subroutine);

        for (SymbolRow row : totalTable) {
            if(row.getKind().equals(kind)){
                i++;
            }
        }

        return i;
    }

    public String kindOf(String name){

        String nameType = null;

        ArrayList<SymbolRow> totalTable = new ArrayList<SymbolRow>();
        totalTable.addAll(this._class);
        totalTable.addAll(this._subroutine);

        for (SymbolRow row : totalTable) {
            if(row.getName().equals(name)){
                if(!row.getKind().isEmpty()){
                    nameType = row.getKind();        
                }
            }
        }

        return nameType;
    }

    public String typeOf(String name){
        String nameType = null;

        ArrayList<SymbolRow> totalTable = new ArrayList<SymbolRow>();
        totalTable.addAll(this._class);
        totalTable.addAll(this._subroutine);

        for (SymbolRow row : totalTable) {
            if(row.getName().equals(name)){
                if(!row.getType().isEmpty()){
                    nameType = row.getType();        
                }
            }
        }

        return nameType;
    }

    public int indexOf(String name){
        int index = 0;

        ArrayList<SymbolRow> totalTable = new ArrayList<SymbolRow>();
        totalTable.addAll(this._class);
        totalTable.addAll(this._subroutine);

        for (SymbolRow row : totalTable) {
            if(row.getName().equals(name)){
                if(!(row.getIndex() != 0)){
                    index = row.getIndex();        
                }
            }
        }

        return index;
    }

    public String kinds(String key){
        HashMap<String, String> k = new HashMap<String, String>();
        k.put("var", "local");
        k.put("argument", "argument");
        k.put("field", "this");
        k.put("static", "static");

        return k.get(key);
    }

}

class SymbolRow{

    private String name;
    private String type;
    private String kind;
    private int index;

    public SymbolRow(String name, String type, String kind, int index){
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }


    public String getName(){
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKind(){
        return this.kind;
    }

    public void setKind(String kind){
        this.kind = kind;
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(int index){
        this.index = index;
    }

}

class VMWritter{
    private PrintStream outputFile;
    public VMWritter(File file){
        try {
            this.outputFile = new PrintStream(file);    
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void writePush(String segment, int index){
        this.outputFile.println(String.format("push %s %d", segment, index));
    }

    public void writePop(String segment, int index){
        this.outputFile.println(String.format("pop %s %d", segment, index));
    }

    public void writeArithmetic(String command){
        String ret = commands(command);
        if(!ret.isEmpty() && ret.length()>0){
            if(ret.equals("Math.multiply")){
                writeCall(ret, 2);
            }else{
                this.outputFile.println(String.format("%s", ret));
            }
        }
    }

    public void writeLabel(String label){
        this.outputFile.println(String.format("label %s", label));
    }

    public void writeGoto(String label){
        this.outputFile.println(String.format("goto %s",label));
    }

    public void writeIf(String label){
        this.outputFile.println(String.format("if-goto %s", label));
    }

    public void writeCall(String name, int nArgs){
        this.outputFile.println(String.format("call %s %d", name, nArgs));
    }

    public void writeFunction(String name, int nArgs){
        this.outputFile.println(String.format("function %s %d", name, nArgs));
    }

    public void writeReturn(){
        this.outputFile.println("return");
    }

    public void close(){
        this.outputFile.close();
    }

    private String commands(String key){
        HashMap<String,String> cmds = new HashMap<String,String>();
        cmds.put("+" , "add");
        cmds.put("-" , "sub");
        cmds.put("~" , "not");
        cmds.put(">" , "gt");
        cmds.put("<" , "lt");
        cmds.put("&" , "and");
        cmds.put("=" , "eq");
        cmds.put("neg" , "neg");
        cmds.put("*" , "Math.multiply");

        if(cmds.get(key) == null){
            return "";
        }else{
            return cmds.get(key);
        }
        
    }

}