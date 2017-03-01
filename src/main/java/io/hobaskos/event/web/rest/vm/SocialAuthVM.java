package io.hobaskos.event.web.rest.vm;

import io.hobaskos.event.domain.enumeration.SocialType;

/**
 * Created by alex on 2/24/17.
 */
public class SocialAuthVM {

    private String accessToken;
    private String langKey;
    private SocialType type;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public SocialType getType() {
        return type;
    }

    public void setType(SocialType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SocialAuthVM{" +
            "accessToken='" + accessToken + '\'' +
            ", langKey='" + langKey + '\'' +
            ", type=" + type +
            '}';
    }
}
