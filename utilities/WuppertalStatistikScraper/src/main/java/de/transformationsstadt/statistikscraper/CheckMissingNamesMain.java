package de.transformationsstadt.statistikscraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class CheckMissingNamesMain {

	public static void main(String[] args) throws Exception {
		List<String> namesFromCsv = getNamesFromCSV();
		List<String> namesFromGeoJson = getNamesFromGeoJSON();
		List<String> notInCsv = new ArrayList<>(namesFromGeoJson);
		notInCsv.removeAll(namesFromCsv);
		System.out.println("notInCsv: " + notInCsv);
		List<String> notInGeoJson = new ArrayList<>(namesFromCsv);
		notInGeoJson.removeAll(namesFromGeoJson);
		System.out.println("notInGeoJson: " + notInGeoJson);
	}

	private static List<String> getNamesFromCSV() throws IOException {
		List<String> namesFromCsv = new ArrayList<>();
		try (CSVReader reader = new CSVReader(
				new InputStreamReader(ScraperMain.class.getClassLoader().getResourceAsStream("./quartiere.csv")),
				';')) {
			String[] nextLine;
			String bezirkName;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);
			while ((nextLine = reader.readNext()) != null) {
				bezirkName = nextLine[1];
				namesFromCsv.add(bezirkName);
			}
		}
		return namesFromCsv;
	}

	private static List<String> getNamesFromGeoJSON() throws Exception {
		List<String> namesFromCsv = new ArrayList<>();
		try (CSVReader reader = new CSVReader(
				new InputStreamReader(new FileInputStream(new File("D://geojson.csv"))),
				';')) {
			String[] nextLine;
			String bezirkName;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);
			while ((nextLine = reader.readNext()) != null) {
				bezirkName = nextLine[1];
				namesFromCsv.add(bezirkName);
			}
		}
		return namesFromCsv;
	}
}
