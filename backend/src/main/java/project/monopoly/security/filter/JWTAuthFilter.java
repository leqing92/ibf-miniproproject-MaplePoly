package project.monopoly.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import project.monopoly.security.service.JWTService;
import project.monopoly.security.service.UserInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;

// this class validate jwt token on every request
@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    // private static final Logger logger = LoggerFactory.getLogger(JWTAuthFilter.class);
    
    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserInfoService userDetailsService;    

    // this method is called for every incoming request to validate JWT token
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                
        // get cookies from the request
        Cookie[] cookies = request.getCookies();
        String token = null;
        String username = null;
        
        // iterate through cookies to find the 'token' cookie
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    try {
                        username = jwtService.extractUsername(token);
                        // logger.info("username extract from token: {}", username);
                    } catch (ExpiredJwtException e) {
                        // if JWT token expire
                        clearTokenCookie(response);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("JWT Token has expired. Please log in again.");
                        return;
                    } catch (Exception e) {
                        clearTokenCookie(response);
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("Invalid JWT Token.");
                        return;
                    }
                    break;
                }
            }
        }
        
        // if username exist and user is not auth
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from the userDetailsService based on the extracted username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token using the JWT service
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                 // Set authentication details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                // logger.info("User {} successfully authenticated with roles: {}", userDetails.getUsername(), userDetails.getAuthorities());
            }
            
        }
        // Continue the filter chain to process the request
        filterChain.doFilter(request, response);
        
    }

    // clear the JWT cookie stored in hhtponly-cookie
    private void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set to true if using HTTPS and change at usercontroller for http & clear cookie also
        cookie.setPath("/"); //so cookie will be included in requests to all URLs in the domain
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

// set secure :
/*
 secure : true && https
 - sent cookie on https only

 secure : true but http
 - cookie will not be sent

 secure : false but https
 - send cookie but might hv security issue

 secure : false && http
 - send cookie but might hv security issue
 */