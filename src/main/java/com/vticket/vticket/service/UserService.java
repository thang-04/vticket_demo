package com.vticket.vticket.service;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.exception.AppException;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.mapper.UserMapper;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserCollection userCollection;
    @Autowired
    private UserMapper userMapper;
//    @Autowired
//    PasswordEncoder passwordEncoder;
    @Autowired
    private ProcessUserService processUserService;


    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        try {
            if (userCollection.getUserInfoByUserName(userCreationRequest.getUsername()) != null) {
                throw new RuntimeException("UserName already in use");
            }
            User user = userMapper.toEntity(userCreationRequest);
            user.setPassword(userCreationRequest.getPassword());
//            processUserService.enQueueUser(user);
            userCollection.insertUser(user);
            //day vao redis
            return userMapper.toResponse(user);

        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    public List<UserResponse> getAllUser(){
        List<User> users = userCollection.getAllUsers();
        if(users.isEmpty()){
            throw new RuntimeException("No users found");
        }
        return users.stream().map(userMapper::toResponse).toList();
    }

    public User getUserById(String id){
        if(StringUtils.isBlank(id)){
            throw new RuntimeException("Id is required");
        }
        User user = userCollection.getUserById(id);
        if(user == null){
            throw new RuntimeException("User not found with id: " + id);
        }
        return user;
    }

    public User getUserFromAccessToken( String token) {
        return null;
    }
}
