package SymbolTable;
import java.util.ArrayList;

public class SymTab {
	public String                 m_name       = null;
	public ArrayList<SymTabEntry> m_symlist    = null;
	public int                    m_size       = 0;
	public int                    m_tablelevel = 0;
	public SymTab m_uppertable = null;

	
	public SymTab(int p_level, SymTab p_uppertable){
		m_tablelevel = p_level;
		m_name = null;
		m_symlist = new ArrayList<SymTabEntry>();
		m_uppertable = p_uppertable;
	}
	
	public SymTab(int p_level, String p_name, SymTab p_uppertable){
		m_tablelevel = p_level;
		m_name = p_name;
		m_symlist = new ArrayList<SymTabEntry>();
		m_uppertable = p_uppertable;
	}
	
	public void addEntry(SymTabEntry p_entry){
		m_symlist.add(p_entry);	
	}
	
	public SymTabEntry lookupName(String p_tolookup) {
		SymTabEntry returnvalue = new SymTabEntry();
		boolean found = false;
		for( SymTabEntry rec : m_symlist) {
			if (rec.m_name.equals(p_tolookup)) {
				returnvalue = rec;
				found = true;
			}
		}
		if (!found) {
			if (m_uppertable != null) {
				returnvalue = m_uppertable.lookupName(p_tolookup);	
			}
		}
		return returnvalue;
	}


	public SymTabEntry lookupFuncTag(String p_tolookup) {
		SymTabEntry returnvalue = new SymTabEntry();
		boolean found = false;
		for( SymTabEntry rec : m_symlist) {
			if (rec.getClass().getSimpleName().equals("FuncEntry") && ((FuncEntry)rec).tag.equals(p_tolookup)) {
				returnvalue = rec;
				found = true;
			}
		}
		if (!found) {
			if (m_uppertable != null) {
				returnvalue = m_uppertable.lookupName(p_tolookup);
			}
		}
		return returnvalue;
	}

	public FuncEntry lookUpFunc(String funcName, String returnType, ArrayList<VarEntry> paramList){
		FuncEntry funcEntry = null;
		System.out.println("find free function : "+ funcName);

		for(SymTabEntry entry : this.m_symlist){

			if(entry.getClass().getSimpleName().equals("FuncEntry")){
				System.out.println("free function : "+ entry.m_name);
				ArrayList<VarEntry> funcDeclareParams = new ArrayList<>();
				for(SymTabEntry subentry : entry.m_subtable.m_symlist){
					if(subentry.m_kind.compareTo("param") == 0){
						funcDeclareParams.add((VarEntry) subentry);
					}
				}

				boolean nameEq = funcName.equals(entry.m_name);
				boolean paramEq = paramList.equals(funcDeclareParams);

				if(nameEq && paramEq){
					return (FuncEntry) entry;
				}
			}
		}

		return funcEntry;
	}


	public int getLevel(){
	    return m_tablelevel;

    }
	public String toString(){
		String stringtoreturn = new String();
		String prelinespacing = new String();
		for (int i = 0; i < this.m_tablelevel; i++)
			prelinespacing += "|    "; 
		stringtoreturn += "\n" + prelinespacing + "===========================================================\n";
		stringtoreturn += prelinespacing + String.format("%-25s" , "| table: " + m_name) + String.format("%-27s" , " scope size: " + m_size) + "|\n";
		stringtoreturn += prelinespacing        + "===========================================================\n";

		for (int i = 0; i < m_symlist.size(); i++){
            m_symlist.get(i).toString();
			stringtoreturn +=  prelinespacing + m_symlist.get(i).toString() + '\n'; 
		}
		stringtoreturn += prelinespacing        + "===========================================================";
		return stringtoreturn;
	}
}
