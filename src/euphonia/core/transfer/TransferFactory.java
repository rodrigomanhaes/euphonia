package euphonia.core.transfer;


public class TransferFactory 
{
	public static TransferStrategy concat()
	{
		return new Concat();
	}
	
	public static TransferStrategy concat(String fill)
	{
		return new Concat(fill);
	}
}