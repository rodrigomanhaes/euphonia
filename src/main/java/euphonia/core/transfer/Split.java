package euphonia.core.transfer;

public class Split implements TransferStrategy 
{
	private String regex;
	
	public Split(String regex) 
	{
		this.regex = regex;
	}

	@Override
	public Object[] transfer(Object... values) 
	{
		return values[0].toString().split(regex);
	}

}
