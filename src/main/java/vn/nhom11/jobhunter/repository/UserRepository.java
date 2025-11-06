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

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String token, String email);

    List<User> findByCompany(Company company);

    @Query("""
                SELECT u FROM User u
                WHERE u.createdBy = :creator OR u.id = :userId
            """)
    Page<User> findAllByCreatorOrSelf(@Param("creator") String creator,
            @Param("userId") long userId,
            Pageable pageable);
}
