package com.sourcellies.java8.start;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcellies.java8.json.WeatherInfoJson;

public class MainApp {

	static final String WEATHER_SERVICE = "http://api.openweathermap.org/data/2.5/weather?zip=${zip},us";

	public static Double captureTemperature(String url) {
		URL urlLink = null;
		HttpURLConnection connection = null;
		try {
			urlLink = new URL(url);
			connection = (HttpURLConnection) urlLink.openConnection();
			ObjectMapper objectMapper = new ObjectMapper();
			WeatherInfoJson weatherInfoJson = objectMapper.readValue(connection.getInputStream(),
					WeatherInfoJson.class);
			return weatherInfoJson.getMain().getTemp();

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0.0;
	}

	public static Double kelvinToFahrenheit(Double Kelvin) {
		return ((Double.valueOf(Kelvin) - 273.15) * 1.8000) + 32;
	}

	public static void main(String agr[]) throws Exception {

		String path = MainApp.class.getClassLoader().getResource("UAS_Zip.txt").getPath();

		List<String> urls = Files.lines(Paths.get(path)).parallel()
											 .limit(100)
											 .map(e -> WEATHER_SERVICE.replace("${zip}", e))
											 .collect(toCollection(ArrayList::new));

		Runnable parallel = () -> {
			Long timeStarte = System.currentTimeMillis();
			System.out.println("Parallel Min Temp: " + 
						urls.parallelStream()
							.map(MainApp::captureTemperature)
							.filter(temp -> temp > 0)
							.map(MainApp::kelvinToFahrenheit)
							.min(Double::compare)
							.get());
			System.out.println("Parallel Took time: " + (System.currentTimeMillis() - timeStarte));
		};

		Runnable sequential = () -> {
			Long timeStarte = System.currentTimeMillis();
			System.out.println("Sequential Min Temp: " + 
						urls.stream().map(MainApp::captureTemperature)
							.filter(temp -> temp > 0)
							.map(MainApp::kelvinToFahrenheit)
							.min(Double::compare)
							.get());
			System.out.println("Sequential Took time: " + (System.currentTimeMillis() - timeStarte));
		};

		new Thread(sequential).start();
		new Thread(parallel).start();

	}

}
