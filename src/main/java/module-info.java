module com.example.obligatoriskoppgave2algo {
    requires javafx.controls;
    requires javafx.fxml;
    requires jsoup;


    opens com.example.obligatoriskoppgave2algo to javafx.fxml;
    exports com.example.obligatoriskoppgave2algo;
}