package euphonia.core.transformation;

import static org.junit.Assert.*;

import org.junit.Test;

public class CaseTransformationTest
{
	@Test
	public void upperTransformation()
	{
		Transformation transformation = CaseTransformation.upper();
		assertEquals("ISTO \u00C9 UM TESTE", transformation.transform("Isto \u00E9 um teste"));
	}
	
	@Test
	public void lowerTransformation()
	{
		Transformation transformation = CaseTransformation.lower();
		assertEquals("isto \u00E9 um teste", transformation.transform("ISTO \u00C9 UM TESTE"));
	}
	
	@Test
	public void capitalizeTransformation()
	{
		Transformation transformation = CaseTransformation.capitalize();
		assertEquals("Isto \u00E9 um Teste", transformation.transform("ISTO \u00C9 UM TESTE"));
	}
}
