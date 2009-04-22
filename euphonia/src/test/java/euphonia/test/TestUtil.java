package euphonia.test;

import java.io.File;

public class TestUtil 
{
	public static boolean deleteDirectory(String path)
	{
		return deleteDirectory(new File(path));
	}
	
	public static boolean deleteDirectory(File path)
	{
		if (path.exists())
		{
			File[] files = path.listFiles();
			for (File file: files)
			{
				if (file.isDirectory())
					deleteDirectory(file);
				else
					file.delete();
			}
		}
		return path.delete();
	}
}
