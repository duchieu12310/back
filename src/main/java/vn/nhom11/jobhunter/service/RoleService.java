package vn.nhom11.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.nhom11.jobhunter.domain.Permission;
import vn.nhom11.jobhunter.domain.Role;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.repository.PermissionRepository;
import vn.nhom11.jobhunter.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role create(Role r) {
        // check permissions
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }

        return this.roleRepository.save(r);
    }

    public Role fetchById(long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (roleOptional.isPresent())
            return roleOptional.get();
        return null;
    }

    public Role update(Role r) {
        Role roleDB = this.fetchById(r.getId());
        // check permissions
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions()
                    .stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }

        roleDB.setName(r.getName());
        roleDB.setDescription(r.getDescription());
        roleDB.setActive(r.isActive());
        roleDB.setPermissions(r.getPermissions());
        roleDB = this.roleRepository.save(roleDB);
        return roleDB;
    }

    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }

    public ResultPaginationDTO getRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pRole = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pRole.getTotalPages());
        mt.setTotal(pRole.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pRole.getContent());
        return rs;
    }

    public ResultPaginationDTO fetchRolesByCreatedBy(String username, Pageable pageable) {
        Specification<Role> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"),
                username);

        return this.getRoles(spec, pageable);
    }

    public long countPermissionsByRoleId(Long roleId) {
        Optional<Role> roleOpt = this.roleRepository.findById(roleId);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            if (role.getPermissions() != null) {
                return role.getPermissions().size();
            }
        }
        return 0;
    }

    public boolean permissionVsRole(Long roleId) {
        long a = this.countPermissionsByRoleId(roleId); // số quyền của role cụ thể
        long b = this.permissionRepository.count(); // tổng số quyền trong hệ thống

        return a == b; // true nếu role này có đủ tất cả quyền, false nếu chưa đủ
    }

    public Role findById(Long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        return roleOptional.orElse(null);
    }

}
