package de.transformationsstadt.statistikscraper;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import au.com.bytecode.opencsv.CSVReader;

public class ScraperMain {
	private static final String URL_PATTERN = "https://www.wuppertal.de/rbs_statistik/quartiere.phtml?bez=%s&aktion_jahr=%s";

	private static Map<String, String> fields = new HashMap<>();
	
	static {
		fields.put("feld1", "Fläche in qkm:");
		fields.put("feld2", "Bevölkerung (gesamt)");
		fields.put("feld3", "Bevölkerung (weiblich)");
		fields.put("feld4", "Bevölkerung (männlich)");
		fields.put("feld5", "Bevölkerung (Ausländer)");
		fields.put("feld6", "Bevölkerungsdichte");
		fields.put("feld7", "Ausländeranteil (in v.H.)");
		fields.put("feld9", "Arbeitslose");
		fields.put("raum1", "Gebäude mit Wohnraum");
		fields.put("raum2", "darin Wohnungen");
		fields.put("raum3", "Zugelassene Kfz privater Halter");
	}
	
	public static void main(String[] args) throws Exception {
		List<Integer> years = new ArrayList<>();
		for(int year=2000; year<2018; year++) {
			years.add(year);
		}
		try (CSVReader reader = new CSVReader(new InputStreamReader(
				ScraperMain.class.getClassLoader().getResourceAsStream(
			    		"./quartiere.csv")), ';')) {
			String [] nextLine;
			String bezirkId;
			String bezirkName;
			String url;
			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			NumberFormat nf = NumberFormat.getInstance();
			NumberFormat df = DecimalFormat.getInstance(Locale.GERMANY);
			nf.setMinimumIntegerDigits(2);
			while ((nextLine = reader.readNext()) != null) {
				bezirkId = nf.format(Integer.valueOf(nextLine[0]));
				bezirkName = nextLine[1];
				JsonObjectBuilder yearDataJsonBuilder = Json.createObjectBuilder();
				for(Integer year : years) {
					url = String.format(URL_PATTERN, bezirkId, year);	
					Document document = Jsoup.connect(url).get();
					JsonObjectBuilder fieldDataJsonBuilder = Json.createObjectBuilder();
					Elements labels = document.getElementsByTag("label");
					for(Element label : labels) {
						int counter = 0;
						if(label.attributes().hasKeyIgnoreCase("for")) {
							for (Attribute att : label.attributes()) {
								if("for".equals(att.getKey())) {
									continue;
								}
								String key = att.getKey().replace("\"", "");
								if(key.startsWith("raum")) {
									key += (counter++);
								}
								if(fields.containsKey(key)) {
									String value = label.parent().text();
									value = value.replace(label.text(), "");
									Number doubleValue = df.parse(value);
									String fieldValue = fields.get(key);
									fieldDataJsonBuilder.add(fieldValue, doubleValue.doubleValue());
								}
							}
						}
					}
					yearDataJsonBuilder.add(String.valueOf(year), fieldDataJsonBuilder.build());
					System.out.println(String.format("Processed %s (%s) for year %s", bezirkName, bezirkId, year));
				}
				jsonBuilder.add(bezirkName, yearDataJsonBuilder.build());
			}
			String json = jsonBuilder.build().toString();
			try (FileWriter fw = new FileWriter(new File("out.json"))) {
				fw.write(json);
			}
		}
	}
}
