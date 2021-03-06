package com.liland.lcc.ws;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.liland.lcc.ws.SecurityConstants.*;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter{
    
    public JWTAuthorizationFilter(AuthenticationManager authManager){
        super(authManager);
    }
    
    /**
     * Checks the Authorization-header for a specific prefix then executes getAuthentication.
     * If the return value isn't null the request will be let through.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException{
        String header = req.getHeader(HEADER_STRING);
        
        if(header == null || !header.startsWith(TOKEN_PREFIX)){
            chain.doFilter(req, res);
            return;
        }
        
        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
    
    /**Checks JWT authentication.*/
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();
    
            if(user != null){
                String role = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                        .build()
                        .verify(token.replace(TOKEN_PREFIX, ""))
                        .getClaim("role").asString();
                
                Set<GrantedAuthority> setAuths = new HashSet<>();
                setAuths.add(new SimpleGrantedAuthority(role));
                List<GrantedAuthority> Result = new ArrayList<>(setAuths);
                
                return new UsernamePasswordAuthenticationToken(user, null, Result);
            }
            return null;
        }
        return null;
    }
}
