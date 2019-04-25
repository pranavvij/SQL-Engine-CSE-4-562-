package queryexec;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import objects.ColumnDefs;
import objects.SchemaStructure;

public class CreateWrapper {
	
	public void saveCreateStructure(Statement query, String querystr) {
		
		CreateTable createtab = (CreateTable) query;
		Table tbal = createtab.getTable();
		
		try {
			File f = new File("createdir/"+tbal.getName().toLowerCase()+".txt");
//			Writer temp = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
//			temp.write(query.toString());
			BufferedWriter bfw = new BufferedWriter(new FileWriter(f,false));
			bfw.write(querystr);
			bfw.close();
//			temp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error storing create data");
			e.printStackTrace();
		}
	}
	public void createHandler(Statement query) {
		CreateTable createtab = (CreateTable) query;
		Table tbal = createtab.getTable();
		
		List<ColumnDefinition> cdef = createtab.getColumnDefinitions();
		List<ColumnDefs> cdfList = new ArrayList<ColumnDefs>();
		
		for (ColumnDefinition cd : cdef) {
			ColumnDefs c = new ColumnDefs();
			c.cdef = cd;
			cdfList.add(c);
			SchemaStructure.columnTableMap.put(cd.getColumnName(), tbal);		
		}
		SchemaStructure.schema.put(tbal.getName(), cdfList);
		SchemaStructure.tableMap.put(tbal.getName(), tbal);
	}
}
