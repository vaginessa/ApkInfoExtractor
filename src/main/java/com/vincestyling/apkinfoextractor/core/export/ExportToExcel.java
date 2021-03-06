/*
 * Copyright 2014 Vince Styling
 * https://github.com/vince-styling/ApkInfoExtractor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vincestyling.apkinfoextractor.core.export;

import com.vincestyling.apkinfoextractor.entity.ApkInfo;
import com.vincestyling.apkinfoextractor.entity.ResultDataProvider;
import com.vincestyling.apkinfoextractor.entity.Solution;
import com.vincestyling.apkinfoextractor.utils.Constancts;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ExportToExcel extends ExportToXml {

	public ExportToExcel(
			Solution solution, ExportProcessCallback callback,
			TextArea txaPattern, ProgressBar prgBar, Button btnExport) {
		super(solution, callback, txaPattern, prgBar, btnExport);
	}

	@Override
	public void export() throws Exception {
		Workbook wb = new HSSFWorkbook();

		Map<String, CellStyle> styles = createStyles(wb);

		Sheet sheet = wb.createSheet(Constancts.APP_NAME);
		sheet.setHorizontallyCenter(true);
		sheet.setFitToPage(true);

		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(45);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("  File generated by ApkInfoExtractor (https://github.com/vince-styling/ApkInfoExtractor), Copyright (C) 2014 Vince Styling");
		titleCell.setCellStyle(styles.get("title"));

		Row headerRow = sheet.createRow(1);
		headerRow.setHeightInPoints(40);

		int cellNum = 0;
		String[] fields = solution.getExtractFields().split(",");
		for (String field : fields) {
			if (field.equals(Constancts.ICON)) continue;
			Cell headerCell = headerRow.createCell(cellNum);
			headerCell.setCellValue(field);
			headerCell.setCellStyle(styles.get("header"));
			sheet.setColumnWidth(cellNum, ApkInfo.getFieldCharacterCount(field) * 256);
			cellNum++;
		}

		int rowNum = 2;
		for (int i = 0; i < solution.getResultCount(); i++) {
			ResultDataProvider provider = solution.getResultList().get(i);
			postProgress(i + 1);

			cellNum = 0;
			Row row = sheet.createRow(rowNum++);
			for (String field : fields) {
				if (field.equals(Constancts.ICON)) continue;
				Cell cell = row.createCell(cellNum);
				cell.setCellStyle(styles.get("cell"));
				String value = getFieldValue(provider.getApkInfo(), field);
				cell.setCellValue(value);
				cellNum++;
			}
			row.setHeight((short) (5 * 256));
		}

		File outputFile =
				new File(solution.getWorkingFolder(),
				solution.generateOutputFileName() + ".xls");
		FileOutputStream out = new FileOutputStream(outputFile);
		wb.write(out);
		out.close();

		callback.onProcessSuccess(outputFile);
	}

	private String getAvaliableTitleFont() {
		// Get local's fonts was too slow, it spend 3s at least.
		//String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		String[] preferedFonts;
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("mac")) {
			preferedFonts = new String[]{
					"SimSun",
					"ST Song",
					"Courier New",
					"Lucida Grande",
			};
		} else if (osName.contains("linux")) {
			preferedFonts = new String[]{
					"Ubuntu",
					"Ubuntu Mono",
					"Liberation Mono",
					"WenQuanYi Micro Hei",
			};
		} else if (osName.contains("windows")) {
			preferedFonts = new String[]{
					"Microsoft YaHei",
					"SimSun",
					"Courier New",
					"Microsoft Sans Serif",
			};
		} else {
			preferedFonts = new String[]{
					"Symbol",
					"Arial",
					"Tahoma",
					"Courier New",
			};
		}

		shuffleArray(preferedFonts);
		return preferedFonts[0];
	}

	private Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<>();
		String fontName = getAvaliableTitleFont();
		CellStyle style;

		Font font = wb.createFont();
		font.setFontName(fontName);
		font.setFontHeightInPoints((short) 14);
		font.setColor(IndexedColors.BROWN.getIndex());

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(font);
		styles.put("title", style);


		font = wb.createFont();
		font.setFontName(fontName);
		font.setFontHeightInPoints((short) 14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setColor(IndexedColors.WHITE.getIndex());

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFont(font);
		style.setWrapText(true);
		setBorder(style);
		styles.put("header", style);


		font = wb.createFont();
		font.setFontName(fontName);
		font.setFontHeightInPoints((short) 12);
		font.setColor(IndexedColors.BLACK.getIndex());

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setWrapText(true);
		style.setFont(font);
		setBorder(style);
		styles.put("cell", style);

		return styles;
	}

	private void setBorder(CellStyle style) {
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	}

	private void shuffleArray(String[] array) {
		int index;
		String temp;
		Random random = new Random(System.currentTimeMillis());
		for (int i = array.length - 1; i > 0; i--) {
			index = random.nextInt(i + 1);
			temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
	}

}
