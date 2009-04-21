package euphonia.core.database;

public class ConnectionFactory
{

	public static DatabaseConnection getConnection(DBMS sgbd)
	{
		return new JDBCConnection(sgbd);
	}

}
