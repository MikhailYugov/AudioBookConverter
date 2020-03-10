package uk.yermak.audiobookconverter;

import com.google.gson.Gson;

import java.util.Map;

public class AudioBookInfo {
    private String writer = "";
    private String narrator = "";
    private String title = "";
    private String series = "";
    private String genre = "";
    private String year = "";
    private int bookNumber = 0;
    private int totalTracks = 0;
    private String comment = "";

    public AudioBookInfo() {
    }

    public AudioBookInfo(Map<String, String> tags) {
        if (tags != null) {
            this.setTitle(tags.get("title"));
            this.setWriter(tags.get("artist"));
            this.setNarrator(tags.get("album_artist"));
            this.setSeries(tags.get("album"));
            this.setYear(tags.get("year"));
            this.setComment(tags.get("comment-0"));
            this.setGenre(tags.get("genre"));
            String trackNumber = tags.get("track number");
            if (trackNumber != null) {
                this.setBookNumber(Integer.parseInt(trackNumber));
            }
            String trackCount = tags.get("track count");
            if (trackCount != null) {
                this.setTotalTracks(Integer.parseInt(trackCount));
            }
        }
    }

    public String getSeries() {
        return series == null ? title : series;
    }

    public void setSeries(final String series) {
        this.series = series;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(final String writer) {
        this.writer = writer;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(final String genre) {
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getBookNumber() {
        return this.bookNumber;
    }

    public void setBookNumber(final int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getNarrator() {
        return narrator;
    }

    public void setNarrator(final String narrator) {
        this.narrator = narrator;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public int getTotalTracks() {
        return totalTracks;
    }

    public void setTotalTracks(final int totalTracks) {
        this.totalTracks = totalTracks;
    }

    public String toString() {
        return (new Gson()).toJson(this);
    }
}

        