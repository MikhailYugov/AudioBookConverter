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
    private SimpleObjectProperty<Conversion> conversion = new SimpleObjectProperty<>(new Conversion());


    private Subscriber subscriber;
    private ObservableList<MediaInfo> selectedMedia = FXCollections.observableArrayList();


    public ConversionContext() {
        conversionQueue.add(conversion.get());
    }


    //TODO simplify Subscriber
    public void startConversion(String outputDestination, ConversionProgress conversionProgress) {
        subscriber.addConversionProgress(conversionProgress);
        conversion.get().start(outputDestination, conversionProgress);
        conversion.set(new Conversion());
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

    public Conversion registerForConversion(ConversionSubscriber conversionSubscriber) {
        conversion.addListener((observable, oldValue, newValue) -> conversionSubscriber.resetForNewConversion(newValue));
        return conversion.get();
    }
}
