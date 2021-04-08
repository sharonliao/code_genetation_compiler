package SymbolTable;

import AST.Node;

import java.util.ArrayList;

public class ClassEntry extends SymTabEntry {

	public ClassEntry(String p_name, SymTab p_subtable){
		super(new String("class"), p_name, p_name, p_subtable);
	}


	public FuncEntry lookUpFunc(String funcName, String returnType, ArrayList<VarEntry> paramList){
		FuncEntry funcEntry = null;

		for(SymTabEntry entry : this.m_subtable.m_symlist){
			if(entry.getClass().getSimpleName().equals("FuncEntry")){

				ArrayList<VarEntry> funcDeclareParams = new ArrayList<>();
				for(SymTabEntry subentry : entry.m_subtable.m_symlist){
					if(subentry.m_kind.compareTo("param") == 0){
						funcDeclareParams.add((VarEntry) subentry);
					}
				}

				boolean nameEq = funcName.equals(entry.m_name);
				boolean paramEq = funcDeclareParams.equals(paramList);


				if(nameEq && paramEq){
					return (FuncEntry) entry;
				}
			}
		}

		return funcEntry;
	}


    public String covertParamlsitToStr_entry(ArrayList<VarEntry> paramList){
        String str = "";
        for(VarEntry param : paramList){
            str = str + param.m_type +",";
        }
        return str;
    }
		
	public String toString(){
//		System.out.println(String.format("%-12s" , "| " + m_kind) +
//			String.format("%-12s" , "| " + m_name) +
//			String.format("%-28s"  , "| " + m_type) +
//			"|" +
//			m_subtable);

		return 	String.format("%-12s" , "| " + m_kind) +
				String.format("%-40s" , "| " + m_name) + 
				"|" + 
				m_subtable;
	}
	
}

