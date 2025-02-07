package ru.gavrilovegor519.hh_autoupdate_resume.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class AutoUpdateResume {

    private final HhApiUtils hhApiUtils;
    private final String resumeId;

    private final Preferences preferences = Preferences.userRoot().node("hh-autoupdate-resume");

    public AutoUpdateResume(HhApiUtils hhApiUtils, @Value("${ru.gavrilovegor519.hh-autoupdate-resume.resumeId}") String resumeId) {
        this.hhApiUtils = hhApiUtils;
        this.resumeId = resumeId;
    }

    @Scheduled(fixedRate = 14400)
    public void updateResume() {
        String accessToken = preferences.get("access_token", null);
        String refreshToken = preferences.get("refresh_token", null);

        if (accessToken != null && refreshToken != null) {
            try {
                hhApiUtils.updateResume(resumeId, accessToken);
            } catch (Exception e) {
                updateTokens(hhApiUtils.getNewToken(refreshToken));
            }
        } else {
            updateTokens(hhApiUtils.getInitialToken());
        }
    }

    private void updateTokens(TokenDto tokenDto) {
        if (tokenDto != null && !tokenDto.getAccess_token().isEmpty() &&
                !tokenDto.getRefresh_token().isEmpty()) {
            preferences.put("access_token", tokenDto.getAccess_token());
            preferences.put("refresh_token", tokenDto.getRefresh_token());
            hhApiUtils.updateResume(resumeId, tokenDto.getAccess_token());
        }
    }

}
