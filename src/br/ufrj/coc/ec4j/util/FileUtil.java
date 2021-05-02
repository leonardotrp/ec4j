package br.ufrj.coc.ec4j.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class FileUtil {

	public static String getFileName(String ID, String relativePath, String filename) {
		URI uri;
		try {
			String root = Properties.RESULTS_ROOT + ID + File.separator;
			uri = new URI(root + relativePath);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		File directory = new File(uri);
		if (!directory.exists())
			directory.mkdirs();
		return directory.getAbsolutePath() + File.separator + filename;
	}

	public static File getInitialPopulationFile(String cvsFilename) {
		File file = null;
		if (cvsFilename.length() > 0) {
			URI uri;
			try {
				String root = Properties.RESULTS_ROOT;
				uri = new URI(root + cvsFilename);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			file = new File(uri);
		}
		return file;
	}

	public static File getInitialPopulationFile() {
		return getInitialPopulationFile(Properties.INITIAL_POPULATION_FILE);
	}
}
