package br.ufrj.coc.cec2015.lab.media;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CopyPNGFiles {
	
	private static void sortByNumber(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }
            private int extractNumber(String name) {
                int i = 0;
                try {
                    int s = name.lastIndexOf('_')+1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0; // if filename does not match the format then default to 0
                }
                return i;
            }
        });
        for(File f : files) {
            System.out.println(f.getName());
        }
    }

	public static void main(String[] args) {
		Path directoryPath = Paths.get("C:", "dev/movies/ipop-jade-eig");

		if (Files.isDirectory(directoryPath)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "*.png")) {
				List<File> filesList = new ArrayList<File>();
				for (Path path : stream) {
					filesList.add(path.toFile());
				}
				File[] files = new File[filesList.size()];
				filesList.toArray(files);

				sortByNumber(files);

				for (int index = 0; index < files.length; index++) {
					InputStream in = new FileInputStream(files[index]);
					
					File copyDir = new File("C:/dev/movies/ipop-jade-eig/copy/");
					if (!copyDir.exists())
						copyDir.mkdir();
					
					Path target = Paths.get(copyDir.getPath() + '/' + index + ".png");
					
					if (index % 5 == 0) {
						System.out.println("Copying... " + files[index].getName());
						Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
