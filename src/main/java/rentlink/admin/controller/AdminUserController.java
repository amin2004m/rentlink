package rentlink.admin.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rentlink.admin.dto.AdminUserResponse;
import rentlink.admin.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {

    AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserResponse> getAll() {
        return adminUserService.getAll();
    }

    @PutMapping("/{id}/block")
    public AdminUserResponse block(@PathVariable String id) {
        return adminUserService.setBlocked(id, true);
    }

    @PutMapping("/{id}/unblock")
    public AdminUserResponse unblock(@PathVariable String id) {
        return adminUserService.setBlocked(id, false);
    }
}
