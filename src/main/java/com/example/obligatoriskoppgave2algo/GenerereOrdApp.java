package com.example.obligatoriskoppgave2algo;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Hovedapplikasjonen for å generere ord basert på trigrammer
 * og vise kilder og antall ord og trigrammer.
 */
public class GenerereOrdApp extends Application {

    private Map<String, Map<String, Integer>> trigramMap = new HashMap<>();
    BorderPane bp = new BorderPane();
    public Pane promtPane, genPane, sourcePane, countPane;
    int width = 1000, height = 800;
    int wordCounter = 2;
    int trioTeller = 0;
    ArrayList<String> kilder = new ArrayList<>();
    final int MAX = 10000;
    TextArea genererOrd = new TextArea();

    /**
     * Start metoden for JavaFX application.
     *
     * @param stage Hovedscnene for Applikasjonen
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */

    @Override
    public void start(Stage stage) throws IOException {

        //prøve å laste inn all tekst fra linkene i data.txt
        try {
            hentData("/data.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //setter opp layoutet ved hjelp av metodekall til de forskjellige pane'ene

        bp.setTop(promtPane());
        bp.setCenter(genPane());
        bp.setRight(sourcePane());
        bp.setBottom(countPane());

        //setter opp scenen og viser den

        Scene scene = new Scene(bp, width, height);
        stage.setTitle("A failed Turing test");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Metode for å lage en pane for å skrive inn ord.
     *
     * @return Promt panelet.
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */

    public Pane promtPane() throws IOException {
        promtPane = new Pane();
        promtPane.setPrefWidth(width);
        promtPane.setPrefHeight(height/8);
        promtPane.setStyle("-fx-background-color: rgba(134,131,131,0.6);");

        HBox input = new HBox();
        input.setSpacing(10);

        //vi fant en annen metode som automatisk skrivde videre basert på det vi startet med i et textfelt som var litt kulere,
        //vi forstod ikke koden nokk til å implementere det i vår egen versjon siden det var chatgpt kode

        Label prompt1 = new Label("Enter a word: ");
        TextField word1 = new TextField();
        Label prompt2 = new Label("Enter a second word: ");
        TextField word2 = new TextField();
        Button generate = new Button("Generate");

        input.setStyle("-fx-padding: 10 20 10 10;");
        input.getChildren().addAll(prompt1, word1, prompt2, word2, generate);
        promtPane.getChildren().add(input);

        generate.setOnAction(e -> {
            String[] words = {word1.getText(), word2.getText()};
            genererOrd.clear();
            genererOrd.appendText(words[0] + " " + words[1]);
            autoSkriv(words);
        });


        return promtPane;
    }

    /**
     * Metode for å generere en pane for å vise genererte ord.
     *
     * @return Genererte ord panelet.
     */

    public Pane genPane() {

        genPane = new Pane();
        genPane.setPrefWidth(width-(width/4));
        genPane.setPrefHeight(height-(height/4));
        genPane.setStyle("-fx-background-color: rgba(115,152,97,0.6);");

        VBox gen = new VBox();
        gen.setSpacing(10);


        Label generated = new Label("Generated words: ");

        genererOrd.setWrapText(true);
        genererOrd.setEditable(false);
        genererOrd.setPrefHeight(550);
        genererOrd.setPrefWidth(700);

        gen.getChildren().addAll(generated, genererOrd);
        genPane.getChildren().add(gen);

        return genPane;
    }

    /**
     * Metode for å generere en pane for å vise kildene.
     *
     * @return Kilde panelet.
     */
    public Pane sourcePane() {
        sourcePane = new Pane();
        sourcePane.setPrefWidth(width/4);
        sourcePane.setPrefHeight(height-(height/4));
        sourcePane.setStyle("-fx-background-color: rgba(140,77,77,0.6);");
        VBox surce = new VBox();
        surce.setSpacing(10);
        surce.setAlignment(Pos.CENTER);



        Label source = new Label("Sources: ");
        TextArea sources = new TextArea();
        sources.setWrapText(true);
        sources.setEditable(false);
        sources.setPrefHeight(550);
        sources.setPrefWidth(200);

        for (String item : kilder) {
            sources.appendText(item + "\n");  // det er for å legge til en linje etter hver link
        }


        surce.getChildren().addAll(source, sources);
        sourcePane.getChildren().add(surce);


        return sourcePane;
    }
    /**
     * Metode for å generere en pane for å vise antall ord og trigrammer.
     *
     * @return Count panelet.
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */
    // dette er egentlig bare bonus som henger igjen etter vi prøvde ut måter å laste inn text fra nettsider
    public Pane countPane() throws IOException {

        countPane = new Pane();
        countPane.setPrefWidth(width);
        countPane.setPrefHeight(height/8);
        countPane.setStyle("-fx-background-color: rgba(203,197,197,0.6);");

        HBox count = new HBox();
        count.setSpacing(30);
        count.setAlignment(Pos.CENTER);



        Label ordCount = new Label("Word count: ");
        TextField wordCount = new TextField(" "+ wordCounter);


        Label trio = new Label("Trigram count: ");
        TextField trigramCount = new TextField(" " + trioTeller);

        count.getChildren().addAll(ordCount, wordCount, trio, trigramCount);
        countPane.getChildren().add(count);

        return countPane;
    }
    /**
     * Metode for å laste lenker fra en .txt fil.
     *
     * @param resourcePath Stien til .txt filen.
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */
    // her startet vi med en måte for bruker å skrive inn lenken som så ble til fil avlesning fra et txt dokument istedet
    private void hentData(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Dataen du ser etter ( " + resourcePath + ") er i et annet slott.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // Last ned teksten fra hver lenke
                    urlTilTekst(line);
                    kilder.add(line);
                    //her lagde vi en litt rar løsning for å sjekke hvilke kilder som ble lastet inn
                    //og gjorde testen om til en del av proskjektet
                }
            }
        }

    }

    /**
     * Metode for å laste tekst fra en URL.
     * 
     * @param url URLen til teksten.
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */
    // dette her får vi fra Jsoup og fra youtube video om hvordan bruke det
    private void urlTilTekst(String url) throws IOException {
        // Hent dokumentet fra nettsiden
        Document doc = Jsoup.connect(url).get();

        // Ekstraher tekst fra nettsiden
        String text = doc.text();

        // Behandle teksten og bygge trigrammer
        triagramMap(text);
    }

    /**
     * Metode for å bygge trigrammer fra en tekst.
     * @param text Teksten som skal brukes til å bygge trigrammer.
     */
    //her er noe som vi ikke har brukt før men som vi fant på nettet og tilpasset til vårt prosjekt
    //vi kunne brukt Split men siden denne metoden lot oss forstå delimiteren bedre så valgte vi denne
    private void triagramMap(String text) {

        StringTokenizer tokenizer = new StringTokenizer(text, " \t\n\r\f");
        //her er det en regex som splitter på mellomrom, tab, new line, carriage return og form feed

        String word1 = null, word2 = null;

        while (tokenizer.hasMoreTokens()) {
            String word3 = tokenizer.nextToken();

            // "Normaliser" ordet, behold Æ, Ø, Å, tall og skilletegn
            word3 = normalizeWord(word3);

            // Sjekk at alle tre ordene er gyldige og ikke tomme før vi bygger trigrammet
            if (word1 != null && word2 != null && !word1.isEmpty() && !word2.isEmpty() && !word3.isEmpty()) {
                String trigramKey = word1 + " " + word2;

                // Legg til det tredje ordet i trigramMap og øke telleren
                trigramMap.putIfAbsent(trigramKey, new HashMap<>());
                trioTeller++;
                Map<String, Integer> thirdWordMap = trigramMap.get(trigramKey);
                thirdWordMap.put(word3, thirdWordMap.getOrDefault(word3, 0) + 1);
            }

            // Flytt ordene fremover for å gjøre det igjen, og øk ordtelleren
            word1 = word2;
            word2 = word3;
            wordCounter++;
        }
    }
    /**
     * Metode for å normalisere et ord.
     * @param word Ordet som skal normaliseres.
     * @return Det normaliserte ordet.
     */
    private String normalizeWord(String word) {
        // Regex som beholder bokstaver, Æ, Ø, Å, tall, punktum, komma og bindestrek
        // vi syntes det var lettere å si hva vi skulle beholde istedet for å si hva vi skulle fjerne
        return word.replaceAll("[^a-zA-ZæøåÆØÅ0-9.,-]", "");
    }


    /**
     * Metode for å fortsette å skrive basert på trigrammer.
     * @param words Ordet som skal brukes til å fortsette skrivingen.
     */
    //her har vi bare en måte å få programmet til å fortsette basert på de to siste ordene så vi ikke bare får ett og ett ord
    //det bare autogenererer ord basert på de to siste ordene
    private void autoSkriv(String[] words) {
        int wordCount = words.length;
        String word1 = normalizeWord(words[words.length - 2]);
        String word2 = normalizeWord(words[words.length - 1]);

        // Fortsett å legge til ord til det ikke finnes trigram eller vi når 1000 ord
        while (wordCount < MAX) {
            String nextWord = tredjeOrd(word1, word2);
            if (nextWord == null) {
                break;  // Stopp hvis programmet ikke finner flere trigrammer
            }

            genererOrd.appendText(" " + nextWord);
            wordCount++;



            // Oppdater de to siste ordene
            word1 = word2;
            word2 = nextWord;
        }
    }
    /**
     * Metode for å hente et tilfeldig tredje ord basert på sannsynligheten.
     * @param word1 Første ord.
     * @param word2 Andre ord.
     * @return Det tilfeldige tredje ordet.
     */

    private String tredjeOrd(String word1, String word2) {
        String trigramKey = word1 + " " + word2;
        Map<String, Integer> alleMulige = trigramMap.get(trigramKey);

        if (alleMulige == null) {
            return null;
        }

        // Velg et tilfeldig tredje ord basert på sannsynligheten
        int antallBruk = alleMulige.values().stream().mapToInt(Integer::intValue).sum();
        int trekkTall = (int) (Math.random() * antallBruk);

        int sum = 0;
        for (Map.Entry<String, Integer> entry : alleMulige.entrySet()) {
            sum += entry.getValue();
            if (trekkTall < sum) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Main metoden for å starte applikasjonen.
     * @param args
     */

    public static void main(String[] args) {
        launch();
    }
}