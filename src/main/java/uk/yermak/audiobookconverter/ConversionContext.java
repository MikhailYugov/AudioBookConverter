package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.yermak.audiobookconverter.fx.ConversionProgress;

import java.util.LinkedList;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConversionContext {

    private LinkedList<Conversion> conversionQueue = new LinkedList<>();
    private SimpleObjectProperty<Conversion> conversion = new SimpleObjectProperty<>();

    private Subscriber subscriber;
    private ObservableList<MediaInfo> selectedMedia = FXCollections.observableArrayList();


    public ConversionContext() {
        conversionQueue.add(conversion.get());
    }


    public void startConversion(String outputDestination, ConversionProgress conversionProgress) {
        subscriber.addConversionProgress(conversionProgress);
        conversion.get().start(outputDestination, conversionProgress);
    }

    @Deprecated
    public Conversion getConversion() {
        return conversion.get();
    }

    public void stopConversions() {
        conversionQueue.forEach(c -> c.stop());
    }

    public void subscribeForStart(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public ObservableList<MediaInfo> getSelectedMedia() {
        return selectedMedia;
    }
}
