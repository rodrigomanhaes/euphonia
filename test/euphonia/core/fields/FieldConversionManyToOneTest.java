package euphonia.core.fields;

import org.junit.Test;

import static org.junit.Assert.*;

public class FieldConversionManyToOneTest 
{
	@Test
	public void shouldConcatPieces()
	{
		FieldConcat concat = new FieldConcat();
		assertEquals("abcdefghijkl", concat.convert("abc", "def", "ghi", "jkl"));
	}
	
	@Test
	public void shouldConcatPiecesWithFill()
	{
		FieldConcat concat = new FieldConcat(", ");
		assertEquals("abc, def, ghi, jkl", concat.convert("abc", "def", "ghi", "jkl"));
	}
}
