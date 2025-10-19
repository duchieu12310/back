package vn.nhom11.jobhunter.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.nhom11.jobhunter.domain.Permission;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission p) {
        return permissionRepository.existsByModuleAndApiPathAndMethod(
                p.getModule(),
                p.getApiPath(),
                p.getMethod());
    }

    public Permission fetchById(long id) {
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        return permissionOptional.orElse(null);
    }

    public Permission create(Permission p) {
        return this.permissionRepository.save(p);
    }

    public Permission update(Permission p) {
        Permission permissionDB = this.fetchById(p.getId());
        if (permissionDB != null) {
            permissionDB.setName(p.getName());
            permissionDB.setApiPath(p.getApiPath());
            permissionDB.setMethod(p.getMethod());
            permissionDB.setModule(p.getModule());
            return this.permissionRepository.save(permissionDB);
        }
        return null;
    }

    public void delete(long id) {
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        Permission currentPermission = permissionOptional.get();
        currentPermission.getRoles().forEach(role -> role.getPermissions().remove(currentPermission));
        this.permissionRepository.delete(currentPermission);
    }

    public ResultPaginationDTO getPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pPermissions = this.permissionRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pPermissions.getTotalPages());
        mt.setTotal(pPermissions.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pPermissions.getContent());
        return rs;
    }

    public boolean isSameName(Permission p) {
        Permission permissionDB = this.fetchById(p.getId());
        return permissionDB != null && permissionDB.getName().equals(p.getName());
    }

    public ResultPaginationDTO fetchPermissionsByCreatedBy(String username, Pageable pageable) {
        Specification<Permission> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"),
                username);
        return this.getPermissions(spec, pageable);
    }

    // ✅ THÊM PHƯƠNG THỨC NÀY
    public ResultPaginationDTO getPermissionsByRoleId(long roleId, Pageable pageable) {
        List<Permission> permissions = this.permissionRepository.findByRoles_Id(roleId);

        // Tạo trang dữ liệu (Page) thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), permissions.size());
        List<Permission> pagedList = permissions.subList(start, end);

        Page<Permission> page = new PageImpl<>(pagedList, pageable, permissions.size());

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }
}
