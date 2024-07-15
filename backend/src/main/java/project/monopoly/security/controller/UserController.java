package project.monopoly.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import jakarta.json.Json;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import project.monopoly.security.entity.AuthRequest;
import project.monopoly.security.entity.UserInfo;
import project.monopoly.security.repository.UserInfoRepository;
import project.monopoly.security.service.JWTService;
import project.monopoly.security.service.UserInfoService;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserInfoRepository userInfoRepo;

    @Autowired
    private UserInfoService userInfoSvc;

    @Autowired
    private JWTService jwtSvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    // only add user
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserInfo userInfo) {
        userInfo.setRoles("ROLE_USER");
        
        return userInfoSvc.addUser(userInfo);
    }

    // can add any user role admin | user
    @PostMapping("/addNewUser")
    public ResponseEntity<String> addNewUser(@RequestBody UserInfo userInfo) {
        return userInfoSvc.addUser(userInfo);
    }

    @GetMapping("/user/userProfile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    @GetMapping("/user/refresh")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String>  retrieveBasicInfo() {
        UserInfo userInfo = userInfoSvc.findByEmail(userInfoSvc.getCurrentUserEmail()).get();
        String userEmail = userInfo.getEmail();
        String userName = userInfo.getName();
        String userGid = "";
        Boolean inGame = false;
        if(null != userInfo.getGid()){
            userGid= userInfo.getGid();
            inGame = userInfo.getInGame();
        };
            
        return ResponseEntity.status(200).body(
                    Json.createObjectBuilder()
                        .add("email", userEmail)
                        .add("username", userName)
                        .add("gid", userGid)
                        .add("inGame", inGame)
                        .build().toString()
                );
    }    

    // for login
    @PostMapping("/generateToken")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        String email = authRequest.getEmail();
        // System.out.println(authRequest.getEmail() + " " + authRequest.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, authRequest.getPassword()));
            
            if (authentication.isAuthenticated()) {
                String token = jwtSvc.generateToken(email);

                // create HttpOnly cookie
                // update JWTSvc also for token expiration
                Cookie cookie = new Cookie("token", token);
                int cookieExpiration = 7 * 24 * 60 * 60;
                // int cookieExpiration = 10;
                cookie.setHttpOnly(true); //set to true to disable JS access
                cookie.setSecure(false); // set to true if using HTTPS and change at AuthFilter also
                cookie.setPath("/"); // set as / to make it send back to backend on all request
                cookie.setMaxAge(cookieExpiration); //set expiry time

                // add HTTPonly cookie to response
                response.addCookie(cookie);

                userInfoSvc.loadUserByUsername(authRequest.getEmail());
                UserInfo userInfo = userInfoRepo.findByEmail(email).get();
                String userEmail = userInfo.getEmail();
                String userName = userInfo.getName();
                String userGid = userInfo.getGid();
                if(null == userGid){
                    userGid = "";
                }
                System.out.println("UserInfo: " + userInfo.toString());

                return ResponseEntity.status(200).body(
                    Json.createObjectBuilder()
                        .add("email", userEmail)
                        .add("username", userName)
                        .add("gid", userGid)
                        .build().toString()
                );
            }
        }
        catch (UsernameNotFoundException ex) {
            // user not found exception ; doesnt work
            return ResponseEntity.status(403)
                    .body(Json.createObjectBuilder()
                            .add("error", "User not found: " + email)
                            .build()
                            .toString());
        } 
        catch (BadCredentialsException ex) {
            // wrong password
            return ResponseEntity.status(403)
                    .body(Json.createObjectBuilder()
                            .add("error", "Invalid credentials")
                            .build()
                            .toString());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("throw Exception");
            return ResponseEntity.status(403)
                    .body(Json.createObjectBuilder()
                            .add("error", "Invalid credentials")
                            .build()
                            .toString());
        }
        return ResponseEntity.status(500)
                .body(Json.createObjectBuilder()
                        .add("error", "Internal server error")
                        .build()
                        .toString());       
    }
    
    // for logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // remove token cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); 
        cookie.setPath("/");
        cookie.setMaxAge(0); // to expire the cookie
        response.addCookie(cookie);

        return ResponseEntity.ok().body("Logout successful");
    }
}
