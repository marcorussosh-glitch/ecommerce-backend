package it.commerce.backend.auth;

import it.commerce.backend.config.SecurityConfig;
import it.commerce.backend.config.TestController;
import it.commerce.backend.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TestController.class, AuthController.class})
@Import(SecurityConfig.class)
@EnableWebSecurity
class AuthControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private WebApplicationContext context;

    @Test
    void loginShouldReturnOkWhenCredentialsAreCorrect() throws Exception {

        //Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        //Act and Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@email.com",
                                "password": "testpassword123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {

        //Arrange
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        //Act and Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@email.com",
                                "password": "testpassword123"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldAllowAccessToProtectedEndpointAfterSuccessfulAuthentication() throws Exception {

        //Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        //Act and Assert
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@email.com",
                                "password": "testpassword123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        mockMvc.perform(get("/api/test").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void logoutShouldNotAllowAccessToProtectedEndpointAfterSuccessfulLogout() throws Exception {

        //Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        //Act and Assert
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "test@email.com",
                                "password": "testpassword123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        mockMvc.perform(post("/api/auth/logout").session(session)
                .with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();

        mockMvc.perform(get("/api/test").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void postShouldReturnForbiddenWhenCsrfTokenIsMissing() throws Exception {

        mockMvc.perform(post("/api/test"))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(roles = "USER")
    void postShouldReturnOkWhenCsrfTokenIsValid() throws Exception {

        mockMvc.perform(post("/api/test")
                        .with(csrf()))
                .andExpect(status().isOk());

    }
}
