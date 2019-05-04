package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.Join;
import objects.ColumnDefs;
import objects.SchemaStructure;

public class Utils {

	public static List<Expression> getExpressionForSelectionPredicate(Table table, List<ColumnDefs> cdefs,
			List<Expression> expressions) {
		List<Expression> lst = new ArrayList<Expression>();
		for (Expression expression : expressions) {
			if (expression instanceof EqualsTo)
				continue;
			String part = expression.toString().split(" ")[0];
			if (part.split("\\.").length == 2) {
				if (cdefs.contains(part)) {
					lst.add(expression);
				}
			} else {
				String val = (String) (table + "." + part);
				for (ColumnDefs cd : cdefs) {
					if (val.equals(cd.cdef.getColumnName())) {
						lst.add(expression);
					}
				}
			}
		}

		return lst;
	}

	public static List<Expression> getExpressionForJoinPredicate(Table table, List<ColumnDefs> cdefs,
			List<Expression> expressions) {
		return null;
	}

	public static List<Expression> splitAndClauses(Expression e) {
		List<Expression> ret = new ArrayList<Expression>();
		if (e != null) {
			if (e instanceof AndExpression) {
				AndExpression a = (AndExpression) e;
				ret.addAll(splitAndClauses(a.getLeftExpression()));
				ret.addAll(splitAndClauses(a.getRightExpression()));
			} else {
				ret.add(e);
			}
		}
		return ret;
	}

	public static String hashString(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
		return convertByteArrayToHexString(hashedBytes);
	}

	private static String convertByteArrayToHexString(byte[] arrayBytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return stringBuffer.toString();
	}

	public static String getDate(Function func) {
		return func.getParameters().getExpressions().get(0).toString();
	}

	public static Expression conquerExpression(List<Expression> elist) {
		if (elist.size() == 2) 
		{
			AndExpression and = new AndExpression();
			and.setLeftExpression(elist.get(0));
			and.setRightExpression(elist.get(1));
			return and;
		}
		Expression result = elist.get(0);

		for (int i = 1; i < elist.size(); i++) 
		{
			AndExpression and = new AndExpression();
			and.setLeftExpression(result);
			and.setRightExpression(elist.get(i));
			result = and;
		}

		return result;
	}

	private static Expression recursion(List<Expression> elist, int index, Expression result) {
		if (index == elist.size() + 2) {
			return result;
		}

		AndExpression and = new AndExpression();
		if (index < elist.size()) {
			System.out.println(" + " + result);
			and.setLeftExpression(result);
			and.setRightExpression(elist.get(index));
			recursion(elist, index + 1, and);

		}
		return result;
	}
	
	public static boolean isContainingColumns(String leftExpression, List<String> columns) {
		for(String column: columns) {
			if(column.equals(leftExpression)) {
				return true;
			}
			String[] columnSplt = column.split("\\.");
			for(String split: columnSplt) {
				if(split.equals(leftExpression)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isFileExists(String path) {
		File tmpDir = new File(path);
	    return tmpDir.exists();
	}
	
	public static void createDirectory(String folderName) {
		File f = new File(folderName);
		f.mkdirs();
	}
	
	private static RandomAccessFile raf = null;
	
	public static String readTuple(String path, int index) {
		String tuple = null;
		try {
			if(raf == null) {
				raf = new RandomAccessFile(path, "rw");
			}
			raf.seek(index);
			tuple =  raf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tuple;
	}
	
	public static boolean isPrimaryKey(String key, List<Index> indexes) {
		for(Index index: indexes) {
			if(index.getType().equals(Constants.PRIMARY_KEY)) {
				for(String primaryKey: index.getColumnsNames()) {
					if(key.equals(primaryKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static void createPrecedenceList() {// sorted by size
		String[] tables = {"LINEITEM", "ORDERS", "PARTSUPP", "CUSTOMER","PART", "SUPPLIER", "NATION", "REGION",};
		Integer[] sizeOfTable = {725, 164, 113, 23, 23, 3, 2, 1};
		for(int i = 0;i < tables.length;i++) {
			SchemaStructure.precedenceMap.put(tables[i], sizeOfTable[i]);
		}
	}
	
	public static boolean isHoldingPrecedence(Table left, Table right) {
		if(SchemaStructure.precedenceMap.get(right.getName()) < SchemaStructure.precedenceMap.get(left.getName())) {
			return true;
		}
		return false;
	}
}






