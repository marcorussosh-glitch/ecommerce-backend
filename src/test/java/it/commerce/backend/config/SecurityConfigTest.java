package it.commerce.backend.config;

import it.commerce.backend.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableWebSecurity
@WebMvcTest(TestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void userShouldReceiveForbiddenWhenAccessingAdminEndpoint() throws Exception {

        mockMvc.perform(get("/api/admin/test")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminShouldReceiveOkWhenAccessingAdminEndpoint()  throws Exception {

        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "USER")
    void userShouldReceiveOkWhenAccessingAuthenticatedEndpoint()  throws Exception {

        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserShouldReceiveUnauthorizedWhenAccessingProtectedEndpoint()  throws Exception {

        mockMvc.perform(get("/api/test")).andExpect(status().isUnauthorized());
    }

}

