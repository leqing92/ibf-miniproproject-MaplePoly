package project.monopoly.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import project.monopoly.security.entity.UserInfo;
import project.monopoly.security.repository.UserInfoRepository;

import java.util.Optional;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository userInfoRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<UserInfo> userDetail = userInfoRepo.findByEmail(email);

        // Converting userDetail to UserDetails
        return userDetail.map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));
    }

    public ResponseEntity<String> addUser(UserInfo userInfo) {
        try {
            userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            userInfoRepo.save(userInfo);

            return ResponseEntity.status(201).body("User Added Successfully");
        } catch (DataIntegrityViolationException e) {
            
            if (e.getMostSpecificCause().getMessage().contains("userinfo.email_UNIQUE")) {
                return ResponseEntity.status(409).body("Email is already in use.");
            } 
            else if (e.getMostSpecificCause().getMessage().contains("userinfo.name_UNIQUE")) {
                return ResponseEntity.status(409).body("Username is already in use.");
            } 
            else {
                return ResponseEntity.status(500).body("Could not add user.");
            }
        }
    }
    
    public Optional<UserInfo> findByEmail(String email) {
        return userInfoRepo.findByEmail(email);
    }

    public boolean updateUserGameStatusByName(String name, String gid, boolean inGame){
        return userInfoRepo.updateUserGameStatusByName(name, gid, inGame);
    }

     // logic to get current user's ID (login ID) from authentication context        
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            return null;
        }
    }

    public String getCurrentUserName(){        
        return userInfoRepo.findByEmail(getCurrentUserEmail()).get().getName();
    }


}

