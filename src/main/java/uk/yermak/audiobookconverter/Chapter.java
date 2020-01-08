package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class Chapter implements Organisable {
    private String details;
    private ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private String customTitle;
    private Part part;


    public Chapter(Part part, List<MediaInfo> nextMedia) {
        this.part = part;
        this.details = nextMedia.get(0).getTitle();
        nextMedia.forEach(mediaInfo -> mediaInfo.setChapter(this));
        media.addAll(nextMedia);
    }

    public String getTitle() {
        if (customTitle != null) {
            return Chapter.this.getNumber() + ":" + customTitle;
        }
        return "Chapter " + getNumber();
    }

    public int getNumber() {
        return part.getChapters().indexOf(this) + 1;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public long getDuration() {
        return media.stream().mapToLong(MediaInfo::getDuration).sum();
    }

    @Override
    public void split() {
        List<Chapter> currentChapters = new ArrayList<>(part.getChapters().subList(0, getNumber() - 1));
        List<Chapter> nextChapters = new ArrayList<>(part.getChapters().subList(getNumber() - 1, part.getChapters().size() - 1));
        part.getChapters().clear();
        part.getChapters().addAll(currentChapters);
        part.createNextPart(nextChapters);
    }

    @Override
    public void remove() {
        part.getChapters().remove(this);
    }

    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public void createNextChapter(List<MediaInfo> nextMedia) {
        int i = part.getChapters().indexOf(this);
        part.getChapters().add(i + 1, new Chapter(part, nextMedia));
    }
}
