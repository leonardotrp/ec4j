package br.ufrj.coc.cec2015.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class FileUtil {

	public static String getFileName(String ID, String relativePath, String filename) {
		URI uri;
		try {
			String root = Properties.RESULTS_ROOT + ID + '/';
			uri = new URI(root + relativePath);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		File directory = new File(uri);
		if (!directory.exists())
			directory.mkdirs();
		return directory.getAbsolutePath() + '\\' + filename;
	}
	
	public static File getInitialPopulationFile() {
		File file = null;
		if (Properties.INITIAL_POPULATION_FILE.length() > 0) {
			URI uri;
			try {
				String root = Properties.RESULTS_ROOT;
				uri = new URI(root + Properties.INITIAL_POPULATION_FILE);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			file = new File(uri);
		}
		return file;
	}
}
