package io.elif.coronavirustracker.services;

import io.elif.coronavirustracker.models.InfosModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<InfosModel> allStats = new ArrayList<>();

    public List<InfosModel> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *") //scheduled first hour of every day
    public void fetchVirusData() throws IOException, InterruptedException {

        List<InfosModel> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        //System.out.println(httpResponse.body());

        StringReader csvBodyReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {

            InfosModel infosModel = new InfosModel();
            infosModel.setState(record.get("Province/State"));
            infosModel.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            infosModel.setLatestTotalCases(latestCases);
            infosModel.setDiffFromPrevDay(latestCases - prevDayCases);
            //System.out.println(locationStats);
            newStats.add(infosModel);
        }
        this.allStats = newStats;
    }
}
