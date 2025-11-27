package vn.nhom11.jobhunter.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.nhom11.jobhunter.domain.Company;
import vn.nhom11.jobhunter.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Tìm user theo email
    User findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // Tìm user theo refresh token và email (login refresh)
    User findByRefreshTokenAndEmail(String token, String email);

    // Tìm tất cả user thuộc 1 công ty
    List<User> findByCompany(Company company);

    // Tìm tất cả user do creator tạo hoặc chính mình
    @Query("""
                SELECT u FROM User u
                WHERE u.createdBy = :creator OR u.id = :userId
            """)
    Page<User> findAllByCreatorOrSelf(@Param("creator") String creator,
            @Param("userId") long userId,
            Pageable pageable);

    // =================== MỚI ===================
    // Tìm user theo verification token
    User findByVerificationToken(String token);

    // Tìm user theo email và đã kích hoạt (enabled)
    User findByEmailAndEnabledTrue(String email);

    // Tìm user theo email và chưa kích hoạt
    User findByEmailAndEnabledFalse(String email);
}
