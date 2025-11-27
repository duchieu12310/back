package vn.nhom11.jobhunter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.service.CompanyService;
import vn.nhom11.jobhunter.service.RoleService;
import vn.nhom11.jobhunter.service.UserService;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    private final CompanyService companyService;
    private final UserService userService;
    private final RoleService roleService;

    public PermissionInterceptorConfiguration(CompanyService companyService, UserService userService,
            RoleService roleService) {
        this.companyService = companyService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/", "/api/v1/auth/**", "/storage/**",
                "/api/v1/companies/**", "/api/v1/jobs/**", "/api/v1/skills/**", "/api/v1/files",
                "/api/v1/resumes/**",
                "/api/notifications/last24h",
                "/api/v1/roles/**",
                "/api/v1/subscribers/**",
                "/api/v1/users/*", // chỉ GET /api/v1/users/{id}
                "/api/v1/users", // chỉ PUT /api/v1/users
                "/api/v1/permissions",
                "/api/v1/permissions/**",
                "api/notifications/last24h"
        };
        List<String> dynamicWhiteList = new ArrayList<>(List.of(whiteList));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            String username = authentication.getName();
            User user = userService.handleGetUserByUsername(username);

            if (user != null && user.getRole() == null) {
                dynamicWhiteList.add("/api/v1/company-registrations/**");

            }
            if (user != null && user.getRole() != null && user.getRole().getId() > 0) {
                dynamicWhiteList.remove("/api/v1/company-registrations/**");

            }
        }

        String[] finalWhiteList = dynamicWhiteList.toArray(new String[0]);
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(finalWhiteList);
    }
}
