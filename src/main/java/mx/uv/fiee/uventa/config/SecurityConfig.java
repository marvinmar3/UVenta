package mx.uv.fiee.uventa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import mx.uv.fiee.uventa.config.MicrosoftLoginSuccessHandler;

@Configuration
public class SecurityConfig {

    private final MicrosoftLoginSuccessHandler microsoftLoginSuccessHandler;

    public SecurityConfig(MicrosoftLoginSuccessHandler microsoftLoginSuccessHandler) {
        this.microsoftLoginSuccessHandler = microsoftLoginSuccessHandler;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/registro", "/verificar", "/reenviar", "/error", "/oauth2/**", "/login/oauth2/**",
                                 "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login")
                .usernameParameter("correo")
                .passwordParameter("password")
                .defaultSuccessUrl("/catalogo", true)
                .failureHandler((req, res, ex) ->
                    res.sendRedirect(ex instanceof DisabledException ? "/login?pendiente" : "/login?error"))
                .permitAll())
            .oauth2Login(o -> o
                .loginPage("/login")
                .successHandler(microsoftLoginSuccessHandler)
                .failureUrl("/login?oauth2error"))
            .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll());
        return http.build();
    }
}