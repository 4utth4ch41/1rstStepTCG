package com.firststep.tcg;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
@Order(-110)
public class AuthRateLimit extends OncePerRequestFilter {
 private record Window(long start,int count) {}
 private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  String path=req.getRequestURI().substring(req.getContextPath().length());
  if(!req.getMethod().equals("POST") || !(path.equals("/api/auth/login") || path.equals("/api/auth/register"))) { chain.doFilter(req,res); return; }
  long now=System.currentTimeMillis();
  windows.entrySet().removeIf(entry -> now-entry.getValue().start()>60_000);
  if(windows.size()>10_000) { reject(res); return; }
  Window window=windows.compute(req.getRemoteAddr(),(key,old) -> old==null || now-old.start()>60_000 ? new Window(now,1) : new Window(old.start(),old.count()+1));
  if(window.count()>15) { reject(res); return; }
  chain.doFilter(req,res);
 }
 private void reject(HttpServletResponse res) throws IOException {
  res.setStatus(429);res.setHeader("Retry-After","60");res.setContentType("application/json;charset=UTF-8");
  res.getWriter().write("{\"message\":\"ลองหลายครั้งเกินไป กรุณารอ 1 นาที\"}");
 }
}
