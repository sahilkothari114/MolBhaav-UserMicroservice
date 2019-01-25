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
        UserDTO userDTO1 = new UserDTO();
        if(Objects.isNull(userDTO) ){
            userDTO1.setMessage("Name, EmailId or Password can not be empty.");
            userDTO1.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        else if(userDTO.getEmailId()==null){
            userDTO1.setMessage("EmailId can not be empty.");
            userDTO1.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        else if(userDTO.getPassword()==null){
            userDTO1.setMessage("Password can not be empty.");
            userDTO1.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        else if(userDTO.getName()==null){
            userDTO1.setMessage("Name can not be empty.");
            userDTO1.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        else if(userDTO.getAddress()==null){
            userDTO1.setMessage("Address can not be empty.");
            userDTO1.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        User user = userService.findByEmailId(userDTO.getEmailId());
        if (Objects.isNull(user)){
            userDTO1.setMessage("Email Id already exists.");
            userDTO1.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        BeanUtils.copyProperties(userDTO,user);
        try {
            user.setPassword(passwordHash.createHash(userDTO.getPassword()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        User signedInUser;
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
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO) {
        LOGGER.info("Received a request for login");
        ResponseEntity<String> response = null;
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        if(Objects.isNull(user)){
            userDTO.setMessage("EmailId or Password can not be empty.");
            userDTO.setStatus(400);
            LOGGER.info("EmailId or Password can not be empty."+userDTO.toString());
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        else if (user.getEmailId()==null){
            userDTO.setMessage("EmailId can not be empty.");
            userDTO.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
        else if (user.getPassword()==null) {
            userDTO.setMessage("Password can not be empty.");
            userDTO.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO, HttpStatus.BAD_REQUEST);
        }
        user = userService.findByEmailId(user.getEmailId());
        if(Objects.isNull(user)){
            userDTO.setMessage("EmailId Does Not Exist");
            userDTO.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }
         boolean isLoginSuccessful = false;
        try {
            isLoginSuccessful = passwordHash.validatePassword(userDTO.getPassword(),user.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(user,userDTO);

        if (isLoginSuccessful){
            userDTO.setMessage("Succussful");
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        else {
            userDTO.setMessage("Login failed");
            userDTO.setStatus(400);
            return new ResponseEntity<UserDTO>(userDTO,HttpStatus.BAD_REQUEST);
        }

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
    public ResponseEntity<UserDTO> profile(@PathVariable("userId") long userId){
        LOGGER.info("Received GET request for profileId:" +userId);
        User user = userService.findOne(userId);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        return new ResponseEntity<>(userDTO,HttpStatus.OK);
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
