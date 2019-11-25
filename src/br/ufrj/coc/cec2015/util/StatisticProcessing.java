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

		String PATH_RESULT = "D:\\Google Drive (COC)\\trabalho de dissertação\\2 - jade with eig\\experimentos\\CR_JADE_EIG";
		
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

	        String[] statHeaderColumns = new String[] {"BEST", "MEDIAN", "MEAN", "STD", "SR", "IPOP_RATE"}; //"ERRDIFF", "MAXDIST", 
	        int idxColumnHeaderAlgorithm = 0;

			Row rowHeaderAlgorithm = sheet.createRow(0);
			Row rowHeaderStats = sheet.createRow(1);

			Map<Integer, Row> rowFunctions = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerBests = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerMedians = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerMeans = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			Map<Integer, Cell> cellLowerStds = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			//Map<Integer, Cell> cellLowerErrorDiffs = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			//Map<Integer, Cell> cellLowerMaxDists = new HashMap<>(Properties.FUNCTION_NUMBERS.length);
			
			int np = 0;
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
						np = arguments.getPopulationSize();
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
				                	double sr = 0.0;
				                	if (columns.length == 11)
				                		sr = Double.valueOf(columns[10].trim());
				                	else if (columns.length == 9)
				                		sr = Double.valueOf(columns[7].trim());

				                	//double sr = Double.valueOf(columns[10].trim());
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
				                	/*
									// ERROR DIFF
				                	double errorDiff = Double.valueOf(columns[8].trim());
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(errorDiff);
				                	Cell cellLowerErrorDiff = cellLowerErrorDiffs.get(functionNumber);
				                	if (cellLowerErrorDiff == null || (errorDiff < cellLowerErrorDiff.getNumericCellValue()))
				                		cellLowerErrorDiffs.put(functionNumber, cell);

									// MAX DIST
				                	double maxDist = Double.valueOf(columns[9].trim());
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(maxDist);
				                	Cell cellLowerMaxDist = cellLowerMaxDists.get(functionNumber);
				                	if (cellLowerMaxDist == null || (maxDist < cellLowerMaxDist.getNumericCellValue()))
				                		cellLowerMaxDists.put(functionNumber, cell);
				                	*/
									// SR
						        	cell = rowFunction.createCell(idxColumnStatValue++);
						        	cell.setCellValue(sr);
						        	if (sr == 1.0)
						        		cell.setCellStyle(boldCellStyle);
						        	else if (sr > 0)
						        		cell.setCellStyle(warningCellStyle);

									// COUNT RESTARTS
						        	if (columns.length == 9) {
							        	double countRestarts = Double.valueOf(columns[8].trim());
							        	cell = rowFunction.createCell(idxColumnStatValue++);
							        	cell.setCellValue(countRestarts);
						        	}
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
			//for (Cell lower : cellLowerErrorDiffs.values())
			//	lower.setCellStyle(boldCellStyle);
			//for (Cell lower : cellLowerMaxDists.values())
			//	lower.setCellStyle(boldCellStyle);
			
	        FileOutputStream fileOut = new FileOutputStream(PATH_RESULT + String.format("\\P%d_D%d_R51.xlsx", np, individualSize));
	        workbook.write(fileOut);
	        fileOut.close();

	        // Closing the workbook
	        workbook.close();
		}
	}
}