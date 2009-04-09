package euphonia.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtil
{
	public static long recordCount(Connection connection, String tableName)
	{
		int count;
		try
		{
			Statement statement = connection.createStatement();
			try
			{
				ResultSet result = statement.executeQuery("select count(*) from " + tableName);
				try
				{
					result.next();
					count = result.getInt(1); 
				}
				finally
				{
					result.close();
				}
			}
			finally
			{
				statement.close();
			}
			return count;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		
	}
}
