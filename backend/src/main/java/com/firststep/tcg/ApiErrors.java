package com.firststep.tcg;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<?> invalid(MethodArgumentNotValidException ex) {
  return ResponseEntity.badRequest().body(Map.of("message","กรุณาตรวจสอบอีเมล ชื่อ และรหัสผ่าน (8–20 ตัวอักษร)"));
 }
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<?> status(ResponseStatusException ex) {
  return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",ObjectsText.reason(ex)));
 }
 private static class ObjectsText { static String reason(ResponseStatusException ex) { return ex.getReason()==null ? "ไม่สามารถดำเนินการได้" : ex.getReason(); } }
}
