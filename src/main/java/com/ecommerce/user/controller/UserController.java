package com.ecommerce.user.controller;

import com.ecommerce.user.DTO.UserDTO;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.service.UserService;
import com.ecommerce.user.util.PasswordHashInterface;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordHashInterface passwordHash;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/signIn", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> signIn(@RequestBody UserDTO userDTO){
        if(Objects.isNull(userDTO) || Objects.isNull(userDTO.getName()) || Objects.isNull(userDTO.getEmailId()) || Objects.isNull(userDTO.getPassword()) ){
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        try {
            user.setPassword(passwordHash.createHash(userDTO.getPassword()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        User signedInUser=new User();
        try {
            signedInUser = userService.save(user);
        }catch (Exception e){
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);

        }
        if (Objects.isNull(signedInUser)){
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        userDTO=new UserDTO();
        BeanUtils.copyProperties(user,userDTO);

        return new ResponseEntity<UserDTO>(userDTO,HttpStatus.CREATED);
    }
    @CrossOrigin(origins = "*")
    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        LOGGER.info("Received a request for login");
        ResponseEntity<String> response = null;
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        try {
            if (!Objects.isNull(user) && !Objects.isNull(user.getEmailId()) && !Objects.isNull(user.getPassword())) {
                final String username = user.getEmailId();
                final User ExistingUser = userService.findByEmailId(username);
                JSONObject exceptionJson = new JSONObject();
                    exceptionJson.put("message", "Invalid login. Please check your name and password.");

                if (Objects.isNull(ExistingUser)) {
                    LOGGER.info("Login failed.");
                    response = new ResponseEntity<>(exceptionJson.toString(), HttpStatus.UNAUTHORIZED);
                } else {
                    final boolean isLoginSuccessful = passwordHash.validatePassword(user.getPassword(),ExistingUser.getPassword());

                    if (isLoginSuccessful) {
                        JSONObject userJson = new JSONObject();
                        userJson.put("userId", ExistingUser.getUserId())
                                .put("name", ExistingUser.getName())
                                .put("emailId", ExistingUser.getEmailId());
                        response = new ResponseEntity<>(userJson.toString(), HttpStatus.OK);
                        LOGGER.info("Login successful");
                    } else {
                        response = new ResponseEntity<>(exceptionJson.toString(), HttpStatus.UNAUTHORIZED);
                        LOGGER.info("Login failed.");
                    }
                }
            } else {
                JSONObject exceptionJson = new JSONObject();
                exceptionJson.put("message", "Email or password cannot be null.");
                response = new ResponseEntity<>(exceptionJson.toString(), HttpStatus.BAD_REQUEST);
                LOGGER.info("Login failed.");
            }
        } catch (JSONException e) {
            LOGGER.error("Error occurred while creating JSON object");
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Edits user profile except email
     *
     * @param user
     *            contains updated values
     * @return user with updated values
     */
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/editProfile", method = RequestMethod.PUT)
    public ResponseEntity<UserDTO> editProfile(@RequestBody User user) {
        LOGGER.info("Received a request for edit profile");
        ResponseEntity<User> response = null;
        final User updatedUser = userService.update(user);
        UserDTO userDTO=new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public UserDTO profile(@PathVariable("userId") long userId){
        LOGGER.info("Received GET request for profileId:" +userId);
        User user = userService.findOne(userId);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        return userDTO;
    }
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public List<UserDTO> findAll(){
        LOGGER.info("Received GEt request to fetch all Users");
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user:userService.findAll()) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user,userDTO);
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }
}
