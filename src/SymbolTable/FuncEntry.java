package SymbolTable;

import java.util.ArrayList;
import java.util.Vector;

public class FuncEntry extends SymTabEntry {
	
	public Vector<VarEntry> m_params   = new Vector<VarEntry>();
	public String classMethod = "";
	public String visibility;
	public boolean definedFlag = false;
	public String tag = "";
	
	public FuncEntry(String p_type, String p_name, String p_classMethod, String p_visibility, SymTab p_table){
		super(new String("func"), p_type, p_name, p_table);
		classMethod = p_classMethod;
		visibility = p_visibility;
	}

	public FuncEntry(String p_type, String p_name, String p_classMethod, SymTab p_table){
		super(new String("func"), p_type, p_name, p_table);
		classMethod = p_classMethod;
	}

	public ArrayList<VarEntry> getParamsEntryList(){
        ArrayList<VarEntry> funcParams = new ArrayList<>();
        for(SymTabEntry subentry : this.m_subtable.m_symlist){
            if(subentry.m_kind.compareTo("param") == 0){
                funcParams.add((VarEntry) subentry);
            }
        }
        return funcParams;
    }

	public String toString(){
//		System.out.println(String.format("%-12s" , "| " + m_kind) +
//				String.format("%-12s" , "| " + m_name) +
//				String.format("%-28s"  , "| " + m_type) +
//				"|" +
//				m_subtable);

		return 	String.format("%-12s" , "| " + m_kind) +
				String.format("%-15s" , "| " + m_name) +
				String.format("%-30s"  , "| " + m_type) +
				"|" + 
				m_subtable;
	}	
}
