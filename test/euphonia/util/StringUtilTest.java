package euphonia.util;

import static org.junit.Assert.*;

import java.util.Locale;

import static euphonia.util.StringUtil.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class StringUtilTest
{
	@BeforeClass
	public static void setup()
	{
		Locale.setDefault(new Locale("pt", "BR"));
	}
	
	@Test
	public void shouldCapitalizeNames()
	{
		assertEquals("Alan Moore", capitalize("ALAN MOORE"));
		assertEquals("Grant Morrison", capitalize("GRANT MORRISON"));
		assertEquals("Al Pratt", capitalize("AL PRATT"));
	}

	@Test
	public void shouldCapitalizeNamesWithSpecialCharacters()
	{
		assertEquals("Lampar\u00E1o Ruim", capitalize("LAMPAR\u00C1O RUIM"));
		assertEquals("Fa\u00E7a Sua Parte", capitalize("FA\u00C7A SUA PARTE"));
	}
	
	@Test
	public void shouldCapitalizeLowerCase()
	{
		assertEquals("Al\u00EDvio Imediato", capitalize("al\u00EDvio imediato"));
	}
	
	@Test
	public void shouldCapitalizeMiguxoCase()
	{
		assertEquals("Canibal Vegetariano Devora Planta Carn\u00EDvora", 
			capitalize("CaNiBaL veGetARIAno DeVoRA PlANtA caRn\u00CDVoRA"));
	}
	
	@Test 
	public void shouldNotCapitalizeConnectors()
	{
		assertEquals("Engenheiros do Hawaii", capitalize("ENGENHEIROS DO HAWAII"));
		assertEquals("Gessinger, Licks e Maltz", capitalize("GESSINGER, LICKS E MALTZ"));
		assertEquals("Siege of Hate", capitalize("SIEGE OF HATE"));
		assertEquals("Chavo del Ocho", capitalize("CHAVO DEL OCHO"));
	}
	
	@Test
	public void shouldCapitalizeConnectorsOnBeginning()
	{
		assertEquals("Os Paralamas do Sucesso", capitalize("OS PARALAMAS DO SUCESSO"));
	}
	
	@Test
	public void shouldOneLetterWordBeLower()
	{
		assertEquals("Isto \u00E9 um Teste", capitalize("ISTO \u00C9 UM TESTE"));
	}
	
	@Test
	public void shouldOneLetterInTheBeginningBeUpper()
	{
		assertEquals("E o Circo Pega Fogo", capitalize("E O CIRCO PEGA FOGO"));
	}
	
}
