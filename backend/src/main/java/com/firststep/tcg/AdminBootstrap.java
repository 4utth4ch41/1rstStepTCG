package com.firststep.tcg;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.boot.*;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminBootstrap {
 @Bean ApplicationRunner bootstrap(Accounts accounts,PasswordEncoder encoder,Environment env) {
  return args -> {
   String email=env.getProperty("ADMIN_EMAIL"); String password=env.getProperty("ADMIN_PASSWORD");
   if(email==null && password==null) return;
   if(email==null || password==null || !java.util.regex.Pattern.compile("^[^@ ]+@[^@ ]+[.][^@ ]+$").matcher(email).matches() || password.length()<12 || password.length()>64 || password.getBytes(StandardCharsets.UTF_8).length>72)
    throw new IllegalStateException("Set a valid ADMIN_EMAIL and ADMIN_PASSWORD (12–64 characters, at most 72 UTF-8 bytes)");
   String normalized=email.trim().toLowerCase(Locale.ROOT);
   var existing=accounts.findByEmail(normalized);
   if(existing.isPresent()) {
    if(!existing.get().role.equals("ADMIN")) throw new IllegalStateException("Admin email already belongs to a customer");
    return;
   }
   accounts.save(new Account(normalized,"ผู้ดูแลร้าน",encoder.encode(password),"ADMIN"));
  };
 }
}
