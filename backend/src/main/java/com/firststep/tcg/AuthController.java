package com.firststep.tcg;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final Accounts accounts;
    private final PasswordEncoder encoder;

    AuthController(Accounts accounts, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.encoder = encoder;
    }

    record Registration(@NotBlank
            @Email
            @Size(max = 254) String email,
            @NotBlank
            @Size(max = 80) String displayName, @NotNull
            @Size(min = 8, max = 20) String password) {

        @Override
        public String toString() {
            return "Registration[redacted]";
        }
    }

    record Profile(String email, String displayName, String role) {}

    @GetMapping("/auth/csrf")
    Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> register(@Valid @RequestBody Registration input) {
        if (input.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "รหัสผ่านยาวเกิน 72 ไบต์");
        }
        var email = input.email().trim().toLowerCase(Locale.ROOT);
        try {
            accounts.saveAndFlush(new Account(email, input.displayName().trim(), encoder.encode(input.password()), "CUSTOMER"));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ไม่สามารถใช้อีเมลนี้สมัครได้");
        }
        return Map.of("message", "สมัครสมาชิกสำเร็จ กรุณาเข้าสู่ระบบ");
    }

    @GetMapping("/auth/me")
    Profile me(Authentication auth) {
        var a = accounts.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new Profile(a.email, a.displayName, a.role);
    }

    @GetMapping("/admin/dashboard")
    Map<String, Object> admin() {
        return Map.of("message", "พื้นที่จัดการร้านสำหรับแอดมิน", "customers", accounts.count() - accounts.findAll().stream().filter(a -> a.role.equals("ADMIN")).count());
    }

    @GetMapping("/customer/dashboard")
    Map<String, String> customer() {
        return Map.of("message", "ยินดีต้อนรับสู่บัญชีลูกค้า");
    }
}
