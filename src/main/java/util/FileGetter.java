package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileGetter {

	/**Helper method to find a file ending with a certain suffix, in a certain folder.
	 * @param dir		folder to search (not recursive?)
	 * @param suffix	suffix to look for
	 * @throws IOException	if more or less than one file was found*/
	public static File getFileEndingWith(File dir, String suffix) throws IOException {
		File[] matchingFiles = dir.listFiles( (File file) -> {
			return file.getName().endsWith(suffix) && file.isFile();
		});

		if(matchingFiles.length>1)
			throw new IOException("More than one file ending in "+suffix+" found.");
		else if(matchingFiles.length<1)
			throw new FileNotFoundException("No file ending in "+suffix+" found.");
		return matchingFiles[0];
	}
}
