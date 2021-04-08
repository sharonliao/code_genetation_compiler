import AST.Node;
import Visitors.AST.ReconstructSourceProgramVisitor;
import Visitors.SemanticChecking.TypeCheckingVisitor;
import Visitors.SymbolTable.SymTabCreationVisitor;

import java.io.IOException;

public class semanticanalyzerdriver {
    public static void main(String[] arg) throws IOException {
        System.out.println("compiler - syntactic analyzer");


        Parser_syntactic parsersyntactic = new Parser_syntactic();
        parsersyntactic.loadgrammar();
        String filename = "myTest";
        ASTNode astTree =  parsersyntactic.parse(filename);
//        ASTNode astTree =  parsersyntactic.parse("polynomial");
//        ASTNode astTree =  parsersyntactic.parse("myTest");
//        ASTNode astTree =  parsersyntactic.parse("bubblesort");
//        polynomialsemanticerrors
        parsersyntactic.writeDotFile(astTree);

        ConvertAST covertAST = new ConvertAST();
        covertAST.filename = filename+"-1";
        Node newAST = covertAST.generateNewAST(astTree);
        covertAST.writeDotFile(newAST);


        String resultFolder = "src/result/";
        String symTabPath =  resultFolder+filename+".outsymboltables";
        String typecheckPath =  resultFolder+filename+".typecheckingError";
        String symTabcheckPath = resultFolder+filename+".symTabError";
        String reScPath =  resultFolder+filename+".reSrc";

        ReconstructSourceProgramVisitor RecnScPgVisitor = new ReconstructSourceProgramVisitor();
        RecnScPgVisitor.m_outputfilename = reScPath;

        SymTabCreationVisitor STCVisitor   = new SymTabCreationVisitor();
        STCVisitor.m_outputfilename = symTabPath;
        STCVisitor.m_errorFilename = symTabcheckPath;


        TypeCheckingVisitor typeCheckVisitor = new TypeCheckingVisitor();
        typeCheckVisitor.m_outputfilename = typecheckPath;


        newAST.accept(STCVisitor);
        newAST.accept(RecnScPgVisitor);
        newAST.accept(typeCheckVisitor);

    }
}



