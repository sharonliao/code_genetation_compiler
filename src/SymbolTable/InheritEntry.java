package SymbolTable;

import java.util.Vector;

public class InheritEntry extends SymTabEntry {


    // SymTab p_table linked 到相应的class

    public InheritEntry(String p_name, SymTab p_table){
        super(new String("inherit"), new String("class"), p_name, p_table);
    }



    public String toString(){
        String ifLinked = "Linked";
        if (this.m_subtable == null){
            ifLinked = "not linked";
        }
        return 	String.format("%-12s" , "| " + m_kind) +
                String.format("%-12s" , "| " + m_name) +
                String.format("%-28s"  , "| " + m_type) +
                "| " + ifLinked;
    }
}


