package com.travis.customclicker.service;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class UpdateServiceTest {
    @Test
    public void comparesReleaseVersionsNumerically() throws Exception {
        assertEquals(UpdateService.Status.AVAILABLE, release("v1.10.0").check("1.9.0").status());
        assertEquals(UpdateService.Status.AVAILABLE, release("v2.0.0").check("1.99.99").status());
        assertEquals(UpdateService.Status.AVAILABLE, release("1.0.1").check("1.0.0").status());
        assertEquals(UpdateService.Status.CURRENT, release("v1.0.0").check("1.0.0").status());
        assertEquals(UpdateService.Status.CURRENT, release("v1.0.0").check("1.1.0").status());
    }

    @Test
    public void noReleaseIsDifferentFromAnError() throws Exception {
        assertEquals(UpdateService.Status.NO_RELEASE,
                response(404, "{}").check("1.0.0").status());
        for (int code : new int[]{403, 429, 500, 401})
            assertThrows(IOException.class, () -> response(code, "{}").check("1.0.0"));
    }

    @Test
    public void invalidResponsesAreNotReportedAsUpToDate() {
        for (String body : new String[]{"not json", "{}", "null", "[]",
                "{\"tag_name\":\"banana\",\"draft\":false,\"prerelease\":false}"})
            assertThrows(IOException.class, () -> response(200, body).check("1.0.0"));
    }

    @Test
    public void ignoresDraftAndPrereleaseVersions() throws Exception {
        assertEquals(UpdateService.Status.NO_RELEASE, response(200,
                "{\"draft\":true,\"prerelease\":false}").check("1.0.0").status());
        assertEquals(UpdateService.Status.NO_RELEASE, response(200,
                "{\"draft\":false,\"prerelease\":true}").check("1.0.0").status());
    }

    @Test
    public void networkFailuresRemainFailures() {
        UpdateService offline = new UpdateService(() -> { throw new IOException("offline"); });
        assertThrows(IOException.class, () -> offline.check("1.0.0"));
        UpdateService interrupted = new UpdateService(() -> { throw new InterruptedException(); });
        assertThrows(InterruptedException.class, () -> interrupted.check("1.0.0"));
    }

    @Test
    public void buildProvidesNumericApplicationVersion() {
        assertTrue(UpdateService.currentVersion().matches("\\d+\\.\\d+\\.\\d+"));
    }

    private UpdateService release(String version) {
        return response(200, "{\"tag_name\":\"" + version + "\",\"draft\":false,\"prerelease\":false}");
    }

    private UpdateService response(int code, String body) {
        return new UpdateService(() -> new UpdateService.Response(code, body));
    }
}
