package uk.co.aegon.eureka.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class ApiGatewaySecurityConfig extends WebSecurityConfigurerAdapter{
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().anyRequest();
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		
	//	http.csrf().disable(); // one way is to close CSRF directly, and the other is to configure URL to release
        
		http.csrf().ignoringAntMatchers("/eureka/**");
        
		super.configure(http);
	}
}
