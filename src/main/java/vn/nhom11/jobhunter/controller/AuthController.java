package vn.nhom11.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.request.ReqLoginDTO;
import vn.nhom11.jobhunter.domain.response.ResCreateUserDTO;
import vn.nhom11.jobhunter.domain.response.ResLoginDTO;
import vn.nhom11.jobhunter.domain.response.ResUpdateUserDTO;
import vn.nhom11.jobhunter.domain.response.ChangePasswordRequest.ChangePasswordRequest;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.SecurityUtil;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;
import vn.nhom11.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${nhom11.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil,
            UserService userService,
            PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // =================== LOGIN ===================
    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) throws IdInvalidException {
        User userDB = userService.handleGetUserByUsername(loginDto.getUsername());
        if (userDB == null) {
            throw new IdInvalidException("Email hoặc mật khẩu không đúng.");
        }

        if (!userDB.isEnabled()) {
            throw new IdInvalidException("Tài khoản chưa xác nhận email.");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                userDB.getId(),
                userDB.getEmail(),
                userDB.getName(),
                userDB.getRole());
        res.setUser(userLogin);

        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(access_token);

        String refresh_token = this.securityUtil.createRefreshToken(loginDto.getUsername(), res);
        this.userService.updateUserToken(refresh_token, loginDto.getUsername());

        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    // =================== REGISTER ===================
    @PostMapping("/auth/register")
    @ApiMessage("Register a new user with email verification")
    public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User postManUser) throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + " đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        postManUser.setEnabled(false); // chưa kích hoạt
        User newUser = this.userService.handleCreateUser(postManUser);

        // Tạo token xác nhận email
        String token = userService.generateVerificationToken(newUser);

        // Gửi email xác nhận
        userService.sendVerificationEmail(newUser, token);

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(newUser));
    }

    // =================== VERIFY EMAIL ===================
    @GetMapping("/auth/verify")
    @ApiMessage("Verify user email by token")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) throws IdInvalidException {
        User user = userService.getUserByVerificationToken(token);
        if (user == null) {
            throw new IdInvalidException("Token xác nhận email không hợp lệ hoặc đã hết hạn.");
        }

        userService.activateUser(user);
        return ResponseEntity.ok("Xác nhận email thành công. Bạn có thể đăng nhập ngay bây giờ!");
    }

    // =================== CHANGE PASSWORD ===================
    @PutMapping("/auth/change-password")
    @ApiMessage("Change user password")
    public ResponseEntity<ResUpdateUserDTO> changePassword(@RequestBody ChangePasswordRequest req)
            throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isEmpty()) {
            throw new IdInvalidException("Access Token không hợp lệ hoặc đã hết hạn.");
        }

        User currentUser = this.userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("Người dùng không tồn tại.");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), currentUser.getPassword())) {
            throw new IdInvalidException("Mật khẩu cũ không chính xác.");
        }

        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new IdInvalidException("Mật khẩu mới không được giống mật khẩu cũ.");
        }

        String newHashedPassword = passwordEncoder.encode(req.getNewPassword());
        currentUser.setPassword(newHashedPassword);
        currentUser = this.userService.handleUpdateUser(currentUser);

        ResUpdateUserDTO res = this.userService.convertToResUpdateUserDTO(currentUser);
        return ResponseEntity.ok(res);
    }

    // =================== ACCOUNT ===================
    @GetMapping("/auth/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        User currentUserDB = this.userService.handleGetUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());

            userGetAccount.setUser(userLogin);
        }

        return ResponseEntity.ok(userGetAccount);
    }

    // =================== REFRESH TOKEN ===================
    @GetMapping("/auth/refresh")
    @ApiMessage("Get User by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token) throws IdInvalidException {

        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Bạn không có refresh token ở cookie");
        }

        // Kiểm tra token hợp lệ
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

        // Lấy user từ token
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ");
        }

        // Tạo access token mới
        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(),
                    currentUserDB.getRole());
            res.setUser(userLogin);
        }

        String access_token = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(access_token);

        // Tạo refresh token mới
        String new_refresh_token = this.securityUtil.createRefreshToken(email, res);
        this.userService.updateUserToken(new_refresh_token, email);

        // Set cookie
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    // =================== LOGOUT ===================
    @PostMapping("/auth/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        if (email.isEmpty()) {
            throw new IdInvalidException("Access Token không hợp lệ");
        }

        // Xóa refresh token của user
        this.userService.updateUserToken(null, email);

        // Xóa cookie refresh token
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }

    // =================== LOGOUT, REFRESH, ACCOUNT ===================
    // Giữ nguyên như trước
}
