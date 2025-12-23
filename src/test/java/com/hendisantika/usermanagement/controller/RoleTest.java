package com.hendisantika.usermanagement.controller;



import com.hendisantika.usermanagement.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testRoleGettersSetters() {
        Role role = new Role();

        role.setId(1L);
        role.setName("ADMIN");
        role.setDescription("ROLE ADMIN");

        assertEquals(1L, role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals("ROLE ADMIN", role.getDescription());
    }

    @Test
    void testRoleAllArgsConstructor() {
        Role role = new Role(2L, "USER", "ROLE USER");

        assertEquals(2L, role.getId());
        assertEquals("USER", role.getName());
        assertEquals("ROLE USER", role.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        Role role1 = new Role(1L, "ADMIN", "ROLE ADMIN");
        Role role2 = new Role(1L, "ADMIN", "ROLE ADMIN");
        Role role3 = new Role(2L, "USER", "ROLE USER");

        // Vérifie equals
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);

        // Vérifie hashCode
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    void testToString() {
        Role role = new Role(1L, "ADMIN", "ROLE ADMIN");
        String expected = "Role(id=1, name=ADMIN, description=ROLE ADMIN)";
        assertEquals(expected, role.toString());
    }
}
