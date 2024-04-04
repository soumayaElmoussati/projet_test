package com.project.application.controller;

import com.project.application.dto.LoginRequest;
import com.project.application.dto.LoginResponse;
import com.project.application.dto.UploadResponse;
import com.project.application.dto.UserDTO;
import com.project.application.jwt.JwtService;
import com.project.application.models.User;
import com.project.application.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    private final JwtService jwtService;



    @GetMapping("/users/generate")
    public ResponseEntity<byte[]> generateUsers(@RequestParam int count) {
        byte[] usersJson = userService.generateUsersJson(count);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.json");
        return new ResponseEntity<>(usersJson, headers, HttpStatus.OK);
    }


    @PostMapping(value = "/users/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadUsers(@RequestParam MultipartFile file) throws IOException {
        try {
            UploadResponse response = userService.importUsers(file);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            // Handle the exception here, e.g., log the error and return an appropriate error response
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Mauvais identifiant ou mot de passe", e);
        }

        User user = userService.findByUsername(loginRequest.getUsername());
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        LoginResponse loginResponse = new LoginResponse(jwtToken);
        return ResponseEntity.ok(loginResponse);
    }

 /*   @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) throws Exception {
        try {
            User user = null;
            String identifier = loginRequest.getUsername();

            // Rechercher l'utilisateur par email
            user = userService.findByEmail(identifier);

            // Si l'utilisateur n'est pas trouvé par email, rechercher par nom d'utilisateur
            if (user == null) {
                user = userService.findByUsername(identifier);
            }

            if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Mauvais identifiant ou mot de passe");
            }

            // Authentifier l'utilisateur avec le nom d'utilisateur récupéré de la base de données
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
            );

            // Générer le jeton JWT et le jeton de rafraîchissement
            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            LoginResponse loginResponse = new LoginResponse(jwtToken);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            throw new Exception("Mauvais identifiant ou mot de passe", e);
        }
    }*/


    @GetMapping("/users/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        try {
            // Extraire le jeton JWT de l'en-tête Authorization
            String jwtToken = jwtService.getJwtFromRequest(request);
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT Token is missing");
            }

            // Valider le jeton JWT
            if (!jwtService.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
            }

            // Extraire le nom d'utilisateur à partir du jeton JWT
            String username = jwtService.extractUsername(jwtToken);

            // Récupérer les détails complets de l'utilisateur à partir du service UserService
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Renvoyer les détails complets de l'utilisateur
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user details");
        }
    }

    private User getUserDetails(String username) {
        // Implémentez la logique pour récupérer les détails de l'utilisateur à partir de la base de données ou d'une autre source de données
        // C'est un exemple fictif, vous devez adapter cette méthode à votre logique métier réelle
        User user = new User();
        user.setUsername(username);
        // Renseignez d'autres détails de l'utilisateur si nécessaire
        return user;
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username, HttpServletRequest request) {
        try {
            // Extraire le jeton JWT de l'en-tête Authorization
            String jwtToken = jwtService.getJwtFromRequest(request);
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT Token is missing");
            }

            // Valider le jeton JWT
            if (!jwtService.isTokenValid(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
            }

            // Extraire le nom d'utilisateur à partir du jeton JWT
            String tokenUsername = jwtService.extractUsername(jwtToken);

            // Récupérer le rôle de l'utilisateur à partir du jeton JWT
            String userRole = getUserRoleFromToken(jwtToken);

            // Si l'utilisateur est un administrateur, il peut accéder au profil de n'importe quel utilisateur
            if ("admin".equals(userRole)) {
                // Recherchez l'utilisateur spécifié par le nom d'utilisateur dans la base de données
                User user = userService.findByUsername(username);
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }
                // Renvoyer les détails de l'utilisateur
                return ResponseEntity.ok(user);
            }

            // Si l'utilisateur n'est pas un administrateur, assurez-vous que le nom d'utilisateur spécifié correspond à celui du jeton JWT
            if (!username.equals(tokenUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            // Recherchez l'utilisateur spécifié par le nom d'utilisateur dans la base de données
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Renvoyer les détails de l'utilisateur
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user details");
        }
    }

    private String getUserRoleFromToken(String jwtToken) {
        // Extraire les revendications du jeton JWT
        Claims claims = jwtService.extractAllClaims(jwtToken);

        // Extraire le rôle de l'utilisateur des revendications du jeton JWT
        String userRole = (String) claims.get("role");

        // Retourner le rôle de l'utilisateur
        return userRole;
    }




}
