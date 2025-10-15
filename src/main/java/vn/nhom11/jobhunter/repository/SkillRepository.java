package vn.nhom11.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.nhom11.jobhunter.domain.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long>,
                JpaSpecificationExecutor<Skill> {

        boolean existsByName(String name);

        List<Skill> findByIdIn(List<Long> id);
}
