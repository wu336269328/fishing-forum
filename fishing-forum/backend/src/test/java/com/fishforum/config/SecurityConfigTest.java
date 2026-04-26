package com.fishforum.config;

import com.fishforum.common.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.TestEndpoints.class)
@Import({SecurityConfig.class, SecurityConfigTest.TestEndpointConfig.class})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @MockBean JwtUtil jwtUtil;

    @Test
    void publicBrowsingGetEndpointsRemainOpen() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string("public"));
    }

    @Test
    void privateGetEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/follows/check/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadEndpointRequiresAuthenticationButUploadedFilesRemainPublic() throws Exception {
        mockMvc.perform(post("/api/upload/image"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/uploads/images/example.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void authenticatedUsersCanUsePrivateEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/upload/image"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointsAreNotCoveredByPublicGetRule() throws Exception {
        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class TestEndpointConfig {
        @Bean
        TestEndpoints testEndpoints() {
            return new TestEndpoints();
        }
    }

    @RestController
    public static class TestEndpoints {
        @GetMapping({"/api/posts", "/api/uploads/images/example.jpg"})
        String publicEndpoint() {
            return "public";
        }

        @GetMapping({"/api/users/me", "/api/messages", "/api/notifications", "/api/favorites",
                "/api/follows/check/2", "/api/admin/statistics"})
        String privateGetEndpoint() {
            return "private";
        }

        @PostMapping("/api/upload/image")
        String uploadEndpoint() {
            return "upload";
        }
    }
}
