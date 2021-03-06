package euphonia.core.database;

import org.junit.Test;

import euphonia.core.database.ConnectionFactory;
import euphonia.core.database.DBMS;
import euphonia.core.database.DatabaseConnection;
import static org.junit.Assert.*;

public class DatabaseConnectionTest
{
	@Test
	public void shouldConnectToDatabase() throws Exception
	{
		DatabaseConnection connection = ConnectionFactory.getConnection(DBMS.DERBY_EMBEDDED);
		connection.open("jdbc:derby:temp/testdb;create=true", null, null);
		assertEquals("Apache Derby", connection.getConnection().getMetaData().getDatabaseProductName());
	}
	
	@Test
	public void shouldCloseConnection() throws Exception
	{
		DatabaseConnection connection = ConnectionFactory.getConnection(DBMS.DERBY_EMBEDDED);
		connection.open("jdbc:derby:temp/testdb;create=true", null, null);
		connection.close();
		assertTrue(connection.getConnection().isClosed());
	}
}