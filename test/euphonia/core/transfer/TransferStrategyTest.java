package euphonia.core.transfer;

import org.junit.Test;

import euphonia.core.transfer.Concat;

import static org.junit.Assert.*;

public class TransferStrategyTest 
{
	@Test
	public void shouldConcatPieces()
	{
		Concat concat = new Concat();
		assertEquals("abcdefghijkl", concat.transfer("abc", "def", "ghi", "jkl")[0]);
	}
	
	@Test
	public void shouldConcatPiecesWithFill()
	{
		Concat concat = new Concat(", ");
		assertEquals("abc, def, ghi, jkl", concat.transfer("abc", "def", "ghi", "jkl")[0]);
	}
}
