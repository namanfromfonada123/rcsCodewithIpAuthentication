package com.messaging.rcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.messaging.rcs.exceptions.FileUploadException;

import au.com.bytecode.opencsv.CSVReader;

public class CSVUtils {
	public static final char DEFAULT_SEPARATOR = ',';

	private CSVUtils() {
		super();
	}

	/** The Constant log. */
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(CSVUtils.class);

	/**
	 * Csv to string list.
	 *
	 * @param file                  the file
	 * @param schemaNumberOfColumns the schema number of columns
	 * @param filePath              the file path
	 * @return the list
	 * @throws IOException         Signals that an I/O exception has occurred.
	 * @throws FileUploadException the file upload exception
	 */
	public static List<String[]> csvToStringList(MultipartFile file, Integer schemaNumberOfColumns, Boolean deleteFile)
			throws IOException, FileUploadException {

		try {
			CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()), DEFAULT_SEPARATOR);
			@SuppressWarnings("unchecked")
			List<String[]> csvEntries = reader.readAll();
			log.info("***** CSV TO STRING LIST *****" + new Gson().toJson(csvEntries).toString());
			return csvEntries;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void validateCsv(String fileName, Integer schemaNumberOfColumns)
			throws IOException, FileUploadException {
		log.info("***** CSVUtils.validateCsv() *****");
		CSVReader reader = new CSVReader(
				new InputStreamReader(new FileInputStream(fileName), StandardCharsets.ISO_8859_1), DEFAULT_SEPARATOR);
		validateHeader(reader.readNext(), schemaNumberOfColumns);
		reader.close();
	}

	/**
	 * Convert multi part file to file.
	 *
	 * @param file     the file
	 * @param filePath the file path
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File convertMultiPartFileToFile(MultipartFile file, String filePath) throws IOException {
		File convertedFile = new File(filePath);
		log.info("***** Convert Multi Part File To File Name *****", convertedFile);
		file.transferTo(convertedFile);
		return convertedFile;
	}

	/**
	 * Validate csv.
	 *
	 * @param file                  the file
	 * @param lines                 the lines
	 * @param schemaNumberOfColumns the schema number of columns
	 * @throws FileUploadException the file upload exception
	 */
	private static void validateCsv(List<String[]> lines, Integer schemaNumberOfColumns) throws FileUploadException {
		if (lines.isEmpty()) {
			log.info("***** VALIDATE CSV *****File cannot be empty");
			throw new FileUploadException(HttpStatus.BAD_REQUEST, "File cannot be empty");
		}
		validateHeader(lines.get(0), schemaNumberOfColumns);
	}

	private static void validateHeader(String[] row, Integer schemaNumberOfColumns) throws FileUploadException {
		if (row.length < schemaNumberOfColumns) {
			log.info("***** VALIDATE HEADER *****Please check the number of mandatory columns");
			throw new FileUploadException(HttpStatus.BAD_REQUEST, "Please check the number of mandatory columns");
		}
	}

	private static void validateExcel1(List<List<String>> lines, Integer schemaNumberOfColumns)
			throws FileUploadException {
		if (lines.isEmpty()) {
			throw new FileUploadException(HttpStatus.BAD_REQUEST, "File cannot be empty");
		}
		if (lines.get(0).size() < schemaNumberOfColumns) {
			throw new FileUploadException(HttpStatus.BAD_REQUEST, "Please check the number of mandatory columns");
		}
	}

	/**
	 * Gets the read xlsx file.
	 *
	 * @param file                  the file
	 * @param schemaNumberOfColumns the schema number of columns
	 * @param filePath              the file path
	 * @return the read xlsx file
	 * @throws IOException         Signals that an I/O exception has occurred.
	 * @throws FileUploadException
	 */
	public static List<List<String>> getReadXlsxFile(Path filePath, Integer schemaNumberOfColumns, Boolean deleteFile)
			throws IOException, FileUploadException {
		File f = filePath.toFile();
		List<List<String>> entries = excelFileReader(f, schemaNumberOfColumns);
		if (Boolean.TRUE.equals(deleteFile) && Objects.nonNull(f)
				&& Files.deleteIfExists(Paths.get(f.getAbsolutePath())))
			log.info("The file {} {}", f.getAbsolutePath(), "is deleted");
		validateExcel(entries, schemaNumberOfColumns);
		return entries;
	}

	public static void validateExcel(File file) throws IOException {
		WorkbookFactory.create(file);
	}

	/**
	 * Excel file reader.
	 *
	 * @param f                     the f
	 * @param schemaNumberOfColumns the schema number of columns
	 * @return the list
	 * @throws EncryptedDocumentException the encrypted document exception
	 * @throws IOException                Signals that an I/O exception has
	 *                                    occurred.
	 */
	private static List<List<String>> excelFileReader(File f, Integer schemaNumberOfColumns) throws IOException {
		List<List<String>> dataSet = new ArrayList<>();

		// Creating a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = WorkbookFactory.create(f);

		// Retrieving the number of sheets in the Workbook
		log.info("Workbook has {} {}", workbook.getNumberOfSheets(), " Sheets");

		// Retrieving Sheets using Java 8 forEach with lambda
		workbook.forEach(sheet -> log.info("=> {}", sheet.getSheetName()));

		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();

		// Iterating over Rows and Columns using Java 8 forEach with lambda
		sheet.forEach(row -> {
			List<String> rowArray = new ArrayList<>();
			if (row.getLastCellNum() > schemaNumberOfColumns / 2) {
				/*
				 * because my excel sheet has max 5 columns, in case last column is empty then
				 * row.getLastCellNum() will
				 */
				int lastColumn = Math.max(row.getLastCellNum(), schemaNumberOfColumns);
				for (int cn = 0; cn < lastColumn; cn++) {
					Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					if (cell.getCellType().equals(CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell)) {
						try {
							String cellValue = new SimpleDateFormat("MM/dd/yyyy").format(cell.getDateCellValue());
							rowArray.add(cellValue);
						} catch (Exception e) {
							rowArray.add(cell.getDateCellValue().toString());
						}
					} else {
						String cellValue = dataFormatter.formatCellValue(cell);
						rowArray.add(cellValue);
					}
				}
				dataSet.add(rowArray);
			}
		});

		workbook.close();

		return dataSet;
	}

	/**
	 * Prints the cell value.
	 *
	 * @param cell the cell
	 */
	public static void printCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case BOOLEAN:
			log.info(String.valueOf(cell.getBooleanCellValue()));
			break;
		case STRING:
			log.info(cell.getRichStringCellValue().getString());
			break;
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				log.info(String.valueOf(cell.getDateCellValue()));
			} else {
				log.info(String.valueOf(cell.getNumericCellValue()));
			}
			break;
		case FORMULA:
			log.info(cell.getCellFormula());
			break;
		case BLANK:
			log.info("");
			break;
		default:
			log.info("");
		}

		log.info("\t");
	}

	public static File writeExcel(List<List<Object>> dataList, String filePath) {

		// Blank workbook
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {

			// Create a blank sheet
			XSSFSheet sheet = workbook.createSheet("Inventory Report");

			int rownum = 0;
			for (List<Object> data : dataList) {
				Row row = sheet.createRow(rownum++);
				int cellnum = 0;
				for (Object obj : data) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
					else if (obj instanceof Long)
						cell.setCellValue((Long) obj);
				}
			}

			// Write the workbook in file system
			File file = new File(filePath);
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();

			return file;

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static List<List<String>> excelToStringList(File is, Integer noOfLength) throws IOException {
		List<List<String>> dataSet = new ArrayList<>();
		// List<String> rowArray=new ArrayList();
		Workbook workbook;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(is));

			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter dataFormatter = new DataFormatter();

			// Iterating over Rows and Columns using Java 8 forEach with lambda
			sheet.forEach(row -> {
				List<String> rowArray = new ArrayList<>();
				if (row.getLastCellNum() > noOfLength / 2) {
					/*
					 * because my excel sheet has max 5 columns, in case last column is empty then
					 * row.getLastCellNum() will
					 */
					int lastColumn = Math.max(row.getLastCellNum(), noOfLength);
					for (int cn = 0; cn < lastColumn; cn++) {
						Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						if (cell.getCellType().equals(CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell)) {
							try {
								String cellValue = new SimpleDateFormat("MM/dd/yyyy").format(cell.getDateCellValue());
								rowArray.add(cellValue);
							} catch (Exception e) {
								rowArray.add(cell.getDateCellValue().toString());
							}
						} else {
							String cellValue = dataFormatter.formatCellValue(cell);
							rowArray.add(cellValue);
						}
					}
					dataSet.add(rowArray);
				}
			});

			workbook.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return dataSet;
	}

	public static String validateExcel(List<List<String>> lines, Integer schemaNumberOfColumns)
			throws FileUploadException {
		String response = "";
		if (lines.isEmpty()) {
			response = "File cannot be empty";
			throw new FileUploadException(HttpStatus.BAD_REQUEST, "File cannot be empty");

		} else if (lines.get(0).size() < schemaNumberOfColumns) {
			response = "Please check the number of mandatory columns";
			throw new FileUploadException(HttpStatus.BAD_REQUEST, "Please check the number of mandatory columns");
		} else {
			response = "Header Success";
		}
		return response;
	}

}
