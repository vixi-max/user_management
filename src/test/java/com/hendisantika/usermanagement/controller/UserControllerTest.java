package com.hendisantika.usermanagement.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hendisantika.usermanagement.dto.ChangePasswordForm;
import com.hendisantika.usermanagement.entity.Role;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.exception.UsernameOrIdNotFound;
import com.hendisantika.usermanagement.repository.RoleRepository;
import com.hendisantika.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UserController sut;

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    private static User user;
    private static Role role1;

    @BeforeAll
    public static void setUpBeforeAll() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Piolo");
        user.setLastName("Pascual");
        user.setEmail("a@a.com");
        user.setUsername("ppascual");
        user.setPassword("dsa");

        role1 = new Role();
        role1.setId(1L);
        role1.setName("SUPER ADMIN");
        role1.setDescription("ROLE SUPER ADMIN");
    }

    @BeforeEach
    public void setUpBeforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(sut).build();
    }

    @Test
    void testCreateUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/userForm")
                        .flashAttr("userForm", user))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    void testGetEditUserFormFound() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(user);

        mockMvc.perform(get("/editUser/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("userForm", user));
    }

    @Test
    void testGetEditUserFormNotFound() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new UsernameOrIdNotFound("User not found"));

        mockMvc.perform(get("/editUser/{id}", 99L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void testLoginAndReturnToIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testSignUpPage() throws Exception {
        when(roleRepository.findAll()).thenReturn(Collections.singletonList(role1));

        mockMvc.perform(get("/signup"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    void testSignUpAction() throws Exception {
        when(roleRepository.findByName(anyString())).thenReturn(role1);
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/signup")
                        .flashAttr("userForm", user))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    void testUserFormPage() throws Exception {
        mockMvc.perform(get("/userForm"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    void testPostEditUserForm() throws Exception {
        when(userService.updateUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/editUser")
                        .flashAttr("userForm", user))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    void testCancelEditUser() throws Exception {
        mockMvc.perform(get("/userForm/cancel"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/userForm"));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(get("/deleteUser/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    void testPostEditUseChangePassword() throws Exception {

        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("dasd");
        form.setNewPassword("awsd");
        form.setConfirmPassword("awsd");

        when(userService.changePassword(any(ChangePasswordForm.class))).thenReturn(user);

        mockMvc.perform(post("/editUser/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(form)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }
    @Test
    void testPostEditUseChangePasswordWithError() throws Exception {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("wrong");
        form.setNewPassword("newPass");
        form.setConfirmPassword("newPass");

        // Simule une exception venant du service
        when(userService.changePassword(any(ChangePasswordForm.class)))
                .thenThrow(new Exception("Current Password invalid."));

        mockMvc.perform(post("/editUser/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(form)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current Password invalid."));
    }
    @Test
    void testSignUpActionWithValidationException() throws Exception {
        // Simule que la méthode createUser lance une exception de validation
        when(roleRepository.findByName(anyString())).thenReturn(role1);
        when(userService.createUser(any(User.class)))
                .thenThrow(new com.hendisantika.usermanagement.exception.CustomFieldValidationException(
                        "Username not available", "username"
                ));

        mockMvc.perform(post("/signup")
                        .flashAttr("userForm", user))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "username"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attribute("signup", true));
    }
    @Test
    void testDeleteUserNotFound() throws Exception {
        // Simule que le service lance une exception pour un ID inexistant
        doThrow(new UsernameOrIdNotFound("User not found")).when(userService).deleteUser(anyLong());

        mockMvc.perform(get("/deleteUser/{id}", 99L))
                .andDo(print())
                .andExpect(status().isOk()) // le controller retourne la page userForm malgré l'erreur
                .andExpect(model().attributeExists("listErrorMessage"))
                .andExpect(model().attribute("listErrorMessage", "User not found"));
    }

    @Test
    void testPostEditUserChangePasswordValidationError() throws Exception {
        // Création d'un formulaire avec des champs vides (violations @NotBlank)
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword(""); // vide
        form.setNewPassword("");     // vide
        form.setConfirmPassword(""); // vide

        // Ici on ne simule pas userService.changePassword, car la validation échoue avant

        mockMvc.perform(post("/editUser/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(form)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("must not be blank")));
    }
    @Test
    void testUserFormPageWithUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));
        when(roleRepository.findAll()).thenReturn(Collections.singletonList(role1));

        mockMvc.perform(get("/userForm"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"))
                .andExpect(model().attributeExists("userList"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attribute("userList", Collections.singletonList(user)));
    }
    @Test
    void testSignUpActionWithGenericException() throws Exception {
        when(roleRepository.findByName(anyString())).thenReturn(role1);
        when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/signup")
                        .flashAttr("userForm", user))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("formErrorMessage"))
                .andExpect(model().attribute("formErrorMessage", "Database error"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attribute("signup", true));
    }
    @Test
    void testGetEditUserFormInternalServerError() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/editUser/{id}", 1L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateUserWithValidationErrors() throws Exception {
        User invalidUser = new User(); // aucun champ rempli

        mockMvc.perform(post("/userForm")
                        .flashAttr("userForm", invalidUser))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userForm"))
                .andExpect(model().attributeExists("userList"))
                .andExpect(model().attributeExists("roles"));
    }
    @Test
    void testPostEditUserFormWithException() throws Exception {
        when(userService.updateUser(any(User.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(post("/editUser")
                        .flashAttr("userForm", user))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("formErrorMessage"))
                .andExpect(model().attribute("editMode", "true"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    void testSignUpPageWhenRolesEmpty() throws Exception {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        when(roleRepository.findByName("USER")).thenReturn(role1);

        mockMvc.perform(get("/signup"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("userForm"))
                .andExpect(model().attribute("signup", true));

        verify(roleRepository, atLeastOnce()).save(any(Role.class));
    }









}



