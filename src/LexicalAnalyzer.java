import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class LexicalAnalyzer {
    String filename;
    String outFile;
    String errorFile;
    String errorString = "";
    Scanner reader; // the file reader, used to read the testing file line by line
    int rowNo; // token position
    char[] charArray; // convert current code line to a char array
    int index; //index of charArray




    public ArrayList<Token> tokenizeFile(String filename) {
        ArrayList<Token> tokenStream = new ArrayList<>();
        try {
            //create the out file and error file
            this.initialOutput(filename);

            //start to read the testing file
            File file = new File("src/testFiles/"+filename+".src");
            this.reader = new Scanner(file);

            //tokenize the file line by line
            while (reader.hasNextLine()) {
                String str = reader.nextLine().trim();
                System.out.println("\n"+str);

                //tokenize a single line code
                ArrayList<Token> tokens = tokenizeOneLine(str);
                tokenStream.addAll(tokens);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokenStream;
    }

    private void initialOutput(String filename) {
        this.rowNo = 0;
        this.filename = filename;
        this.outFile = this.filename + ".outlextoens";
        this.errorFile = this.filename + ".outlexerrors";
        try {
            File output = new File("src/result/"+this.outFile);
            if (output.createNewFile()) {
                System.out.println(outFile+" created. ");
            } else {
                System.out.println("File already exists.");
                output.delete();
                output.createNewFile();
                System.out.println(outFile+" deleted and created a new noe. ");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            File error = new File("src/result/"+this.errorFile);
            if (error.createNewFile()) {
                System.out.println(errorFile+" created. ");
            } else {
                System.out.println("File already exists.");
                error.delete();
                error.createNewFile();
                System.out.println(errorFile+" deleted and created a new noe. ");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    private void writeResult(ArrayList<Token> tokens) throws IOException {
        FileWriter outputWrite = new FileWriter("src/result/"+this.outFile,true);
        FileWriter errorWrite = new FileWriter("src/result/"+this.errorFile,true);
        String outStr = "\n";
        String errorStr = "";
        for (Token token : tokens) {
            if (token.valid == true) {
            //[eq, ==, 1]
                outStr += "["+token.tokenType+", "+token.lexeme+", "+token.position+"] ";
            } else {
                String type = "";
                String errorType = "";
                if (token.tokenType == "intlit" || token.tokenType == "floatlit") {
                    type = "invalidnum";
                    errorType = "number";
                } else if (token.tokenType == "char") {
                    type = "invalidchar";
                    errorType = "character";
                } else if (token.tokenType == "id") {
                    type = "invalidid";
                    errorType = "identifier";
                }else if (token.tokenType == "blockcmt") {
                    type = "invalidblockcmt";
                    errorType = "block comment";
                }else if (token.tokenType == "inlinecmt") {
                    type = "invalidinlinecmt";
                    errorType = "inline comment";
                }else if (token.tokenType == "stringlit") {
                    type = "invalidstringLit";
                    errorType = "string literal";
                } else {
                    type = "invalid" + token.tokenType;
                    errorType = token.tokenType;
                }

                outStr += "["+type+", "+token.lexeme+", "+token.position+"] ";
                errorStr += "Lexical error: Invalid "+errorType+": \""+token.lexeme+"\" : line "+token.position+".\n";
            }
        }
        errorString += errorStr;
        outputWrite.write(outStr);
        errorWrite.write(errorStr);
        outputWrite.close();
        errorWrite.close();
    }



    private void initialContext(String codeLine) {
        this.rowNo += 1;
        this.index = -1;
        this.charArray = codeLine.toCharArray();
    }

    private ArrayList<Token> tokenizeOneLine(String codeLine) throws IOException {

        ArrayList<Token> tokens = new ArrayList<>();

        // update rowNo, and cover the current code string
        // to a char array and reset index of array
        initialContext(codeLine);

        // while loop call nextToken() to generate tokens
        while (index < charArray.length-1) {
            Token token = nextToken();
            if (token.tokenType != null) {
                tokens.add(token);
                System.out.println(token.toString());
            }
        }

        // write tokens and errors to the files
        writeResult(tokens);
        return tokens;
    }


    ArrayList<String> reservedWords = new ArrayList<>(Arrays.asList(
            "if", "then", "else", "integer", "float", "string", "void",
            "public", "private", "func", "var", "class", "while", "read",
            "write", "return", "main", "inherits", "break","continue","and","or")
    );

    ArrayList<Character> transferChar = new ArrayList<>(
            Arrays.asList('.',' ','+','-','*','/','=','<','>','(',')','{','}','[',']',';',',',':','|','&','!','?','\t','\0'));


    private char nextChar() {
        index += 1 ;
        if (index < charArray.length) {
            return charArray[index];
        } else {
            return '\0';
        }
    }

    private void backupChar() {
        if (index > 0) {
            index -= 1;
        }
    }

    public Token nextToken() {
        // create a new token
        Token token = new Token();

        //use StringBuffer to store the lexeme
        StringBuffer lexeme = new StringBuffer();

        char c =  nextChar();

        while (c == ' ' | c == '\t' ){
            c = nextChar();
        }
        // match id and reserved words
        if (isLetter(c)) {
            lexeme.append(c);
            c = nextChar();
            while (isLetter(c) || isDigit(c) || c == '_') {
                lexeme.append(c);
                c = nextChar();
            }
            //token end
            // check if id end correctly
            if (isTransferChar(c)){
                // reserved words are prior than id, so check if token is a reserved word

                // if true then save token as a reserved word otherwise save as an id
                if (isReservedWord(lexeme.toString())) {
                    //save token (type, lexeme, is valid, rowNO)
                    saveToken(token, lexeme.toString(), lexeme,true);
                } else {
                    saveToken(token, "id", lexeme,true);
                }
                // need back up one char
                backupChar();

            } else {
                // catch the entire error token
                // ex: abg@1133
                while ( !isTransferChar(c)){
                    lexeme.append(c);
                    c = nextChar();
                }
                saveToken(token, "id", lexeme,false);
                backupChar();
            }
        //  match int
            // start with 0
        } else if( c == '0') {
            lexeme.append(c);
            c = nextChar();
            // match float
            if (c == '.') { // 0.1222
                lexeme.append(c);
                c = nextChar();
                //a helper function, which is a recursive function, to check if this token is valid
                floatState1Check(c,lexeme,token);
            } else {
                // check if this token end correctly, ex: 0abbb, invalid; 0 abb, good
                checkIfEndCorrect(c,token,"intlit",lexeme);
            }
        // start with 1-9
        } else if(isDigit(c)) {
            lexeme.append(c);
            c = nextChar();
            while ( isDigit(c) ) {
                lexeme.append(c);
                c = nextChar();
            }
            // match float
            if (c == '.') { // 0.1222
                lexeme.append(c);
                c = nextChar();
                floatState1Check(c,lexeme,token);
            } else {
                checkIfEndCorrect(c,token,"intlit",lexeme);
            }
        } else if ( c == '+') {
            saveToken(token,  "+", new StringBuffer().append("+"),true);
        }else if ( c == '-') {
            saveToken(token,  "-", new StringBuffer().append("-"),true);
        }else if ( c == '*') {
            saveToken(token,  "*", new StringBuffer().append("*"),true);
        }else if ( c == '|') {
            saveToken(token,  "|", new StringBuffer().append("|"),true);
        }else if ( c == '&') {
            saveToken(token,  "&", new StringBuffer().append("&"),true);
        }else if ( c == '!') {
            saveToken(token,  "not", new StringBuffer().append("!"),true);
        }else if ( c == '?') {
            saveToken(token,  "qm", new StringBuffer().append("?"),true);
        }else if ( c == '(') {
            saveToken(token,  "(", new StringBuffer().append("("),true);
        }else if ( c == ')') {
            saveToken(token,  ")", new StringBuffer().append(")"),true);
        }else if ( c == '{') {
            saveToken(token,  "{", new StringBuffer().append("{"),true);
        }else if ( c == '}') {
            saveToken(token,  "}", new StringBuffer().append("}"),true);
        }else if ( c == '[') {
            saveToken(token,  "[", new StringBuffer().append("["),true);
        }else if ( c == ']') {
            saveToken(token,  "]", new StringBuffer().append("]"),true);
        }else if ( c == ';') {
            saveToken(token,  ";", new StringBuffer().append(";"),true);
        }else if ( c == '.') {
            saveToken(token,  ".", new StringBuffer().append("."),true);
        }else if ( c == ',') {
            saveToken(token,  ",", new StringBuffer().append(","),true);
        }else if ( c == ':') {
            lexeme.append(c);
            c = nextChar();
            if ( c == ':') {
                lexeme.append(c);
                saveToken(token,  "sr", lexeme,true);
            } else {
                saveToken(token,  ":", lexeme,true);
                backupChar();
            }
        } else if ( c == '=') {
            lexeme.append(c);
            c = nextChar();
            if ( c == '=') {
                lexeme.append(c);
                saveToken(token,  "eq", lexeme,true);
            } else {
                saveToken(token,  "=", lexeme,true);
                backupChar();
            }
        }else if ( c == '<') {
            lexeme.append(c);
            c = nextChar();
            if ( c == '>') {
                lexeme.append(c);
                saveToken(token,  "neq", lexeme,true);
            } else if ( c == '=') {
                lexeme.append(c);
                saveToken(token,  "leq", lexeme,true);
            } else {
                saveToken(token,  "lt", lexeme,true);
                backupChar();
            }
        }else if ( c == '>') {
            lexeme.append(c);
            c = nextChar();
            if ( c == '=') {
                lexeme.append(c);
                saveToken(token,  "geq", lexeme,true);
            } else {
                saveToken(token,  "gt", lexeme,true);
                backupChar();
            }
        }else if ( c == '/') {
            lexeme.append(c);
            c = nextChar();
            if ( c == '/') { // catch the entire comment line
                while (index < charArray.length-1) {
                    lexeme.append(c);
                    c = nextChar();
                }
                lexeme.append(c);
                saveToken(token,  "inlinecmt", lexeme,true);
            } else if ( c == '*') { // catch the entire block comment
                lexeme.append(c);
                c = nextChar();
                token.position = rowNo; // record the rowNo of the first line
                // define a recursive helper function to catch the entire block comment
                blockcmtEndCheck(c,lexeme,token);
            } else {
                // div
                saveToken(token,  "/", lexeme,true);
                backupChar();
            }

        } else if ( c == '\"') {
            lexeme.append(c);
            c = nextChar();
            // catch the entire string
            while (!(index == charArray.length | c == '\"')) {
                lexeme.append(c);
                c = nextChar();
            }
            lexeme.append(c);
            // check if string ends correctly
            if(c == '\"') {
                saveToken(token,  "stringlit", lexeme,true);
            } else {
                saveToken(token,  "stringlit", lexeme,false);
            }

        }  else { // catch the entire error token
            System.out.println("error: " + c);
            while ( !isTransferChar(c)){
                lexeme.append(c);
                c = nextChar();
            }
            if (isTransferChar(c)) {
                backupChar();
            }

            System.out.println("error: " + c);

            token.lexeme = lexeme.toString();
            if (token.lexeme.length() == 1){
                token.tokenType = "char";
            } else {
                token.tokenType = "id";
            }
            token.valid = false;
            token.position = rowNo;
        }

        return token;
    }

    private void floatState1Check(char c, StringBuffer lexeme, Token token) {
        // a recursive function

        if (isDigit(c)) { //0-9
            lexeme.append(c);
            c = nextChar();
            while (c != '0' & isDigit(c)) { //1-9 no end with 0
                lexeme.append(c);
                c = nextChar();
            }
            if (c == 'e') { // check other branch e
                lexeme.append(c);
                c = nextChar();
                if (c == '0') {
                    lexeme.append(c);
                    c = nextChar();
                    checkIfEndCorrect(c,token,"floatlit", lexeme);

                } else if (isDigit(c)) { //1-9
                    while (isDigit(c)) {
                        lexeme.append(c);
                        c = nextChar();
                    }
                    checkIfEndCorrect(c,token,"floatlit", lexeme);
                } else if (c == '+' | c == '-') {
                    lexeme.append(c);
                    c = nextChar();
                    if (c == '0') {
                        lexeme.append(c);
                        c = nextChar();
                        checkIfEndCorrect(c,token,"floatlit", lexeme);
                    } else if (isDigit(c)){
                        while (isDigit(c)) {
                            lexeme.append(c);
                            c = nextChar();
                        }
                        checkIfEndCorrect(c,token,"floatlit", lexeme);
                    }
                }
            } else if (c == '0') { // 0
                while (c == '0') { //1-9 no end with 0
                    lexeme.append(c);
                    c = nextChar();
                }
                if (!isDigit(c)) { //error1 end of 0 ,ex: 10.010
                    if (isTransferChar(c)){
                        saveToken(token, "floatlit", lexeme,false);
                        backupChar();
                    } else { //error2 end of alpha, ex:10.01a
                        while ( !isTransferChar(c)){
                            lexeme.append(c);
                            c = nextChar();
                        }
                        saveToken(token, "floatlit", lexeme,false);
                        backupChar();
                    }
                } else {
                    //go back to float 1-9
                    floatState1Check(c, lexeme, token);
                }
            } else { //this token is end
                checkIfEndCorrect(c,token,"floatlit", lexeme);
            }
        }else { // two errors
            System.out.println("float error:" + c);
            if (isTransferChar(c)) { // ex: 10.+10, + is a transfer char, then the error token is 10.
                saveToken(token, "floatlit", lexeme,false);
                backupChar();
            }else { // end error, catch whole wrong token, ex: 100.abbb
                while ( !isTransferChar(c)){
                    lexeme.append(c);
                    c = nextChar();
                }
                backupChar();
                c = lexeme.charAt(lexeme.length()-1);
                if (isDigit(c)){
                    saveToken(token, "floatlit", lexeme,false);
                } else {
                    saveToken(token, "id", lexeme,false);
                }
            }
        }
    }


    private void blockcmtEndCheck(char c, StringBuffer lexeme, Token token) {
        while (c != '/') {
            lexeme.append(c);
            if (this.index < this.charArray.length-1){
                c = nextChar();
            } else if (this.reader.hasNextLine()) {
                String str = reader.nextLine().trim();
                initialContext(str);
                lexeme.append('\\');
                lexeme.append('n');
                c = nextChar();
            } else { // don't match */
                break;
            }
        }

        lexeme.append(c);
        if (c == '/'){
            int indexOfStar = this.index - 1;

            if (indexOfStar >= 0 && this.charArray[indexOfStar] == '*'){ // success find end of cmt */
                token.tokenType = "blockcmt";
                token.lexeme = lexeme.toString();
                token.valid = true;
            } else if (this.index < this.charArray.length-1){
                c = nextChar();
                blockcmtEndCheck(c,lexeme,token);
            } else if (this.reader.hasNextLine()) {
                String str = reader.nextLine().trim();
                initialContext(str);
                lexeme.append('\\');
                lexeme.append('n');
                c = nextChar();
                blockcmtEndCheck(c,lexeme,token);
            } else {
                token.tokenType = "blockcmt";
                token.lexeme = lexeme.toString();
                token.valid = false;
            }

        } else {
            token.tokenType = "blockcmt";
            token.lexeme = lexeme.toString();
            token.valid = false;
        }
    }


    private boolean isLetter(char c) {
            return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isTransferChar(char c){
        return transferChar.contains(c) ? true : false;
    }


    private boolean isReservedWord(String word) {
        return reservedWords.contains(word) ? true : false;
    }

    private void checkIfEndCorrect(char c, Token token, String tokenType, StringBuffer lexeme){

        if (isTransferChar(c)) { // float end correctly e0
            saveToken(token, tokenType, lexeme,true);
            backupChar();
        }else { // end error, catch whole wrong token
            while ( !isTransferChar(c)){
                lexeme.append(c);
                c = nextChar();
            }
            backupChar();
            c = lexeme.charAt(lexeme.length()-1);
            if(isLetter(c)){
                saveToken(token, "id", lexeme,false);
            } else {
                saveToken(token, tokenType, lexeme,false);
            }
        }
    }


    private void saveToken(Token token, String tokenType, StringBuffer lexeme, boolean valid) {
        token.tokenType = tokenType;
        token.lexeme = lexeme.toString();
        token.valid = valid;
        token.position = rowNo;
    }


    public static void main(String[] arg){
        System.out.println("compiler - lexical analyzer");
        LexicalAnalyzer lexerAnalyzer = new LexicalAnalyzer();

        lexerAnalyzer.tokenizeFile("myTest");
    }
}
