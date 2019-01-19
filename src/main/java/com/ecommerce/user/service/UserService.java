package com.ecommerce.user.service;

import com.ecommerce.user.entity.User;

import java.util.List;

public interface UserService {
    public User save(User user);
    public User findOne(long userId);
    public  User update(User user);
    public  void delete(long userId);
    public List<User> findAll();

    public User findByEmailId(String emailId);

}
