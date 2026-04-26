package com.fishforum.common;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CommonUtilityTest {

    @Test
    void resultFactoriesPopulateCodeMessageAndData() {
        Result<String> ok = Result.ok("created", "payload");
        Result<Object> empty = Result.ok();
        Result<Object> error = Result.error(404, "missing");

        assertThat(ok.getCode()).isEqualTo(200);
        assertThat(ok.getMessage()).isEqualTo("created");
        assertThat(ok.getData()).isEqualTo("payload");
        assertThat(empty.getCode()).isEqualTo(200);
        assertThat(error.getCode()).isEqualTo(404);
        assertThat(error.getMessage()).isEqualTo("missing");
    }

    @Test
    void jwtRoundTripPreservesUserClaimsAndRejectsTamperedToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "FishingForumSecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm2024");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        String token = jwtUtil.generateToken(7L, "angler", "ADMIN");

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(7L);
        assertThat(jwtUtil.getUsername(token)).isEqualTo("angler");
        assertThat(jwtUtil.getRole(token)).isEqualTo("ADMIN");
        assertThat(jwtUtil.validateToken(token + "x")).isFalse();
    }
}
