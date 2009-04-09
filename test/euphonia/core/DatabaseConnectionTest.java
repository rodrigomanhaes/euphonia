package euphonia.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseConnectionTest
{
	@Test
	public void shouldConnectToDatabase() throws Exception
	{
		DatabaseConnection connection = ConnectionFactory.getConnection(DBMS.DERBY);
		connection.open("jdbc:derby:dbteste;create=true", null, null);
		assertEquals("Apache Derby", connection.getConnection().getMetaData().getDatabaseProductName());
	}
	
	@Test
	public void shouldCloseConnection() throws Exception
	{
		DatabaseConnection connection = ConnectionFactory.getConnection(DBMS.DERBY);
		connection.open("jdbc:derby:dbteste;create=true", null, null);
		connection.close();
		assertTrue(connection.getConnection().isClosed());
	}
}