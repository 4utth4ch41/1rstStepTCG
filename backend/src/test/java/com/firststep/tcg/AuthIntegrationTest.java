package com.firststep.tcg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:auth-tests;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","ADMIN_EMAIL=admin@example.test","ADMIN_PASSWORD=test-admin-pass-123"})
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired Accounts accounts;
 @Test void guestAndCsrfBoundaries() throws Exception {
  mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
  mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}" )).andExpect(status().isForbidden());
 }
 @Test void registrationLoginRolesAndLogout() throws Exception {
  var body="""
   {"email":"Player@Example.test","displayName":"Player","password":"a-long-test-password","role":"ADMIN"}
   """;
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
  var account=accounts.findByEmail("player@example.test").orElseThrow();
  assertThat(account.role).isEqualTo("CUSTOMER");
  assertThat(account.passwordHash).startsWith("$2").isNotEqualTo("a-long-test-password");
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
  mvc.perform(post("/api/auth/login").with(csrf()).param("email","player@example.test").param("password","incorrect")).andExpect(status().isUnauthorized());
  var login=mvc.perform(post("/api/auth/login").with(csrf()).param("email","PLAYER@example.test").param("password","a-long-test-password")).andExpect(status().isNoContent()).andReturn();
  var session=(MockHttpSession)login.getRequest().getSession(false);
  assertThat(session).isNotNull();
  mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.role").value("CUSTOMER"));
  mvc.perform(get("/api/customer/dashboard").session(session)).andExpect(status().isOk());
  mvc.perform(get("/api/admin/dashboard").session(session)).andExpect(status().isForbidden());
  mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
  assertThat(session.isInvalid()).isTrue();
  mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
 }
 @Test void adminLoginAndRoleBoundary() throws Exception {
  var login=mvc.perform(post("/api/auth/login").with(csrf()).param("email","admin@example.test").param("password","test-admin-pass-123")).andExpect(status().isNoContent()).andReturn();
  var session=(MockHttpSession)login.getRequest().getSession(false);
  mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.role").value("ADMIN"));
  mvc.perform(get("/api/admin/dashboard").session(session)).andExpect(status().isOk());
  mvc.perform(get("/api/customer/dashboard").session(session)).andExpect(status().isForbidden());
 }
 @Test void enforcesRegistrationPasswordLengthBounds() throws Exception {
  var body="""
   {"email":"boundary%d@example.test","displayName":"Player","password":"%s"}
   """;
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content(body.formatted(7,"a".repeat(7)))).andExpect(status().isBadRequest());
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content(body.formatted(8,"a".repeat(8)))).andExpect(status().isCreated());
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content(body.formatted(21,"a".repeat(21)))).andExpect(status().isBadRequest());
 }
 @Test void limitsRepeatedLoginAttempts() throws Exception {
  for(int i=0;i<15;i++) mvc.perform(post("/api/auth/login").with(csrf()).with(req -> {req.setRemoteAddr("192.0.2.55");return req;}).param("email","absent@example.test").param("password","wrong")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/login").with(csrf()).with(req -> {req.setRemoteAddr("192.0.2.55");return req;})).andExpect(status().isTooManyRequests());
 }

}
