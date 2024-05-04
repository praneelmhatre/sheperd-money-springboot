package com.shepherdmoney.interviewproject.controller;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.model.User;



@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository; // Wire in the user repository

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
         // Create a new user entity
        User user = new User();
        user.setName(payload.getName());
        user.setEmail(payload.getEmail()); // Create a user entity with information given in the payload
        userRepository.save(user); // Store it in the database
        return ResponseEntity.ok(user.getId()); // Return the id of the user in 200 OK response
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        if (userRepository.existsById(userId)) { // Check if a user with the given ID exists
            userRepository.deleteById(userId); // Delete the user
            return ResponseEntity.ok("User deleted successfully"); // Return 200 OK if the deletion is successful
        } else {
            return ResponseEntity.badRequest().body("User with ID " + userId + " does not exist"); // Return 400 Bad Request if a user with the ID does not exist
        }
    }
}
