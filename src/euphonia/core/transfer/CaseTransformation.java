package euphonia.core.transfer;

import euphonia.util.StringUtil;
import static euphonia.util.CollectionUtil.array;

public class CaseTransformation implements TransferStrategy
{
	private CaseType type;
	
	public CaseTransformation(CaseType type)
	{
		this.type = type; 
	}
	
	public static TransferStrategy upper()
	{
		return new CaseTransformation(CaseType.UPPER);
	}

	public static TransferStrategy lower()
	{
		return new CaseTransformation(CaseType.LOWER);
	}

	public static TransferStrategy capitalize()
	{
		return new CaseTransformation(CaseType.CAPITALIZED);
	}

	@Override
	public Object[] transfer(Object... values) 
	{
		return array(type.transform((String) values[0]));
	}
	 
}

enum CaseType 
{
	UPPER
	{
		public String transform(String input)
		{
			return input.toUpperCase();
		}
	},
	
	LOWER
	{
		public String transform(String input)
		{
			return input.toLowerCase();
		}
	},
	
    CAPITALIZED
    {
		public String transform(String input)
		{
			return StringUtil.capitalize(input);
		}
    };
    
	public abstract String transform(String input);
}
