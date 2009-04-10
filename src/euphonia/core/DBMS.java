package euphonia.core;

public enum DBMS
{
	DERBY_EMBEDDED("org.apache.derby.jdbc.EmbeddedDriver"),
	POSTGRESQL("org.postgresql.Driver"),
	FIREBIRD("org.firebirdsql.jdbc.FBDriver");
	
	private String driverClassName;
	
	private DBMS(String driverClassName)
	{
		this.driverClassName = driverClassName;
	}

	public String driverClassName()
	{
		return driverClassName;
	}

}
