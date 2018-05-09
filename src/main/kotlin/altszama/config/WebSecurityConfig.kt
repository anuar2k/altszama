package altszama.config;

import altszama.auth.TokenAuthFilter;
import com.google.common.collect.ImmutableList;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

  override fun configure(httpSecurity: HttpSecurity) {
    val permittedPaths = arrayOf(
      "/", "/home", "/login*", "/signin/**", "/signup/**", "/register/**", "/static/**", "/manifest.*.json",
      "/sw.js", "/webjars/**", "/restaurantImport/**", "/restaurantImportFromPayload/**", "/auth/**", "/__webpack_hmr", "/static2/index.html",
      "/static/index.html", "/index.html", "/service-worker.js", "/notification/**", "/custom-service-worker.js"
    )

    httpSecurity
      .csrf().disable()
      .cors().and()
      .authorizeRequests()
        .antMatchers(*permittedPaths).permitAll()
        .anyRequest().authenticated()
        .and()
      .exceptionHandling()
        .authenticationEntryPoint(Http401AuthenticationEntryPoint("headerValue")).and()
      .logout().permitAll().and()
        .addFilterBefore(jwtAuthenticationTokenFilter(), BasicAuthenticationFilter::class.java)
  }

  @Bean
  open fun jwtAuthenticationTokenFilter(): TokenAuthFilter {
    return TokenAuthFilter()
  }

  @Bean
  open fun encoder(): PasswordEncoder {
    return BCryptPasswordEncoder(11)
  }

  @Bean
  open fun corsConfigurationSource(): CorsConfigurationSource {
    val configuration = CorsConfiguration()
    configuration.allowedOrigins = ImmutableList.of("*")
    configuration.allowedMethods = ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")

    // setAllowCredentials(true) is important, otherwise:
    // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
    configuration.allowCredentials = true

    // setAllowedHeaders is important! Without it, OPTIONS preflight request
    // will fail with 403 Invalid CORS request
    configuration.allowedHeaders = ImmutableList.of("Authorization", "Cache-Control", "Content-Type")

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)

    return source
  }
}