package vn.nhom11.jobhunter.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.nhom11.jobhunter.domain.Permission;
import vn.nhom11.jobhunter.domain.Role;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.repository.PermissionRepository;
import vn.nhom11.jobhunter.repository.RoleRepository;
import vn.nhom11.jobhunter.repository.UserRepository;
import vn.nhom11.jobhunter.util.constant.GenderEnum;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        if (countPermissions == 0) {
            ArrayList<Permission> arr = new ArrayList<>();

            // === CÔNG TY ===
            arr.add(new Permission("Tạo công ty", "/api/v1/companies", "POST", "COMPANIES"));
            arr.add(new Permission("Cập nhật công ty", "/api/v1/companies", "PUT", "COMPANIES"));
            arr.add(new Permission("Xóa công ty", "/api/v1/companies/{id}", "DELETE", "COMPANIES"));
            arr.add(new Permission("Lấy thông tin công ty theo ID", "/api/v1/companies/{id}", "GET", "COMPANIES"));
            arr.add(new Permission("Lấy danh sách công ty (phân trang)", "/api/v1/companies", "GET", "COMPANIES"));

            // === CÔNG VIỆC ===
            arr.add(new Permission("Tạo công việc", "/api/v1/jobs", "POST", "JOBS"));
            arr.add(new Permission("Cập nhật công việc", "/api/v1/jobs", "PUT", "JOBS"));
            arr.add(new Permission("Xóa công việc", "/api/v1/jobs/{id}", "DELETE", "JOBS"));
            arr.add(new Permission("Lấy thông tin công việc theo ID", "/api/v1/jobs/{id}", "GET", "JOBS"));
            arr.add(new Permission("Lấy danh sách công việc (phân trang)", "/api/v1/jobs", "GET", "JOBS"));
            arr.add(new Permission("Lấy công việc theo người tạo", "/api/v1/jobs/by-created/{username}", "GET",
                    "JOBS"));

            // === QUYỀN (PERMISSION) ===
            arr.add(new Permission("Tạo quyền", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Cập nhật quyền", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            arr.add(new Permission("Xóa quyền", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            arr.add(new Permission("Lấy quyền theo ID", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Lấy danh sách quyền (phân trang)", "/api/v1/permissions", "GET", "PERMISSIONS"));

            // === HỒ SƠ (RESUME) ===
            arr.add(new Permission("Tạo hồ sơ", "/api/v1/resumes", "POST", "RESUMES"));
            arr.add(new Permission("Cập nhật hồ sơ", "/api/v1/resumes", "PUT", "RESUMES"));
            arr.add(new Permission("Xóa hồ sơ", "/api/v1/resumes/{id}", "DELETE", "RESUMES"));
            arr.add(new Permission("Lấy hồ sơ theo ID", "/api/v1/resumes/{id}", "GET", "RESUMES"));
            arr.add(new Permission("Lấy danh sách hồ sơ (phân trang)", "/api/v1/resumes", "GET", "RESUMES"));

            // === VAI TRÒ (ROLE) ===
            arr.add(new Permission("Tạo vai trò", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Cập nhật vai trò", "/api/v1/roles", "PUT", "ROLES"));
            arr.add(new Permission("Xóa vai trò", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            arr.add(new Permission("Lấy vai trò theo ID", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Lấy danh sách vai trò (phân trang)", "/api/v1/roles", "GET", "ROLES"));

            // === NGƯỜI DÙNG (USER) ===
            arr.add(new Permission("Tạo người dùng", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Cập nhật người dùng", "/api/v1/users", "PUT", "USERS"));
            arr.add(new Permission("Xóa người dùng", "/api/v1/users/{id}", "DELETE", "USERS"));
            arr.add(new Permission("Lấy người dùng theo ID", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Lấy danh sách người dùng (phân trang)", "/api/v1/users", "GET", "USERS"));

            // === NGƯỜI ĐĂNG KÝ (SUBSCRIBER) ===
            arr.add(new Permission("Tạo người đăng ký", "/api/v1/subscribers", "POST", "SUBSCRIBERS"));
            arr.add(new Permission("Cập nhật người đăng ký", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"));
            arr.add(new Permission("Xóa người đăng ký", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"));
            arr.add(new Permission("Lấy người đăng ký theo ID", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"));
            arr.add(new Permission("Lấy danh sách người đăng ký (phân trang)", "/api/v1/subscribers", "GET",
                    "SUBSCRIBERS"));

            // === TỆP TIN (FILE) ===
            arr.add(new Permission("Tải xuống tệp", "/api/v1/files", "POST", "FILES"));
            arr.add(new Permission("Tải lên tệp", "/api/v1/files", "GET", "FILES"));

            // === ĐĂNG KÝ CÔNG TY (COMPANY REGISTRATION) ===
            arr.add(new Permission("Tạo yêu cầu đăng ký công ty", "/api/v1/company-registrations", "POST",
                    "COMPANY_REGISTRATIONS"));
            arr.add(new Permission("Xem danh sách yêu cầu đăng ký công ty", "/api/v1/company-registrations", "GET",
                    "COMPANY_REGISTRATIONS"));
            arr.add(new Permission("Cập nhật trạng thái đăng ký công ty", "/api/v1/company-registrations/{id}/status",
                    "PUT", "COMPANY_REGISTRATIONS"));
            arr.add(new Permission("Lấy thông tin đăng ký công ty theo ID", "/api/v1/company-registrations/{id}", "GET",
                    "COMPANY_REGISTRATIONS"));
            arr.add(new Permission("Từ chối đăng ký công ty", "/api/v1/company-registrations/{id}/reject", "PUT",
                    "COMPANY_REGISTRATIONS"));
            arr.add(new Permission("Xóa yêu cầu đăng ký công ty", "/api/v1/company-registrations/{id}", "DELETE",
                    "COMPANY_REGISTRATIONS"));

            // === TIN NHẮN (MESSAGE) ===
            arr.add(new Permission("Gửi tin nhắn", "/api/v1/messages", "POST", "MESSAGES"));
            arr.add(new Permission("Xóa tin nhắn", "/api/v1/messages/{id}", "DELETE", "MESSAGES"));
            arr.add(new Permission("Lấy tin nhắn theo ID", "/api/v1/messages/{id}", "GET", "MESSAGES"));
            arr.add(new Permission("Lấy danh sách tin nhắn giữa 2 người dùng", "/api/v1/messages/conversation", "GET",
                    "MESSAGES"));

            this.permissionRepository.saveAll(arr);
        }

        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            // === ROLE SUPER_ADMIN ===
            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Quản trị viên có toàn quyền trong hệ thống");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);
            this.roleRepository.save(adminRole);

            // === ROLE MANAGER ===
            Role managerRole = this.roleRepository.findByName("MANAGER");
            if (managerRole == null) {
                List<Permission> managerPermissions = allPermissions.stream()
                        .filter(p -> p.getModule().equalsIgnoreCase("USERS")
                                || p.getModule().equalsIgnoreCase("COMPANIES")
                                || p.getModule().equalsIgnoreCase("RESUMES"))
                        .toList();

                managerRole = new Role();
                managerRole.setName("MANAGER");
                managerRole.setDescription("Người quản lý có quyền với người dùng, công ty và hồ sơ");
                managerRole.setActive(true);
                managerRole.setPermissions(managerPermissions);

                this.roleRepository.save(managerRole);
                System.out.println(">>> Đã tạo ROLE: MANAGER với quyền USERS, COMPANIES, RESUMES");
            }
        }

        if (countUsers == 0) {
            // === ADMIN USER ===
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("Hà Nội");
            adminUser.setAge(25);
            adminUser.setGender(GenderEnum.MALE);
            adminUser.setName("Super Admin");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }
            this.userRepository.save(adminUser);

            // // // === MANAGER USER ===
            // // User managerUser = new User();
            // // managerUser.setEmail("manager@gmail.com");
            // // managerUser.setAddress("Hồ Chí Minh");
            // // managerUser.setAge(30);
            // // managerUser.setGender(GenderEnum.FEMALE);
            // // managerUser.setName("Manager");
            // // managerUser.setPassword(this.passwordEncoder.encode("123456"));

            // // Role managerRole = this.roleRepository.findByName("MANAGER");
            // // if (managerRole != null) {
            // // managerUser.setRole(managerRole);
            // // }
            // this.userRepository.save(managerUser);
        }

        if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else {
            System.out.println(">>> END INIT DATABASE");
        }
    }
}
