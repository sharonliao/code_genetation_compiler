import AST.Node;
import Visitors.AST.ReconstructSourceProgramVisitor;
import Visitors.CodeGeneration.StackBasedCodeGenerationVisitor;
import Visitors.SemanticChecking.TypeCheckingVisitor;
import Visitors.SymbolTable.ComputeMemSizeVisitor;
import Visitors.SymbolTable.SymTabCreationVisitor;

import java.io.IOException;

public class compilerdriver {
    public static void main(String[] arg) throws IOException {
        System.out.println("compiler - compiler driver");

        //polynomialsemanticerrors,simple，polynomial,bubblesort
        String filename = "myTest";
        String resultFolder = "src/result/";
        String symTabPath =  resultFolder+filename+".outsymboltables";
        String typecheckPath =  resultFolder+filename+".typecheckingError";
        String symTabcheckPath = resultFolder+filename+".symTabError";
        String reScPath =  resultFolder+filename+".reSrc";
        String moon_outfilePath =  resultFolder+filename+".moon";

        //syntactic parsing
        Parser_syntactic parsersyntactic = new Parser_syntactic();
        parsersyntactic.loadgrammar();

        ASTNode astTree =  parsersyntactic.parse(filename);
        parsersyntactic.writeDotFile(astTree);

        ConvertAST covertAST = new ConvertAST();
        covertAST.filename = filename+"-1";

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
        newAST.accept(RecnScPgVisitor);
        newAST.accept(typeCheckVisitor);
        newAST.accept(CMSVisitor3);
        newAST.accept(stackCodeGeneratorVisiter);


    }
}



