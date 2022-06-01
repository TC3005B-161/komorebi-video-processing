package com.komorebi.video.DBUtils;


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.Set;

@DynamoDbBean
public class Recording {
    private String videoID;
    private String agentID;
    private String name;
    private String lastName;
    private String date;
    private String categoryID;
    private Set<String> noteIDs;
    private Set<String> tagsID;
    private Integer duration;
    private String startHour;
    private String configID;
    private Boolean succesfulOutcome;
    private Boolean isProcessed;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("videoID")
    public String getVideoID() {
        return videoID;
    }
    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    @DynamoDbAttribute("agentID")
    public String getAgentID() {
        return agentID;
    }
    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @DynamoDbAttribute("lastName")
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @DynamoDbAttribute("date")
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    @DynamoDbAttribute("categoryID")
    public String getCategoryID() {
        return categoryID;
    }
    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    @DynamoDbAttribute("notesID")
    public Set<String> getNotesID() {
        return noteIDs;
    }
    public void setNotesID(Set<String> noteIDs) {
        this.noteIDs = noteIDs;
    }

    @DynamoDbAttribute("tagsID")
    public Set<String> getTagsID() {
        return tagsID;
    }
    public void setTagsID(Set<String> tagsID) {
        this.tagsID = tagsID;
    }

    @DynamoDbAttribute("durationID")
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @DynamoDbAttribute("startHour")
    public String getStartHour() {
        return startHour;
    }
    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    @DynamoDbAttribute("configID")
    public String getConfigID() {
        return configID;
    }
    public void setConfigID(String configID) {
        this.configID = configID;
    }

    @DynamoDbAttribute("succesfulOutcome")
    public Boolean getSuccesfulOutcome() {
        return succesfulOutcome;
    }
    public void setSuccesfulOutcome(Boolean succesfulOutcome) {
        this.succesfulOutcome = succesfulOutcome;
    }

    @DynamoDbAttribute("isProcessed")
    public Boolean getIsProcessed() {
        return isProcessed;
    }
    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}
