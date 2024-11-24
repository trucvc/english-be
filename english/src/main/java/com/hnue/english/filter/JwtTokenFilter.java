package com.hnue.english.filter;

import com.hnue.english.component.JwtTokenUtil;
import com.hnue.english.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter{
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (isBypassToken(request)){
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"Unauthorized\"}");
                return;
            }
            final String token = authHeader.substring(7);
            final String email = jwtTokenUtil.extractEmail(token);
            if (email !=null
                    && SecurityContextHolder.getContext().getAuthentication() == null){
                User userDetails = (User) userDetailsService.loadUserByUsername(email);
                if (jwtTokenUtil.validateToken(token, userDetails)){
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }else{
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"message\": \"Invalid token\"}");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        }catch (Exception e){
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
        }
    }

    private boolean isBypassToken(@NonNull HttpServletRequest request){
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of("api/users/register", "POST"),
                Pair.of("api/users/login", "POST"),
                Pair.of("api/users/reset", "POST"),
                Pair.of("api/users/new_password", "POST"),
                Pair.of("api/course/user", "GET"),
                Pair.of("api/topics/user", "GET")
        );

        for(Pair<String, String> bypassToken: bypassTokens) {
            if (request.getServletPath().contains(bypassToken.getFirst()) &&
                    request.getMethod().equals(bypassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }
}
