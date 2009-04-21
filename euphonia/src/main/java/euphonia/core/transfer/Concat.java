package euphonia.core.transfer;

import static euphonia.util.CollectionUtil.array;

public class Concat implements TransferStrategy 
{
	private String fill;	
	
	public Concat()
	{
	}
	
	public Concat(String fill)
	{
		this.fill = fill;
	}
	
	private boolean hasFill()
	{
		return fill != null;
	}
	
	@Override
	public Object[] transfer(Object... values) 
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Object piece: values)
		{
			if (!first && hasFill())
				builder.append(fill);
			else
				first = false;	
			builder.append(piece);
		}
		return array(builder.toString());
	}

}
