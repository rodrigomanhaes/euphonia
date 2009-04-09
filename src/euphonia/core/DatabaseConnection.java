package euphonia.core;

import java.sql.Connection;
import java.sql.ResultSet;

public interface DatabaseConnection
{

	DatabaseConnection open(String url, String user, char[] password);

	Connection getConnection();

	void execute(String sql);
	
	ResultSet executeQuery(String query);

	void close();
}
