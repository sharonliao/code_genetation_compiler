package SymbolTable;

import java.util.Vector;

public class VarEntry extends SymTabEntry {
	public String visibility;
		
	public VarEntry(String p_kind, String p_type, String p_name, Vector<Integer> p_dims, String visibility ){
		super(p_kind, p_type, p_name, null);
		m_dims = p_dims;
		this.visibility = visibility;
	}

	public VarEntry(String p_kind, String p_type, String p_name, Vector<Integer> p_dims ){
		super(p_kind, p_type, p_name, null);
		m_dims = p_dims;
		this.visibility = "";
	}

	public boolean equals(Object o){
		if (o == this) {
			return true;
		}
		if (!(o instanceof VarEntry)) {
			return false;
		}
		VarEntry var = (VarEntry) o;

		boolean typeEq = var.m_type.equals(this.m_type);
        boolean dimEq = var.m_dims.size() == this.m_dims.size();


		if(typeEq && dimEq ){
			return true;
		}else {
			return false;
		}
	}

	public String getDimsString(){
	    String str = "";
	    for(int dim : m_dims){
	        if (dim != -1){
                str = str + "[" + dim + "]";
            }else {
                str = str + "[" +  " ]";
            }
        }
        return str;
    }
		
	public String toString(){
		return 	String.format("%-12s" , "| " + m_kind) +
				String.format("%-15s" , "| " + m_name) +
				String.format("%-15s"  , "| " + (m_type + getDimsString())) +
				String.format("%-8s"  , "| " + m_size) + 
				String.format("%-8s"  , "| " + m_offset)
		        + "|";
	}
}
