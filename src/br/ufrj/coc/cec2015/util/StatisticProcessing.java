package br.ufrj.coc.cec2015.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

import br.ufrj.coc.cec2015.Main;
import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.AlgorithmArguments;

public class StatisticProcessing {

	public static void main(String[] args) throws Exception {

		String PATH_RESULT = "C:\\dev\\workspace\\CEC2015\\results\\8ee5c7d3-ba65-4013-8e4b-3963a17a0f9d";
		
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
	        CellStyle headerCellStyle = workbook.createCellStyle();
	        headerCellStyle.setFont(headerFont);

	        // Create a Font for styling header cells
	        Font boldFont = workbook.createFont();
	        boldFont.setBold(true);
	        CellStyle boldCellStyle = workbook.createCellStyle();
	        boldCellStyle.setFont(boldFont);

	        // Create a Font for styling header cells
	        Font warningFont = workbook.createFont();
	        warningFont.setBold(true);
	        warningFont.setColor(IndexedColors.RED.getIndex());
	        CellStyle warningCellStyle = workbook.createCellStyle();
	        warningCellStyle.setFont(warningFont);

	        String[] statHeaderColumns = new String[] {"BEST", "MEDIAN", "MEAN", "STD", "SR"};
	        int idxColumnHeaderAlgorithm = 0;

			Row rowHeaderAlgorithm = sheet.createRow(0);
			Row rowHeaderStats = sheet.createRow(1);

			Map<Integer, Row> rowFunctions = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerBests = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerMedians = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerMeans = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerStds = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			
			for (String algotithmName : Properties.ALGORITHMS) { // loop algorithms

				String className = Algorithm.class.getPackage().getName() + '.' + algotithmName.toLowerCase() + '.' + algotithmName;
				Algorithm algorithm = Main.newInstanceAlgorithm(className);

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
						BufferedReader br = null;
						try {
							br = new BufferedReader(new FileReader(fileRoundErrorsName));
							String line;
							while ((line = br.readLine()) != null) {

				                // use comma as separator
				                String[] columns = line.split(",");
				                if (columns.length > 0 && columns[0].trim().equals("F(" + functionNumber + ")")) {

									// pega a linha da função
									Row rowFunction = rowFunctions.get(functionNumber);
									if (rowFunction == null) {
										rowFunction = sheet.createRow(idxRowFunction++);
										rowFunctions.put(functionNumber, rowFunction);
									}

									int idxColumnStatValue = idxColumnStat;
									
									// BEST
				                	double best = Double.valueOf(columns[1].trim());
				                	double sr = Double.valueOf(columns[8].trim());
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(best);
						        	if (best <= Properties.MIN_ERROR_VALUE)
						        		cell.setCellStyle(boldCellStyle);
				                	Cell cellLowerBest = cellLowerBests.get(functionNumber);
				                	if (cellLowerBest == null || (best < cellLowerBest.getNumericCellValue()))
				                		cellLowerBests.put(functionNumber, cell);

									// MEDIAN
				                	double median = Double.valueOf(columns[3].trim());
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(median);
						        	if (median <= Properties.MIN_ERROR_VALUE || (sr == 1.0))
						        		cell.setCellStyle(boldCellStyle);
				                	Cell cellLowerMedian = cellLowerMedians.get(functionNumber);
				                	if (cellLowerMedian == null || (median < cellLowerMedian.getNumericCellValue()))
				                		cellLowerMedians.put(functionNumber, cell);

				                	// MEAN
				                	double mean = Double.valueOf(columns[4].trim());
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(mean);
						        	if (mean <= Properties.MIN_ERROR_VALUE || (sr == 1.0))
						        		cell.setCellStyle(boldCellStyle);
				                	Cell cellLowerMean = cellLowerMeans.get(functionNumber);
				                	if (cellLowerMean == null || (mean < cellLowerMean.getNumericCellValue()))
				                		cellLowerMeans.put(functionNumber, cell);

									// STD
				                	double std = Double.valueOf(columns[5].trim());
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(std);
						        	if (std <= Properties.MIN_ERROR_VALUE || (sr == 1.0))
						        		cell.setCellStyle(boldCellStyle);
				                	Cell cellLowerStd = cellLowerStds.get(functionNumber);
				                	if (cellLowerStd == null || (std < cellLowerStd.getNumericCellValue()))
				                		cellLowerStds.put(functionNumber, cell);

									// SR
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(sr);
						        	if (sr == 1.0)
						        		cell.setCellStyle(boldCellStyle);
						        	else if (sr > 0)
						        		cell.setCellStyle(warningCellStyle);
				                }
				            }
						} catch (FileNotFoundException e) {
							System.err.println("File not found: " + fileRoundErrorsName);
						} finally {
							if (br != null)
								br.close();
						}
					}
				}
			}
			
			for (Cell lower : cellLowerBests.values())
				lower.setCellStyle(boldCellStyle);
			for (Cell lower : cellLowerMedians.values())
				lower.setCellStyle(boldCellStyle);
			for (Cell lower : cellLowerMeans.values())
				lower.setCellStyle(boldCellStyle);
			for (Cell lower : cellLowerStds.values())
				lower.setCellStyle(boldCellStyle);
			
	        FileOutputStream fileOut = new FileOutputStream(PATH_RESULT + "\\P40_D" + individualSize + "_R51.xlsx");
	        workbook.write(fileOut);
	        fileOut.close();

	        // Closing the workbook
	        workbook.close();
		}
	}
}