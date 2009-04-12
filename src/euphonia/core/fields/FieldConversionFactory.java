package euphonia.core.fields;

public class FieldConversionFactory 
{
	public static FieldConversionManyToOne concat()
	{
		return new FieldConcat();
	}
	
	public static FieldConversionManyToOne concat(String fill)
	{
		return new FieldConcat(fill);
	}
}