package euphonia.core.transformation;

import euphonia.util.StringUtil;

public class CaseTransformation implements Transformation
{
	private CaseType type;
	
	public CaseTransformation(CaseType type)
	{
		this.type = type; 
	}
	
	@Override
	public Object transform(Object input)
	{
		return type.transform((String) input);
	}
	
	public static Transformation upper()
	{
		return new CaseTransformation(CaseType.UPPER);
	}

	public static Transformation lower()
	{
		return new CaseTransformation(CaseType.LOWER);
	}

	public static Transformation capitalize()
	{
		return new CaseTransformation(CaseType.CAPITALIZED);
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
