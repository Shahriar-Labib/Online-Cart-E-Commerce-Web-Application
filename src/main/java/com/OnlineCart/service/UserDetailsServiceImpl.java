package com.OnlineCart.service;

import com.OnlineCart.Utils.AppConstant;
import com.OnlineCart.model.UserDatas;
import com.OnlineCart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

    @Override
    public UserDatas saveUser(UserDatas user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
       UserDatas userDatas = userRepository.save(user);
        return userDatas;
    }

    @Override
    public UserDatas getUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDatas> getUsers(String role) {
       return userRepository.findByRole(role);

    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {

        Optional<UserDatas> findByUsers = userRepository.findById(id);

        if(findByUsers.isPresent())
        {
            UserDatas userDatas = findByUsers.get();
            userDatas.setIsEnable(status);
            userRepository.save(userDatas);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDatas user) {
       int attempt = user.getFailedAttempt() + 1;
       user.setFailedAttempt(attempt);
       userRepository.save(user);
    }

    @Override
    public void userAccountLock(UserDatas user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public Boolean unlockAccountTimeExpired(UserDatas user) {
       long locktime = user.getLockTime().getTime();
       long unlockTime = locktime + AppConstant.UNLOCK_DURATION_TIME;

       long currentTime = System.currentTimeMillis();

       if(unlockTime < currentTime)
       {
           user.setAccountNonLocked(true);
           user.setFailedAttempt(0);
           user.setLockTime(null);
           userRepository.save(user);
           return true;
       }

        return false;
    }

    @Override
    public void resetAttempt(int userId) {


    }

    @Override
    public void updateUserRestToken(String email, String resetToken) {
       UserDatas userByEmail = userRepository.findByEmail(email);
       userByEmail.setResetToken(resetToken);
       userRepository.save(userByEmail);

    }

    @Override
    public UserDatas getUserByToken(String token) {

        return userRepository.findByResetToken(token);


    }

    @Override
    public UserDatas updateUser(UserDatas user) {
        return userRepository.save(user);
    }

    @Override
    public UserDatas updateUserProfile(UserDatas user, MultipartFile img) {
       UserDatas dbUser = userRepository.findById(user.getId()).get();

       if(!img.isEmpty())
       {
           dbUser.setProfileImage(img.getOriginalFilename());
       }
       if(!ObjectUtils.isEmpty(dbUser))
       {
           dbUser.setName(user.getName());
           dbUser.setMobile(user.getMobile());
           dbUser.setAddress(user.getAddress());
           dbUser.setCity(user.getCity());
           dbUser.setState(user.getState());
           dbUser.setPincode(user.getPincode());
           dbUser = userRepository.save(dbUser);
       }
        try {
            if (!img.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
                        + img.getOriginalFilename());

                System.out.println("Dekhi to path ta " + path);

                Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbUser;
    }
}
