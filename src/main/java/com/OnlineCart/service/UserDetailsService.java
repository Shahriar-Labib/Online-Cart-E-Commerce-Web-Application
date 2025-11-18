package com.OnlineCart.service;

import com.OnlineCart.model.UserDatas;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserDetailsService {

    public UserDatas saveUser(UserDatas user);

    public UserDatas getUserByEmail(String email);

    public List<UserDatas> getUsers(String role);

    public Boolean updateAccountStatus(Integer id,Boolean status);

    public void increaseFailedAttempt(UserDatas user);

    public void userAccountLock(UserDatas user);

    public Boolean unlockAccountTimeExpired(UserDatas user);

    public void resetAttempt(int userId);

    public void updateUserRestToken(String email, String resetToken);

    public UserDatas getUserByToken(String token);

    public UserDatas updateUser(UserDatas user);

    public UserDatas updateUserProfile(UserDatas user, MultipartFile img);

    public UserDatas saveAdmin(UserDatas user);

}
