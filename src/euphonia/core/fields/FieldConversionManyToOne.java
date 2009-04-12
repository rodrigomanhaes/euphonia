package euphonia.core.fields;

/**
 * Joins more than one field into one 
 */
public interface FieldConversionManyToOne
{
	public Object convert(Object... many);
}
