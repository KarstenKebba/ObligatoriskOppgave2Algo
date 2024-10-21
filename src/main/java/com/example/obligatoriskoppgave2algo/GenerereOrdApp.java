package com.example.obligatoriskoppgave2algo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
 */
public class GenerereOrdApp extends Application {

    private Map<String, Map<String, Integer>> trigramMap = new HashMap<>();
    BorderPane bp = new BorderPane();
    public Pane promtPane, genPane, sourcePane, countPane;
    int width = 1000, height = 800;
    int wordCounter = 2;
    int trioTeller = 0;
    ArrayList<String> kilder = new ArrayList<>();
    final int MAX_WORD_COUNT = 10000;
    TextArea generatedWords = new TextArea();

    /**
     * Start metoden for JavaFX application.
     *
     * @param stage Hovedscnene for Applikasjonen
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */

    @Override
    public void start(Stage stage) throws IOException {

        // Last inn trigrammer fra data.txt ved oppstart
        try {
            loadLinksFromResource("/data.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        bp.setTop(promtPane());
        bp.setCenter(genPane());
        bp.setRight(sourcePane());
        bp.setBottom(countPane());

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
            generatedWords.clear();
            generatedWords.appendText(words[0] + " " + words[1]);
            continueWriting(words);
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

        generatedWords.setWrapText(true);
        generatedWords.setEditable(false);
        generatedWords.setPrefHeight(550);
        generatedWords.setPrefWidth(700);

        gen.getChildren().addAll(generated, generatedWords);
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
            sources.appendText(item + "\n");  // Legg til hver streng med en ny linje
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
     * Metode for å laste lenker fra en ressurs.
     *
     * @param resourcePath Stien til ressursen.
     * @throws IOException Hvis det oppstår en feil ved lasting av dataen.
     */

    private void loadLinksFromResource(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Ressursen " + resourcePath + " ble ikke funnet.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // Last ned tekst fra hver lenke
                    loadTextFromUrl(line);
                    kilder.add(line);
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

    private void loadTextFromUrl(String url) throws IOException {
        // Hent dokumentet fra nettsiden
        Document doc = Jsoup.connect(url).get();

        // Ekstraher tekst fra nettsiden
        String text = doc.text();

        // Behandle teksten og bygge trigrammer
        buildTrigramMap(text);
    }

    /**
     * Metode for å bygge trigrammer fra en tekst.
     * @param text Teksten som skal brukes til å bygge trigrammer.
     */

    private void buildTrigramMap(String text) {
        // Bruker regex for å fange både ord, tall og spesialtegn som punktum og komma
        StringTokenizer tokenizer = new StringTokenizer(text, " \t\n\r\f");

        String word1 = null, word2 = null;

        // Iterer over alle ordene i teksten
        while (tokenizer.hasMoreTokens()) {
            String word3 = tokenizer.nextToken();

            // Normaliser ordet, behold Æ, Ø, Å, tall og skilletegn
            word3 = normalizeWord(word3);

            // Sjekk at alle tre ordene er gyldige og ikke tomme før du bygger trigrammet
            if (word1 != null && word2 != null && !word1.isEmpty() && !word2.isEmpty() && !word3.isEmpty()) {
                String trigramKey = word1 + " " + word2;

                // Legg til det tredje ordet i trigramMap
                trigramMap.putIfAbsent(trigramKey, new HashMap<>());
                trioTeller++;
                Map<String, Integer> thirdWordMap = trigramMap.get(trigramKey);
                thirdWordMap.put(word3, thirdWordMap.getOrDefault(word3, 0) + 1);
            }

            // Flytt ordene fremover for å lage neste trigram
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
        return word.replaceAll("[^a-zA-ZæøåÆØÅ0-9.,-]", "");
    }


    /**
     * Metode for å fortsette å skrive basert på trigrammer.
     * @param words Ordet som skal brukes til å fortsette skrivingen.
     */

    private void continueWriting(String[] words) {
        int wordCount = words.length;
        String word1 = normalizeWord(words[words.length - 2]);
        String word2 = normalizeWord(words[words.length - 1]);

        // Fortsett å legge til ord til det ikke finnes trigram eller vi når 1000 ord
        while (wordCount < MAX_WORD_COUNT) {
            String nextWord = getRandomThirdWord(word1, word2);
            if (nextWord == null) {
                break;  // Stopp hvis det ikke finnes flere trigrammer
            }

            generatedWords.appendText(" " + nextWord);
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

    private String getRandomThirdWord(String word1, String word2) {
        String trigramKey = word1 + " " + word2;
        Map<String, Integer> thirdWordMap = trigramMap.get(trigramKey);

        if (thirdWordMap == null) {
            return null;
        }

        // Velg et tilfeldig tredje ord basert på sannsynligheten
        int totalOccurrences = thirdWordMap.values().stream().mapToInt(Integer::intValue).sum();
        int randomIndex = (int) (Math.random() * totalOccurrences);

        int sum = 0;
        for (Map.Entry<String, Integer> entry : thirdWordMap.entrySet()) {
            sum += entry.getValue();
            if (randomIndex < sum) {
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