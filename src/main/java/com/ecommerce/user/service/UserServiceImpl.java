package com.ecommerce.user.service;

import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findOne(long userId) {
        return userRepository.findOne(userId);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(long userId) {
        userRepository.delete(userId);
    }

    @Override
    public List<User> findAll() {
        List<User> userList= new ArrayList<>();
        Iterable<User> employeeIterable = userRepository.findAll();
        employeeIterable.forEach(userList::add);
        return userList;
    }

    @Override
    public User findByEmailId(String emailId) {
        return userRepository.findByEmailId(emailId);
    }
}
