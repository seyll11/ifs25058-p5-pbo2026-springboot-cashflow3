package org.delcom.ifs00000springbootcashflow.domain.entity;

public record User(
        int id,
        String username,
        String passwordHash,
        String fullName,
        Role role,
        String photoPath) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public User withId(int newId) {
        return new User(newId, username, passwordHash, fullName, role, photoPath);
    }

    public User withProfile(String newFullName, String newPasswordHash) {
        return new User(id, username, newPasswordHash, newFullName, role, photoPath);
    }

    public User withPhoto(String newPhotoPath) {
        return new User(id, username, passwordHash, fullName, role, newPhotoPath);
    }
}
