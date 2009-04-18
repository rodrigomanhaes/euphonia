package euphonia.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCConnection implements DatabaseConnection
{
	private Connection connection;
	
	private DBMS sgbd;
	
	public JDBCConnection(DBMS sgbd)
	{
		this.sgbd = sgbd;
		try
		{
			Class.forName(this.sgbd.driverClassName());
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Connection getConnection()
	{
		return connection;
	}

	@Override
	public JDBCConnection open(String url, String user, char[] password)
	{
		try
		{
			connection = DriverManager.getConnection(url, user, password != null ? new String(password) : null);
			return this;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(String sql)
	{
		try
		{
			connection.createStatement().execute(sql);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResultSet executeQuery(String query)
	{
		try
		{
			return connection.createStatement().executeQuery(query);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close()
	{
		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
}
