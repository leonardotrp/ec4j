package br.ufrj.coc.ec4j.lab.media;
/**
 * Using NIO e JCodec to convert multiple sequence png images to mp4 video file
 * Copyright (C) 2019  Leonardo Pereira (www.leonardopereira.com.br)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

/**
 * @author Leonardo Pereira
 * 18/03/2019 23:01
 */
public class JCodecPNGtoMP4 {

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
        /*
        for(File f : files) {
            System.out.println(f.getName());
        }
        */
    }
	
	private static void generateVideoBySequenceImages(String videoFilename, String pathImages, String imageExt) throws Exception {
		SeekableByteChannel out = null;
		try {
			out = NIOUtils.writableFileChannel(videoFilename);
			
			// for Android use: AndroidSequenceEncoder
			AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(25, 1));

			Path directoryPath = Paths.get(new File(pathImages).toURI());

			if (Files.isDirectory(directoryPath)) {
				DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "*." + imageExt);

				List<File> filesList = new ArrayList<File>();
				for (Path path : stream) {
					filesList.add(path.toFile());
				}
				File[] files = new File[filesList.size()];
				filesList.toArray(files);

				sortByNumber(files);
				
				for (File img : files) {
					System.err.println("Encoding image " + img.getName());
					// Generate the image, for Android use Bitmap
					BufferedImage image = ImageIO.read(img);
					// Encode the image
					encoder.encodeImage(image);
				}
			}
			// Finalize the encoding, i.e. clear the buffers, write the header, etc.
			encoder.finish();
		} finally {
			NIOUtils.closeQuietly(out);
		}
	}
	
	public static void main(String[] args) throws Exception {
		String videoFilename = "C:/dev/movies/ipop-jade-eig/output.mp4";
		String pathImages = "C:/dev/movies/ipop-jade-eig/copy";
		generateVideoBySequenceImages(videoFilename, pathImages, "png");
	}
}
