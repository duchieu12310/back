package vn.nhom11.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.nhom11.jobhunter.domain.CompanyRegistration;

@Repository
public interface CompanyRegistrationRepository extends JpaRepository<CompanyRegistration, Long>,
        JpaSpecificationExecutor<CompanyRegistration> {

}
