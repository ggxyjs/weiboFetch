package common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager1 
{
    private Connection mConn;
    private Statement mStmt;
    private PreparedStatement mPreStmt;
    private ResultSet mRS;
    
    public DBManager1()
    {
    	this.mConn = null;
    	this.mStmt = null;
    	this.mPreStmt = null;
    	this.mRS = null;
    	
    	try 
	 	{
    		Class.forName(DBConst1.DRIVER_NAME);
	 	} 
    	catch (ClassNotFoundException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public Connection getConn()
    {
    	if(null == this.mConn)
    	{
	    	try 
		 	{
	    		this.mConn = DriverManager.getConnection(DBConst1.CONNET_URL, DBConst1.USERNAME, DBConst1.PASSWORD);
		 	} 
		 	catch (SQLException e)
		 	{
		 		e.printStackTrace();
		 	}
    	}
        return this.mConn;
    }
    
    public Statement getStmt()
    {
    	if(null == this.mStmt)
    	{
	    	try 
	    	{
	    		this.mStmt = this.getConn().createStatement();
			} 
	    	catch (SQLException e) 
	    	{
				e.printStackTrace();
			}
    	}
    	return this.mStmt;
    }
    
    public PreparedStatement getPreStmt(String aSql, boolean aAutoGeneratedKeys)
    {
    	this.mPreStmt = null;
    	if(null!=aSql && !aSql.equals(""))
    	{
	    	try 
	    	{
	    		if(!aAutoGeneratedKeys)
	    		{
	    			this.mPreStmt = this.getConn().prepareStatement(aSql);
	    		}
	    		else
	    		{
	    			this.mPreStmt = this.getConn().prepareStatement(aSql, 
										Statement.RETURN_GENERATED_KEYS);
	    		}
			} 
	    	catch (SQLException e) 
	    	{
				e.printStackTrace();
			}
    	}
    	return this.mPreStmt;
    }

    private boolean doStmt(Statement aStmt, String aSql, boolean aAutoGeneratedKeys)
    {
    	boolean state = false;
    	int retryCount = 5;  
        boolean transactionCompleted = false; 
    	if(null != aStmt)
    	{
	    	if(null!=aSql && !aSql.equals(""))
	    	{
	    		do { 
	    			try 
			    {retryCount=0;transactionCompleted = true;
			    	if(!aAutoGeneratedKeys)
			    	{//aStmt.execute("set names utf-8");
			    		state = aStmt.execute(aSql);
			    	}
			    	else
			    	{
			    		state = aStmt.execute(aSql, Statement.RETURN_GENERATED_KEYS);
			    	}
				} 
			   	catch (SQLException e) 
			   	{
					e.printStackTrace();
					 String sqlState = e.getSQLState();  
			           // 这个08S01就是这个异常的sql状态。单独处理手动重新链接就可以了。  
			            if ("08S01".equals(sqlState) || "40001".equals(sqlState))   
			                {                  
			                    retryCount--;              
			                 } else {                  
			                     retryCount = 0;              
			                     }          
				}
	    		}while (!transactionCompleted && (retryCount > 0));
    	}
    	}
    	return state;
    }
    
    public ResultSet retrieveByStmt(String sql)
    {
    	this.mRS = null;
    	try
    	{
    		doStmt(this.getStmt(), sql, false);
    		this.mRS = this.getStmt().getResultSet();
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return this.mRS;
    }
    
    public ResultSet insertByStmtAGK(String aSql)
    {
    	this.mRS = null;
	    try 
	    {
			doStmt(this.getStmt(), aSql, true);
			this.mRS = this.getStmt().getGeneratedKeys();
		} 
	    catch (SQLException e) 
	    {
			e.printStackTrace();
		}
    	return this.mRS;
    }
    
    public int insertByStmt(String aSql)
    {
    	int count = 0;
    	try
    	{
	    	doStmt(this.getStmt(), aSql, false);
	    	count = this.getStmt().getUpdateCount();
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return count;
    }
    
    public int deleteByStmt(String aSql)
    {
    	int count = 0;
    	try
    	{
	    	doStmt(this.getStmt(), aSql, false);
	    	count = this.getStmt().getUpdateCount();
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return count;
    }
    
    public int updateByStmt(String aSql)
    {
    	int count = 0;
    	try
    	{
	    	doStmt(this.getStmt(), aSql, false);
	    	count = this.getStmt().getUpdateCount();
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return count;
    }

    private boolean doPreStmt(PreparedStatement aPreStmt, Object[] aParams)
    {
    	boolean state = false;
    	
    	if(null != aPreStmt)
    	{
    		if(null == aParams)
    		{
    			aParams = new Object[0];
    		}
    		
    		try
    		{
	    		for(int i=0; i<aParams.length; i++)
	    		{
	    			aPreStmt.setObject(i+1, aParams[i]);
	    		}
	    		state = aPreStmt.execute();
    		}
    		catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    	
    	return state;
    }
    
    public ResultSet retrieveByPreStmt(String aSql, Object[] aParams)
    {
    	this.mRS = null;
    	try
	    {
    		PreparedStatement preStmt = this.getPreStmt(aSql, false);
    		if(null != preStmt)
    		{
		    	doPreStmt(preStmt, aParams);
		    	this.mRS = preStmt.getResultSet();
    		}
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return this.mRS;
    }
    
    public ResultSet insertByPreStmtAGK(String aSql, Object[] aParams)
    {
    	this.mRS = null;
    	try
    	{
    		PreparedStatement preStmt = this.getPreStmt(aSql, true);
    		if(null != preStmt)
    		{
		    	doPreStmt(preStmt, aParams);
		    	this.mRS = preStmt.getGeneratedKeys();
    		}
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return this.mRS;
    }
    
    public int insertByPreStmt(String aSql, Object[] aParams)
    {
    	int count = 0;
    	try
    	{
    		PreparedStatement preStmt = this.getPreStmt(aSql, false);
    		if(null != preStmt)
    		{
		    	doPreStmt(preStmt, aParams);
		    	count = preStmt.getUpdateCount();
    		}
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return count;
    }
    
    public int deleteByPreStmt(String sql, Object[] params)
    {
    	int count = 0;
    	try
    	{
    		PreparedStatement preStmt = this.getPreStmt(sql, false);
    		if(null != preStmt)
    		{
		    	doPreStmt(preStmt, params);
		    	count = preStmt.getUpdateCount();
    		}
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return count;
    }
    
    public int updateByPreStmt(String sql, Object[] params)
    {
    	int count = 0;
    	try
    	{
    		PreparedStatement preStmt = this.getPreStmt(sql, false);
    		if(null != preStmt)
    		{
		    	doPreStmt(preStmt, params);
		    	count = preStmt.getUpdateCount();
    		}
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return count;
    }
    
    public static int getRowNum(ResultSet rs)
    {
    	int count = 0;
    	if(null != rs)
    	{
    		try
    		{
	    		rs.last();
	    		count = rs.getRow();
    		}
    		catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    	return count;
    }
    
    public static void closeConn(Connection conn)
    {
    	if(null != conn)
    	{
    		try
    		{
    			conn.close();
    	    	conn = null;
    		}
    		catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void closeStmt(Statement stmt)
    {
    	if(null != stmt)
    	{
    		try
    		{
    			stmt.close();
    	    	stmt = null;
    		}
    		catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void closePreStmt(PreparedStatement preStmt)
    {
    	if(null != preStmt)
    	{
    		try
    		{
    			preStmt.close();
    	    	preStmt = null;
    		}
    		catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void closeRS(ResultSet rs)
    {
    	if(null != rs)
    	{
    		try
    		{
    			rs.close();
    	    	rs = null;
    		}
    		catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void close(DBManager1 dbm)
    {
    	if(null != dbm)
    	{
    		dbm.close();
    	}
    }
    
    public void close()
    {
    	DBManager1.closeConn(this.mConn);
    	DBManager1.closeStmt(this.mStmt);
    	DBManager1.closePreStmt(this.mPreStmt);
    	DBManager1.closeRS(this.mRS);
    }
    
}
