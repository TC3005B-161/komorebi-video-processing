package com.komorebi.video.DBUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RecordingDTO {

    private final String videoID;
    private final String agentID;
    private final Calendar date;

    public RecordingDTO(Recording recording) throws ParseException {
        this.videoID = recording.getVideoID();
        this.agentID = recording.getAgentID();
        this.date = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date simpleDate = dateFormat.parse(recording.getDate());
        this.date.setTime(simpleDate);
    }

    public String buildACRecordingPrefix(){
        String prefix = String.format(
                "connect/ccm-connect-test/CallRecordings/%d/%d/%d/%s",
                this.date.get(Calendar.YEAR),
                this.date.get(Calendar.MONTH),
                this.date.get(Calendar.DAY_OF_MONTH),
                this.videoID
        );
        return prefix;
    }

    public String buildKomorebiInputRecordingPath(){
        String path = String.format(
            "%d/%d/%d/%s/%s.mp4",
            this.date.get(Calendar.YEAR),
            this.date.get(Calendar.MONTH),
            this.date.get(Calendar.DAY_OF_MONTH),
            this.agentID,
            this.videoID
        );
        return path;
    }

    public String buildKomorebiOutputVideoPath(){
        String path = String.format(
            "%d/%d/%d/%s/%s/recording.mp4",
            this.date.get(Calendar.YEAR),
            this.date.get(Calendar.MONTH),
            this.date.get(Calendar.DAY_OF_MONTH),
            this.agentID,
            this.videoID
        );
        return path;
    }

    public String buildKomorebiOutputThumbnailPath(){
        String path = String.format(
            "%d/%d/%d/%s/%s/thumbnail.mp4",
            this.date.get(Calendar.YEAR),
            this.date.get(Calendar.MONTH),
            this.date.get(Calendar.DAY_OF_MONTH),
            this.agentID,
            this.videoID
        );
        return path;
    }

    public String getVideoID() {
        return videoID;
    }

    public String getAgentID() {
        return agentID;
    }

    public Calendar getDate() {
        return date;
    }
}
