package com.xjtu.multithread;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
public class DbHelper {
	public static String driver = "com.mysql.jdbc.Driver";
	 
    private static String host;
 
    private static String user;
 
    private static String pwd;
 
    private static Connection conn = null;
 
    private static Statement stmt = null;
 
    public static void connect(String host, String user, String pwd) {
    	DbHelper.close();
    	DbHelper.host = host;
    	DbHelper.user = user;
    	DbHelper.pwd = pwd;
    }
    public static int excutesql(String sql) {
    
    	int ret = 1;
    	if (stmt == null) {
        	DbHelper.statement();
        }
        try {
        	stmt.execute(sql);           
        } catch (SQLException e) {
        	ret = 0;
            e.printStackTrace();
        }        
    	return ret;
    	
    }
    public static synchronized List<HashMap<String, String>> query(String sql) {
        return DbHelper.result(sql);
    }
 
    public static synchronized void close() {
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    private static void connectMySQL() {
        try {
            Class.forName(driver).newInstance();
            conn = (Connection) DriverManager.getConnection("jdbc:mysql://"
                    + host + "?useUnicode=true&characterEncoding=UTF8", user,
                    pwd);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
 
    }
 
    private static void statement() {
        if (conn == null) {
        	DbHelper.connectMySQL();
        }
        try {
            stmt = (Statement) conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    private static ResultSet resultSet(String sql) {
        ResultSet rs = null;
        if (stmt == null) {
        	DbHelper.statement();
        }
        try {
        	//stmt.execute(sql);
           rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
 
    private static List<HashMap<String, String>> result(String sql) {
        ResultSet rs = DbHelper.resultSet(sql);        
        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        try {            
            while (rs!= null && rs.next()) {
            	ResultSetMetaData md = rs.getMetaData();
                int cc = md.getColumnCount();
                HashMap<String, String> columnMap = new HashMap<String, String>();
                for (int i = 1; i <= cc; i++) {
                    columnMap.put(md.getColumnName(i), rs.getString(i));
                }
                result.add(columnMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
