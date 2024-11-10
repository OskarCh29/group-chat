package pl.chat.groupchat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.chat.groupchat.services.LoginService;

@EnableScheduling
@SpringBootApplication
public class GroupChatApplication implements CommandLineRunner {
	@Autowired
	private LoginService loginService;

	public static void main(String[] args) {
		SpringApplication.run(GroupChatApplication.class, args);
	}
	@Override
	public void run(String... args)throws Exception{
		loginService.startChat();
	}


}
