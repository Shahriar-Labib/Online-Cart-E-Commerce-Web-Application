package com.OnlineCart.repository;

import com.OnlineCart.model.UserDatas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserDatas,Integer> {

    public UserDatas findByEmail(String email);

    public List<UserDatas> findByRole(String role);

    public UserDatas findByResetToken(String token);

    public Boolean existsByEmail(String email);
}
