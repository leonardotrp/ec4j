package br.ufrj.coc.cec2015.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.AlgorithmArguments;

public class StatisticProcessing {

	public static void main(String[] args) throws Exception {
		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - dpade with eig\\results\\P100\\JADE_DPADE_woA";

		for (int individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions

	        // Create a Workbook
	        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
	        // Create a Sheet
	        Sheet sheet = workbook.createSheet("StatisticProcessing");

	        // Create a Font for styling header cells
	        Font headerFont = workbook.createFont();
	        headerFont.setBold(true);
	        headerFont.setFontHeightInPoints((short) 10);
	        headerFont.setColor(IndexedColors.BLUE.getIndex());
	        
	        // Create a CellStyle with the font
	        CellStyle headerCellStyle = workbook.createCellStyle();
	        headerCellStyle.setFont(headerFont);

	        // Create a Font for styling header cells
	        Font bestFont = workbook.createFont();
	        bestFont.setBold(true);
	        CellStyle bestCellStyle = workbook.createCellStyle();
	        bestCellStyle.setFont(bestFont);

	        String[] statHeaderColumns = new String[] {"BEST", "MEAN", "STD", "SR"};
	        int idxColumnHeaderAlgorithm = 0;

			Row rowHeaderAlgorithm = sheet.createRow(0);
			Row rowHeaderStats = sheet.createRow(1);
			Map<Integer, Row> rowFunctions = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			
			for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms

				String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
				Algorithm algorithm = (Algorithm) Class.forName(className).newInstance();

				for (String variant : algorithm.getVariants()) {  // loop variants

			        int idxRowFunction = 2;
					int idxColumnStat = idxColumnHeaderAlgorithm;
					
					// LINHA 1: Cabeçalho Principal - Nome dos algoritmos
					Cell cell = rowHeaderAlgorithm.createCell(idxColumnHeaderAlgorithm++);
					cell.setCellValue(algotithmName + '(' + variant + ')');
					cell.setCellStyle(headerCellStyle);
			        for (int idx = 0; idx < statHeaderColumns.length - 1; idx++)
			        	rowHeaderAlgorithm.createCell(idxColumnHeaderAlgorithm++);
		
					// LINHA 2: Cabeçalho Secundário - Colunas de estatística
			        int idxColumnHeaderStat = idxColumnStat;
			        for (String statColumn : statHeaderColumns) {
			        	cell = rowHeaderStats.createCell(idxColumnHeaderStat++);
			        	cell.setCellValue(statColumn);
			        	cell.setCellStyle(headerCellStyle);
			        }
					
					for (int functionNumber : Properties.FUNCTION_NUMBERS) { // loop functions

						AlgorithmArguments arguments = new AlgorithmArguments(algotithmName, variant, algorithm.getInfo(), functionNumber, individualSize);

						String fileRoundErrorsName = PATH_RESULT + '\\' + algotithmName + '\\' + arguments.getPrefixFile() + "_statistics.csv";
						BufferedReader br = new BufferedReader(new FileReader(fileRoundErrorsName));
						String line;
						double[] statValues = new double[4];
						while ((line = br.readLine()) != null) {

			                // use comma as separator
			                String[] columns = line.split(",");
			                if (columns[0].trim().equals("F(" + functionNumber + ")")) {
			                	double best = Double.valueOf(columns[1].trim()); statValues[0] = best;
			                	double mean = Double.valueOf(columns[3].trim()); statValues[1] = mean;
			                	double std = Double.valueOf(columns[4].trim()); statValues[2] = std;
			                	double sr = Double.valueOf(columns[8].trim()); statValues[3] = sr;
			                }
			            }
						br.close();
						
						// pega a linha da função
						Row rowFunction = rowFunctions.get(functionNumber);
						if (rowFunction == null) {
							rowFunction = sheet.createRow(idxRowFunction++);
							rowFunctions.put(functionNumber, rowFunction);
						}
						int idxColumnStatValue = idxColumnStat;
				        for (double statValue : statValues) {
				        	cell = rowFunction.createCell(idxColumnStatValue++);
				        	cell.setCellValue(statValue);
				        }
					}
				}
			}
	        // Write the output to a file
			File directory = new File(PATH_RESULT + "\\D" + individualSize + '\\');
			if (!directory.exists())
				directory.mkdirs();

	        FileOutputStream fileOut = new FileOutputStream(directory.getAbsolutePath() + "\\JADE_DPADE_woA_Statistics.xlsx");
	        workbook.write(fileOut);
	        fileOut.close();

	        // Closing the workbook
	        workbook.close();
		}
	}
}