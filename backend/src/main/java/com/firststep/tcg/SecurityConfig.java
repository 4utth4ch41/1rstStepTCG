package com.firststep.tcg;
import java.util.Locale;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.http.HttpStatus;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
 @Bean UserDetailsService users(Accounts accounts) {
  return email -> accounts.findByEmail(email.trim().toLowerCase(Locale.ROOT))
   .map(a -> User.withUsername(a.email).password(a.passwordHash).roles(a.role).build())
   .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
 }
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  var csrf = new HttpSessionCsrfTokenRepository();
  csrf.setHeaderName("X-CSRF-TOKEN");
  return http.csrf(c -> c.csrfTokenRepository(csrf))
   .authorizeHttpRequests(a -> a.requestMatchers("/api/auth/csrf", "/api/auth/register", "/api/auth/login").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/customer/**").hasRole("CUSTOMER").anyRequest().authenticated())
   .formLogin(f -> f.loginProcessingUrl("/api/auth/login").usernameParameter("email")
    .successHandler((req,res,auth) -> res.setStatus(204))
    .failureHandler((req,res,ex) -> { res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); res.getWriter().write("{\"message\":\"อีเมลหรือรหัสผ่านไม่ถูกต้อง\"}"); }))
   .logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((req,res,auth) -> res.setStatus(204)))
   .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> res.setStatus(HttpStatus.UNAUTHORIZED.value()))
    .accessDeniedHandler((req,res,ex) -> res.setStatus(HttpStatus.FORBIDDEN.value())))
   .build();
 }
}
