package uk.yermak.audiobookconverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MediaInfoBean implements MediaInfo {
    private String fileName;
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;
    private AudioBookInfo bookInfo;
    private ArtWork artWork;
    private String codec;
    private Chapter chapter;

    public String fileName() {
        return this.fileName;
    }

    private int channels() {
        return this.channels;
    }

    private int frequency() {
        return this.frequency;
    }

    private int bitrate() {
        return this.bitrate;
    }

    private long duration() {
        return this.duration;
    }

    private AudioBookInfo bookInfo() {
        return this.bookInfo;
    }

    private ArtWork artWork() {
        return this.artWork;
    }

    private String codec() {
        return this.codec;
    }

    public void setChannels(final int channels) {
        this.channels = channels;
    }

    public void setFrequency(final int frequency) {
        this.frequency = frequency;
    }

    public void setBitrate(final int bitrate) {
        this.bitrate = bitrate;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public int getChannels() {
        return this.channels();
    }

    public int getFrequency() {
        return this.frequency();
    }

    public int getBitrate() {
        return this.bitrate();
    }

    @Override
    public String getTitle() {
        return this.getBookInfo().getTitle();
    }

    @Override
    public String getDetails() {
        return this.getFileName();
    }

    public long getDuration() {
        return this.duration();
    }


    public int getNumber() {
        return chapter.getMedia().indexOf(this) + 1;
    }
    @Override
    public void split() {
        List<MediaInfo> currentMedia = new ArrayList<>(chapter.getMedia().subList(0, getNumber() - 1));
        List<MediaInfo> nextMedia = new ArrayList<>(chapter.getMedia().subList(getNumber() - 1, chapter.getMedia().size()));
        chapter.getMedia().clear();
        chapter.getMedia().addAll(currentMedia);
        chapter.createNextChapter(nextMedia);
    }

    @Override
    public void remove() {
        chapter.getMedia().remove(this);
        if (chapter.getMedia().isEmpty()){
            chapter.remove();
        }
    }

    public String getFileName() {
        return this.fileName();
    }

    public void setBookInfo(final AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public AudioBookInfo getBookInfo() {
        return this.bookInfo();
    }

    public ArtWork getArtWork() {
        return this.artWork();
    }

    public void setArtWork(final ArtWork artWork) {
        this.artWork = artWork;
    }

    public String getCodec() {
        return this.codec();
    }

    public void setCodec(final String codec) {
        this.codec = codec;
    }

    @Override
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public MediaInfoBean(final String fileName) {
        this.fileName = fileName;
        this.channels = 2;
        this.frequency = 44100;
        this.bitrate = 128000;
        this.duration = 0L;
        this.codec = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaInfo)) return false;
        MediaInfo that = (MediaInfo) o;
        return getFileName().equals(that.getFileName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName());
    }

}

        