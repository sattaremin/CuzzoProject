package com.amazon.controller;

import com.amazon.dto.UserDto;
import com.amazon.exception.UserNotFoundException;
import com.amazon.service.CompanyService;
import com.amazon.service.RoleService;
import com.amazon.service.UserService;
import com.amazon.service.impl.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final CompanyService companyService;
    private final UserServiceImpl userServiceImpl;

    public UserController(UserService userService,
                          RoleService roleService,
                          CompanyService companyService,
                          UserServiceImpl userServiceImpl) {

        this.userService = userService;
        this.roleService = roleService;
        this.companyService = companyService;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/list")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.listAllByLoggedInUser());
        return "user/user-list";
    }

    @GetMapping("/create")
    public String createUser(Model model) {
        model.addAttribute("newUser", new UserDto());
        model.addAttribute("userRoles", roleService.listAllRoles());
        model.addAttribute("companies", companyService.getCompaniesBasedOnLoggedInUser());
        return "user/user-create";
    }

    @PostMapping("/create")
    public String insertUser(@Valid @ModelAttribute("newUser") UserDto user,
                             BindingResult bindingResult,
                             Model model) {

        boolean isUsernameExist = userService.isUserNameExists(user);
        if (isUsernameExist) {
            bindingResult.rejectValue("username", " ",
                    "A user with this email already exists. Please try with different email.");
        }
        if (bindingResult.hasErrors()) {

            model.addAttribute("userRoles", roleService.listAllRoles());
            model.addAttribute("companies", companyService.listAllCompanies());
            return "user/user-create";
        }
        userService.save(user);
        return "redirect:/users/list";
    }

    @GetMapping("/update/{id}")
    public String editUser(@PathVariable("id") Long id, Model model) throws UserNotFoundException {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("userRoles", roleService.listAllRoles());
        model.addAttribute("companies", companyService.listAllCompanies());
        return "user/user-update";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@Valid @ModelAttribute("user") UserDto user,
                             BindingResult bindingResult,
                             Model model) throws UserNotFoundException {
        boolean isUsernameExist = userService.isUserNameExists(user);
        if (isUsernameExist) {

            model.addAttribute("userRoles", roleService.listAllRoles());
            model.addAttribute("companies", companyService.listAllCompanies());
            bindingResult.rejectValue("username", " ", "A user with this email already exists. Please try with different email.");
        }
        if (bindingResult.hasErrors()) {

            model.addAttribute("userRoles", roleService.listAllRoles());
            model.addAttribute("companies", companyService.listAllCompanies());
            return "user/user-update";
        }
        userService.update(user);
        return "redirect:/users/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userServiceImpl.deleteById(id);
        return "redirect:/users/list";
    }

}
