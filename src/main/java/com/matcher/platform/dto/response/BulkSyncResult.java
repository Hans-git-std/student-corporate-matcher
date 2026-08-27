package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Summary result of bulk syncing companies and hiring criteria from JSON files")
public class BulkSyncResult {

    @Schema(example = "155")
    private int totalFilesScanned;

    @Schema(example = "155")
    private int createdCount;

    @Schema(example = "0")
    private int skippedCount;

    @Schema(example = "0")
    private int errorCount;

    private List<String> messages = new ArrayList<>();

    public BulkSyncResult() {
    }

    public BulkSyncResult(int totalFilesScanned, int createdCount, int skippedCount, int errorCount, List<String> messages) {
        this.totalFilesScanned = totalFilesScanned;
        this.createdCount = createdCount;
        this.skippedCount = skippedCount;
        this.errorCount = errorCount;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public int getTotalFilesScanned() {
        return totalFilesScanned;
    }

    public void setTotalFilesScanned(int totalFilesScanned) {
        this.totalFilesScanned = totalFilesScanned;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }
}
