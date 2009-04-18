package euphonia.core.transfer;

import static org.junit.Assert.*;

import org.junit.Test;

import euphonia.core.transfer.CaseTransformation;
import static euphonia.util.CollectionUtil.array;

public class CaseTransformationTest
{
	@Test
	public void upperTransformation()
	{
		TransferStrategy transformation = CaseTransformation.upper();
		assertArrayEquals(array("ISTO \u00C9 UM TESTE"), transformation.transfer("Isto \u00E9 um teste"));
	}
	
	@Test
	public void lowerTransformation()
	{
		TransferStrategy transformation = CaseTransformation.lower();
		assertArrayEquals(array("isto \u00E9 um teste"), transformation.transfer("ISTO \u00C9 UM TESTE"));
	}
	
	@Test
	public void capitalizeTransformation()
	{
		TransferStrategy transformation = CaseTransformation.capitalize();
		assertArrayEquals(array("Isto \u00E9 um Teste"), transformation.transfer("ISTO \u00C9 UM TESTE"));
	}
}
