package uk.yermak.audiobookconverter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressComponent extends VBox {
    @FXML
    private Label state;
    @FXML
    private Label filesCount;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label message;
    private ConversionProgress conversionProgress;

    public ProgressComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "progress.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }


//        progressBar.progressProperty().bind(conversionProgress.getProgress());


//        filesCount.textProperty().bindBidirectional(conversionProgress.get new NumberStringConverter());

    }

    public void setConversionProgress(ConversionProgress conversionProgress) {
        this.conversionProgress = conversionProgress;
    }
}
