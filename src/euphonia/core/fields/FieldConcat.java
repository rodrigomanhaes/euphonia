package euphonia.core.fields;

public class FieldConcat implements FieldConversionManyToOne 
{
	private String fill;	
	
	public FieldConcat()
	{
	}
	
	public FieldConcat(String fill)
	{
		this.fill = fill;
	}
	
	private boolean hasFill()
	{
		return fill != null;
	}
	
	@Override
	public Object convert(Object... many) 
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Object piece: many)
		{
			if (!first && hasFill())
				builder.append(fill);
			else
				first = false;	
			builder.append(piece);
		}
		return builder.toString();
	}

}
