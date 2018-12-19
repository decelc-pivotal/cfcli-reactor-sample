package io.pivotal.cfclireactorsample;

import java.util.List;

import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.uaa.users.ListUsersRequest;
import org.cloudfoundry.uaa.users.ListUsersResponse;
import org.cloudfoundry.uaa.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.xml.bind.DatatypeConverter;
import org.cloudfoundry.uaa.tokens.GetTokenByPasswordResponse;
import org.cloudfoundry.uaa.tokens.GetTokenByPasswordRequest;
import org.cloudfoundry.uaa.tokens.TokenFormat;
import org.cloudfoundry.uaa.tokens.TokenKey;

import org.cloudfoundry.uaa.clients.ListClientsRequest;
import org.cloudfoundry.uaa.clients.ListClientsResponse;
import org.cloudfoundry.uaa.clients.Client;
@RestController
@SpringBootApplication
public class CfcliReactorSampleApplication {

	@Bean
	DefaultConnectionContext connectionContext(@Value("${cf.apiHost}") String apiHost,
			@Value("${cf.skipSslValidation:true}") Boolean skipSslValidation) {
		return DefaultConnectionContext.builder().apiHost(apiHost).skipSslValidation(skipSslValidation).build();
	}

	@Bean
	PasswordGrantTokenProvider tokenProvider(@Value("${cf.username}") String username,
			@Value("${cf.password}") String password) {
		return PasswordGrantTokenProvider.builder().username(username).password(password).build();
	}

	@Bean
	ClientCredentialsGrantTokenProvider clientTokenProvider(@Value("${cf.username}") String username,
			@Value("${cf.password}") String password) {
		return ClientCredentialsGrantTokenProvider.builder().clientSecret(password).clientId(username).build();
	}

	@Bean
	ReactorUaaClient uaaClient(ConnectionContext connectionContext, ClientCredentialsGrantTokenProvider tokenProvider) {
		return ReactorUaaClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider).build();
	}

	@Autowired
	UaaClient uaaClient;

	public static void main(String[] args) {
		SpringApplication.run(CfcliReactorSampleApplication.class, args);
	}

	@GetMapping("/getUserName")
	public @ResponseBody String getUser(@RequestParam(value="id") String id) {
		//String userId="bf9fa74d-1857-42bd-9518-6bb80410f7cf";

		ListUsersRequest r = ListUsersRequest.builder()
							.filter(String.format("id eq \"%s\"", id))
							.build();

		return uaaClient.users()
				.list(r)
				.flatMapIterable(ListUsersResponse::getResources)
				.map(User::getUserName).blockLast();

	}

	@GetMapping("/listUsers")
	public ResponseEntity <List<User>> listUsers() {
		ListUsersRequest r = ListUsersRequest.builder()
							.build();

		return uaaClient.users().list(r).map(user -> ResponseEntity.ok(user.getResources())).block();
	}

	@GetMapping("/listClients")
	public ResponseEntity <List<Client>> listClients() {
		ListClientsRequest r = ListClientsRequest.builder()
							.build();

		return uaaClient.clients().list(r).map(client -> ResponseEntity.ok(client.getResources())).block();
	}
}
