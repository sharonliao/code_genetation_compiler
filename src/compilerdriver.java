import AST.Node;
import Visitors.AST.ReconstructSourceProgramVisitor;
import Visitors.CodeGeneration.StackBasedCodeGenerationVisitor;
import Visitors.SemanticChecking.TypeCheckingVisitor;
import Visitors.SymbolTable.ComputeMemSizeVisitor;
import Visitors.SymbolTable.SymTabCreationVisitor;

import java.io.*;

public class compilerdriver {

    public static void main(String[] arg) throws IOException {
        System.out.println("compiler - compiler driver");

        //lexnegativegrading, syntax_myTest, lexical_test, semantic_test,myTest, simplemain
        String filename = "myTest";
        String resultFolder = "src/result/";
        String symTabPath =  resultFolder+filename+".outsymboltables";
        String typecheckPath =  resultFolder+filename+".typecheckingError";
        String symTabcheckPath = resultFolder+filename+".symTabError";
        String reScPath =  resultFolder+filename+".reSrc";
        String moon_outfilePath =  resultFolder+filename+".moon";
        String globalErrorFile = resultFolder+filename+".ErrorForAll";


        //syntactic parsing
        Parser_syntactic parsersyntactic = new Parser_syntactic();
        parsersyntactic.loadgrammar();

        ASTNode astTree =  parsersyntactic.parse(filename);
        parsersyntactic.writeDotFile(astTree);

        //out error in a single file
        errorFilechecking(globalErrorFile);
        String lexcicalError = parsersyntactic.lexerAnalyzer.errorString;
        writeError(lexcicalError,globalErrorFile);
        String syntaxError = parsersyntactic.errorStr;
        writeError(syntaxError,globalErrorFile);
        // if there are errors in syntax phrase, the compiler will not continue to next phrase.
        if(syntaxError.length()>1){
            return;
        }


        ConvertAST covertAST = new ConvertAST();
        covertAST.filename = filename;

        //generate AST
        Node newAST = covertAST.generateNewAST(astTree);
        covertAST.writeDotFile(newAST);

        ReconstructSourceProgramVisitor RecnScPgVisitor = new ReconstructSourceProgramVisitor();
        RecnScPgVisitor.m_outputfilename = reScPath;

        //create symbol table
        SymTabCreationVisitor STCVisitor   = new SymTabCreationVisitor();
        STCVisitor.m_outputfilename = symTabPath;
        STCVisitor.m_errorFilename = symTabcheckPath;

        //type checking
        TypeCheckingVisitor typeCheckVisitor = new TypeCheckingVisitor();
        typeCheckVisitor.m_outputfilename = typecheckPath;
        typeCheckVisitor.m_symTabfilename = symTabPath;


        //computer member size and offset
        ComputeMemSizeVisitor CMSVisitor3   = new ComputeMemSizeVisitor();
        CMSVisitor3.m_outputfilename = symTabPath;

        //code generating
        StackBasedCodeGenerationVisitor stackCodeGeneratorVisiter   = new StackBasedCodeGenerationVisitor(moon_outfilePath);

        newAST.accept(STCVisitor);
        writeError(STCVisitor.m_errors,globalErrorFile);
//        if(STCVisitor.m_errors.length()>1){
//            return;
//        }

        newAST.accept(RecnScPgVisitor);
        newAST.accept(typeCheckVisitor);
        // write errors to a global error file

        writeError(typeCheckVisitor.m_errors,globalErrorFile);
        if(typeCheckVisitor.m_errors.length()>1){
            return;
        }
        newAST.accept(CMSVisitor3);
        newAST.accept(stackCodeGeneratorVisiter);
    }

    public static void writeError(String errorStr, String filePath){
        File file = new File(filePath);
        FileWriter fr = null;
        BufferedWriter br = null;
        PrintWriter pr = null;
        try {
            // to append to file, you need to initialize FileWriter using below constructor
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            pr = new PrintWriter(br);
            pr.println(errorStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                pr.close();
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void errorFilechecking(String filePath){
        try {
            File error = new File(filePath);
            if (error.createNewFile()) {
                System.out.println(filePath+" created. ");
            } else {
                System.out.println("File already exists.");
                error.delete();
                error.createNewFile();
                System.out.println(filePath+" deleted and created a new noe. ");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}



