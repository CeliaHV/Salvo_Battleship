package com.codeoftheweb.salvo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;


@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}
        
        @Bean
        public PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
        
        @Bean
        public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, 
               GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
        return (args) -> {
            Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
            Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
            Player player3 = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
            playerRepository.save(player1);
            playerRepository.save(player2);
            playerRepository.save(player3);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime now2 = now.plusHours(1);
            LocalDateTime now3 = now.plusHours(2);
            Game game1 = new Game(now);
            Game game2 = new Game(now2);
            Game game3 = new Game(now3);
            gameRepository.save(game1);
            gameRepository.save(game2);
            gameRepository.save(game3);
            GamePlayer gameplayer1 = new GamePlayer(game1,player1, now);
            GamePlayer gameplayer2 = new GamePlayer(game1,player2, now);
            GamePlayer gameplayer3 = new GamePlayer(game2,player1, now2);
            Ship destroyer1 = new Ship(ShipType.DESTROYER , new ArrayList<>(Arrays.asList("H2", "H3", "H4")));
	    Ship submarine1 = new Ship(ShipType.SUBMARINE , new ArrayList<>(Arrays.asList("E1", "F1", "G1")));
            Ship patrolBoat1 = new Ship(ShipType.PATROL_BOAT , new ArrayList<>(Arrays.asList("B4", "B5")));
            Ship destroyer2 = new Ship(ShipType.DESTROYER , new ArrayList<>(Arrays.asList("B5", "C5", "D5")));
            Ship patrolBoat2 = new Ship(ShipType.PATROL_BOAT , new ArrayList<>(Arrays.asList("F1", "F2")));
            gameplayer1.addShips(destroyer1);
            gameplayer1.addShips(submarine1);
            gameplayer1.addShips(patrolBoat1);
            gameplayer2.addShips(destroyer2);
            gameplayer2.addShips(patrolBoat2);
            
            Salvo salvo1a = new Salvo( 1, new ArrayList<>(Arrays.asList("B5", "C5","F1")));
            Salvo salvo1b = new Salvo( 1, new ArrayList<>(Arrays.asList("B4", "B5","B6")));
            Salvo salvo2a = new Salvo( 2, new ArrayList<>(Arrays.asList("F2", "D5")));
            Salvo salvo2b = new Salvo( 2, new ArrayList<>(Arrays.asList("E1", "H3","A2")));
            gameplayer1.addSalvo(salvo1a);
            gameplayer2.addSalvo(salvo1b);
            gameplayer1.addSalvo(salvo2a);
            gameplayer2.addSalvo(salvo2b);
            
            
            
            gamePlayerRepository.save(gameplayer1);
            gamePlayerRepository.save(gameplayer2);
            gamePlayerRepository.save(gameplayer3);
            
            Score score1 = new Score(player1, game1, 2, now);
            Score score2 = new Score(player2, game2, 2, now2);
            Score score3 = new Score(player1, game3, 12, now3);
            scoreRepository.save(score1);
            scoreRepository.save(score2);
            scoreRepository.save(score3);
            
            
            };
        }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

  @Autowired
  PlayerRepository playerRepository;
  
  @Autowired
  PasswordEncoder passwordEncoder;

  @Override
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(inputName-> {
      Player player = playerRepository.findByUserName(inputName);
        if (player != null) {
          return new User(player.getUserName(), player.getPassword(),
                  AuthorityUtils.createAuthorityList("USER"));
        } else {
          throw new UsernameNotFoundException("Unknown user: " + inputName);
        }
    }).passwordEncoder(passwordEncoder);
  }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        
        http.authorizeRequests()
               .antMatchers("/rest/**").hasAuthority("ADMIN")
               .antMatchers("/api/game_view/**").hasAnyAuthority("USER","ADMIN");
                
                      
        http.formLogin()
                .usernameParameter("name")
                .passwordParameter("pwd")
                .loginPage("/api/login");
    
        http.logout().logoutUrl("/api/logout");
        
        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
     }
  
  private void clearAuthenticationAttributes(HttpServletRequest request) {
  HttpSession session = request.getSession(false);
  if (session != null) {
    session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}