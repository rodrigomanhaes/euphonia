package euphonia.core;

public enum DBMS
{
	DERBY("org.apache.derby.jdbc.EmbeddedDriver");
	
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
