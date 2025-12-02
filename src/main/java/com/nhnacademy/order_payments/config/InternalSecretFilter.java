package com.nhnacademy.order_payments.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class InternalSecretFilter extends OncePerRequestFilter {

    private final String secret;

    public InternalSecretFilter(String secret) {
        this.secret = secret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // /api/payments/ 로 시작하는 요청만 검사
        String path = request.getServletPath();
        return !path.startsWith("/api/payments/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("X-Internal-Secret");

        if (header == null || !header.equals(secret)) {
            // 비밀값이 없거나 틀리면 401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // 통과하면 인증된 것으로 간주
        var auth = new UsernamePasswordAuthenticationToken(
                "internal-client",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
