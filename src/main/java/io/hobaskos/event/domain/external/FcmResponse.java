package io.hobaskos.event.domain.external;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FcmResponse {

    @SerializedName("multicast_id")
    private Long multicastId;

    private int success;

    private int failure;

    @SerializedName("canonical_ids")
    private int canonicalIds;

    private List<FcmResponseResult> results;

    public Long getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(Long multicastId) {
        this.multicastId = multicastId;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonicalIds() {
        return canonicalIds;
    }

    public void setCanonicalIds(int canonicalIds) {
        this.canonicalIds = canonicalIds;
    }

    public List<FcmResponseResult> getResults() {
        return results;
    }

    public void setResults(List<FcmResponseResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "FcmResponse{" +
            "multicastId=" + multicastId +
            ", success=" + success +
            ", failure=" + failure +
            ", canonicalIds=" + canonicalIds +
            ", results=" + results +
            '}';
    }
}
