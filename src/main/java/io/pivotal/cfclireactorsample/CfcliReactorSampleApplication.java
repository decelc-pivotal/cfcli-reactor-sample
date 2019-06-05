package io.pivotal.cfclireactorsample;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.client.CloudFoundryClient;
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

import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksRequest;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksResponse;
import org.cloudfoundry.client.v2.buildpacks.BuildpackEntity;
import org.cloudfoundry.client.v2.buildpacks.BuildpackResource;
import java.net.URL;

@RestController
@SpringBootApplication
public class CfcliReactorSampleApplication {

	private static final Logger logger = LoggerFactory.getLogger(CfcliReactorSampleApplication.class);

	@Bean
	public ReactorCloudFoundryClient reactorCloudFoundryClient(@Value("${cf.apiHost}") String apiHost, @Value("${cf.skipSslValidation:true}") Boolean skipSslValidation, @Value("${cf.username}") String username,
			@Value("${cf.password}") String password)  {
			logger.trace("In reactorCloudFoundryClient with properties: \n{}", apiHost);

			return ReactorCloudFoundryClient.builder()
							.connectionContext(cloudControllerConnectionContext(apiHost, skipSslValidation))
							.tokenProvider(clientCredentialsGrantTokenProvider(username, password))
							.build();
	}

	@Bean
	public ReactorUaaClient reactorUaaClient(@Value("${cf.apiHost}") String apiHost, @Value("${cf.skipSslValidation:true}") Boolean skipSslValidation, @Value("${cf.username}") String username,
			@Value("${cf.password}") String password) {
			logger.trace("In reactorCloudFoundryClient with properties: \n{}", apiHost);

			return ReactorUaaClient.builder()
							.connectionContext(uaaConnectionContext(apiHost,skipSslValidation))
							.tokenProvider(clientCredentialsGrantTokenProvider(username, password))
							.build();
	}

	private ConnectionContext cloudControllerConnectionContext(String apiHost, Boolean skipSslValidation) {
			//final URL baseUrl = new URL(cfProperties.getBaseUrl());
			return DefaultConnectionContext.builder()
							.apiHost(apiHost)
							//.port(baseUrl.getPort())
							.skipSslValidation(skipSslValidation)
							.build();
	}

	private ConnectionContext uaaConnectionContext(String apiHost, Boolean skipSslValidation) {
			//final URL uaaUrl = new URL(cfProperties.getOauth2().getClient().getAccessTokenUri());
			return DefaultConnectionContext.builder()
							.apiHost(apiHost)
							//.port(uaaUrl.getPort())
							.skipSslValidation(skipSslValidation)
							.build();
	}

	private TokenProvider clientCredentialsGrantTokenProvider(String username, String password) {
			return ClientCredentialsGrantTokenProvider.builder()
							.clientId(username)
							.clientSecret(password).build();
	}

	@Autowired
	UaaClient uaaClient;

	@Autowired
	CloudFoundryClient cfClient;

	public static void main(String[] args) {
		SpringApplication.run(CfcliReactorSampleApplication.class, args);
	}


	@GetMapping("/buildPacks")
	public ResponseEntity <List<BuildpackResource>> buildPacks() {

		ListBuildpacksRequest r = ListBuildpacksRequest.builder().build();

		return cfClient
			.buildpacks()
			.list(r)
			.map(buildpack -> ResponseEntity.ok(buildpack.getResources()))
			.block();
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
